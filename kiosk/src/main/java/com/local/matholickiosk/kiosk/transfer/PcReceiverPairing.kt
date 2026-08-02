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
        val encoded = encodeBytes()
        return try {
            String(encoded, StandardCharsets.US_ASCII)
        } finally {
            encoded.fill(0)
        }
    }

    fun encodeBytes(): ByteArray {
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
        val encodedPayload = Base64.getUrlEncoder().withoutPadding().encode(payload)
        return try {
            PREFIX_BYTES + encodedPayload
        } finally {
            encodedPayload.fill(0)
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
        private val PREFIX_BYTES = PREFIX.toByteArray(StandardCharsets.US_ASCII)

        fun decode(value: String): PcReceiverPairing {
            val bytes = value.toByteArray(StandardCharsets.US_ASCII)
            return try {
                decode(bytes)
            } finally {
                bytes.fill(0)
            }
        }

        fun decode(value: ByteArray): PcReceiverPairing {
            require(
                value.size > PREFIX_BYTES.size &&
                    value.indices.take(PREFIX_BYTES.size).all { value[it] == PREFIX_BYTES[it] },
            ) { "PC pairing prefix is invalid" }
            val encoded = value.copyOfRange(PREFIX_BYTES.size, value.size)
            require(encoded.isNotEmpty() && encoded.none { it.toInt().toChar().isWhitespace() }) {
                encoded.fill(0)
                "PC pairing payload is invalid"
            }
            val payload = try {
                runCatching {
                    Base64.getUrlDecoder().decode(encoded)
                }.getOrElse {
                    throw IllegalArgumentException("PC pairing payload is invalid", it)
                }
            } finally {
                encoded.fill(0)
            }
            var receiverId: ByteArray? = null
            var secret: ByteArray? = null
            var ownershipTransferred = false
            try {
                require(payload.size >= FIXED_BYTES + 2) { "PC pairing payload is truncated" }
                val buffer = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN)
                require(buffer.get().toInt() and 0xff == VERSION) {
                    "PC pairing version is invalid"
                }
                receiverId = ByteArray(RECEIVER_ID_BYTES).also(buffer::get)
                secret = ByteArray(SECRET_BYTES).also(buffer::get)
                val port = buffer.short.toInt() and 0xffff
                val hostLength = buffer.get().toInt() and 0xff
                require(hostLength > 0 && buffer.remaining() > hostLength) {
                    "PC pairing host length is invalid"
                }
                val hostBytes = ByteArray(hostLength).also(buffer::get)
                try {
                    require(hostBytes.all { byte -> byte.toInt() in 0x21..0x7e }) {
                        "PC pairing host is invalid"
                    }
                    val nameLength = buffer.get().toInt() and 0xff
                    require(nameLength > 0 && buffer.remaining() == nameLength) {
                        "PC pairing name length is invalid"
                    }
                    val nameBytes = ByteArray(nameLength).also(buffer::get)
                    try {
                        val pairing = PcReceiverPairing(
                            receiverId = requireNotNull(receiverId),
                            secret = requireNotNull(secret),
                            host = String(hostBytes, StandardCharsets.US_ASCII),
                            port = port,
                            displayName = String(nameBytes, StandardCharsets.UTF_8),
                        )
                        ownershipTransferred = true
                        return pairing
                    } finally {
                        nameBytes.fill(0)
                    }
                } finally {
                    hostBytes.fill(0)
                }
            } finally {
                if (!ownershipTransferred) {
                    receiverId?.fill(0)
                    secret?.fill(0)
                }
                payload.fill(0)
            }
        }
    }
}
