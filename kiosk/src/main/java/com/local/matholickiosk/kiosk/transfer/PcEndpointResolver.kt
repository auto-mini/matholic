package com.local.matholickiosk.kiosk.transfer

import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.min

object PcSubnetCandidates {
    private const val MAX_CANDIDATES = 254

    fun samePrivateSubnet(
        localAddress: String,
        prefixLength: Int,
        previousHost: String,
    ): List<String> {
        val local = localAddress.toIpv4Int()
        require(local.isPrivateIpv4()) { "PC recovery requires a private IPv4 network" }
        val effectivePrefix = prefixLength.coerceAtLeast(24)
        require(effectivePrefix in 24..30) {
            "PC recovery network prefix is not supported"
        }
        val mask = -1 shl (32 - effectivePrefix)
        val network = local and mask
        val broadcast = network or mask.inv()
        val previous = previousHost.toIpv4IntOrNull()
        return (network + 1 until broadcast)
            .asSequence()
            .filter { candidate ->
                candidate != local &&
                    candidate != previous &&
                    candidate.isPrivateIpv4()
            }
            .sortedWith(
                compareBy<Int> { candidate ->
                    previous?.let { abs(candidate.toLong() - it.toLong()) } ?: 0L
                }.thenBy { it.toUnsignedLong() },
            )
            .take(MAX_CANDIDATES)
            .map { it.toIpv4String() }
            .toList()
    }

    private fun String.toIpv4Int(): Int {
        val address = InetAddress.getByName(this)
        require(address is Inet4Address && address.hostAddress == this) {
            "IPv4 address is invalid"
        }
        return address.address.fold(0) { result, byte ->
            (result shl 8) or (byte.toInt() and 0xff)
        }
    }

    private fun String.toIpv4IntOrNull(): Int? =
        runCatching { toIpv4Int() }.getOrNull()

    private fun Int.isPrivateIpv4(): Boolean {
        val first = this ushr 24 and 0xff
        val second = this ushr 16 and 0xff
        return first == 10 ||
            (first == 172 && second in 16..31) ||
            (first == 192 && second == 168)
    }

    private fun Int.toIpv4String(): String = listOf(
        this ushr 24 and 0xff,
        this ushr 16 and 0xff,
        this ushr 8 and 0xff,
        this and 0xff,
    ).joinToString(".")

    private fun Int.toUnsignedLong(): Long = toLong() and 0xffff_ffffL
}

class PcEndpointResolver(
    private val probe: (PcReceiverPairing) -> Unit = { candidate ->
        PcControlClient(
            connectTimeoutMs = 250,
            readTimeoutMs = 1_000,
        ).sendStatus(
            pairing = candidate,
            state = "PC 주소 자동 복구",
            studentName = null,
            notify = false,
        )
    },
    private val parallelism: Int = 32,
    private val overallTimeoutMs: Long = 6_000,
) {
    fun resolve(
        pairing: PcReceiverPairing,
        candidateHosts: List<String>,
    ): PcReceiverPairing {
        val hosts = candidateHosts
            .asSequence()
            .filter { it != pairing.host }
            .distinct()
            .take(254)
            .toList()
        require(hosts.isNotEmpty()) { "PC 주소 복구 후보가 없습니다." }
        val executor = Executors.newFixedThreadPool(min(parallelism, hosts.size))
        val completion = ExecutorCompletionService<PcReceiverPairing?>(executor)
        val resolved = AtomicBoolean(false)
        val futures = hosts.map { host ->
            completion.submit {
                if (resolved.get()) return@submit null
                val candidate = pairing.withHost(host)
                try {
                    probe(candidate)
                    if (resolved.compareAndSet(false, true)) {
                        candidate
                    } else {
                        candidate.clearSensitiveData()
                        null
                    }
                } catch (_: Exception) {
                    candidate.clearSensitiveData()
                    null
                }
            }
        }
        val deadline = System.nanoTime() +
            TimeUnit.MILLISECONDS.toNanos(overallTimeoutMs)
        try {
            var remainingTasks = hosts.size
            while (remainingTasks > 0) {
                val remaining = deadline - System.nanoTime()
                if (remaining <= 0L) break
                val completed = completion.poll(remaining, TimeUnit.NANOSECONDS)
                    ?: break
                remainingTasks -= 1
                completed.get()?.let { return it }
            }
            throw IOException("같은 네트워크에서 지정 PC를 찾지 못했습니다.")
        } finally {
            resolved.set(true)
            futures.forEach { it.cancel(true) }
            executor.shutdownNow()
        }
    }
}
