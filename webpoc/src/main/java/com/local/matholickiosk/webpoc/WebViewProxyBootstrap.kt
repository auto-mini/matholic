package com.local.matholickiosk.webpoc

import android.os.Handler
import android.os.Looper
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import java.util.concurrent.Executor

internal enum class ProxyBootstrapResult {
    READY,
    UNSUPPORTED,
    FAILED,
}

/** Owns the loopback tunnel and process-wide WebView proxy override for the app process lifetime. */
internal object WebViewProxyBootstrap {
    private enum class State {
        NEW,
        CONFIGURING,
        READY,
        UNSUPPORTED,
        FAILED,
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val mainExecutor = Executor { task ->
        if (Looper.myLooper() == Looper.getMainLooper()) task.run() else mainHandler.post(task)
    }
    private val callbacks = mutableListOf<(ProxyBootstrapResult) -> Unit>()
    private var state = State.NEW
    private var proxy: LoopbackConnectProxy? = null

    fun ensureConfigured(callback: (ProxyBootstrapResult) -> Unit) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { ensureConfigured(callback) }
            return
        }

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
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            finish(State.UNSUPPORTED, ProxyBootstrapResult.UNSUPPORTED)
            return
        }

        val candidate = runCatching { LoopbackConnectProxy.start() }.getOrElse {
            finish(State.FAILED, ProxyBootstrapResult.FAILED)
            return
        }
        proxy = candidate
        state = State.CONFIGURING
        val config = ProxyConfig.Builder()
            .addProxyRule("127.0.0.1:${candidate.port}")
            .build()
        runCatching {
            ProxyController.getInstance().setProxyOverride(
                config,
                mainExecutor,
                Runnable { finish(State.READY, ProxyBootstrapResult.READY) },
            )
        }.onFailure {
            finish(State.FAILED, ProxyBootstrapResult.FAILED)
        }
    }

    private fun finish(next: State, result: ProxyBootstrapResult) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { finish(next, result) }
            return
        }
        if (state == State.READY || state == State.UNSUPPORTED || state == State.FAILED) return
        state = next
        if (next != State.READY) {
            proxy?.close()
            proxy = null
        }
        val pending = callbacks.toList()
        callbacks.clear()
        pending.forEach { it(result) }
    }
}
