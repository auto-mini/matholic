package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.transfer.PcEndpointResolver
import com.local.matholickiosk.kiosk.transfer.PcReceiverPairing
import com.local.matholickiosk.kiosk.transfer.PcSubnetCandidates
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PcEndpointResolverTest {
    @Test
    fun `candidate list stays in current private slash 24 and prioritizes old vicinity`() {
        val candidates = PcSubnetCandidates.samePrivateSubnet(
            localAddress = "192.168.219.77",
            prefixLength = 24,
            previousHost = "192.168.219.224",
        )

        assertEquals("192.168.219.223", candidates.first())
        assertEquals("192.168.219.225", candidates[1])
        assertTrue("192.168.219.230" in candidates)
        assertFalse("192.168.219.0" in candidates)
        assertFalse("192.168.219.77" in candidates)
        assertFalse("192.168.219.224" in candidates)
        assertFalse("192.168.219.255" in candidates)
        assertTrue(candidates.all { it.startsWith("192.168.219.") })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `candidate list rejects public network`() {
        PcSubnetCandidates.samePrivateSubnet(
            localAddress = "203.0.113.9",
            prefixLength = 24,
            previousHost = "203.0.113.10",
        )
    }

    @Test
    fun `initial pairing accepts only current private subnet`() {
        PcSubnetCandidates.requireSamePrivateSubnet(
            localAddress = "192.168.219.77",
            prefixLength = 24,
            candidateHost = "192.168.219.224",
        )
        assertTrue(
            runCatching {
                PcSubnetCandidates.requireSamePrivateSubnet(
                    localAddress = "192.168.219.77",
                    prefixLength = 24,
                    candidateHost = "192.168.220.224",
                )
            }.isFailure,
        )
        assertTrue(
            runCatching {
                PcSubnetCandidates.requireSamePrivateSubnet(
                    localAddress = "192.168.219.77",
                    prefixLength = 24,
                    candidateHost = "203.0.113.7",
                )
            }.isFailure,
        )
    }

    @Test
    fun `resolver accepts only host that returns authenticated probe`() {
        val attempted = ConcurrentLinkedQueue<String>()
        val resolver = PcEndpointResolver(
            probe = { candidate ->
                attempted += candidate.host
                if (candidate.host != "192.168.219.230") {
                    throw IOException("not the paired receiver")
                }
            },
            parallelism = 4,
            overallTimeoutMs = 2_000,
        )
        val original = pairing("192.168.219.224")
        val resolved = resolver.resolve(
            original,
            listOf(
                "192.168.219.223",
                "192.168.219.225",
                "192.168.219.230",
            ),
        )
        try {
            assertEquals("192.168.219.230", resolved.host)
            assertTrue("192.168.219.230" in attempted)
            assertEquals("192.168.219.224", original.host)
        } finally {
            original.clearSensitiveData()
            resolved.clearSensitiveData()
        }
    }

    @Test(expected = IOException::class)
    fun `resolver fails closed when no candidate authenticates`() {
        val resolver = PcEndpointResolver(
            probe = { throw IOException("not paired") },
            parallelism = 2,
            overallTimeoutMs = 1_000,
        )
        val original = pairing("192.168.219.224")
        try {
            resolver.resolve(
                original,
                listOf("192.168.219.223", "192.168.219.225"),
            )
        } finally {
            original.clearSensitiveData()
        }
    }

    private fun pairing(host: String) = PcReceiverPairing(
        receiverId = ByteArray(16) { it.toByte() },
        secret = ByteArray(32) { (it + 16).toByte() },
        host = host,
        port = 48129,
        displayName = "DESKTOP-TEST",
    )
}
