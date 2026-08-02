package com.local.matholickiosk.webpoc

internal enum class ProxyBootstrapResult {
    READY,
    UNSUPPORTED,
    FAILED,
}

internal interface ProxyBootstrapHandle {
    val port: Int
    fun close()
}

internal fun interface ProxyBootstrapTimeout {
    fun cancel()
}

internal interface ProxyBootstrapPlatform {
    fun isSupported(): Boolean
    fun startProxy(onUnexpectedTermination: () -> Unit): ProxyBootstrapHandle
    fun scheduleTimeout(onTimeout: () -> Unit): ProxyBootstrapTimeout
    fun applyOverride(proxyPort: Int, onReady: () -> Unit)
}

/**
 * Serial, main-thread state machine for process-wide WebView proxy setup.
 *
 * Android-specific feature checks and proxy APIs are injected so every startup failure can
 * complete deterministically and release a partially started loopback proxy.
 */
internal class ProxyBootstrapCoordinator(
    private val platform: ProxyBootstrapPlatform,
) {
    private enum class State {
        NEW,
        CONFIGURING,
        READY,
        UNSUPPORTED,
        FAILED,
    }

    private val callbacks = mutableListOf<(ProxyBootstrapResult) -> Unit>()
    private var state = State.NEW
    private var proxy: ProxyBootstrapHandle? = null
    private var timeout: ProxyBootstrapTimeout? = null
    private var runtimeRestartUsed = false

    fun ensureConfigured(callback: (ProxyBootstrapResult) -> Unit) {
        when (state) {
            State.READY -> callback(ProxyBootstrapResult.READY)
            State.UNSUPPORTED -> callback(ProxyBootstrapResult.UNSUPPORTED)
            State.FAILED -> callback(ProxyBootstrapResult.FAILED)
            State.CONFIGURING -> callbacks += callback
            State.NEW -> {
                callbacks += callback
                configure()
            }
        }
    }

    private fun configure() {
        try {
            if (!platform.isSupported()) {
                finish(State.UNSUPPORTED, ProxyBootstrapResult.UNSUPPORTED)
                return
            }

            val candidate = platform.startProxy(::handleUnexpectedTermination)
            proxy = candidate
            state = State.CONFIGURING
            val candidateTimeout = platform.scheduleTimeout {
                finish(State.FAILED, ProxyBootstrapResult.FAILED)
            }
            if (state != State.CONFIGURING) {
                cancelTimeout(candidateTimeout)
                return
            }
            timeout = candidateTimeout
            platform.applyOverride(candidate.port) {
                finish(State.READY, ProxyBootstrapResult.READY)
            }
        } catch (_: RuntimeException) {
            finish(State.FAILED, ProxyBootstrapResult.FAILED)
        }
    }

    private fun handleUnexpectedTermination() {
        if (state != State.CONFIGURING && state != State.READY) return
        val failedProxy = proxy
        proxy = null
        timeout?.let(::cancelTimeout)
        timeout = null
        runCatching { failedProxy?.close() }
        state = State.NEW
        if (runtimeRestartUsed) {
            finish(State.FAILED, ProxyBootstrapResult.FAILED)
            return
        }
        runtimeRestartUsed = true
        configure()
    }

    private fun finish(next: State, result: ProxyBootstrapResult) {
        if (state == State.READY || state == State.UNSUPPORTED || state == State.FAILED) return
        state = next
        timeout?.let(::cancelTimeout)
        timeout = null
        if (next != State.READY) {
            val failedProxy = proxy
            proxy = null
            try {
                failedProxy?.close()
            } catch (_: RuntimeException) {
                // Cleanup failure must not suppress the terminal bootstrap result.
            }
        }
        val pending = callbacks.toList()
        callbacks.clear()
        pending.forEach { it(result) }
    }

    private fun cancelTimeout(pendingTimeout: ProxyBootstrapTimeout) {
        try {
            pendingTimeout.cancel()
        } catch (_: RuntimeException) {
            // Watchdog cleanup failure must not suppress the terminal bootstrap result.
        }
    }
}
