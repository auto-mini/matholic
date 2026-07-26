package com.local.matholickiosk.kiosk.domain

import java.util.concurrent.atomic.AtomicBoolean

internal class SensitiveTask(
    private val cleanup: () -> Unit,
    private val operation: () -> Unit,
) : Runnable {
    private val claimed = AtomicBoolean(false)

    override fun run() {
        if (!claimed.compareAndSet(false, true)) return
        try {
            operation()
        } finally {
            cleanup()
        }
    }

    fun discard() {
        if (claimed.compareAndSet(false, true)) cleanup()
    }
}
