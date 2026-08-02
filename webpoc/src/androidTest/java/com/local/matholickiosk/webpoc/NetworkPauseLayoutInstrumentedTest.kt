package com.local.matholickiosk.webpoc

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkPauseLayoutInstrumentedTest {
    @Test
    fun pausePanelIsOpaqueFocusableAndAccessibilityVisible() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val root = LayoutInflater.from(context).inflate(R.layout.activity_main, null, false)
        val panel = root.findViewById<View>(R.id.network_pause_panel)
        val background = panel.background as ColorDrawable

        assertEquals(255, background.color ushr 24 and 0xff)
        assertTrue(panel.isClickable)
        assertTrue(panel.isFocusable)
        assertEquals(
            View.IMPORTANT_FOR_ACCESSIBILITY_YES,
            panel.importantForAccessibility,
        )
    }
}
