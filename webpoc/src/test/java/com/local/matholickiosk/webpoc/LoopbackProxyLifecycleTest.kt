package com.local.matholickiosk.webpoc

import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LoopbackProxyLifecycleTest {
    @Test
    fun `actual listener rejects the connection above its tunnel cap`() {
        val baselineWorkers = proxyWorkerThreadCount()
        val proxy = LoopbackConnectProxy.start()
        val heldClients = mutableListOf<Socket>()
        try {
            repeat(EXPECTED_MAX_TUNNELS) {
                heldClients += Socket().apply {
                    connect(InetSocketAddress("127.0.0.1", proxy.port), CONNECT_TIMEOUT_MS)
                    soTimeout = READ_TIMEOUT_MS
                    getOutputStream().apply {
                        write('C'.code)
                        flush()
                    }
                }
            }
            assertTrue(
                "all capped handlers should own their connection before overflow",
                waitUntil(WORKER_START_TIMEOUT_MS) {
                    proxyWorkerThreadCount() >= baselineWorkers + EXPECTED_MAX_TUNNELS
                },
            )

            Socket().use { overflow ->
                overflow.connect(InetSocketAddress("127.0.0.1", proxy.port), CONNECT_TIMEOUT_MS)
                overflow.soTimeout = READ_TIMEOUT_MS
                val statusLine = overflow.getInputStream()
                    .bufferedReader(StandardCharsets.ISO_8859_1)
                    .readLine()

                assertEquals("HTTP/1.1 503 Service Unavailable", statusLine)
            }
        } finally {
            heldClients.forEach { runCatching { it.close() } }
            proxy.close()
        }
    }

    @Test
    fun `actual successful tunnel closes after its configured idle timeout`() {
        ServerSocket(0).use { upstreamServer ->
            val proxy = LoopbackConnectProxy.startForTesting(
                upstreamSocketConnector = UpstreamSocketConnector { _, timeoutMs ->
                    Socket().apply {
                        connect(
                            InetSocketAddress("127.0.0.1", upstreamServer.localPort),
                            timeoutMs,
                        )
                    }
                },
                tunnelIdleTimeoutMs = TEST_TUNNEL_IDLE_TIMEOUT_MS,
            )
            try {
                Socket().use { client ->
                    client.connect(InetSocketAddress("127.0.0.1", proxy.port), CONNECT_TIMEOUT_MS)
                    client.soTimeout = READ_TIMEOUT_MS
                    client.getOutputStream().apply {
                        write(
                            "CONNECT idle-probe.matholic.com:443 HTTP/1.1\r\n\r\n"
                                .toByteArray(StandardCharsets.ISO_8859_1),
                        )
                        flush()
                    }
                    val reader = client.getInputStream()
                        .bufferedReader(StandardCharsets.ISO_8859_1)
                    assertEquals("HTTP/1.1 200 Connection Established", reader.readLine())
                    assertEquals("", reader.readLine())

                    val idleStartedAt = System.nanoTime()
                    assertEquals(-1, reader.read())
                    val idleElapsedMs = TimeUnit.NANOSECONDS.toMillis(
                        System.nanoTime() - idleStartedAt,
                    )

                    assertTrue(
                        "idle tunnel closed too early after ${idleElapsedMs}ms",
                        idleElapsedMs >= MIN_EXPECTED_IDLE_ELAPSED_MS,
                    )
                    assertTrue(
                        "idle tunnel did not close within ${READ_TIMEOUT_MS}ms",
                        idleElapsedMs < READ_TIMEOUT_MS,
                    )
                }
            } finally {
                proxy.close()
            }
        }
    }

    @Test
    fun `failed upstream connect closes the allocated socket`() {
        val socket = Socket()
        val connector = DirectUpstreamSocketConnector { socket }

        assertThrows(IOException::class.java) {
            connector.connect(
                ConnectTarget("127.0.0.1", 0),
                CONNECT_TIMEOUT_MS,
            )
        }

        assertTrue(socket.isClosed)
    }

    @Test
    fun `unexpected listener failure reaches the health callback`() {
        val terminated = CountDownLatch(1)
        val proxy = LoopbackConnectProxy.start(terminated::countDown)
        try {
            val serverSocket = LoopbackConnectProxy::class.java
                .getDeclaredField("serverSocket")
                .apply { isAccessible = true }
                .get(proxy) as ServerSocket

            serverSocket.close()

            assertTrue(terminated.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        } finally {
            proxy.close()
        }
    }

    @Test
    fun `intentional listener close does not report a health failure`() {
        val terminated = CountDownLatch(1)
        val proxy = LoopbackConnectProxy.start(terminated::countDown)

        proxy.close()

        assertFalse(terminated.await(CLOSE_SETTLE_MS, TimeUnit.MILLISECONDS))
    }

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

    private fun proxyWorkerThreadCount(): Int = Thread.getAllStackTraces().keys.count {
        it.isAlive && it.name == PROXY_WORKER_THREAD_NAME
    }

    private fun waitUntil(timeoutMs: Long, condition: () -> Boolean): Boolean {
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs)
        while (System.nanoTime() < deadline) {
            if (condition()) return true
            Thread.sleep(POLL_INTERVAL_MS)
        }
        return condition()
    }

    private companion object {
        const val EXPECTED_MAX_TUNNELS = 32
        const val CONNECT_TIMEOUT_MS = 2_000
        const val READ_TIMEOUT_MS = 2_000
        const val WORKER_START_TIMEOUT_MS = 5_000L
        const val CALLBACK_TIMEOUT_MS = 2_000L
        const val CLOSE_SETTLE_MS = 250L
        const val POLL_INTERVAL_MS = 10L
        const val TEST_TUNNEL_IDLE_TIMEOUT_MS = 250
        const val MIN_EXPECTED_IDLE_ELAPSED_MS = 100L
        const val PROXY_WORKER_THREAD_NAME = "matholic-loopback-proxy"
    }
}
