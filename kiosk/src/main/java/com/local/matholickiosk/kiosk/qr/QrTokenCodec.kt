package com.local.matholickiosk.kiosk.qr

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class IssuedQrToken internal constructor(
    val payload: String,
    val hash: ByteArray,
)

sealed interface QrParseResult {
    data object Ignore : QrParseResult
    data class Invalid(val reason: String) : QrParseResult
    data class Valid(val hash: ByteArray) : QrParseResult
}

enum class QrFrameRejection {
    MULTIPLE_QR,
    INVALID_QR,
}

sealed interface QrFrameDecision {
    data object Ignore : QrFrameDecision
    data class Reject(val reason: QrFrameRejection) : QrFrameDecision
    data class Accept(val tokenHash: ByteArray) : QrFrameDecision
}

class QrTokenCodec(
    private val secureRandom: SecureRandom = SecureRandom(),
) {
    fun issue(): IssuedQrToken {
        val token = ByteArray(TOKEN_BYTES).also(secureRandom::nextBytes)
        return try {
            val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(token)
            IssuedQrToken(PREFIX + encoded, sha256(token))
        } finally {
            token.fill(0)
        }
    }

    fun parse(payload: String?): QrParseResult {
        if (payload == null || !payload.startsWith(PREFIX)) return QrParseResult.Ignore
        val encoded = payload.removePrefix(PREFIX)
        if (encoded.length != ENCODED_LENGTH || !URL_SAFE.matches(encoded)) {
            return QrParseResult.Invalid("MALFORMED_TOKEN")
        }
        val token = runCatching { Base64.getUrlDecoder().decode(encoded) }
            .getOrElse { return QrParseResult.Invalid("MALFORMED_TOKEN") }
        return try {
            if (token.size != TOKEN_BYTES) {
                QrParseResult.Invalid("TOKEN_LENGTH")
            } else {
                QrParseResult.Valid(sha256(token))
            }
        } finally {
            token.fill(0)
        }
    }

    fun decideFrame(rawValues: List<String?>): QrFrameDecision {
        val candidates = rawValues.filter { it?.startsWith(PREFIX) == true }
        if (candidates.isEmpty()) return QrFrameDecision.Ignore
        if (candidates.size > 1) return QrFrameDecision.Reject(QrFrameRejection.MULTIPLE_QR)
        return when (val parsed = parse(candidates.single())) {
            QrParseResult.Ignore -> QrFrameDecision.Ignore
            is QrParseResult.Invalid -> QrFrameDecision.Reject(QrFrameRejection.INVALID_QR)
            is QrParseResult.Valid -> QrFrameDecision.Accept(parsed.hash)
        }
    }

    companion object {
        const val PREFIX = "MQR1:"
        const val TOKEN_BYTES = 32
        private const val ENCODED_LENGTH = 43
        private val URL_SAFE = Regex("^[A-Za-z0-9_-]+$")

        fun sha256(bytes: ByteArray): ByteArray =
            MessageDigest.getInstance("SHA-256").digest(bytes)
    }
}
