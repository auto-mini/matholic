package com.local.matholickiosk.kiosk

import android.app.Activity
import android.content.SharedPreferences
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.TextView

internal class RemoteSupportWindowController(
    private val activity: Activity,
    private val handler: Handler,
    private val store: RemoteSupportStore,
    private val onStateChanged: (Boolean) -> Unit = {},
) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val badges: List<TextView> by lazy {
        listOfNotNull(
            activity.findViewById(R.id.remote_support_badge),
            activity.findViewById(R.id.scanner_remote_support_badge),
        )
    }
    private var started = false
    private var sensitiveScreen = true
    private val expireRunnable = Runnable(::refresh)

    fun start() {
        if (started) return
        started = true
        store.registerListener(this)
        refresh()
    }

    fun refresh() {
        handler.removeCallbacks(expireRunnable)
        val activeUntil = store.activeUntilEpochMillis()
        val active = activeUntil != null
        if (RemoteSupportPolicy.canCapture(active, sensitiveScreen)) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        if (active) {
            showBadge()
            val remaining = (activeUntil - System.currentTimeMillis()).coerceAtLeast(1L)
            handler.postDelayed(expireRunnable, remaining.coerceAtMost(MAX_TIMER_DELAY_MILLIS))
        } else {
            removeBadge()
        }
        onStateChanged(active)
    }

    fun setSensitiveScreen(sensitive: Boolean) {
        if (sensitiveScreen == sensitive) return
        sensitiveScreen = sensitive
        refresh()
    }

    fun stop() {
        if (!started) return
        started = false
        store.unregisterListener(this)
        handler.removeCallbacks(expireRunnable)
        removeBadge()
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?,
    ) {
        if (key == RemoteSupportStore.KEY_EXPIRES_AT ||
            key == RemoteSupportStore.KEY_BOOT_COUNT
        ) {
            handler.post(::refresh)
        }
    }

    private fun showBadge() {
        badges.forEach { badge -> badge.visibility = View.VISIBLE }
    }

    private fun removeBadge() {
        badges.forEach { badge -> badge.visibility = View.GONE }
    }

    private companion object {
        const val MAX_TIMER_DELAY_MILLIS = 60L * 60L * 1_000L
    }
}
