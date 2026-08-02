package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.LatestValueDispatcher
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LatestValueDispatcherTest {
    @Test
    fun `coalesces queued values to the latest update`() {
        val consumed = Collections.synchronizedList(mutableListOf<String>())
        val firstStarted = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val latestConsumed = CountDownLatch(1)
        val dispatcher = LatestValueDispatcher<String>("latest-value-test") { value ->
            consumed += value
            if (value == "first") {
                firstStarted.countDown()
                releaseFirst.await(2, TimeUnit.SECONDS)
            }
            if (value == "latest") latestConsumed.countDown()
        }
        try {
            dispatcher.submit("first")
            assertTrue(firstStarted.await(2, TimeUnit.SECONDS))
            dispatcher.submit("stale")
            dispatcher.submit("latest")
            releaseFirst.countDown()
            assertTrue(latestConsumed.await(2, TimeUnit.SECONDS))

            assertEquals(listOf("first", "latest"), consumed)
        } finally {
            releaseFirst.countDown()
            dispatcher.close()
        }
    }
}
