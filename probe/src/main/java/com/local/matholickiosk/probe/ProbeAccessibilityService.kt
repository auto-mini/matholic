package com.local.matholickiosk.probe

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.lang.ref.WeakReference

class ProbeAccessibilityService : AccessibilityService() {
    private lateinit var windowManager: WindowManager
    private var overlay: LinearLayout? = null
    private var overlayStatus: TextView? = null
    private var captureReceiverRegistered = false
    private val redactionPolicy = RedactionPolicy()
    private val reportStore by lazy { ReportStore(this) }
    private val snapshotter by lazy { NodeSnapshotter(this, redactionPolicy) }

    override fun onServiceConnected() {
        super.onServiceConnected()
        current = WeakReference(this)
        serviceInfo = serviceInfo.apply {
            packageNames = arrayOf(ProbeConstants.TARGET_PACKAGE)
        }
        registerCaptureRequestReceiver()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!TargetPackagePolicy.isAllowed(event?.packageName)) return
        showOverlay()
    }

    override fun onInterrupt() {
        overlayStatus?.setText(R.string.service_interrupted)
    }

    override fun onDestroy() {
        hideOverlay()
        unregisterCaptureRequestReceiver()
        current = null
        super.onDestroy()
    }

    private val captureRequestReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_CAPTURE_REQUEST) captureCurrentWindow()
        }
    }

    private fun registerCaptureRequestReceiver() {
        if (captureReceiverRegistered) return
        registerReceiver(
            captureRequestReceiver,
            IntentFilter(ACTION_CAPTURE_REQUEST),
            Context.RECEIVER_NOT_EXPORTED,
        )
        captureReceiverRegistered = true
    }

    private fun unregisterCaptureRequestReceiver() {
        if (!captureReceiverRegistered) return
        try {
            unregisterReceiver(captureRequestReceiver)
        } catch (_: IllegalArgumentException) {
            // The system may already have detached the receiver with the service process.
        } finally {
            captureReceiverRegistered = false
        }
    }

    private fun showOverlay() {
        if (overlay != null) return

        val density = resources.displayMetrics.density
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((10 * density).toInt(), (6 * density).toInt(), (10 * density).toInt(), (6 * density).toInt())
            background = GradientDrawable().apply {
                cornerRadius = 12 * density
                setColor(Color.argb(235, 13, 71, 161))
            }
        }
        val status = TextView(this).apply {
            setText(R.string.overlay_gate)
            setTextColor(Color.WHITE)
            textSize = 13f
            setPadding(0, 0, (8 * density).toInt(), 0)
        }
        val captureButton = Button(this).apply {
            setText(R.string.capture_nodes)
            isAllCaps = false
            setOnClickListener { captureCurrentWindow() }
        }
        container.addView(status)
        container.addView(
            captureButton,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = (20 * density).toInt()
            title = "MatholicGate1Probe"
        }

        try {
            windowManager.addView(container, params)
            overlay = container
            overlayStatus = status
        } catch (_: RuntimeException) {
            overlay = null
            overlayStatus = null
        }
    }

    private fun captureCurrentWindow() {
        val root = rootInActiveWindow
        if (root == null || !TargetPackagePolicy.isAllowed(root.packageName)) {
            overlayStatus?.setText(R.string.not_target_screen)
            return
        }

        val window: AccessibilityWindowInfo? = windows.firstOrNull { it.id == root.windowId }
        try {
            val result = snapshotter.capture(root, window)
            reportStore.save(result.json)
            overlayStatus?.text = if (result.truncated) {
                getString(R.string.capture_saved_truncated, result.nodeCount)
            } else {
                getString(R.string.capture_saved, result.nodeCount)
            }
        } catch (error: Exception) {
            overlayStatus?.text = getString(R.string.capture_failed, error.javaClass.simpleName)
        }
    }

    private fun hideOverlay() {
        val view = overlay ?: return
        try {
            windowManager.removeView(view)
        } catch (_: RuntimeException) {
            // Already detached by the system.
        } finally {
            overlay = null
            overlayStatus = null
        }
    }

    companion object {
        const val ACTION_CAPTURE_REQUEST =
            "com.local.matholickiosk.probe.action.CAPTURE_REDACTED"

        @Volatile
        private var current: WeakReference<ProbeAccessibilityService>? = null

        fun hideOverlayIfConnected() {
            current?.get()?.hideOverlay()
        }

        fun showOverlayIfConnected() {
            current?.get()?.showOverlay()
        }

        fun isConnected(): Boolean = current?.get() != null
    }
}
