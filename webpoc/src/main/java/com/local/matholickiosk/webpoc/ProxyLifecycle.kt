package com.local.matholickiosk.webpoc

import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.Semaphore

internal class ResourcePermitPool(maxConcurrent: Int) {
    private val permits = Semaphore(maxConcurrent, true)

    fun tryAcquire(): Boolean = permits.tryAcquire()

    fun release() {
        permits.release()
    }
}

/**
 * Serializes resource registration with shutdown so a resource arriving after the close snapshot
 * is rejected and closed instead of being orphaned.
 */
internal class CloseableRegistry<T>(
    private val closeItem: (T) -> Unit,
) {
    @Volatile
    private var closed = false
    private val items = mutableSetOf<T>()

    val isClosed: Boolean
        get() = closed

    fun register(item: T): Boolean {
        val accepted = synchronized(this) {
            if (closed) {
                false
            } else {
                items += item
                true
            }
        }
        if (!accepted) closeItem(item)
        return accepted
    }

    fun close(item: T) {
        synchronized(this) {
            items -= item
        }
        closeItem(item)
    }

    fun closeAll(beforeItems: () -> Unit = {}): Boolean {
        val snapshot = synchronized(this) {
            if (closed) return false
            closed = true
            val registeredItems = items.toList()
            items.clear()
            registeredItems
        }
        try {
            beforeItems()
        } finally {
            snapshot.forEach(closeItem)
        }
        return true
    }
}

internal object ProxyTaskSubmission {
    fun submit(
        executor: Executor,
        task: () -> Unit,
        onRejected: () -> Unit,
    ): Boolean = try {
        executor.execute(task)
        true
    } catch (_: RejectedExecutionException) {
        onRejected()
        false
    }
}
