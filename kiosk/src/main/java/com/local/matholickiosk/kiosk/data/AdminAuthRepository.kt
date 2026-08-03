package com.local.matholickiosk.kiosk.data

import com.local.matholickiosk.kiosk.security.AdminLockoutPolicy
import com.local.matholickiosk.kiosk.security.AdminPin
import com.local.matholickiosk.kiosk.security.PinVerifier

sealed interface AdminAuthResult {
    data object Success : AdminAuthResult
    data class Rejected(val retryAfterMillis: Long) : AdminAuthResult
    data object NotEnrolled : AdminAuthResult
}

class AdminAuthRepository(
    private val database: KioskDatabase,
    private val nowEpochMs: () -> Long = System::currentTimeMillis,
) {
    fun isEnrolled(): Boolean = database.adminDao().enrolledCount() > 0

    fun enrolledPinLength(): Int? = database.adminDao().enrolledPinLength()
        ?.takeIf { it in 6..12 }

    fun enroll(pin: CharArray) {
        try {
            check(database.adminDao().enrolledCount() == 0) { "Admin PIN is already enrolled" }
            val verifier = AdminPin.create(pin)
            try {
                val now = nowEpochMs()
                database.adminDao().save(
                    AdminCredentialEntity(
                        salt = verifier.salt,
                        derivedKey = verifier.derivedKey,
                        iterations = verifier.iterations,
                        verifierVersion = verifier.version,
                        pinLength = pin.size,
                        consecutiveFailures = 0,
                        lockedUntilEpochMs = 0,
                        updatedAtEpochMs = now,
                    ),
                )
            } finally {
                verifier.clear()
            }
        } finally {
            pin.fill('\u0000')
        }
    }

    fun authenticate(pin: CharArray): AdminAuthResult {
        var entity: AdminCredentialEntity? = null
        var verifier: PinVerifier? = null
        try {
            entity = database.adminDao().get() ?: return AdminAuthResult.NotEnrolled
            val now = nowEpochMs()
            if (now < entity.lockedUntilEpochMs) {
                return AdminAuthResult.Rejected(entity.lockedUntilEpochMs - now)
            }
            verifier = PinVerifier(
                salt = entity.salt.copyOf(),
                derivedKey = entity.derivedKey.copyOf(),
                iterations = entity.iterations,
                version = entity.verifierVersion,
            )
            if (AdminPin.verify(pin, verifier)) {
                database.adminDao().save(
                    entity.copy(
                        pinLength = pin.size,
                        consecutiveFailures = 0,
                        lockedUntilEpochMs = 0,
                        updatedAtEpochMs = now,
                    ),
                )
                return AdminAuthResult.Success
            }
            val failures = entity.consecutiveFailures + 1
            val delay = AdminLockoutPolicy.delayMillis(failures)
            database.adminDao().save(
                entity.copy(
                    consecutiveFailures = failures,
                    lockedUntilEpochMs = now + delay,
                    updatedAtEpochMs = now,
                ),
            )
            return AdminAuthResult.Rejected(delay)
        } finally {
            verifier?.clear()
            entity?.salt?.fill(0)
            entity?.derivedKey?.fill(0)
            pin.fill('\u0000')
        }
    }
}
