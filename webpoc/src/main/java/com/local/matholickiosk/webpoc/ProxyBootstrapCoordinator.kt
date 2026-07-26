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

internal interface ProxyBootstrapPlatform {
    fun isSupported(): Boolean
    fun startProxy(): ProxyBootstrapHandle
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

            val candidate = platform.startProxy()
            proxy = candidate
            state = State.CONFIGURING
            platform.applyOverride(candidate.port) {
                finish(State.READY, ProxyBootstrapResult.READY)
            }
        } catch (_: RuntimeException) {
            finish(State.FAILED, ProxyBootstrapResult.FAILED)
        }
    }

    private fun finish(next: State, result: ProxyBootstrapResult) {
        if (state == State.READY || state == State.UNSUPPORTED || state == State.FAILED) return
        state = next
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
}
