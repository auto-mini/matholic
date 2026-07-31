package com.local.matholickiosk.kiosk

import java.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteQrTestPayloadTest {
    @Test
    fun acceptsExactlyOneHashOnlyWhileRemoteSupportIsActive() {
        val hash = ByteArray(32) { it.toByte() }
        val encoded = Base64.getEncoder().encodeToString(hash)

        assertArrayEquals(hash, RemoteQrTestPayload.decode(encoded, supportActive = true))
        assertNull(RemoteQrTestPayload.decode(encoded, supportActive = false))
    }

    @Test
    fun rejectsMalformedOrWrongLengthPayloads() {
        assertNull(RemoteQrTestPayload.decode(null, supportActive = true))
        assertNull(RemoteQrTestPayload.decode("not-base64", supportActive = true))
        assertNull(
            RemoteQrTestPayload.decode(
                Base64.getEncoder().encodeToString(ByteArray(31)),
                supportActive = true,
            ),
        )
    }
}
