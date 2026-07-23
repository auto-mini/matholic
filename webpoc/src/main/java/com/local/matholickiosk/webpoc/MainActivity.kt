package com.local.matholickiosk.webpoc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebViewDatabase
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import org.json.JSONObject
import org.json.JSONTokener

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var setupPanel: FrameLayout
    private lateinit var blocker: FrameLayout
    private lateinit var progress: ProgressBar
    private lateinit var blockerMessage: TextView
    private lateinit var recoveryButton: Button
    private lateinit var statusBadge: TextView
    private lateinit var finishButton: Button
    private lateinit var expectedNameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var setupError: TextView
    private lateinit var startButton: Button
    private lateinit var setupTitle: TextView
    private lateinit var setupMessage: TextView
    private lateinit var gate3AccountALabel: TextView
    private lateinit var gate3AccountBPanel: LinearLayout
    private lateinit var gate3ExpectedNameBInput: EditText
    private lateinit var gate3UsernameBInput: EditText
    private lateinit var gate3PasswordBInput: EditText
    private lateinit var gate3ModeButton: Button
    private lateinit var gate3StartButton: Button
    private lateinit var gate3CancelButton: Button
    private lateinit var gate3AbortButton: Button
    private lateinit var gate3Result: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val preferences by lazy { getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE) }

    private var state = WebPocState.IDLE
    private var expectedDisplayName: String? = null
    private var ephemeralCredentials: EphemeralCredentials? = null
    private var timeoutGeneration = 0
    private var logoutAttempt = 0
    private var postClearVerificationPending = false
    private var pendingLockReason: String? = null
    private var preflightDnsRetryScheduled = false
    private var preflightDnsRetryStarted = false
    private var loginProbeGeneration = 0
    private var gate3Session: Gate3RunSession? = null
    private var gate3StartedAtElapsedMs = 0L
    private var uiInitialized = false
    private var destroyed = false
    private var secureKioskSession = false
    private var secureResultDelivered = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        if (preferences.getString(KEY_GATE3_STATUS, null) == GATE3_STATUS_RUNNING) {
            preferences.edit().putString(KEY_GATE3_STATUS, GATE3_STATUS_ABORTED).commit()
        }
        WebViewProxyBootstrap.ensureConfigured { result ->
            if (destroyed) return@ensureConfigured
            val finishInitialization = Runnable {
                if (!destroyed) {
                    initializeUi()
                    when (result) {
                        ProxyBootstrapResult.READY -> {
                            if (isSecureKioskLaunch(intent)) {
                                beginSecureKioskSession(intent)
                            } else {
                                resumeFromPersistedState()
                            }
                        }
                        ProxyBootstrapResult.UNSUPPORTED -> showLocked("WEB_PROXY_UNSUPPORTED")
                        ProxyBootstrapResult.FAILED -> showLocked("WEB_PROXY_SETUP")
                    }
                }
            }
            if (result == ProxyBootstrapResult.READY) {
                handler.postDelayed(finishInitialization, PROXY_STABILIZATION_DELAY_MS)
            } else {
                finishInitialization.run()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (uiInitialized && isSecureKioskLaunch(intent)) {
            beginSecureKioskSession(intent)
        }
    }

    private fun initializeUi() {
        setContentView(R.layout.activity_main)
        bindViews()
        configureSensitiveInputs()
        configureWebView()
        configureActions()
        registerBackHandler()
        uiInitialized = true
        showBlocking(getString(R.string.status_preparing))
    }

    private fun resumeFromPersistedState() {
        val savedState = preferences.getString(KEY_STATE, WebPocState.IDLE.name)
            ?.let { runCatching { WebPocState.valueOf(it) }.getOrNull() }
            ?: WebPocState.RECOVERY_REQUIRED

        when {
            savedState == WebPocState.LOCKED -> {
                showLocked("PREVIOUS_LOCK")
            }
            savedState == WebPocState.MAINTENANCE_REQUIRED -> {
                showMaintenance("PREVIOUS_MAINTENANCE")
            }
            savedState.requiresRecoveryAfterRestart() -> beginRecovery()
            else -> prepareLoginPage()
        }
    }

    private fun isSecureKioskLaunch(candidate: Intent?): Boolean =
        candidate?.action == ACTION_START_SECURE_SESSION &&
            candidate.data?.authority == CREDENTIAL_BRIDGE_AUTHORITY

    private fun beginSecureKioskSession(launchIntent: Intent) {
        if (secureKioskSession || secureResultDelivered) return
        secureKioskSession = true
        val savedState = preferences.getString(KEY_STATE, WebPocState.IDLE.name)
            ?.let { runCatching { WebPocState.valueOf(it) }.getOrNull() }
            ?: WebPocState.RECOVERY_REQUIRED
        if (savedState != WebPocState.IDLE) {
            showLocked("WEB_SESSION_NOT_CLEAN")
            return
        }
        val uri = launchIntent.data ?: run {
            showLocked("CREDENTIAL_BRIDGE_URI")
            return
        }
        val payload = runCatching {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst() || cursor.count != 1) return@use null
                val expected = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPECTED_NAME))
                val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
                val password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
                if (expected.isNullOrBlank() || username.isNullOrEmpty() || password.isNullOrEmpty()) {
                    null
                } else {
                    SecureBridgePayload(expected, username, password)
                }
            }
        }.getOrNull()
        if (payload == null) {
            showLocked("CREDENTIAL_BRIDGE_EMPTY")
            return
        }
        expectedDisplayName = payload.expectedDisplayName
        ephemeralCredentials = EphemeralCredentials(payload.username, payload.password)
        showBlocking(getString(R.string.status_login))
        transition(WebPocState.LOGIN_FILL)
        webView.loadUrl(WebSecurityPolicy.LOGIN_URL)
        scheduleTimeout(LOGIN_TIMEOUT_MS, "LOGIN_PREP_TIMEOUT")
    }

    private fun bindViews() {
        webView = findViewById(R.id.web_view)
        setupPanel = findViewById(R.id.setup_panel)
        blocker = findViewById(R.id.blocker)
        progress = findViewById(R.id.progress)
        blockerMessage = findViewById(R.id.blocker_message)
        recoveryButton = findViewById(R.id.recovery_button)
        statusBadge = findViewById(R.id.status_badge)
        finishButton = findViewById(R.id.finish_button)
        expectedNameInput = findViewById(R.id.expected_name)
        usernameInput = findViewById(R.id.username)
        passwordInput = findViewById(R.id.password)
        setupError = findViewById(R.id.setup_error)
        startButton = findViewById(R.id.start_button)
        setupTitle = findViewById(R.id.setup_title)
        setupMessage = findViewById(R.id.setup_message)
        gate3AccountALabel = findViewById(R.id.gate3_account_a_label)
        gate3AccountBPanel = findViewById(R.id.gate3_account_b_panel)
        gate3ExpectedNameBInput = findViewById(R.id.gate3_expected_name_b)
        gate3UsernameBInput = findViewById(R.id.gate3_username_b)
        gate3PasswordBInput = findViewById(R.id.gate3_password_b)
        gate3ModeButton = findViewById(R.id.gate3_mode_button)
        gate3StartButton = findViewById(R.id.gate3_start_button)
        gate3CancelButton = findViewById(R.id.gate3_cancel_button)
        gate3AbortButton = findViewById(R.id.gate3_abort_button)
        gate3Result = findViewById(R.id.gate3_result)
    }

    private fun configureSensitiveInputs() {
        listOf(
            expectedNameInput,
            usernameInput,
            passwordInput,
            gate3ExpectedNameBInput,
            gate3UsernameBInput,
            gate3PasswordBInput,
        ).forEach {
            it.isSaveEnabled = false
            it.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            it.filterTouchesWhenObscured = true
        }
        listOf(
            startButton,
            finishButton,
            recoveryButton,
            gate3ModeButton,
            gate3StartButton,
            gate3CancelButton,
            gate3AbortButton,
            webView,
        ).forEach {
            it.filterTouchesWhenObscured = true
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Suppress("DEPRECATION")
    private fun configureWebView() {
        WebView.setWebContentsDebuggingEnabled(false)
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = false
            allowFileAccess = false
            allowContentAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            mediaPlaybackRequiresUserGesture = true
            saveFormData = false
            builtInZoomControls = false
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            safeBrowsingEnabled = true
        }
        webView.isSaveEnabled = false
        webView.setOnLongClickListener { true }
        webView.setDownloadListener { _, _, _, _, _ -> showLocked("DOWNLOAD_BLOCKED") }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, false)
        }
        WebViewDatabase.getInstance(this).clearFormData()

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean = true

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: android.webkit.GeolocationPermissions.Callback?,
            ) {
                callback?.invoke(origin, false, false)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (request?.isForMainFrame != true) return false
                val target = request.url?.toString()
                if (WebSecurityPolicy.isAllowedTopLevelUrl(target)) return false
                showLocked("NAVIGATION_BLOCKED")
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (url != null && url != "about:blank" && !WebSecurityPolicy.isAllowedTopLevelUrl(url)) {
                    view?.stopLoading()
                    showLocked("NAVIGATION_BLOCKED")
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (
                    destroyed || isTerminalState() || url == null ||
                    !WebSecurityPolicy.isAllowedTopLevelUrl(url)
                ) return
                if (
                    WebFailurePolicy.shouldIgnorePageFinishedWhilePreflightRetryPending(
                        state,
                        preflightDnsRetryScheduled,
                        preflightDnsRetryStarted,
                    )
                ) return
                when {
                    WebSecurityPolicy.isLoginUrl(url) -> handleLoginPage()
                    WebSecurityPolicy.isPortalUrl(url) -> handlePortalPage()
                }
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.cancel()
                showLocked("TLS_ERROR")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                val failureReason = NetworkFailureReason.fromWebViewErrorCode(
                    error?.errorCode,
                    request?.url?.host,
                )
                if (
                    WebFailurePolicy.canRetryPreflightLoginDns(
                        request?.isForMainFrame == true,
                        state,
                        failureReason,
                        preflightDnsRetryStarted,
                    )
                ) {
                    if (!preflightDnsRetryScheduled) {
                        preflightDnsRetryScheduled = true
                        view?.stopLoading()
                        handler.postDelayed({
                            if (!destroyed && state == WebPocState.PREFLIGHT) {
                                preflightDnsRetryStarted = true
                                webView.loadUrl(WebSecurityPolicy.LOGIN_URL)
                            }
                        }, PREFLIGHT_DNS_RETRY_DELAY_MS)
                    }
                    return
                }
                if (
                    WebFailurePolicy.shouldLockForMainFrameFailure(
                        request?.isForMainFrame == true,
                        state,
                    )
                ) {
                    showLocked(failureReason)
                }
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?,
            ) {
                if (
                    (errorResponse?.statusCode ?: 0) >= 400 &&
                    WebFailurePolicy.shouldLockForMainFrameFailure(
                        request?.isForMainFrame == true,
                        state,
                    )
                ) {
                    showLocked("HTTP_ERROR")
                }
            }

            override fun onSafeBrowsingHit(
                view: WebView?,
                request: WebResourceRequest?,
                threatType: Int,
                callback: SafeBrowsingResponse?,
            ) {
                callback?.backToSafety(true)
                showLocked("SAFE_BROWSING")
            }

            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                showLocked("WEB_PROCESS_GONE")
                return true
            }
        }
    }

    private fun configureActions() {
        startButton.setOnClickListener { startLogin() }
        gate3ModeButton.setOnClickListener { setGate3SetupMode(true) }
        gate3CancelButton.setOnClickListener { setGate3SetupMode(false) }
        gate3StartButton.setOnClickListener { startGate3Run() }
        gate3AbortButton.setOnClickListener { abortGate3AndRecover() }
        finishButton.setOnClickListener {
            if (state == WebPocState.ACTIVE) {
                pendingLockReason = null
                beginLogout()
            }
        }
        recoveryButton.setOnClickListener { restartForRecovery() }
    }

    private fun registerBackHandler() {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
        ) {
            if (state == WebPocState.ACTIVE && webView.canGoBack()) webView.goBack()
        }
    }

    private fun prepareLoginPage() {
        pendingLockReason = null
        postClearVerificationPending = false
        preflightDnsRetryScheduled = false
        preflightDnsRetryStarted = false
        transition(WebPocState.PREFLIGHT)
        showBlocking(getString(R.string.status_preparing))
        webView.loadUrl(WebSecurityPolicy.LOGIN_URL)
        scheduleTimeout(PAGE_TIMEOUT_MS, "PREFLIGHT_TIMEOUT")
    }

    private fun beginRecovery() {
        wipeRuntimeSecrets()
        pendingLockReason = null
        postClearVerificationPending = false
        logoutAttempt = 0
        transition(WebPocState.RECOVERY_REQUIRED)
        showBlocking(getString(R.string.status_logout))
        webView.loadUrl(WebSecurityPolicy.COURSE_URL)
        scheduleTimeout(PAGE_TIMEOUT_MS, "RECOVERY_TIMEOUT")
    }

    private fun startLogin() {
        if (state != WebPocState.IDLE) return
        val expected = expectedNameInput.text?.toString().orEmpty()
        val username = usernameInput.text ?: ""
        val password = passwordInput.text ?: ""
        if (WebSecurityPolicy.normalizeDisplayName(expected).isEmpty() || username.isEmpty() || password.isEmpty()) {
            setupError.setText(R.string.input_required)
            setupError.visibility = View.VISIBLE
            return
        }

        expectedDisplayName = expected
        ephemeralCredentials = EphemeralCredentials(username, password)
        clearSetupFields()
        setupPanel.visibility = View.GONE
        showBlocking(getString(R.string.status_login))
        transition(WebPocState.LOGIN_FILL)
        webView.loadUrl(WebSecurityPolicy.LOGIN_URL)
        scheduleTimeout(LOGIN_TIMEOUT_MS, "LOGIN_PREP_TIMEOUT")
    }

    private fun setGate3SetupMode(enabled: Boolean) {
        if (state != WebPocState.IDLE || gate3Session != null) return
        clearSetupFields()
        gate3Result.visibility = View.GONE
        if (enabled) {
            setupTitle.setText(R.string.gate3_setup_title)
            setupMessage.setText(R.string.gate3_setup_message)
            gate3AccountALabel.visibility = View.VISIBLE
            gate3AccountBPanel.visibility = View.VISIBLE
            startButton.visibility = View.GONE
            gate3ModeButton.visibility = View.GONE
            gate3StartButton.visibility = View.VISIBLE
            gate3CancelButton.visibility = View.VISIBLE
        } else {
            setupTitle.setText(R.string.setup_title)
            setupMessage.setText(R.string.setup_message)
            gate3AccountALabel.visibility = View.GONE
            gate3AccountBPanel.visibility = View.GONE
            startButton.visibility = View.VISIBLE
            gate3ModeButton.visibility = View.VISIBLE
            gate3StartButton.visibility = View.GONE
            gate3CancelButton.visibility = View.GONE
        }
        expectedNameInput.requestFocus()
    }

    private fun startGate3Run() {
        if (state != WebPocState.IDLE || gate3Session != null) return
        val accountA = gate3Input(
            expectedNameInput,
            usernameInput,
            passwordInput,
        )
        val accountB = gate3Input(
            gate3ExpectedNameBInput,
            gate3UsernameBInput,
            gate3PasswordBInput,
        )
        if (accountA == null || accountB == null) {
            setupError.setText(R.string.gate3_all_fields_required)
            setupError.visibility = View.VISIBLE
            return
        }
        if (
            WebSecurityPolicy.displayNamesMatch(
                accountA.expectedDisplayName,
                accountB.expectedDisplayName,
            )
        ) {
            setupError.setText(R.string.gate3_distinct_names_required)
            setupError.visibility = View.VISIBLE
            return
        }

        gate3Session = Gate3RunSession(accountA, accountB)
        gate3StartedAtElapsedMs = SystemClock.elapsedRealtime()
        if (!preferences.edit()
                .putString(KEY_GATE3_STATUS, GATE3_STATUS_RUNNING)
                .putInt(KEY_GATE3_COMPLETED, 0)
                .putInt(KEY_GATE3_TARGET, Gate3RunSession.DEFAULT_TARGET_CYCLES)
                .remove(KEY_GATE3_DURATION_MS)
                .commit()
        ) {
            showLocked("GATE3_PROGRESS_PERSISTENCE")
            return
        }
        clearSetupFields()
        setupPanel.visibility = View.GONE
        startGate3Cycle()
    }

    private fun gate3Input(
        expectedNameView: EditText,
        usernameView: EditText,
        passwordView: EditText,
    ): Gate3AccountInput? {
        val expectedName = expectedNameView.text?.toString().orEmpty()
        val username = usernameView.text?.toString().orEmpty()
        val password = passwordView.text?.toString().orEmpty()
        if (
            WebSecurityPolicy.normalizeDisplayName(expectedName).isEmpty() ||
            username.isEmpty() ||
            password.isEmpty()
        ) return null
        return Gate3AccountInput(expectedName, username, password)
    }

    private fun startGate3Cycle() {
        val session = gate3Session ?: return
        if (session.isComplete) {
            finishGate3Run()
            return
        }
        val attempt = session.nextAttempt()
        expectedDisplayName = attempt.expectedDisplayName
        ephemeralCredentials = attempt.credentials
        showGate3Progress()
        transition(WebPocState.LOGIN_FILL)
        webView.loadUrl(WebSecurityPolicy.LOGIN_URL)
        scheduleTimeout(LOGIN_TIMEOUT_MS, "LOGIN_PREP_TIMEOUT")
    }

    private fun showGate3Progress() {
        val session = gate3Session ?: return
        showBlocking(
            getString(
                R.string.gate3_progress,
                session.completedCycles + 1,
                session.targetCycles,
                session.nextSlotLabel,
            ),
        )
    }

    private fun abortGate3AndRecover() {
        val session = gate3Session ?: return
        persistGate3Outcome(GATE3_STATUS_ABORTED, session.completedCycles)
        session.wipe()
        gate3Session = null
        beginRecovery()
    }

    private fun submitLoginOnce() {
        val credentials = ephemeralCredentials ?: run {
            showLocked("CREDENTIALS_MISSING")
            return
        }
        val username = credentials.usernameForImmediateUse()
        val password = credentials.passwordForImmediateUse()
        val script = WebDomScripts.login(username, password)
        credentials.wipe()
        ephemeralCredentials = null
        transition(WebPocState.LOGIN_SUBMIT)
        evaluate(script) { result ->
            if (result?.optBoolean("ok") != true) {
                showMaintenance(loginFingerprintReason(result))
                return@evaluate
            }
            if (state == WebPocState.LOGIN_SUBMIT) {
                val resultTimeout = if (gate3Session != null) {
                    GATE3_LOGIN_RESULT_TIMEOUT_MS
                } else {
                    LOGIN_TIMEOUT_MS
                }
                scheduleTimeout(resultTimeout, "LOGIN_TIMEOUT")
            }
        }
    }

    private fun handleLoginPage() {
        val purpose = state
        val expectedState = when (purpose) {
            WebPocState.LOGIN_FILL -> WebPocState.LOGIN_FILL
            WebPocState.LOGOUT_NAVIGATE,
            WebPocState.LOGOUT_SUBMIT,
            WebPocState.LOGOUT_VERIFY,
            WebPocState.RECOVERY_REQUIRED,
            -> WebPocState.LOGOUT_VERIFY
            else -> WebPocState.SESSION_SANITIZE
        }
        transition(expectedState)
        val generation = ++loginProbeGeneration
        probeSanitizedLogin(
            purpose = purpose,
            expectedState = expectedState,
            retriesRemaining = LOGIN_DOM_PROBE_RETRIES,
            stableReadsRemaining = LOGIN_STABLE_READS_REQUIRED - 1,
            generation = generation,
        )
    }

    private fun probeSanitizedLogin(
        purpose: WebPocState,
        expectedState: WebPocState,
        retriesRemaining: Int,
        stableReadsRemaining: Int,
        generation: Int,
    ) {
        if (state != expectedState || generation != loginProbeGeneration) return
        evaluate(WebDomScripts.sanitizeLoginAndFingerprint) { result ->
            if (state != expectedState || generation != loginProbeGeneration) return@evaluate
            if (!isValidSanitizedLogin(result)) {
                if (retriesRemaining > 0) {
                    handler.postDelayed({
                        probeSanitizedLogin(
                            purpose = purpose,
                            expectedState = expectedState,
                            retriesRemaining = retriesRemaining - 1,
                            stableReadsRemaining = LOGIN_STABLE_READS_REQUIRED - 1,
                            generation = generation,
                        )
                    }, LOGIN_DOM_PROBE_DELAY_MS)
                } else {
                    rejectInvalidLoginFingerprint(loginFingerprintReason(result))
                }
                return@evaluate
            }
            if (stableReadsRemaining > 0) {
                handler.postDelayed({
                    probeSanitizedLogin(
                        purpose = purpose,
                        expectedState = expectedState,
                        retriesRemaining = retriesRemaining,
                        stableReadsRemaining = stableReadsRemaining - 1,
                        generation = generation,
                    )
                }, LOGIN_STABILITY_DELAY_MS)
                return@evaluate
            }
            when (purpose) {
                WebPocState.LOGIN_FILL -> submitLoginOnce()
                WebPocState.LOGIN_SUBMIT,
                WebPocState.LOGIN_VERIFY,
                -> {
                    rejectUnverifiedLogin()
                }
                WebPocState.LOGOUT_NAVIGATE,
                WebPocState.LOGOUT_SUBMIT,
                WebPocState.LOGOUT_VERIFY,
                WebPocState.RECOVERY_REQUIRED,
                -> finishLogoutVerification()
                else -> showSetup()
            }
        }
    }

    private fun handlePortalPage() {
        when (state) {
            WebPocState.LOGIN_SUBMIT,
            WebPocState.LOGIN_VERIFY,
            -> {
                transition(WebPocState.LOGIN_VERIFY)
                probePortalForLogin(PORTAL_PROBE_RETRIES)
            }
            WebPocState.RECOVERY_REQUIRED -> {
                pendingLockReason = null
                beginLogout()
            }
            WebPocState.LOGOUT_NAVIGATE -> openLogoutMenu(PORTAL_PROBE_RETRIES)
            WebPocState.PREFLIGHT,
            WebPocState.SESSION_SANITIZE,
            WebPocState.IDLE,
            -> {
                transition(WebPocState.RECOVERY_REQUIRED)
                beginLogout()
            }
            else -> Unit
        }
    }

    private fun probePortalForLogin(retriesRemaining: Int) {
        if (state != WebPocState.LOGIN_VERIFY) return
        evaluate(WebDomScripts.portalFingerprint) { result ->
            if (state != WebPocState.LOGIN_VERIFY) return@evaluate
            if (result?.optBoolean("ok") != true) {
                if (retriesRemaining > 0) {
                    handler.postDelayed({ probePortalForLogin(retriesRemaining - 1) }, PROBE_DELAY_MS)
                } else {
                    showMaintenance("PORTAL_FINGERPRINT")
                }
                return@evaluate
            }
            val actualName = result.optString("actualName")
            val expected = expectedDisplayName
            if (expected == null || !WebSecurityPolicy.displayNamesMatch(expected, actualName)) {
                pendingLockReason = "STUDENT_MISMATCH"
                beginLogout()
                return@evaluate
            }
            cancelTimeout()
            expectedDisplayName = null
            transition(WebPocState.ACTIVE)
            if (gate3Session != null) {
                showGate3Progress()
                handler.postDelayed({
                    if (state == WebPocState.ACTIVE && gate3Session != null) beginLogout()
                }, GATE3_ACTIVE_DWELL_MS)
            } else {
                showActive()
            }
        }
    }

    private fun beginLogout() {
        cancelTimeout()
        wipeCredentialOnly()
        logoutAttempt = 0
        postClearVerificationPending = false
        transition(WebPocState.INPUT_BLOCKED)
        if (gate3Session != null) showGate3Progress() else showBlocking(getString(R.string.status_logout))
        startLogoutAttempt()
    }

    private fun startLogoutAttempt() {
        transition(WebPocState.LOGOUT_NAVIGATE)
        webView.loadUrl(WebSecurityPolicy.COURSE_URL)
        scheduleTimeout(LOGOUT_TIMEOUT_MS, "LOGOUT_TIMEOUT") { retryLogoutOrLock("LOGOUT_TIMEOUT") }
    }

    private fun openLogoutMenu(retriesRemaining: Int) {
        if (state != WebPocState.LOGOUT_NAVIGATE) return
        evaluate(WebDomScripts.portalFingerprint) { fingerprint ->
            if (state != WebPocState.LOGOUT_NAVIGATE) return@evaluate
            if (fingerprint?.optBoolean("ok") != true) {
                if (retriesRemaining > 0) {
                    handler.postDelayed({ openLogoutMenu(retriesRemaining - 1) }, PROBE_DELAY_MS)
                } else {
                    retryLogoutOrLock("PORTAL_FINGERPRINT")
                }
                return@evaluate
            }
            evaluate(WebDomScripts.openAccountMenu) { opened ->
                if (opened?.optBoolean("ok") != true) {
                    retryLogoutOrLock("ACCOUNT_MENU")
                    return@evaluate
                }
                handler.postDelayed({
                    if (state == WebPocState.LOGOUT_NAVIGATE) {
                        clickLogout(LOGOUT_CONTROL_PROBE_RETRIES)
                    }
                }, MENU_OPEN_DELAY_MS)
            }
        }
    }

    private fun clickLogout(probesRemaining: Int) {
        if (state != WebPocState.LOGOUT_NAVIGATE) return
        evaluate(WebDomScripts.clickLogout) { result ->
            if (result?.optBoolean("ok") != true) {
                val candidateCount = result?.optInt("count", -1) ?: -1
                if (candidateCount == 0 && probesRemaining > 0) {
                    handler.postDelayed({
                        clickLogout(probesRemaining - 1)
                    }, LOGOUT_CONTROL_PROBE_DELAY_MS)
                } else {
                    retryLogoutOrLock(logoutControlReason(result))
                }
            } else {
                if (state == WebPocState.LOGOUT_NAVIGATE) {
                    transition(WebPocState.LOGOUT_SUBMIT)
                    scheduleTimeout(LOGOUT_TIMEOUT_MS, "LOGOUT_TIMEOUT") {
                        retryLogoutOrLock("LOGOUT_TIMEOUT")
                    }
                }
            }
        }
    }

    private fun logoutControlReason(result: JSONObject?): String {
        fun bounded(name: String): Int = result?.optInt(name, -1)?.coerceIn(-1, 99) ?: -1
        val submenuVisible = if (result?.optBoolean("submenuVisible") == true) 1 else 0
        return "LOGOUT_CONTROL" +
            "_C${bounded("count")}" +
            "_A${bounded("exactAllCount")}" +
            "_V${bounded("visibleExactCount")}" +
            "_H${bounded("leafExactCount")}" +
            "_S$submenuVisible"
    }

    private fun retryLogoutOrLock(reason: String) {
        cancelTimeout()
        if (logoutAttempt < MAX_LOGOUT_RETRIES) {
            logoutAttempt += 1
            startLogoutAttempt()
        } else {
            pendingLockReason = reason
            showLocked(reason)
        }
    }

    private fun finishLogoutVerification() {
        if (state != WebPocState.LOGOUT_VERIFY) return
        cancelTimeout()
        if (!postClearVerificationPending) {
            postClearVerificationPending = true
            clearWebSessionAndReloadLogin()
            return
        }

        postClearVerificationPending = false
        wipeAttemptSecrets()
        val lockReason = pendingLockReason
        pendingLockReason = null
        when {
            lockReason != null -> showLocked(lockReason)
            secureKioskSession -> finishSecureKioskSession()
            gate3Session != null -> recordGate3CycleAndContinue()
            else -> showSetup()
        }
    }

    private fun finishSecureKioskSession() {
        if (!secureKioskSession || secureResultDelivered) return
        wipeRuntimeSecrets()
        transition(WebPocState.IDLE)
        secureResultDelivered = true
        secureKioskSession = false
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun finishSecureKioskSessionWithFailure(reason: String) {
        if (!secureKioskSession || secureResultDelivered) return
        secureResultDelivered = true
        secureKioskSession = false
        setResult(
            Activity.RESULT_CANCELED,
            Intent().putExtra(EXTRA_FAILURE_REASON, reason.take(MAX_BRIDGE_REASON_LENGTH)),
        )
        finish()
    }

    private fun recordGate3CycleAndContinue() {
        val session = gate3Session ?: return
        session.recordCompletedCycle()
        if (!preferences.edit()
                .putInt(KEY_GATE3_COMPLETED, session.completedCycles)
                .commit()
        ) {
            showLocked("GATE3_PROGRESS_PERSISTENCE")
            return
        }
        transition(WebPocState.IDLE)
        if (session.isComplete) {
            finishGate3Run()
            return
        }
        showGate3Progress()
        handler.postDelayed({
            if (state == WebPocState.IDLE && gate3Session === session) startGate3Cycle()
        }, GATE3_INTER_CYCLE_DELAY_MS)
    }

    private fun finishGate3Run() {
        val session = gate3Session ?: return
        val completed = session.completedCycles
        val target = session.targetCycles
        if (!persistGate3Outcome(GATE3_STATUS_PASSED, completed)) {
            showLocked("GATE3_PROGRESS_PERSISTENCE")
            return
        }
        session.wipe()
        gate3Session = null
        showSetup()
        gate3Result.text = getString(R.string.gate3_complete, completed, target)
        gate3Result.visibility = View.VISIBLE
    }

    private fun persistGate3Outcome(status: String, completed: Int): Boolean {
        val duration = (SystemClock.elapsedRealtime() - gate3StartedAtElapsedMs).coerceAtLeast(0L)
        return preferences.edit()
            .putString(KEY_GATE3_STATUS, status)
            .putInt(KEY_GATE3_COMPLETED, completed)
            .putInt(KEY_GATE3_TARGET, Gate3RunSession.DEFAULT_TARGET_CYCLES)
            .putLong(KEY_GATE3_DURATION_MS, duration)
            .commit()
    }

    @Suppress("DEPRECATION")
    private fun clearWebSessionAndReloadLogin() {
        webView.clearHistory()
        webView.clearFormData()
        webView.clearCache(true)
        webView.clearSslPreferences()
        WebViewDatabase.getInstance(this).clearFormData()
        WebStorage.getInstance().deleteAllData()
        scheduleTimeout(LOGOUT_TIMEOUT_MS, "SESSION_CLEAR_TIMEOUT")
        CookieManager.getInstance().removeAllCookies {
            CookieManager.getInstance().flush()
            handler.postDelayed({
                if (!destroyed && state == WebPocState.LOGOUT_VERIFY) {
                    webView.loadUrl(WebSecurityPolicy.LOGIN_URL)
                }
            }, STORAGE_CLEAR_DELAY_MS)
        }
    }

    private fun restartForRecovery() {
        cancelTimeout()
        wipeRuntimeSecrets()
        transition(WebPocState.RECOVERY_REQUIRED)
        recreate()
    }

    private fun showSetup() {
        cancelTimeout()
        wipeRuntimeSecrets()
        transition(WebPocState.IDLE)
        blocker.visibility = View.GONE
        progress.visibility = View.VISIBLE
        recoveryButton.visibility = View.GONE
        finishButton.visibility = View.GONE
        statusBadge.visibility = View.GONE
        webView.visibility = View.INVISIBLE
        setupError.visibility = View.GONE
        setupPanel.visibility = View.VISIBLE
        gate3AbortButton.visibility = View.GONE
        setGate3SetupMode(false)
        expectedNameInput.requestFocus()
    }

    private fun showActive() {
        blocker.visibility = View.GONE
        setupPanel.visibility = View.GONE
        webView.visibility = View.VISIBLE
        finishButton.visibility = View.VISIBLE
        gate3AbortButton.visibility = View.GONE
        statusBadge.visibility = View.VISIBLE
        statusBadge.setText(R.string.status_active)
    }

    private fun showBlocking(message: String) {
        setupPanel.visibility = View.GONE
        finishButton.visibility = View.GONE
        statusBadge.visibility = View.GONE
        blocker.visibility = View.VISIBLE
        progress.visibility = View.VISIBLE
        recoveryButton.visibility = View.GONE
        gate3AbortButton.visibility = if (gate3Session != null) View.VISIBLE else View.GONE
        blockerMessage.text = message
    }

    private fun showLocked(reason: String) {
        if (state == WebPocState.MAINTENANCE_REQUIRED) return
        cancelTimeout()
        failGate3RunIfActive()
        wipeRuntimeSecrets()
        transition(WebPocState.LOCKED, reason)
        setupPanel.visibility = View.GONE
        finishButton.visibility = View.GONE
        statusBadge.visibility = View.GONE
        blocker.visibility = View.VISIBLE
        progress.visibility = View.GONE
        gate3AbortButton.visibility = View.GONE
        blockerMessage.text = getString(R.string.status_locked_with_code, reason)
        recoveryButton.visibility = View.VISIBLE
        finishSecureKioskSessionWithFailure(reason)
    }

    internal fun rejectUnverifiedLogin() {
        showLocked("LOGIN_NOT_VERIFIED")
    }

    internal fun rejectInvalidLoginFingerprint(reason: String) {
        showMaintenance(reason)
    }

    private fun showMaintenance(reason: String) {
        if (state == WebPocState.LOCKED) return
        cancelTimeout()
        failGate3RunIfActive()
        wipeRuntimeSecrets()
        transition(WebPocState.MAINTENANCE_REQUIRED, reason)
        setupPanel.visibility = View.GONE
        finishButton.visibility = View.GONE
        statusBadge.visibility = View.GONE
        blocker.visibility = View.VISIBLE
        progress.visibility = View.GONE
        gate3AbortButton.visibility = View.GONE
        blockerMessage.text = getString(R.string.status_maintenance_with_code, reason)
        recoveryButton.visibility = View.VISIBLE
        finishSecureKioskSessionWithFailure(reason)
    }

    private fun failGate3RunIfActive() {
        val session = gate3Session ?: return
        persistGate3Outcome(GATE3_STATUS_FAILED, session.completedCycles)
        session.wipe()
        gate3Session = null
    }

    private fun isValidSanitizedLogin(result: JSONObject?): Boolean {
        result ?: return false
        return result.optBoolean("ok") &&
            result.optString("version") == WebDomScripts.CONTRACT_VERSION &&
            result.optBoolean("usernameEmpty") &&
            result.optBoolean("passwordEmpty") &&
            !result.optBoolean("rememberChecked", true)
    }

    private fun loginFingerprintReason(result: JSONObject?): String {
        fun bounded(name: String): Int = result?.optInt(name, -1)?.coerceIn(-1, 99) ?: -1
        fun flag(name: String): Int = if (result?.optBoolean(name) == true) 1 else 0
        val contractVersion = if (result?.optString("version") == WebDomScripts.CONTRACT_VERSION) 1 else 0
        return "LOGIN_FINGERPRINT" +
            "_U${bounded("usernameCount")}" +
            "_P${bounded("passwordCount")}" +
            "_C${bounded("checkboxCount")}" +
            "_B${bounded("submitCount")}" +
            "_F${bounded("formCount")}" +
            "_A${flag("actionOk")}" +
            "_E${flag("usernameEmpty")}${flag("passwordEmpty")}" +
            "_R${flag("rememberChecked")}" +
            "_V$contractVersion"
    }

    private fun evaluate(script: String, callback: (JSONObject?) -> Unit) {
        if (destroyed) return
        webView.evaluateJavascript(script) { raw ->
            if (!destroyed && !isTerminalState()) callback(parseJavascriptObject(raw))
        }
    }

    private fun isTerminalState(): Boolean =
        state == WebPocState.LOCKED || state == WebPocState.MAINTENANCE_REQUIRED

    private fun parseJavascriptObject(raw: String?): JSONObject? = runCatching {
        val outer = JSONTokener(raw ?: return null).nextValue()
        when (outer) {
            is JSONObject -> outer
            is String -> JSONObject(outer)
            else -> null
        }
    }.getOrNull()

    @SuppressLint("ApplySharedPref")
    private fun transition(next: WebPocState, reason: String? = null) {
        state = next
        val editor = preferences.edit().putString(KEY_STATE, next.name)
        if (reason == null) editor.remove(KEY_REASON) else editor.putString(KEY_REASON, reason)
        if (!editor.commit()) {
            state = WebPocState.LOCKED
            preferences.edit()
                .putString(KEY_STATE, WebPocState.LOCKED.name)
                .putString(KEY_REASON, "STATE_PERSISTENCE")
                .commit()
            throw IllegalStateException("Failed to persist fail-closed state")
        }
    }

    private fun scheduleTimeout(milliseconds: Long, reason: String, action: (() -> Unit)? = null) {
        val generation = ++timeoutGeneration
        handler.postDelayed({
            if (!destroyed && generation == timeoutGeneration) {
                if (action != null) action() else showLocked(reason)
            }
        }, milliseconds)
    }

    private fun cancelTimeout() {
        timeoutGeneration += 1
    }

    private fun clearSetupFields() {
        expectedNameInput.text?.clear()
        usernameInput.text?.clear()
        passwordInput.text?.clear()
        gate3ExpectedNameBInput.text?.clear()
        gate3UsernameBInput.text?.clear()
        gate3PasswordBInput.text?.clear()
        setupError.visibility = View.GONE
    }

    private fun wipeCredentialOnly() {
        ephemeralCredentials?.wipe()
        ephemeralCredentials = null
    }

    private fun wipeAttemptSecrets() {
        wipeCredentialOnly()
        expectedDisplayName = null
        clearSetupFields()
    }

    private fun wipeRuntimeSecrets() {
        wipeAttemptSecrets()
        gate3Session?.wipe()
        gate3Session = null
    }

    override fun onStop() {
        if (!destroyed && uiInitialized) {
            val activeGate3 = gate3Session
            when {
                secureKioskSession && !isFinishing -> {
                    showLocked("SECURE_SESSION_BACKGROUND")
                }
                activeGate3 != null -> {
                    persistGate3Outcome(GATE3_STATUS_ABORTED, activeGate3.completedCycles)
                    beginRecovery()
                }
                state.requiresRecoveryAfterRestart() && state != WebPocState.RECOVERY_REQUIRED -> {
                    beginRecovery()
                }
                else -> clearSetupFields()
            }
        }
        super.onStop()
    }

    override fun onDestroy() {
        destroyed = true
        cancelTimeout()
        if (uiInitialized) {
            gate3Session?.let {
                persistGate3Outcome(GATE3_STATUS_ABORTED, it.completedCycles)
            }
            wipeRuntimeSecrets()
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.clearCache(true)
            webView.clearSslPreferences()
            webView.removeAllViews()
            webView.destroy()
        }
        super.onDestroy()
    }

    private data class SecureBridgePayload(
        val expectedDisplayName: String,
        val username: String,
        val password: String,
    )

    private companion object {
        const val ACTION_START_SECURE_SESSION =
            "com.local.matholickiosk.action.START_SECURE_WEB_SESSION"
        const val CREDENTIAL_BRIDGE_AUTHORITY =
            "com.local.matholickiosk.kiosk.credentials"
        const val COLUMN_EXPECTED_NAME = "expected_name"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
        const val EXTRA_FAILURE_REASON = "failure_reason"
        const val MAX_BRIDGE_REASON_LENGTH = 80
        const val PREFERENCES_NAME = "web_poc_state"
        const val KEY_STATE = "state"
        const val KEY_REASON = "reason"
        const val KEY_GATE3_STATUS = "gate3_status"
        const val KEY_GATE3_COMPLETED = "gate3_completed"
        const val KEY_GATE3_TARGET = "gate3_target"
        const val KEY_GATE3_DURATION_MS = "gate3_duration_ms"
        const val GATE3_STATUS_RUNNING = "RUNNING"
        const val GATE3_STATUS_PASSED = "PASSED"
        const val GATE3_STATUS_FAILED = "FAILED"
        const val GATE3_STATUS_ABORTED = "ABORTED"
        const val PAGE_TIMEOUT_MS = 20_000L
        const val PROXY_STABILIZATION_DELAY_MS = 2_000L
        const val PREFLIGHT_DNS_RETRY_DELAY_MS = 3_500L
        const val LOGIN_TIMEOUT_MS = 30_000L
        const val GATE3_LOGIN_RESULT_TIMEOUT_MS = 60_000L
        const val LOGOUT_TIMEOUT_MS = 20_000L
        const val PROBE_DELAY_MS = 600L
        const val LOGIN_DOM_PROBE_DELAY_MS = 400L
        const val LOGIN_STABILITY_DELAY_MS = 800L
        const val LOGIN_DOM_PROBE_RETRIES = 10
        const val LOGIN_STABLE_READS_REQUIRED = 2
        const val MENU_OPEN_DELAY_MS = 350L
        const val LOGOUT_CONTROL_PROBE_DELAY_MS = 350L
        const val LOGOUT_CONTROL_PROBE_RETRIES = 10
        const val STORAGE_CLEAR_DELAY_MS = 300L
        const val GATE3_ACTIVE_DWELL_MS = 750L
        const val GATE3_INTER_CYCLE_DELAY_MS = 5_000L
        const val PORTAL_PROBE_RETRIES = 8
        const val MAX_LOGOUT_RETRIES = 1
    }
}
