package com.local.matholickiosk.kiosk.domain

import java.util.concurrent.Executors

internal class LatestValueDispatcher<T>(
    threadName: String,
    private val preserve: (T) -> Boolean = { false },
    private val consume: (T) -> Unit,
) : AutoCloseable {
    private val lock = Any()
    private val executor = Executors.newSingleThreadExecutor { task ->
        Thread(task, threadName).apply { isDaemon = true }
    }
    private val preserved = ArrayDeque<T>()
    private var pendingLatest: T? = null
    private var drainScheduled = false
    private var closed = false

    fun submit(value: T) {
        val shouldSchedule = synchronized(lock) {
            if (closed) return
            if (preserve(value)) {
                preserved.addLast(value)
            } else {
                pendingLatest = value
            }
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
                preserved.clear()
                pendingLatest = null
            }
        }
    }

    private fun drain() {
        while (true) {
            val next = synchronized(lock) {
                if (preserved.isNotEmpty()) {
                    preserved.removeFirst()
                } else {
                    pendingLatest.also {
                        pendingLatest = null
                        if (it == null) drainScheduled = false
                    }
                }
            } ?: return
            runCatching { consume(next) }
        }
    }

    override fun close() {
        synchronized(lock) {
            closed = true
            preserved.clear()
            pendingLatest = null
        }
        executor.shutdownNow()
    }
}
