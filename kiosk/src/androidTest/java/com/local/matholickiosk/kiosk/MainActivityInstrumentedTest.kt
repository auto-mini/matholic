package com.local.matholickiosk.kiosk

import android.content.Context
import android.view.View
import android.view.WindowManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.data.AdminAuthRepository
import com.local.matholickiosk.kiosk.data.KioskDatabase
import com.local.matholickiosk.kiosk.data.StudentRepository
import com.local.matholickiosk.kiosk.security.AndroidKeystoreCredentialCipher
import org.junit.Assert.assertArrayEquals
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

    @Test
    fun qrReissueRequiresExplicitConfirmation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())
        val repository = StudentRepository(
            database = database,
            cipher = AndroidKeystoreCredentialCipher(),
            appVersion = "instrumented-test",
        )
        val registered = repository.registerStudent(
            displayNameExact = "가상학생-확인",
            username = "synthetic-user".toCharArray(),
            password = "synthetic-password".toCharArray(),
        )
        val originalHash = registered.issuedQr.hash.copyOf()

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.auth_panel).visibility == View.VISIBLE
            }
            scenario.onActivity { activity ->
                activity.findViewById<android.widget.EditText>(R.id.pin_input)
                    .setText("654321")
                activity.findViewById<View>(R.id.auth_submit).performClick()
            }
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE &&
                    activity.findViewById<android.widget.Spinner>(R.id.student_spinner).count == 1
            }
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.reissue_qr_button).performClick()
            }

            Thread.sleep(500)

            assertArrayEquals(
                originalHash,
                database.studentDao().findById(registered.studentId)!!.qrTokenHash,
            )
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
