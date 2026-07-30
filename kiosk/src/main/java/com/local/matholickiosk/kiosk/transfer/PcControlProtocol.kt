package com.local.matholickiosk.kiosk.transfer

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class EncodedPcControlRequest(
    val frame: ByteArray,
    val requestId: ByteArray,
)

data class DecodedPcControlResponse(
    val operation: Int,
    val accepted: Boolean,
    val label: String,
    val payload: ByteArray,
)

object PcControlProtocol {
    const val OP_STATUS = 1
    const val OP_FETCH_CSV = 2
    const val HEADER_BYTES = 65
    const val MAX_PAYLOAD_BYTES = 1024 * 1024
    private const val VERSION: Byte = 1
    private const val REQUEST_ID_BYTES = 16
    private const val NONCE_BYTES = 12
    private const val MAX_LABEL_BYTES = 160
    private const val GCM_TAG_BITS = 128
    private val requestMagic = "MATHCTL1".toByteArray(StandardCharsets.US_ASCII)
    private val responseMagic = "MATHRSP1".toByteArray(StandardCharsets.US_ASCII)
    private val hkdfInfo = "matholic-pdf-transfer-v1".toByteArray(StandardCharsets.US_ASCII)

    fun encodeRequest(
        pairing: PcReceiverPairing,
        operation: Int,
        label: String = "",
        payload: ByteArray = ByteArray(0),
        timestamp: Long = System.currentTimeMillis() / 1_000,
        requestId: ByteArray = randomBytes(REQUEST_ID_BYTES),
        nonce: ByteArray = randomBytes(NONCE_BYTES),
    ): EncodedPcControlRequest {
        require(operation in 1..255) { "PC control operation is invalid" }
        val labelBytes = label.toByteArray(StandardCharsets.UTF_8)
        require(labelBytes.size <= MAX_LABEL_BYTES) { "PC control label is invalid" }
        require(payload.size <= MAX_PAYLOAD_BYTES) { "PC control payload is too large" }
        require(requestId.size == REQUEST_ID_BYTES && nonce.size == NONCE_BYTES) {
            "PC control randomness is invalid"
        }
        val plaintext = ByteBuffer
            .allocate(1 + 2 + 4 + labelBytes.size + payload.size)
            .order(ByteOrder.BIG_ENDIAN)
            .put(operation.toByte())
            .putShort(labelBytes.size.toShort())
            .putInt(payload.size)
            .put(labelBytes)
            .put(payload)
            .array()
        val frame = encodeSecureFrame(
            pairing = pairing,
            magic = requestMagic,
            plaintext = plaintext,
            timestamp = timestamp,
            requestId = requestId,
            nonce = nonce,
        )
        plaintext.fill(0)
        return EncodedPcControlRequest(frame, requestId.copyOf())
    }

    fun decodeResponse(
        pairing: PcReceiverPairing,
        frame: ByteArray,
        expectedRequestId: ByteArray,
        nowEpochSeconds: Long = System.currentTimeMillis() / 1_000,
    ): DecodedPcControlResponse {
        val decoded = decodeSecureFrame(
            pairing,
            frame,
            responseMagic,
            nowEpochSeconds,
        )
        require(MessageDigest.isEqual(decoded.requestId, expectedRequestId)) {
            "PC control response does not match the request"
        }
        val plaintext = decoded.plaintext
        try {
            require(plaintext.size >= 8) { "PC control response is truncated" }
            val buffer = ByteBuffer.wrap(plaintext).order(ByteOrder.BIG_ENDIAN)
            val operation = buffer.get().toInt() and 0xff
            val accepted = buffer.get().toInt() and 0xff
            val labelLength = buffer.short.toInt() and 0xffff
            val payloadLength = buffer.int
            require(
                operation > 0 &&
                    accepted in 0..1 &&
                    labelLength <= MAX_LABEL_BYTES &&
                    payloadLength in 0..MAX_PAYLOAD_BYTES &&
                    buffer.remaining() == labelLength + payloadLength
            ) {
                "PC control response lengths are invalid"
            }
            val labelBytes = ByteArray(labelLength).also(buffer::get)
            val payload = ByteArray(payloadLength).also(buffer::get)
            return DecodedPcControlResponse(
                operation = operation,
                accepted = accepted == 1,
                label = String(labelBytes, StandardCharsets.UTF_8),
                payload = payload,
            )
        } finally {
            plaintext.fill(0)
        }
    }

