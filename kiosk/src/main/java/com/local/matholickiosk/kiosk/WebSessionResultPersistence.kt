package com.local.matholickiosk.kiosk

internal data class PersistedWebSessionResult<T>(
    val passed: Boolean,
    val session: T,
)

internal object WebSessionResultPersistence {
    fun <T> persist(
        passed: Boolean,
        persistTransition: () -> Unit,
        loadSession: () -> T?,
    ): Result<PersistedWebSessionResult<T>> = runCatching {
        persistTransition()
        PersistedWebSessionResult(
            passed = passed,
            session = checkNotNull(loadSession()) {
                "Web session result was stored without an active session"
            },
        )
    }
}
