package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.SensitiveTask
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SensitiveTaskTest {
    @Test
    fun discardedQueuedTaskWipesWithoutRunningAndCannotRunLater() {
        val executions = AtomicInteger()
        val cleanups = AtomicInteger()
        val task = SensitiveTask(
            cleanup = { cleanups.incrementAndGet() },
            operation = { executions.incrementAndGet() },
        )

        task.discard()
        task.run()
        task.discard()

        assertEquals(0, executions.get())
        assertEquals(1, cleanups.get())
    }

    @Test
    fun startedTaskWipesExactlyOnceEvenWhenOperationFails() {
        val cleanups = AtomicInteger()
        val task = SensitiveTask(
            cleanup = { cleanups.incrementAndGet() },
            operation = { error("synthetic failure") },
        )

        assertThrows(IllegalStateException::class.java) { task.run() }
        task.discard()

        assertEquals(1, cleanups.get())
    }
}
