package com.local.matholickiosk.kiosk.transfer

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.Base64

class PcReceiverPairing(
    val receiverId: ByteArray,
    val secret: ByteArray,
    val host: String,
    val port: Int,
    val displayName: String,
) {
    init {
        require(receiverId.size == RECEIVER_ID_BYTES) { "Receiver ID must be 16 bytes" }
        require(secret.size == SECRET_BYTES) { "Pairing secret must be 32 bytes" }
        require(host.isNotBlank() && host.length <= 255) { "Pairing host is invalid" }
        require(port in 1024..65535) { "Pairing port is invalid" }
        require(displayName.isNotBlank() && displayName.length <= 80) {
            "Pairing display name is invalid"
        }
    }

    fun copySensitive(): PcReceiverPairing = PcReceiverPairing(
        receiverId = receiverId.copyOf(),
        secret = secret.copyOf(),
        host = host,
        port = port,
        displayName = displayName,
    )

    fun withHost(updatedHost: String): PcReceiverPairing = PcReceiverPairing(
        receiverId = receiverId.copyOf(),
        secret = secret.copyOf(),
        host = updatedHost,
        port = port,
        displayName = displayName,
    )

    fun encode(): String {
        val hostBytes = host.toByteArray(StandardCharsets.US_ASCII)
        val nameBytes = displayName.toByteArray(StandardCharsets.UTF_8)
        require(hostBytes.size in 1..255 && nameBytes.size in 1..255) {
            "PC pairing fields are too large"
        }
        val payload = ByteBuffer
            .allocate(FIXED_BYTES + hostBytes.size + 1 + nameBytes.size)
            .order(ByteOrder.BIG_ENDIAN)
            .put(VERSION.toByte())
            .put(receiverId)
            .put(secret)
            .putShort(port.toShort())
            .put(hostBytes.size.toByte())
            .put(hostBytes)
            .put(nameBytes.size.toByte())
            .put(nameBytes)
            .array()
        return try {
            PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(payload)
        } finally {
            payload.fill(0)
            hostBytes.fill(0)
            nameBytes.fill(0)
        }
    }

    fun clearSensitiveData() {
        receiverId.fill(0)
        secret.fill(0)
    }

    companion object {
        const val PREFIX = "MATHOLIC-PC1:"
        private const val VERSION = 1
        private const val RECEIVER_ID_BYTES = 16
        private const val SECRET_BYTES = 32
        private const val FIXED_BYTES = 1 + RECEIVER_ID_BYTES + SECRET_BYTES + 2 + 1

        fun decode(value: String): PcReceiverPairing {
            require(value.startsWith(PREFIX)) { "PC pairing prefix is invalid" }
            val encoded = value.removePrefix(PREFIX)
            require(encoded.isNotBlank() && encoded.none(Char::isWhitespace)) {
                "PC pairing payload is invalid"
            }
            val payload = runCatching {
                Base64.getUrlDecoder().decode(encoded)
            }.getOrElse {
                throw IllegalArgumentException("PC pairing payload is invalid", it)
            }
            require(payload.size >= FIXED_BYTES + 2) { "PC pairing payload is truncated" }
            val buffer = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN)
            require(buffer.get().toInt() and 0xff == VERSION) {
                "PC pairing version is invalid"
            }
            val receiverId = ByteArray(RECEIVER_ID_BYTES).also(buffer::get)
            val secret = ByteArray(SECRET_BYTES).also(buffer::get)
            try {
                val port = buffer.short.toInt() and 0xffff
                val hostLength = buffer.get().toInt() and 0xff
                require(hostLength > 0 && buffer.remaining() > hostLength) {
                    "PC pairing host length is invalid"
                }
                val hostBytes = ByteArray(hostLength).also(buffer::get)
                require(hostBytes.all { byte -> byte.toInt() in 0x21..0x7e }) {
                    "PC pairing host is invalid"
                }
                val nameLength = buffer.get().toInt() and 0xff
                require(nameLength > 0 && buffer.remaining() == nameLength) {
                    "PC pairing name length is invalid"
                }
                val nameBytes = ByteArray(nameLength).also(buffer::get)
                return PcReceiverPairing(
                    receiverId = receiverId,
                    secret = secret,
                    host = String(hostBytes, StandardCharsets.US_ASCII),
                    port = port,
                    displayName = String(nameBytes, StandardCharsets.UTF_8),
                )
            } catch (error: Exception) {
                receiverId.fill(0)
                secret.fill(0)
                throw error
            } finally {
                payload.fill(0)
            }
        }
    }
}