    fun responseFrameLength(header: ByteArray): Int {
        require(header.size == HEADER_BYTES) { "PC control header length is invalid" }
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN)
        val magic = ByteArray(responseMagic.size).also(buffer::get)
        val version = buffer.get()
        buffer.position(HEADER_BYTES - 4)
        val bodyLength = buffer.int
        require(
            magic.contentEquals(responseMagic) &&
                version == VERSION &&
                bodyLength in 16..(MAX_PAYLOAD_BYTES + MAX_LABEL_BYTES + 64)
        ) {
            "PC control response header is invalid"
        }
        return HEADER_BYTES + bodyLength
    }

    private data class SecurePlaintext(
        val requestId: ByteArray,
        val plaintext: ByteArray,
    )

    private fun encodeSecureFrame(
        pairing: PcReceiverPairing,
        magic: ByteArray,
        plaintext: ByteArray,
        timestamp: Long,
        requestId: ByteArray,
        nonce: ByteArray,
    ): ByteArray {
        val authenticatedHeader = ByteBuffer
            .allocate(HEADER_BYTES - 4)
            .order(ByteOrder.BIG_ENDIAN)
            .put(magic)
            .put(VERSION)
            .put(pairing.receiverId)
            .put(requestId)
            .putLong(timestamp)
            .put(nonce)
            .array()
        val key = deriveKey(pairing)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(key, "AES"),
            GCMParameterSpec(GCM_TAG_BITS, nonce),
        )
        cipher.updateAAD(authenticatedHeader)
        val ciphertext = cipher.doFinal(plaintext)
        key.fill(0)
        return ByteBuffer
            .allocate(HEADER_BYTES + ciphertext.size)
            .order(ByteOrder.BIG_ENDIAN)
            .put(authenticatedHeader)
            .putInt(ciphertext.size)
            .put(ciphertext)
            .array()
    }

    private fun decodeSecureFrame(
        pairing: PcReceiverPairing,
        frame: ByteArray,
        expectedMagic: ByteArray,
        nowEpochSeconds: Long,
    ): SecurePlaintext {
        require(frame.size >= HEADER_BYTES) { "PC control response is truncated" }
        val buffer = ByteBuffer.wrap(frame).order(ByteOrder.BIG_ENDIAN)
        val magic = ByteArray(expectedMagic.size).also(buffer::get)
        val version = buffer.get()
        val receiverId = ByteArray(16).also(buffer::get)
        val requestId = ByteArray(REQUEST_ID_BYTES).also(buffer::get)
        val timestamp = buffer.long
        val nonce = ByteArray(NONCE_BYTES).also(buffer::get)
        val ciphertextLength = buffer.int
        require(
            magic.contentEquals(expectedMagic) &&
                version == VERSION &&
                MessageDigest.isEqual(receiverId, pairing.receiverId) &&
                ciphertextLength == buffer.remaining() &&
                kotlin.math.abs(nowEpochSeconds - timestamp) <= 300
        ) {
            "PC control response target is invalid"
        }
        val ciphertext = ByteArray(ciphertextLength).also(buffer::get)
        val aad = frame.copyOfRange(0, HEADER_BYTES - 4)
        val key = deriveKey(pairing)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key, "AES"),
                GCMParameterSpec(GCM_TAG_BITS, nonce),
            )
            cipher.updateAAD(aad)
            SecurePlaintext(requestId, cipher.doFinal(ciphertext))
        } catch (error: Exception) {
            throw IllegalArgumentException("PC control response authentication failed", error)
        } finally {
            key.fill(0)
            ciphertext.fill(0)
        }
    }

    private fun deriveKey(pairing: PcReceiverPairing): ByteArray {
        val extract = Mac.getInstance("HmacSHA256")
        extract.init(SecretKeySpec(pairing.receiverId, "HmacSHA256"))
        val pseudoRandomKey = extract.doFinal(pairing.secret)
        val expand = Mac.getInstance("HmacSHA256")
        expand.init(SecretKeySpec(pseudoRandomKey, "HmacSHA256"))
        val key = expand.doFinal(hkdfInfo + byteArrayOf(1))
        pseudoRandomKey.fill(0)
        return key
    }

    private fun randomBytes(size: Int): ByteArray =
        ByteArray(size).also(SecureRandom()::nextBytes)
}
