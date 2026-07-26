package com.local.matholickiosk.webpoc

import android.os.Handler
import android.os.Looper
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import java.util.concurrent.Executor

/** Owns the loopback tunnel and process-wide WebView proxy override for the app process lifetime. */
internal object WebViewProxyBootstrap {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val mainExecutor = Executor { task ->
        if (Looper.myLooper() == Looper.getMainLooper()) task.run() else mainHandler.post(task)
    }
    private val coordinator = ProxyBootstrapCoordinator(
        object : ProxyBootstrapPlatform {
            override fun isSupported(): Boolean =
                WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)

            override fun startProxy(): ProxyBootstrapHandle {
                val proxy = LoopbackConnectProxy.start()
                return object : ProxyBootstrapHandle {
                    override val port: Int = proxy.port

                    override fun close() {
                        proxy.close()
                    }
                }
            }

            override fun scheduleTimeout(onTimeout: () -> Unit): ProxyBootstrapTimeout {
                val timeout = Runnable(onTimeout)
                check(mainHandler.postDelayed(timeout, PROXY_BOOTSTRAP_TIMEOUT_MS)) {
                    "Web proxy bootstrap watchdog was rejected"
                }
                return ProxyBootstrapTimeout {
                    mainHandler.removeCallbacks(timeout)
                }
            }

            override fun applyOverride(proxyPort: Int, onReady: () -> Unit) {
                val config = ProxyConfig.Builder()
                    .addProxyRule("127.0.0.1:$proxyPort")
                    .build()
                ProxyController.getInstance().setProxyOverride(
                    config,
                    mainExecutor,
                    Runnable(onReady),
                )
            }
        },
    )

    fun ensureConfigured(callback: (ProxyBootstrapResult) -> Unit) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { ensureConfigured(callback) }
            return
        }

        coordinator.ensureConfigured(callback)
    }

    private const val PROXY_BOOTSTRAP_TIMEOUT_MS = 10_000L
}
