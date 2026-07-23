package com.local.matholickiosk.kiosk.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.math.min

data class PinVerifier(
    val salt: ByteArray,
    val derivedKey: ByteArray,
    val iterations: Int = DEFAULT_ITERATIONS,
    val version: Int = CURRENT_VERSION,
) {
    companion object {
        const val CURRENT_VERSION = 1
        const val DEFAULT_ITERATIONS = 600_000
    }
}

object AdminPin {
    private const val MIN_LENGTH = 6
    private const val MAX_LENGTH = 12
    private const val SALT_BYTES = 16
    private const val KEY_BITS = 256

    fun isValidFormat(pin: CharArray): Boolean =
        pin.size in MIN_LENGTH..MAX_LENGTH && pin.all(Char::isDigit)

    fun create(
        pin: CharArray,
        secureRandom: SecureRandom = SecureRandom(),
        iterations: Int = PinVerifier.DEFAULT_ITERATIONS,
    ): PinVerifier {
        require(isValidFormat(pin)) { "Admin PIN must contain 6 to 12 digits" }
        require(iterations >= 100_000) { "PBKDF2 iteration count is too low" }
        val salt = ByteArray(SALT_BYTES).also(secureRandom::nextBytes)
        return PinVerifier(salt, derive(pin, salt, iterations), iterations)
    }

    fun verify(pin: CharArray, verifier: PinVerifier): Boolean {
        if (!isValidFormat(pin) || verifier.version != PinVerifier.CURRENT_VERSION) return false
        val candidate = derive(pin, verifier.salt, verifier.iterations)
        return try {
            MessageDigest.isEqual(verifier.derivedKey, candidate)
        } finally {
            candidate.fill(0)
        }
    }

    private fun derive(pin: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin, salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}

object AdminLockoutPolicy {
    private const val MAX_DELAY_SECONDS = 300L

    fun delayMillis(consecutiveFailures: Int): Long {
        if (consecutiveFailures <= 0) return 0
        val exponent = min(consecutiveFailures - 1, 9)
        return min(1L shl exponent, MAX_DELAY_SECONDS) * 1_000L
    }
}
