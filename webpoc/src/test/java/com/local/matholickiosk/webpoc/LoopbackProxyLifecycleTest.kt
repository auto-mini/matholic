package com.local.matholickiosk.webpoc

import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoopbackProxyLifecycleTest {
    @Test
    fun `resource permit pool rejects work above its hard cap`() {
        val pool = ResourcePermitPool(2)

        assertTrue(pool.tryAcquire())
        assertTrue(pool.tryAcquire())
        assertFalse(pool.tryAcquire())

        pool.release()
        assertTrue(pool.tryAcquire())
    }

    @Test
    fun `registry closes items registered after shutdown and retains none`() {
        val closed = mutableListOf<String>()
        val registry = CloseableRegistry<String> { closed += it }

        assertTrue(registry.register("existing"))
        assertTrue(registry.closeAll())
        assertFalse(registry.register("late"))

        assertEquals(listOf("existing", "late"), closed)
        assertFalse(registry.closeAll())
    }

    @Test
    fun `rejected proxy task runs cleanup instead of leaking ownership`() {
        val rejectingExecutor = Executor {
            throw RejectedExecutionException("synthetic shutdown race")
        }
        var taskRuns = 0
        var cleanupRuns = 0

        val accepted = ProxyTaskSubmission.submit(
            executor = rejectingExecutor,
            task = { taskRuns += 1 },
            onRejected = { cleanupRuns += 1 },
        )

        assertFalse(accepted)
        assertEquals(0, taskRuns)
        assertEquals(1, cleanupRuns)
    }

    @Test
    fun `accepted proxy task does not run rejection cleanup`() {
        var taskRuns = 0
        var cleanupRuns = 0

        val accepted = ProxyTaskSubmission.submit(
            executor = Executor { it.run() },
            task = { taskRuns += 1 },
            onRejected = { cleanupRuns += 1 },
        )

        assertTrue(accepted)
        assertEquals(1, taskRuns)
        assertEquals(0, cleanupRuns)
    }
}
