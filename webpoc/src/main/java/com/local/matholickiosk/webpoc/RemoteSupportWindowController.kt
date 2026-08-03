package com.local.matholickiosk.webpoc

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

internal class RemoteSupportWindowController(
    private val activity: Activity,
    private val handler: Handler,
    private val store: RemoteSupportStore,
    private val onActiveChanged: (Boolean) -> Unit = {},
) : SharedPreferences.OnSharedPreferenceChangeListener {
    private var badge: TextView? = null
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
        val captureAllowed = RemoteSupportPolicy.canCapture(
            supportActive = activeUntil != null,
            sensitiveScreen = sensitiveScreen,
        )
        onActiveChanged(captureAllowed)
        if (captureAllowed) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        if (activeUntil != null) {
            showBadge()
            val remaining = (activeUntil - System.currentTimeMillis()).coerceAtLeast(1L)
            handler.postDelayed(expireRunnable, remaining.coerceAtMost(MAX_TIMER_DELAY_MILLIS))
        } else {
            removeBadge()
        }
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
        onActiveChanged(false)
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
        if (badge?.parent != null) return
        val content = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        val view = badge ?: TextView(activity).apply {
            text = "원격 점검 중"
            setTextColor(Color.WHITE)
            textSize = 12f
            setPadding(dp(10), dp(5), dp(10), dp(5))
            background = GradientDrawable().apply {
                setColor(Color.rgb(183, 28, 28))
                cornerRadius = dp(14).toFloat()
            }
            elevation = dp(12).toFloat()
        }.also { badge = it }
        content.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END,
            ).apply {
                topMargin = dp(8)
                marginEnd = dp(8)
            },
        )
    }

    private fun removeBadge() {
        (badge?.parent as? ViewGroup)?.removeView(badge)
    }

    private fun dp(value: Int): Int =
        (value * activity.resources.displayMetrics.density).toInt()

    private companion object {
        const val MAX_TIMER_DELAY_MILLIS = 60L * 60L * 1_000L
    }
}
