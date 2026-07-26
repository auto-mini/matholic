package com.local.matholickiosk.webpoc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProxyBootstrapCoordinatorTest {
    @Test
    fun `feature detection failure completes as failed`() {
        val platform = FakePlatform(
            supportFailure = IllegalStateException("synthetic feature detection failure"),
        )
        val results = mutableListOf<ProxyBootstrapResult>()

        ProxyBootstrapCoordinator(platform).ensureConfigured(results::add)

        assertEquals(listOf(ProxyBootstrapResult.FAILED), results)
        assertEquals(0, platform.startCalls)
    }

    @Test
    fun `override setup failure closes the started proxy and completes as failed`() {
        val platform = FakePlatform(
            applyFailure = IllegalStateException("synthetic override failure"),
        )
        val results = mutableListOf<ProxyBootstrapResult>()

        ProxyBootstrapCoordinator(platform).ensureConfigured(results::add)

        assertEquals(listOf(ProxyBootstrapResult.FAILED), results)
        assertEquals(1, platform.handle.closeCalls)
        platform.readyCallback?.invoke()
        assertEquals(listOf(ProxyBootstrapResult.FAILED), results)
    }

    @Test
    fun `unsupported platform completes without starting a proxy`() {
        val platform = FakePlatform(supported = false)
        val results = mutableListOf<ProxyBootstrapResult>()

        ProxyBootstrapCoordinator(platform).ensureConfigured(results::add)

        assertEquals(listOf(ProxyBootstrapResult.UNSUPPORTED), results)
        assertEquals(0, platform.startCalls)
        assertNull(platform.readyCallback)
    }

    @Test
    fun `configuration queues callbacks and retains the proxy after success`() {
        val platform = FakePlatform()
        val coordinator = ProxyBootstrapCoordinator(platform)
        val first = mutableListOf<ProxyBootstrapResult>()
        val second = mutableListOf<ProxyBootstrapResult>()

        coordinator.ensureConfigured(first::add)
        coordinator.ensureConfigured(second::add)
        assertEquals(emptyList<ProxyBootstrapResult>(), first)
        assertEquals(emptyList<ProxyBootstrapResult>(), second)

        platform.readyCallback?.invoke()

        assertEquals(listOf(ProxyBootstrapResult.READY), first)
        assertEquals(listOf(ProxyBootstrapResult.READY), second)
        assertEquals(0, platform.handle.closeCalls)

        val late = mutableListOf<ProxyBootstrapResult>()
        coordinator.ensureConfigured(late::add)
        assertEquals(listOf(ProxyBootstrapResult.READY), late)
    }

    @Test
    fun `proxy close failure does not suppress the failed result`() {
        val platform = FakePlatform(
            applyFailure = IllegalStateException("synthetic override failure"),
        )
        platform.handle.closeFailure = IllegalStateException("synthetic close failure")
        val results = mutableListOf<ProxyBootstrapResult>()

        ProxyBootstrapCoordinator(platform).ensureConfigured(results::add)

        assertEquals(listOf(ProxyBootstrapResult.FAILED), results)
        assertEquals(1, platform.handle.closeCalls)
    }

    private class FakeHandle : ProxyBootstrapHandle {
        override val port: Int = 43210
        var closeCalls = 0
        var closeFailure: RuntimeException? = null

        override fun close() {
            closeCalls += 1
            closeFailure?.let { throw it }
        }
    }

    private class FakePlatform(
        private val supported: Boolean = true,
        private val supportFailure: RuntimeException? = null,
        private val applyFailure: RuntimeException? = null,
    ) : ProxyBootstrapPlatform {
        val handle = FakeHandle()
        var startCalls = 0
        var readyCallback: (() -> Unit)? = null

        override fun isSupported(): Boolean {
            supportFailure?.let { throw it }
            return supported
        }

        override fun startProxy(): ProxyBootstrapHandle {
            startCalls += 1
            return handle
        }

        override fun applyOverride(proxyPort: Int, onReady: () -> Unit) {
            assertEquals(handle.port, proxyPort)
            readyCallback = onReady
            applyFailure?.let { throw it }
        }
    }
}
