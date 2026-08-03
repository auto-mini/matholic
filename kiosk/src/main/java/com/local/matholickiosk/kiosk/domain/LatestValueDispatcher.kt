package com.local.matholickiosk.kiosk.domain

import java.util.concurrent.Executors

internal class LatestValueDispatcher<T>(
    threadName: String,
    private val consume: (T) -> Unit,
) : AutoCloseable {
    private val lock = Any()
    private val executor = Executors.newSingleThreadExecutor { task ->
        Thread(task, threadName).apply { isDaemon = true }
    }
    private var pending: T? = null
    private var drainScheduled = false
    private var closed = false

    fun submit(value: T) {
        val shouldSchedule = synchronized(lock) {
            if (closed) return
            pending = value
            if (drainScheduled) {
                false
            } else {
                drainScheduled = true
                true
            }
        }
        if (!shouldSchedule) return
        try {
            executor.execute(::drain)
        } catch (_: RuntimeException) {
            synchronized(lock) {
                drainScheduled = false
                pending = null
            }
        }
    }

    private fun drain() {
        while (true) {
            val next = synchronized(lock) {
                pending.also {
                    pending = null
                    if (it == null) drainScheduled = false
                }
            } ?: return
            runCatching { consume(next) }
        }
    }

    override fun close() {
        synchronized(lock) {
            closed = true
            pending = null
        }
        executor.shutdownNow()
    }
}
