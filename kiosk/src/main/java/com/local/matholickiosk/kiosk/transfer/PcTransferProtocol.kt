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

data class EncodedPcTransfer(
    val frame: ByteArray,
    val requestId: ByteArray,
    val pdfSha256: ByteArray,
)

object PcTransferProtocol {
    const val MAX_PDF_BYTES = 5 * 1024 * 1024
    const val ACK_BYTES = 89
    private const val VERSION: Byte = 1
    private const val REQUEST_HEADER_BYTES = 65
    private const val REQUEST_ID_BYTES = 16
    private const val NONCE_BYTES = 12
    private const val MAX_FILENAME_BYTES = 240
    private const val GCM_TAG_BITS = 128
    private val requestMagic = "MATHPDF1".toByteArray(StandardCharsets.US_ASCII)
    private val ackMagic = "MATHACK1".toByteArray(StandardCharsets.US_ASCII)
    private val hkdfInfo = "matholic-pdf-transfer-v1".toByteArray(StandardCharsets.US_ASCII)
    private val pdfMagic = "%PDF-".toByteArray(StandardCharsets.US_ASCII)

    fun encodeRequest(
        pairing: PcReceiverPairing,
        filename: String,
        pdf: ByteArray,
        timestamp: Long = System.currentTimeMillis() / 1_000,
        requestId: ByteArray = randomBytes(REQUEST_ID_BYTES),
        nonce: ByteArray = randomBytes(NONCE_BYTES),
    ): EncodedPcTransfer {
        val filenameBytes = filename.toByteArray(StandardCharsets.UTF_8)
        require(filenameBytes.size in 1..MAX_FILENAME_BYTES) { "PDF filename is invalid" }
        require(pdf.size in 1..MAX_PDF_BYTES && pdf.startsWith(pdfMagic)) {
            "PDF payload is invalid"
        }
        require(requestId.size == REQUEST_ID_BYTES && nonce.size == NONCE_BYTES) {
            "Transfer randomness is invalid"
        }
        val plaintext = ByteBuffer
            .allocate(2 + 4 + filenameBytes.size + pdf.size)
            .order(ByteOrder.BIG_ENDIAN)
            .putShort(filenameBytes.size.toShort())
            .putInt(pdf.size)
            .put(filenameBytes)
            .put(pdf)
            .array()
        val authenticatedHeader = ByteBuffer
            .allocate(REQUEST_HEADER_BYTES - 4)
            .order(ByteOrder.BIG_ENDIAN)
            .put(requestMagic)
            .put(VERSION)
            .put(pairing.receiverId)
            .put(requestId)
            .putLong(timestamp)
            .put(nonce)
            .array()
        var key: ByteArray? = null
        var ciphertext: ByteArray? = null
        return try {
            key = deriveKey(pairing)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(key, "AES"),
                GCMParameterSpec(GCM_TAG_BITS, nonce),
            )
            cipher.updateAAD(authenticatedHeader)
            ciphertext = cipher.doFinal(plaintext)
            val frame = ByteBuffer
                .allocate(REQUEST_HEADER_BYTES + requireNotNull(ciphertext).size)
                .order(ByteOrder.BIG_ENDIAN)
                .put(authenticatedHeader)
                .putInt(requireNotNull(ciphertext).size)
                .put(ciphertext)
                .array()
            val pdfHash = MessageDigest.getInstance("SHA-256").digest(pdf)
            EncodedPcTransfer(
                frame = frame,
                requestId = requestId.copyOf(),
                pdfSha256 = pdfHash,
            )
        } finally {
            plaintext.fill(0)
            filenameBytes.fill(0)
            key?.fill(0)
            ciphertext?.fill(0)
        }
    }

    fun verifyAck(
        pairing: PcReceiverPairing,
        frame: ByteArray,
        expectedRequestId: ByteArray,
        expectedPdfSha256: ByteArray,
    ) {
        require(frame.size == ACK_BYTES) { "PC acknowledgement length is invalid" }
        val buffer = ByteBuffer.wrap(frame).order(ByteOrder.BIG_ENDIAN)
        val magic = ByteArray(ackMagic.size).also(buffer::get)
        val status = buffer.get().toInt() and 0xff
        val requestId = ByteArray(REQUEST_ID_BYTES).also(buffer::get)
        val pdfHash = ByteArray(32).also(buffer::get)
        val signature = ByteArray(32).also(buffer::get)
        var signed: ByteArray? = null
        var key: ByteArray? = null
        var expectedSignature: ByteArray? = null
        try {
            require(magic.contentEquals(ackMagic) && status == 1) {
                "PC rejected the PDF transfer"
            }
            require(
                MessageDigest.isEqual(requestId, expectedRequestId) &&
                    MessageDigest.isEqual(pdfHash, expectedPdfSha256)
            ) {
                "PC acknowledgement does not match the transfer"
            }
            signed = ByteBuffer
                .allocate(ackMagic.size + 1 + requestId.size + pdfHash.size)
                .put(ackMagic)
                .put(status.toByte())
                .put(requestId)
                .put(pdfHash)
                .array()
            key = deriveKey(pairing)
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(key, "HmacSHA256"))
            expectedSignature = mac.doFinal(signed)
            require(MessageDigest.isEqual(signature, expectedSignature)) {
                "PC acknowledgement authentication failed"
            }
        } finally {
            magic.fill(0)
            requestId.fill(0)
            pdfHash.fill(0)
            signature.fill(0)
            signed?.fill(0)
            key?.fill(0)
            expectedSignature?.fill(0)
        }
    }

    private fun deriveKey(pairing: PcReceiverPairing): ByteArray {
        val extract = Mac.getInstance("HmacSHA256")
        extract.init(SecretKeySpec(pairing.receiverId, "HmacSHA256"))
        val pseudoRandomKey = extract.doFinal(pairing.secret)
        val infoBlock = hkdfInfo + byteArrayOf(1)
        return try {
            val expand = Mac.getInstance("HmacSHA256")
            expand.init(SecretKeySpec(pseudoRandomKey, "HmacSHA256"))
            expand.doFinal(infoBlock)
        } finally {
            pseudoRandomKey.fill(0)
            infoBlock.fill(0)
        }
    }

    private fun randomBytes(size: Int): ByteArray =
        ByteArray(size).also(SecureRandom()::nextBytes)

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && prefix.indices.all { index -> this[index] == prefix[index] }
}
