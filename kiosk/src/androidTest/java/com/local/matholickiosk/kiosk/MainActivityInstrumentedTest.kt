package com.local.matholickiosk.kiosk

import android.content.Context
import android.view.View
import android.view.WindowManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.data.AdminAuthRepository
import com.local.matholickiosk.kiosk.data.KioskDatabase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {
    @Test
    fun adminPinUnlocksUiAndWindowRemainsSecure() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.auth_panel).visibility == View.VISIBLE
            }
            scenario.onActivity { activity ->
                assertTrue(
                    activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0,
                )
                assertTrue(
                    activity.window.attributes.flags and
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0,
                )
                activity.findViewById<android.widget.EditText>(R.id.pin_input)
                    .setText("654321")
                activity.findViewById<View>(R.id.auth_submit).performClick()
            }
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE
            }
            scenario.onActivity {
                it.onBackPressedDispatcher.onBackPressed()
            }
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.auth_panel).visibility == View.VISIBLE
            }
        }
        database.clearAllTables()
    }

    private fun waitUntil(
        scenario: ActivityScenario<MainActivity>,
        condition: (MainActivity) -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + 15_000
        while (System.currentTimeMillis() < deadline) {
            var matched = false
            scenario.onActivity { matched = condition(it) }
            if (matched) return
            Thread.sleep(100)
        }
        throw AssertionError("UI condition was not met before timeout")
    }
}
