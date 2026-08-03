package com.local.matholickiosk.webpoc

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkPauseLayoutInstrumentedTest {
    @Test
    fun pausePanelIsOpaqueFocusableAndAccessibilityVisible() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        var alpha = 0
        var clickable = false
        var focusable = false
        var accessibilityImportance = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        instrumentation.runOnMainSync {
            val root = LayoutInflater.from(context).inflate(
                R.layout.activity_main,
                null,
                false,
            )
            val panel = root.findViewById<View>(R.id.network_pause_panel)
            val background = panel.background as ColorDrawable
            alpha = background.color ushr 24 and 0xff
            clickable = panel.isClickable
            focusable = panel.isFocusable
            accessibilityImportance = panel.importantForAccessibility
            root.findViewById<WebView>(R.id.web_view).destroy()
        }

        assertEquals(255, alpha)
        assertTrue(clickable)
        assertTrue(focusable)
        assertEquals(
            View.IMPORTANT_FOR_ACCESSIBILITY_YES,
            accessibilityImportance,
        )
    }
}
