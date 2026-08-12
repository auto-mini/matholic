package com.local.matholickiosk.webpoc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.ValueCallback
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject
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
    fun recoveryCanonicalizesRejectedLoginRedirectAndReturnsIdle() {
        writeState(WebPocState.RECOVERY_REQUIRED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }
            scenario.onUiInitialized { activity ->
                assertEquals(
                    WebSecurityPolicy.LOGIN_URL,
                    activity.findViewById<WebView>(R.id.web_view).url,
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

    @Suppress("DEPRECATION")
    @Test
    fun systemBackIsConsumedWithoutFinishingWebActivity() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                assertFalse(activity.isFinishing)
                activity.onBackPressed()
                assertFalse(activity.isFinishing)
                assertTrue(
                    activity.onKeyDown(
                        KeyEvent.KEYCODE_BACK,
                        KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK),
                    ),
                )
                assertTrue(
                    activity.onKeyUp(
                        KeyEvent.KEYCODE_BACK,
                        KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK),
                    ),
                )
                assertFalse(activity.isFinishing)
            }
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(
                KeyEvent.KEYCODE_BACK,
            )
            scenario.onUiInitialized { activity ->
                assertFalse(activity.isFinishing)
                assertFalse(activity.isDestroyed)
            }
        }
    }

    @Test
    fun untrustedSecureSessionCallerIsRejectedWithoutChangingPersistedState() {
        writeState(WebPocState.IDLE)
        val intent = Intent(context, MainActivity::class.java)
            .setAction(ACTION_START_SECURE_SESSION)
            .setData(Uri.parse("content://$CREDENTIAL_BRIDGE_AUTHORITY/v1/untrusted"))

        ActivityScenario.launchActivityForResult<MainActivity>(intent).use { scenario ->
            val result = scenario.result
            assertEquals(Activity.RESULT_CANCELED, result.resultCode)
            assertEquals(
                "SECURE_SESSION_CALLER",
                result.resultData?.getStringExtra(EXTRA_FAILURE_REASON),
            )
        }

        assertEquals(WebPocState.IDLE, readState())
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
    fun navigationStopFailureStillFailsClosedWithoutEscaping() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { }
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }

            scenario.onActivity { activity ->
                val client = activity.findViewById<WebView>(R.id.web_view).webViewClient
                val replacement = ThrowingStopLoadingWebView(activity)
                replaceWebView(activity, replacement)

                assertTrue(
                    runCatching {
                        client.onPageStarted(
                            replacement,
                            "https://example.invalid/",
                            null,
                        )
                    }.isSuccess,
                )
            }

            assertEquals(WebPocState.LOCKED, readState())
            assertEquals("NAVIGATION_BLOCKED", preferences().getString(KEY_REASON, null))
        }
    }

    @Test
    fun studentNavigationRestoreFailureStillFailsClosedWithoutEscaping() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { }
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }

            scenario.onActivity { activity ->
                val client = activity.findViewById<WebView>(R.id.web_view).webViewClient
                val replacement = ThrowingFirstLoadUrlWebView(activity)
                replaceWebView(activity, replacement)
                MainActivity::class.java.getDeclaredField("state").apply {
                    isAccessible = true
                    set(activity, WebPocState.ACTIVE)
                }

                assertTrue(
                    runCatching {
                        client.onPageStarted(
                            replacement,
                            WebSecurityPolicy.COURSE_URL,
                            null,
                        )
                    }.isSuccess,
                )
            }

            assertEquals(WebPocState.LOCKED, readState())
            assertEquals("NAVIGATION_BLOCKED", preferences().getString(KEY_REASON, null))
        }
    }

    @Test
    fun recoveryNavigationFailureStillFailsClosedWithoutEscaping() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                val replacement = ThrowingFirstLoadUrlWebView(activity)
                replaceWebView(activity, replacement)
                val beginRecovery = MainActivity::class.java.getDeclaredMethod(
                    "beginRecovery",
                ).apply { isAccessible = true }

                assertTrue(runCatching { beginRecovery.invoke(activity) }.isSuccess)
            }

            assertEquals(WebPocState.LOCKED, readState())
            assertEquals("WEB_NAVIGATION", preferences().getString(KEY_REASON, null))
        }
    }

    @Test
    fun preflightDnsRetryStopFailureStillFailsClosedWithoutEscaping() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { }
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }

            scenario.onActivity { activity ->
                val replacement = ThrowingStopLoadingWebView(activity)
                replaceWebView(activity, replacement)
                MainActivity::class.java.getDeclaredField("state").apply {
                    isAccessible = true
                    set(activity, WebPocState.PREFLIGHT)
                }
                val scheduleRetry = MainActivity::class.java.getDeclaredMethod(
                    "schedulePreflightDnsRetry",
                    WebView::class.java,
                ).apply { isAccessible = true }

                assertTrue(
                    runCatching {
                        scheduleRetry.invoke(activity, replacement)
                    }.isSuccess,
                )
            }

            assertEquals(WebPocState.LOCKED, readState())
            assertEquals("WEB_NAVIGATION", preferences().getString(KEY_REASON, null))
        }
    }

    @Test
    fun webThreatRejectionFailureStillFailsClosedWithoutEscaping() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { }
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }

            scenario.onActivity { activity ->
                val rejectAndLock = MainActivity::class.java.getDeclaredMethod(
                    "rejectWebContentAndLock",
                    String::class.java,
                    kotlin.jvm.functions.Function0::class.java,
                ).apply { isAccessible = true }

                assertTrue(
                    runCatching {
                        rejectAndLock.invoke(
                            activity,
                            "TLS_ERROR",
                            { throw IllegalStateException("synthetic rejection failure") },
                        )
                    }.isSuccess,
                )
            }

            assertEquals(WebPocState.LOCKED, readState())
            assertEquals("TLS_ERROR", preferences().getString(KEY_REASON, null))
        }
    }

    @Test
    fun rendererCrashRemovesUnusableWebViewAndFailsClosed() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                activity.findViewById<WebView>(R.id.web_view).loadUrl("chrome://crash")
            }

            assertTrueWithin(10) {
                readState() == WebPocState.LOCKED &&
                    preferences().getString(KEY_REASON, null) == "WEB_PROCESS_GONE"
            }
            scenario.onActivity { activity ->
                assertNull(activity.findViewById<WebView?>(R.id.web_view))
                activity.findViewById<View>(R.id.recovery_button).performClick()
            }
            scenario.onUiInitialized { activity ->
                assertTrue(activity.findViewById<WebView?>(R.id.web_view) != null)
            }
        }
    }

    @Test
    fun recoveryRendererRecycleRecreatesActivityWithFreshWebView() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var discardedWebView: WebView
            scenario.onUiInitialized { activity ->
                discardedWebView = activity.findViewById(R.id.web_view)
                MainActivity::class.java.getDeclaredField("state").apply {
                    isAccessible = true
                    set(activity, WebPocState.RECOVERY_REQUIRED)
                }
                MainActivity::class.java.getDeclaredField(
                    "recoveryRendererRecycleAttempted",
                ).apply {
                    isAccessible = true
                    setBoolean(activity, true)
                }
                MainActivity::class.java.getDeclaredField(
                    "recoveryRendererRecyclePending",
                ).apply {
                    isAccessible = true
                    setBoolean(activity, true)
                }
                writeState(WebPocState.RECOVERY_REQUIRED)
                assertTrue(
                    activity.findViewById<WebView>(R.id.web_view).webViewClient
                        .onRenderProcessGone(discardedWebView, null),
                )
            }

            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(UI_TIMEOUT_SECONDS)
            var freshWebViewObserved = false
            while (System.nanoTime() < deadline && !freshWebViewObserved) {
                scenario.onActivity { activity ->
                    val current = activity.findViewById<WebView?>(R.id.web_view)
                    freshWebViewObserved =
                        current != null &&
                        current !== discardedWebView &&
                        activity.intent.getBooleanExtra(
                            "com.local.matholickiosk.extra.RECOVERY_RENDERER_RECYCLED",
                            false,
                        )
                }
                if (!freshWebViewObserved) TimeUnit.MILLISECONDS.sleep(100)
            }
            assertTrue("recovery did not create a fresh WebView", freshWebViewObserved)
        }
    }

    @Test
    fun persistentUnresponsiveRendererExpiresGraceAndFailsClosed() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                MainActivity::class.java.getDeclaredField("state").apply {
                    isAccessible = true
                    set(activity, WebPocState.ACTIVE)
                }
                writeState(WebPocState.ACTIVE)
                val activeWebView = activity.findViewById<WebView>(R.id.web_view)
                activeWebView.webViewRenderProcessClient
                    ?.onRenderProcessUnresponsive(activeWebView, null)

                assertEquals(WebPocState.ACTIVE, readState())
                assertSame(
                    activeWebView,
                    MainActivity::class.java.getDeclaredField("unresponsiveWebView")
                        .apply { isAccessible = true }
                        .get(activity),
                )
                MainActivity::class.java.getDeclaredMethod(
                    "expireUnresponsiveRendererGrace",
                ).apply { isAccessible = true }
                    .invoke(activity)
            }

            assertTrueWithin(5) {
                readState() == WebPocState.LOCKED &&
                    preferences().getString(KEY_REASON, null) ==
                    "WEB_PROCESS_UNRESPONSIVE"
            }
        }
    }

    @Test
    fun responsiveRendererCallbackCancelsGraceWithoutLocking() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                MainActivity::class.java.getDeclaredField("state").apply {
                    isAccessible = true
                    set(activity, WebPocState.ACTIVE)
                }
                writeState(WebPocState.ACTIVE)
                val activeWebView = activity.findViewById<WebView>(R.id.web_view)
                val client = activeWebView.webViewRenderProcessClient
                client?.onRenderProcessUnresponsive(activeWebView, null)
                client?.onRenderProcessResponsive(activeWebView, null)

                assertNull(
                    MainActivity::class.java.getDeclaredField("unresponsiveWebView")
                        .apply { isAccessible = true }
                        .get(activity),
                )
                MainActivity::class.java.getDeclaredMethod(
                    "expireUnresponsiveRendererGrace",
                ).apply { isAccessible = true }
                    .invoke(activity)
                assertEquals(WebPocState.ACTIVE, readState())
            }
        }
    }

    @Test
    fun rendererCleanupFailureStillFailsClosedWithoutEscaping() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { }
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }

            scenario.onActivity { activity ->
                val client = activity.findViewById<WebView>(R.id.web_view).webViewClient
                val replacement = ThrowingDestroyWebView(activity)
                replaceWebView(activity, replacement)

                assertTrue(
                    runCatching {
                        client.onRenderProcessGone(replacement, null)
                    }.isSuccess,
                )
            }

            assertEquals(WebPocState.LOCKED, readState())
            assertEquals("WEB_PROCESS_GONE", preferences().getString(KEY_REASON, null))
            scenario.onActivity { activity ->
                assertNull(activity.findViewById<WebView?>(R.id.web_view))
            }
        }
    }

    @Test
    fun synchronousJavascriptEvaluationFailureFailsClosedWithoutEscaping() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { }
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }

            scenario.onActivity { activity ->
                replaceWebView(activity, ThrowingEvaluateWebView(activity))
                invokeEvaluate(activity) { }
            }

            assertEquals(WebPocState.LOCKED, readState())
            assertEquals("WEB_EVALUATION", preferences().getString(KEY_REASON, null))
        }
    }

    @Test
    fun asynchronousJavascriptCallbackFailureFailsClosedWithoutEscaping() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { }
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }

            scenario.onActivity { activity ->
                val replacement = CapturingEvaluateWebView(activity)
                replaceWebView(activity, replacement)
                invokeEvaluate(activity) {
                    throw IllegalStateException("synthetic callback failure")
                }

                assertTrue(runCatching { replacement.deliver("null") }.isSuccess)
            }

            assertEquals(WebPocState.LOCKED, readState())
            assertEquals("WEB_CALLBACK", preferences().getString(KEY_REASON, null))
        }
    }

    @Test
    fun activityDestroyCleanupContinuesAfterIndividualWebViewFailure() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                val replacement = ThrowingStopLoadingWebView(activity)
                replaceWebView(activity, replacement)

                val cleanup = MainActivity::class.java.getDeclaredMethod(
                    "disposeWebViewForActivityDestroy",
                    WebView::class.java,
                ).apply { isAccessible = true }

                assertTrue(runCatching { cleanup.invoke(activity, replacement) }.isSuccess)
                assertTrue(replacement.blankLoadAttempted)
                assertEquals(1, replacement.destroyCalls)
                assertNull(replacement.parent)

                MainActivity::class.java.getDeclaredField("webViewReference").apply {
                    isAccessible = true
                    set(activity, null)
                }
            }
        }
    }

    @Test
    fun synchronousSessionCleanupFailureContinuesAndFailsClosed() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                val replacement = ThrowingSessionCleanupWebView(activity)
                replaceWebView(activity, replacement)
                MainActivity::class.java.getDeclaredField("state").apply {
                    isAccessible = true
                    set(activity, WebPocState.LOGOUT_VERIFY)
                }
                val cleanup = MainActivity::class.java.getDeclaredMethod(
                    "clearWebAuthenticationAndReloadLogin",
                ).apply { isAccessible = true }

                assertTrue(runCatching { cleanup.invoke(activity) }.isSuccess)
                assertEquals(1, replacement.clearCacheCalls)
            }

            assertEquals(WebPocState.LOCKED, readState())
            assertEquals("SESSION_CLEAR", preferences().getString(KEY_REASON, null))
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
                assertEquals(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                )
            }
        }
    }

    @Test
    fun activeRemoteSupportKeepsCredentialSetupSecureAcrossScreenTransitions() {
        val remoteSupportStore = RemoteSupportStore(context)
        remoteSupportStore.disable()
        remoteSupportStore.enable(RemoteSupportPolicy.DEFAULT_DURATION_MILLIS)
        writeState(WebPocState.IDLE)

        try {
            assertTrue(remoteSupportStore.activeUntilEpochMillis() != null)
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onUiInitialized { activity ->
                    val showSetup = MainActivity::class.java.getDeclaredMethod("showSetup")
                        .apply { isAccessible = true }
                    val showActive = MainActivity::class.java.getDeclaredMethod(
                        "showActive",
                        String::class.java,
                    ).apply { isAccessible = true }

                    showSetup.invoke(activity)
                    assertEquals(
                        View.VISIBLE,
                        activity.findViewById<View>(R.id.setup_panel).visibility,
                    )
                    assertEquals(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        activity.window.attributes.flags and
                            WindowManager.LayoutParams.FLAG_SECURE,
                    )

                    showActive.invoke(activity, WebSecurityPolicy.COURSE_URL)
                    assertEquals(
                        0,
                        activity.window.attributes.flags and
                            WindowManager.LayoutParams.FLAG_SECURE,
                    )

                    showSetup.invoke(activity)
                    assertEquals(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        activity.window.attributes.flags and
                            WindowManager.LayoutParams.FLAG_SECURE,
                    )
                }
            }
        } finally {
            remoteSupportStore.disable()
        }
    }

    @Test
    fun activeStudentSessionUsesEightyPercentBrightnessAndRestoresPreviousValue() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val originalBrightness = activity.window.attributes.screenBrightness
                val transition = MainActivity::class.java.getDeclaredMethod(
                    "transition",
                    WebPocState::class.java,
                    String::class.java,
                ).apply { isAccessible = true }

                transition.invoke(activity, WebPocState.ACTIVE, null)
                assertEquals(
                    0.8f,
                    activity.window.attributes.screenBrightness,
                    0.001f,
                )

                transition.invoke(activity, WebPocState.LOCKED, "TEST_COMPLETE")
                assertEquals(
                    originalBrightness,
                    activity.window.attributes.screenBrightness,
                    0.001f,
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
    fun gate3LoginRejectionPersistsFailedBeforeWipingSession() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { }
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }
            scenario.onUiInitialized { activity ->
                startSyntheticGate3(activity)
                activity.rejectUnverifiedLogin()
            }

            assertEquals(WebPocState.LOCKED, readState())
            assertEquals("LOGIN_NOT_VERIFIED", preferences().getString(KEY_REASON, null))
            assertEquals("FAILED", preferences().getString(KEY_GATE3_STATUS, null))
            assertEquals(0, preferences().getInt(KEY_GATE3_COMPLETED, -1))
        }
    }

    @Test
    fun gate3FingerprintRejectionPersistsFailedBeforeWipingSession() {
        writeState(WebPocState.IDLE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { }
            assertTrueWithin(TIMEOUT_SECONDS) { readState() == WebPocState.IDLE }
            scenario.onUiInitialized { activity ->
                startSyntheticGate3(activity)
                activity.rejectInvalidLoginFingerprint("LOGIN_FINGERPRINT_TEST")
            }

            assertEquals(WebPocState.MAINTENANCE_REQUIRED, readState())
            assertEquals("LOGIN_FINGERPRINT_TEST", preferences().getString(KEY_REASON, null))
            assertEquals("FAILED", preferences().getString(KEY_GATE3_STATUS, null))
            assertEquals(0, preferences().getInt(KEY_GATE3_COMPLETED, -1))
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

    @Test
    fun completedResultOffersSameStudentContinuationButIncompleteResultDoesNot() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                MainActivity::class.java.getDeclaredField("state").apply {
                    isAccessible = true
                    set(activity, WebPocState.ACTIVE)
                }
                MainActivity::class.java.getDeclaredMethod(
                    "showResultSummary",
                    List::class.java,
                ).apply { isAccessible = true }
                    .invoke(activity, listOf(1))

                val continueButton = activity.findViewById<Button>(
                    R.id.result_continue_button,
                )
                assertEquals(View.VISIBLE, continueButton.visibility)
                assertEquals("다른 학습지 계속 채점", continueButton.text.toString())

                MainActivity::class.java.getDeclaredField("resultSummaryDisplayed").apply {
                    isAccessible = true
                    setBoolean(activity, false)
                }
                MainActivity::class.java.getDeclaredMethod(
                    "showResultSummaryUnavailable",
                    JSONObject::class.java,
                ).apply { isAccessible = true }
                    .invoke(activity, JSONObject())

                assertEquals(View.GONE, continueButton.visibility)

                MainActivity::class.java.getDeclaredField("resultSummaryDisplayed").apply {
                    isAccessible = true
                    setBoolean(activity, false)
                }
                MainActivity::class.java.getDeclaredMethod(
                    "showResultSummary",
                    List::class.java,
                ).apply { isAccessible = true }
                    .invoke(activity, emptyList<Int>())
                continueButton.performClick()

                assertEquals(
                    View.GONE,
                    activity.findViewById<View>(R.id.result_summary_panel).visibility,
                )
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.blocker).visibility)
                assertEquals(
                    "학습 화면을 안전하게 준비 중입니다",
                    activity.findViewById<android.widget.TextView>(
                        R.id.blocker_message,
                    ).text.toString(),
                )
                assertFalse(
                    MainActivity::class.java.getDeclaredField(
                        "resultSummaryDisplayed",
                    ).apply { isAccessible = true }.getBoolean(activity),
                )
                assertFalse(
                    MainActivity::class.java.getDeclaredField(
                        "resultContinuationAllowed",
                    ).apply { isAccessible = true }.getBoolean(activity),
                )
            }
        }
    }

    @Test
    fun finishButtonRequiresASecondConfirmedAction() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                val stateField = MainActivity::class.java.getDeclaredField("state").apply {
                    isAccessible = true
                    set(activity, WebPocState.ACTIVE)
                }
                val finish = activity.findViewById<Button>(R.id.finish_button)
                finish.visibility = View.VISIBLE
                finish.performClick()

                assertEquals(WebPocState.ACTIVE, stateField.get(activity))
                val dialogField = MainActivity::class.java.getDeclaredField(
                    "finishConfirmationDialog",
                ).apply { isAccessible = true }
                val dialog = dialogField.get(activity) as android.app.AlertDialog
                assertTrue(dialog.isShowing)
                assertEquals(
                    "주의: 답안을 입력하는 버튼이 아닙니다",
                    dialog.findViewById<android.widget.TextView>(
                        activity.resources.getIdentifier("alertTitle", "id", "android"),
                    )?.text.toString(),
                )
                assertEquals(
                    "채점 종료·로그아웃",
                    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).text.toString(),
                )
                assertEquals(
                    "문제로 돌아가기",
                    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).text.toString(),
                )
                assertTrue(
                    dialog.findViewById<android.widget.TextView>(android.R.id.message)
                        ?.text?.contains("문제 화면의 ‘입력’ 버튼") == true,
                )
                assertTrue(
                    dialog.findViewById<android.widget.TextView>(android.R.id.message)
                        ?.text?.contains("채점 종료 절차가 바로 시작") == true,
                )
                assertEquals(
                    android.graphics.Color.rgb(183, 28, 28),
                    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).currentTextColor,
                )

                dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).performClick()
                assertEquals(WebPocState.ACTIVE, stateField.get(activity))
                assertNull(dialogField.get(activity))
            }
        }
    }

    @Test
    fun inactivityWarningExplainsDeadlineAndExpiresThroughSafeLogout() {
        writeState(WebPocState.LOCKED)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onUiInitialized { activity ->
                val stateField = MainActivity::class.java.getDeclaredField("state").apply {
                    isAccessible = true
                    set(activity, WebPocState.ACTIVE)
                }
                val generationField = MainActivity::class.java.getDeclaredField(
                    "inactivityGeneration",
                ).apply {
                    isAccessible = true
                    setInt(activity, 42)
                }
                val showWarning = MainActivity::class.java.getDeclaredMethod(
                    "showInactivityWarning",
                    Int::class.javaPrimitiveType,
                ).apply { isAccessible = true }
                activity.findViewById<View>(R.id.network_pause_panel).visibility = View.GONE
                showWarning.invoke(activity, 42)

                assertEquals(
                    "5분 동안 입력이 없었습니다",
                    activity.findViewById<android.widget.TextView>(
                        R.id.idle_warning_title,
                    ).text.toString(),
                )
                assertTrue(
                    activity.findViewById<android.widget.TextView>(
                        R.id.idle_warning_message,
                    ).text.contains("1분 안에"),
                )
                assertTrue(
                    activity.findViewById<android.widget.TextView>(
                        R.id.idle_warning_message,
                    ).text.contains("QR 화면으로 돌아갑니다"),
                )
                assertEquals(
                    View.VISIBLE,
                    activity.findViewById<View>(R.id.idle_warning_panel).visibility,
                )

                activity.findViewById<Button>(R.id.idle_continue_button).performClick()
                assertEquals(WebPocState.ACTIVE, stateField.get(activity))
                assertEquals(
                    View.GONE,
                    activity.findViewById<View>(R.id.idle_warning_panel).visibility,
                )
                val resumedGeneration = generationField.getInt(activity)
                assertTrue(resumedGeneration > 42)
                showWarning.invoke(activity, resumedGeneration)

                MainActivity::class.java.getDeclaredMethod(
                    "expireInactivityWarning",
                    Int::class.javaPrimitiveType,
                ).apply { isAccessible = true }.invoke(activity, resumedGeneration)

                assertEquals(WebPocState.LOGOUT_NAVIGATE, stateField.get(activity))
                assertEquals(
                    View.GONE,
                    activity.findViewById<View>(R.id.idle_warning_panel).visibility,
                )
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

    private fun startSyntheticGate3(activity: MainActivity) {
        activity.findViewById<View>(R.id.gate3_mode_button).performClick()
        activity.findViewById<EditText>(R.id.expected_name).setText("가상 학생 A")
        activity.findViewById<EditText>(R.id.username).setText("virtual-a")
        activity.findViewById<EditText>(R.id.password).setText("virtual-pass-a")
        activity.findViewById<EditText>(R.id.gate3_expected_name_b).setText("가상 학생 B")
        activity.findViewById<EditText>(R.id.gate3_username_b).setText("virtual-b")
        activity.findViewById<EditText>(R.id.gate3_password_b).setText("virtual-pass-b")
        activity.findViewById<View>(R.id.gate3_start_button).performClick()
        assertEquals("RUNNING", preferences().getString(KEY_GATE3_STATUS, null))
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

    private fun replaceWebView(activity: MainActivity, replacement: WebView) {
        val original = activity.findViewById<WebView>(R.id.web_view)
        val parent = original.parent as FrameLayout
        replacement.id = R.id.web_view
        replacement.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        parent.removeView(original)
        original.destroy()
        parent.addView(replacement, 0)
        MainActivity::class.java.getDeclaredField("webViewReference").apply {
            isAccessible = true
            set(activity, replacement)
        }
    }

    private fun invokeEvaluate(activity: MainActivity, callback: (Any?) -> Unit) {
        MainActivity::class.java.getDeclaredMethod(
            "evaluate",
            String::class.java,
            kotlin.jvm.functions.Function1::class.java,
        ).apply { isAccessible = true }
            .invoke(activity, "({ ok: true })", callback)
    }

    private class ThrowingEvaluateWebView(context: Context) : WebView(context) {
        override fun evaluateJavascript(
            script: String,
            resultCallback: ValueCallback<String>?,
        ) {
            throw IllegalStateException("synthetic evaluation failure")
        }
    }

    private class CapturingEvaluateWebView(context: Context) : WebView(context) {
        private var pendingCallback: ValueCallback<String>? = null

        override fun evaluateJavascript(
            script: String,
            resultCallback: ValueCallback<String>?,
        ) {
            pendingCallback = resultCallback
        }

        fun deliver(raw: String) {
            checkNotNull(pendingCallback).onReceiveValue(raw)
        }
    }

    private class ThrowingFirstLoadUrlWebView(context: Context) : WebView(context) {
        private var loadAttempts = 0

        override fun loadUrl(url: String) {
            loadAttempts += 1
            if (loadAttempts == 1) {
                throw IllegalStateException("synthetic navigation restore failure")
            }
        }
    }

    private class ThrowingDestroyWebView(context: Context) : WebView(context) {
        override fun destroy() {
            throw IllegalStateException("synthetic destroy failure")
        }
    }

    private class ThrowingStopLoadingWebView(context: Context) : WebView(context) {
        var blankLoadAttempted = false
            private set
        var destroyCalls = 0
            private set

        override fun stopLoading() {
            throw IllegalStateException("synthetic stopLoading failure")
        }

        override fun loadUrl(url: String) {
            if (url == "about:blank") blankLoadAttempted = true
        }

        override fun destroy() {
            destroyCalls += 1
            super.destroy()
        }
    }

    private class ThrowingSessionCleanupWebView(context: Context) : WebView(context) {
        var clearCacheCalls = 0
            private set

        override fun clearHistory() {
            throw IllegalStateException("synthetic session clear failure")
        }

        override fun clearCache(includeDiskFiles: Boolean) {
            clearCacheCalls += 1
            super.clearCache(includeDiskFiles)
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "web_poc_state"
        const val KEY_STATE = "state"
        const val KEY_REASON = "reason"
        const val KEY_GATE3_STATUS = "gate3_status"
        const val KEY_GATE3_COMPLETED = "gate3_completed"
        const val ACTION_START_SECURE_SESSION =
            "com.local.matholickiosk.action.START_SECURE_WEB_SESSION"
        const val CREDENTIAL_BRIDGE_AUTHORITY =
            "com.local.matholickiosk.kiosk.credentials"
        const val EXTRA_FAILURE_REASON = "failure_reason"
        const val UI_TIMEOUT_SECONDS = 10L
        const val TIMEOUT_SECONDS = 40L
    }
}
