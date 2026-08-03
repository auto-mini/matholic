package com.local.matholickiosk.kiosk.domain

import java.util.concurrent.atomic.AtomicBoolean

internal interface DiscardableSensitiveTask {
    fun discard()
}

internal class SensitiveTask(
    private val cleanup: () -> Unit,
    private val operation: () -> Unit,
) : Runnable, DiscardableSensitiveTask {
    private val claimed = AtomicBoolean(false)

    override fun run() {
        if (!claimed.compareAndSet(false, true)) return
        try {
            operation()
        } finally {
            cleanup()
        }
    }

    override fun discard() {
        if (claimed.compareAndSet(false, true)) cleanup()
    }
}

/**
 * A queued sensitive task that may hand ownership to another thread. The operation returns true
 * only after that next owner has accepted responsibility for cleanup.
 */
internal class SensitiveHandoffTask(
    private val cleanup: () -> Unit,
    private val operation: () -> Boolean,
) : Runnable, DiscardableSensitiveTask {
    private val claimed = AtomicBoolean(false)

    override fun run() {
        if (!claimed.compareAndSet(false, true)) return
        val transferred = try {
            operation()
        } catch (failure: Throwable) {
            cleanup()
            throw failure
        }
        if (!transferred) cleanup()
    }

    override fun discard() {
        if (claimed.compareAndSet(false, true)) cleanup()
    }
}
