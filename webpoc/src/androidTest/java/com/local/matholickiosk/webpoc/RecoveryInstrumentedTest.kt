package com.local.matholickiosk.webpoc

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RecoveryInstrumentedTest {
    private lateinit var context: Context

    @Before
    fun resetState() {
        context = ApplicationProvider.getApplicationContext()
        preferences().edit().clear().commit()
    }

    @After
    fun leaveSafeState() {
        preferences().edit().putString(KEY_STATE, WebPocState.IDLE.name).commit()
    }

    @Test
    fun idleLaunchPassesPublicLoginPreflight() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use {
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }
        }
    }

    @Test
    fun activeRestartNeverSilentlyResumesSensitiveState() {
        writeState(WebPocState.ACTIVE)
        ActivityScenario.launch(MainActivity::class.java).use {
            assertTrueWithin(TIMEOUT_SECONDS) {
                readState() in setOf(
                    WebPocState.IDLE,
                    WebPocState.LOCKED,
                    WebPocState.MAINTENANCE_REQUIRED,
                )
            }
        }
    }

    @Test
    fun explicitLockDoesNotAutoResume() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use {
            TimeUnit.SECONDS.sleep(2)
            assertEquals(WebPocState.LOCKED, readState())
        }
    }

    @Test
    fun corruptStoredStateAbortsGate3AndRecoversSafely() {
        preferences().edit()
            .putString(KEY_STATE, "CORRUPT_STATE")
            .putString(KEY_GATE3_STATUS, "RUNNING")
            .putInt(KEY_GATE3_COMPLETED, 23)
            .commit()

        ActivityScenario.launch(MainActivity::class.java).use {
            assertTrueWithin(TIMEOUT_SECONDS) {
                readState() in setOf(
                    WebPocState.IDLE,
                    WebPocState.LOCKED,
                    WebPocState.MAINTENANCE_REQUIRED,
                )
            }
            assertEquals("ABORTED", preferences().getString(KEY_GATE3_STATUS, null))
            assertEquals(23, preferences().getInt(KEY_GATE3_COMPLETED, -1))
        }
    }

    @Test
    fun lockedGate3RestartRemainsLockedAndNeverResumes() {
        preferences().edit()
            .putString(KEY_STATE, WebPocState.LOCKED.name)
            .putString(KEY_GATE3_STATUS, "RUNNING")
            .putInt(KEY_GATE3_COMPLETED, 23)
            .commit()

        ActivityScenario.launch(MainActivity::class.java).use {
            assertTrueWithin(5) { readState() == WebPocState.LOCKED }
            assertEquals("ABORTED", preferences().getString(KEY_GATE3_STATUS, null))
            assertEquals(23, preferences().getInt(KEY_GATE3_COMPLETED, -1))
        }
    }

    @Test
    fun maintenanceGate3RestartRemainsTerminalAndNeverResumes() {
        preferences().edit()
            .putString(KEY_STATE, WebPocState.MAINTENANCE_REQUIRED.name)
            .putString(KEY_GATE3_STATUS, "RUNNING")
            .putInt(KEY_GATE3_COMPLETED, 23)
            .commit()

        ActivityScenario.launch(MainActivity::class.java).use {
            assertTrueWithin(5) { readState() == WebPocState.MAINTENANCE_REQUIRED }
            assertEquals("ABORTED", preferences().getString(KEY_GATE3_STATUS, null))
            assertEquals(23, preferences().getInt(KEY_GATE3_COMPLETED, -1))
        }
    }

    @Test
    fun unapprovedTopLevelNavigationFailsClosed() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { }
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }
            scenario.onUiInitialized { activity ->
                activity.findViewById<android.webkit.WebView>(R.id.web_view)
                    .loadUrl("https://example.invalid/")
            }
            assertTrueWithin(5) { readState() == WebPocState.LOCKED }
        }
    }

    @Test
    fun activityPreventsScreenshotsAndRecentTaskPreview() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val flags = activity.window.attributes.flags
                assertEquals(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    flags and WindowManager.LayoutParams.FLAG_SECURE,
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun webViewDisablesPersistentCacheAndLocalFileAccess() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                val settings = activity.findViewById<android.webkit.WebView>(R.id.web_view).settings
                assertEquals(WebSettings.LOAD_NO_CACHE, settings.cacheMode)
                assertFalse(settings.allowFileAccess)
                assertFalse(settings.allowContentAccess)
                assertFalse(settings.allowFileAccessFromFileURLs)
                assertFalse(settings.allowUniversalAccessFromFileURLs)
                assertFalse(settings.saveFormData)
            }
        }
    }

    @Test
    fun interruptedGate3RunIsMarkedAbortedAndNeverResumed() {
        preferences().edit()
            .putString(KEY_STATE, WebPocState.IDLE.name)
            .putString(KEY_GATE3_STATUS, "RUNNING")
            .putInt(KEY_GATE3_COMPLETED, 17)
            .commit()

        ActivityScenario.launch(MainActivity::class.java).use {
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }
            assertEquals("ABORTED", preferences().getString(KEY_GATE3_STATUS, null))
            assertEquals(17, preferences().getInt(KEY_GATE3_COMPLETED, -1))
        }
    }

    @Test
    fun interruptedGate3LoginIsAbortedAndRecoveredWithoutResume() {
        assertInterruptedGate3SensitiveState(WebPocState.LOGIN_SUBMIT)
    }

    @Test
    fun interruptedGate3ActiveSessionIsAbortedAndRecoveredWithoutResume() {
        assertInterruptedGate3SensitiveState(WebPocState.ACTIVE)
    }

    @Test
    fun interruptedGate3LogoutIsAbortedAndRecoveredWithoutResume() {
        assertInterruptedGate3SensitiveState(WebPocState.LOGOUT_SUBMIT)
    }

    @Test
    fun gate3RejectsSameExpectedNameBeforeLogin() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { }
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }
            scenario.onUiInitialized { activity ->
                activity.findViewById<View>(R.id.gate3_mode_button).performClick()
                activity.findViewById<EditText>(R.id.expected_name).setText("가상 학생")
                activity.findViewById<EditText>(R.id.username).setText("virtual-a")
                activity.findViewById<EditText>(R.id.password).setText("virtual-pass-a")
                activity.findViewById<EditText>(R.id.gate3_expected_name_b).setText("  가상   학생  ")
                activity.findViewById<EditText>(R.id.gate3_username_b).setText("virtual-b")
                activity.findViewById<EditText>(R.id.gate3_password_b).setText("virtual-pass-b")
                activity.findViewById<View>(R.id.gate3_start_button).performClick()

                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.setup_error).visibility)
            }
            assertEquals(WebPocState.IDLE, readState())
            assertNull(preferences().getString(KEY_GATE3_STATUS, null))
        }
    }

    @Test
    fun allGate3InputsDisableStateSavingAndAutofill() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                listOf(
                    R.id.expected_name,
                    R.id.username,
                    R.id.password,
                    R.id.gate3_expected_name_b,
                    R.id.gate3_username_b,
                    R.id.gate3_password_b,
                ).forEach { id ->
                    val input = activity.findViewById<EditText>(id)
                    assertFalse(input.isSaveEnabled)
                    assertEquals(View.IMPORTANT_FOR_AUTOFILL_NO, input.importantForAutofill)
                    assertTrue(input.filterTouchesWhenObscured)
                }
            }
        }
    }

    @Test
    fun visibilityLossWipesAllGate3SetupInputs() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrueWithin(5) { readState() == WebPocState.LOCKED }
            scenario.onUiInitialized { activity ->
                listOf(
                    R.id.expected_name,
                    R.id.username,
                    R.id.password,
                    R.id.gate3_expected_name_b,
                    R.id.gate3_username_b,
                    R.id.gate3_password_b,
                ).forEachIndexed { index, id ->
                    activity.findViewById<EditText>(id).setText("synthetic-$index")
                }
            }

            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)

            scenario.onUiInitialized { activity ->
                listOf(
                    R.id.expected_name,
                    R.id.username,
                    R.id.password,
                    R.id.gate3_expected_name_b,
                    R.id.gate3_username_b,
                    R.id.gate3_password_b,
                ).forEach { id ->
                    assertTrue(activity.findViewById<EditText>(id).text.isNullOrEmpty())
                }
            }
        }
    }

    private fun preferences() = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private fun assertInterruptedGate3SensitiveState(interruptedState: WebPocState) {
        preferences().edit()
            .putString(KEY_STATE, interruptedState.name)
            .putString(KEY_GATE3_STATUS, "RUNNING")
            .putInt(KEY_GATE3_COMPLETED, 17)
            .commit()

        ActivityScenario.launch(MainActivity::class.java).use {
            assertTrueWithin(TIMEOUT_SECONDS) {
                readState() in setOf(
                    WebPocState.IDLE,
                    WebPocState.LOCKED,
                    WebPocState.MAINTENANCE_REQUIRED,
                )
            }
            assertEquals("ABORTED", preferences().getString(KEY_GATE3_STATUS, null))
            assertEquals(17, preferences().getInt(KEY_GATE3_COMPLETED, -1))
        }
    }

    private fun writeState(state: WebPocState) {
        preferences().edit().putString(KEY_STATE, state.name).commit()
    }

    private fun readState(): WebPocState {
        val stored = preferences().getString(KEY_STATE, null) ?: return WebPocState.RECOVERY_REQUIRED
        return runCatching { WebPocState.valueOf(stored) }.getOrDefault(WebPocState.RECOVERY_REQUIRED)
    }

    private fun ActivityScenario<MainActivity>.onUiInitialized(block: (MainActivity) -> Unit) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(UI_TIMEOUT_SECONDS)
        while (System.nanoTime() < deadline) {
            var invoked = false
            onActivity { activity ->
                if (activity.findViewById<View?>(R.id.web_view) != null) {
                    block(activity)
                    invoked = true
                }
            }
            if (invoked) return
            TimeUnit.MILLISECONDS.sleep(100)
        }
        throw AssertionError("Activity UI was not initialized within $UI_TIMEOUT_SECONDS seconds")
    }

    private fun assertTrueWithin(seconds: Long, predicate: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds)
        while (System.nanoTime() < deadline) {
            if (predicate()) return
            TimeUnit.MILLISECONDS.sleep(250)
        }
        val reason = preferences().getString(KEY_REASON, null)
        throw AssertionError("state did not reach expected safe value; final=${readState()}, reason=$reason")
    }

    private companion object {
        const val PREFERENCES_NAME = "web_poc_state"
        const val KEY_STATE = "state"
        const val KEY_REASON = "reason"
        const val KEY_GATE3_STATUS = "gate3_status"
        const val KEY_GATE3_COMPLETED = "gate3_completed"
        const val UI_TIMEOUT_SECONDS = 10L
        const val TIMEOUT_SECONDS = 40L
    }
}
