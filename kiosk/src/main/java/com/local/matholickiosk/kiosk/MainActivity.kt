package com.local.matholickiosk.kiosk

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.VibratorManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import com.local.matholickiosk.kiosk.admin.KioskLockTaskController
import com.local.matholickiosk.kiosk.bridge.CredentialBridgeContract
import com.local.matholickiosk.kiosk.bridge.OneTimeCredentialBroker
import com.local.matholickiosk.kiosk.data.ActiveSessionEntity
import com.local.matholickiosk.kiosk.data.AdminAuthRepository
import com.local.matholickiosk.kiosk.data.AdminAuthResult
import com.local.matholickiosk.kiosk.data.BatchIssuedQr
import com.local.matholickiosk.kiosk.data.KioskDatabase
import com.local.matholickiosk.kiosk.data.MovedReusableCard
import com.local.matholickiosk.kiosk.data.StudentRepository
import com.local.matholickiosk.kiosk.data.StudentCsvParser
import com.local.matholickiosk.kiosk.data.ParsedStudentCsv
import com.local.matholickiosk.kiosk.data.ReusableCardSlotSummary
import com.local.matholickiosk.kiosk.data.StudentCsvImportPreview
import com.local.matholickiosk.kiosk.data.ValidatedStudent
import com.local.matholickiosk.kiosk.domain.CameraFacing
import com.local.matholickiosk.kiosk.domain.CameraFacingPolicy
import com.local.matholickiosk.kiosk.domain.ClassRosterSelectionState
import com.local.matholickiosk.kiosk.domain.DedicatedDevicePolicy
import com.local.matholickiosk.kiosk.domain.DiscardableSensitiveTask
import com.local.matholickiosk.kiosk.domain.FixedClassSlots
import com.local.matholickiosk.kiosk.domain.KioskState
import com.local.matholickiosk.kiosk.domain.LatestValueDispatcher
import com.local.matholickiosk.kiosk.domain.PcGradingCompletionMessage
import com.local.matholickiosk.kiosk.domain.RefreshableSelectionState
import com.local.matholickiosk.kiosk.domain.SensitiveHandoffTask
import com.local.matholickiosk.kiosk.domain.SensitiveTask
import com.local.matholickiosk.kiosk.domain.SessionPreflightInput
import com.local.matholickiosk.kiosk.domain.SessionPreflightPolicy
import com.local.matholickiosk.kiosk.domain.SingleFlightGate
import com.local.matholickiosk.kiosk.domain.StudentLoginPcNotificationPolicy
import com.local.matholickiosk.kiosk.domain.StudentLoginPcStage
import com.local.matholickiosk.kiosk.domain.ScannerCameraResumeAction
import com.local.matholickiosk.kiosk.domain.ScannerCameraResumePolicy
import com.local.matholickiosk.kiosk.print.BatchQrCard
import com.local.matholickiosk.kiosk.print.BatchQrPdfExporter
import com.local.matholickiosk.kiosk.print.QrPdfExporter
import com.local.matholickiosk.kiosk.print.QrPdfShareIntentFactory
import com.local.matholickiosk.kiosk.qr.QrFrameDecision
import com.local.matholickiosk.kiosk.qr.QrFrameGuidance
import com.local.matholickiosk.kiosk.qr.QrFrameRejection
import com.local.matholickiosk.kiosk.qr.QrFrameQuality
import com.local.matholickiosk.kiosk.qr.QrImageAnalyzer
import com.local.matholickiosk.kiosk.qr.QrImageRenderer
import com.local.matholickiosk.kiosk.qr.clearSensitiveData
import com.local.matholickiosk.kiosk.security.AndroidKeystoreCredentialCipher
import com.local.matholickiosk.kiosk.transfer.PcPairingStore
import com.local.matholickiosk.kiosk.transfer.PcControlClient
import com.local.matholickiosk.kiosk.transfer.PcCsvDownload
import com.local.matholickiosk.kiosk.transfer.PcEndpointResolver
import com.local.matholickiosk.kiosk.transfer.PcPdfSender
import com.local.matholickiosk.kiosk.transfer.PcReceiverPairing
import com.local.matholickiosk.kiosk.transfer.PcSubnetCandidates
import com.local.matholickiosk.kiosk.transfer.PcTransferProtocol
import java.io.File
import java.net.Inet4Address
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var appHeader: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var deviceModeText: TextView
    private lateinit var authPanel: LinearLayout
    private lateinit var authTitle: TextView
    private lateinit var authDescription: TextView
    private lateinit var pinInput: EditText
    private lateinit var pinConfirmInput: EditText
    private lateinit var authError: TextView
    private lateinit var authSubmit: Button
    private lateinit var adminPanel: LinearLayout
    private lateinit var classNameInput: EditText
    private lateinit var createClassButton: Button
    private lateinit var classSpinner: Spinner
    private lateinit var quickClassGrid: GridLayout
    private lateinit var classRosterText: TextView
    private lateinit var manageClassMembersButton: Button
    private lateinit var deleteClassButton: Button
    private lateinit var studentSpinner: Spinner
    private lateinit var registerStudentButton: Button
    private lateinit var importStudentCsvButton: Button
    private lateinit var reissueQrButton: Button
    private lateinit var updateProfileButton: Button
    private lateinit var updateCredentialsButton: Button
    private lateinit var deactivateStudentButton: Button
    private lateinit var reusableCardsButton: Button
    private lateinit var addTemporaryButton: Button
    private lateinit var startSessionButton: Button
    private lateinit var resumeSessionButton: Button
    private lateinit var selfTestButton: Button
    private lateinit var feedbackSettingsButton: Button
    private lateinit var keypadLayoutButton: Button
    private lateinit var remoteSupportButton: Button
    private lateinit var recoverSessionButton: Button
    private lateinit var adminMessage: TextView
    private lateinit var undoAdminButton: Button
    private lateinit var qrCardName: TextView
    private lateinit var qrImage: ImageView
    private lateinit var exportQrPdfButton: Button
    private lateinit var batchQrButton: Button
    private lateinit var pendingCardsPdfButton: Button
    private lateinit var cardStatusButton: Button
    private lateinit var pairPcButton: Button
    private lateinit var sendPcPdfButton: Button
    private lateinit var scannerPanel: FrameLayout
    private lateinit var scannerCenterContent: LinearLayout
    private lateinit var scannerActionControls: LinearLayout
    private lateinit var scannerInstruction: TextView
    private lateinit var scannerLensPointer: TextView
    private lateinit var scannerMessage: TextView
    private lateinit var cancelQrLoginButton: Button
    private lateinit var switchCameraButton: ImageButton
    private lateinit var sessionAdminButton: ImageButton
    private lateinit var scannerHelpButton: ImageButton
    private lateinit var scannerHelpPanel: FrameLayout
    private lateinit var scannerHelpCloseButton: Button

    private val mainHandler = Handler(Looper.getMainLooper())
    private var automaticAuthenticationGeneration = 0
    private var scannerHelpPausedAnalyzer = false
    private val scannerDisplayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit

        override fun onDisplayRemoved(displayId: Int) = Unit

        override fun onDisplayChanged(displayId: Int) {
            if (
                ::scannerPanel.isInitialized &&
                scannerPanel.display?.displayId == displayId
            ) {
                updateScannerLensPointer()
            }
        }
    }
    private var enrolledAdminPinLength: Int? = null
    private val automaticAuthenticationRunnable = Runnable {
        if (
            authEnrollmentMode || authBusy || authPanel.visibility != View.VISIBLE ||
            pinInput.text.length != enrolledAdminPinLength
        ) return@Runnable
        submitAuthentication()
    }
    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val pcControlExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var database: KioskDatabase
    private lateinit var authRepository: AdminAuthRepository
    private lateinit var studentRepository: StudentRepository
    private lateinit var lockTaskController: KioskLockTaskController
    private lateinit var pcPairingStore: PcPairingStore
    private lateinit var pcStatusDispatcher: LatestValueDispatcher<PcStatusUpdate>
    private lateinit var diagnosticLog: PrivateDiagnosticLog
    private lateinit var remoteSupportStore: RemoteSupportStore
    private lateinit var remoteSupportWindowController: RemoteSupportWindowController
    private val pcPdfSender = PcPdfSender()
    private val pcControlClient = PcControlClient()
    private val pcEndpointResolver = PcEndpointResolver()

    private var authEnrollmentMode = false
    private var authBusy = false
    private var initialStateLoadFailed = false
    private var reusableCardBootstrapMessage: String? = null
    private var classes: List<Choice> = emptyList()
    private var students: List<StudentChoice> = emptyList()
    private var reusableCardSlots: List<ReusableCardSlotSummary> = emptyList()
    private val classRosterState = ClassRosterSelectionState()
    private val studentSelectionState = RefreshableSelectionState()
    private val webRecoveryGate = SingleFlightGate()
    private val adminDataOperationGate = SingleFlightGate()
    private val studentLaunchGate = SingleFlightGate()
    private var issuedQrPreview: QrPreview? = null
    private var currentSession: ActiveSessionEntity? = null
    private var pendingTemporaryStudentIds: Set<String> = emptySet()
    private var suppressClassSelectionCallback = false
    private var suppressStudentSelectionCallback = false
    private var cameraProvider: ProcessCameraProvider? = null
    private var qrAnalyzer: QrImageAnalyzer? = null
    private var scannerVisible = false
    private var preferredCameraFacing = CameraFacing.FRONT
    private var activeCameraFacing = CameraFacing.FRONT
    private var cameraBindGeneration = 0
    private var qrGuidanceGeneration = 0
    private val scannerNoticeGate = ScannerNoticeGate()
    private var destroyed = false
    private var pendingCredentialBridgeId: String? = null
    private var pendingWebSessionId: String? = null
    private var relockAdminOnStart = false
    private var suppressNextAdminStopRelock = false
    private var dedicatedDevicePolicyFailed = false
    private var pendingRecoveryAction: PendingRecoveryAction = PendingRecoveryAction.None
    private var pendingSharedPdf: File? = null
    @Volatile
    private var pcPairingMode = false
    private var pairedPcDisplayName: String? = null
    private val quickClassButtons = linkedMapOf<String, Button>()
    private var manualStudentSelectionOnly = false
    private var manualStudentSelectionFlowActive = false
    private var sessionAdminActionFlowActive = false
    private var qrAcceptanceGeneration = 0
    private var studentFlowGeneration = 0
    private var activeStudentDisplayName: String? = null
    private var pendingAdminUndo: PendingAdminUndo? = null
    private var adminUndoGeneration = 0

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            bindCamera()
        } else {
            scannerMessage.text = "카메라 권한이 필요합니다\n선생님 확인이 필요합니다"
            statusText.text = "CAMERA_PERMISSION_REQUIRED"
        }
    }

    private val webSessionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        studentLaunchGate.finish()
        pendingCredentialBridgeId?.let(OneTimeCredentialBroker::revoke)
        pendingCredentialBridgeId = null
        val expectedSessionId = pendingWebSessionId
        pendingWebSessionId = null
        if (expectedSessionId == null) {
            diagnosticLog.record("STALE_WEB_SESSION_RESULT")
            return@registerForActivityResult
        }
        val failureReason = result.data
            ?.getStringExtra(CredentialBridgeContract.EXTRA_FAILURE_REASON)
            ?.take(80)
            ?: "WEB_SESSION_FAILED"
        val pcCompletionMessage = if (result.resultCode == Activity.RESULT_OK) {
            PcGradingCompletionMessage.fromWire(
                outcome = result.data
                    ?.getStringExtra(CredentialBridgeContract.EXTRA_GRADING_RESULT),
                wrongProblemNumbers = result.data
                    ?.getIntArrayExtra(CredentialBridgeContract.EXTRA_WRONG_PROBLEM_NUMBERS),
            )
        } else {
            null
        }
        persistWebSessionResult(
            passed = result.resultCode == Activity.RESULT_OK,
            failureReason = failureReason,
            expectedSessionId = expectedSessionId,
            pcCompletionMessage = pcCompletionMessage,
        )
    }

    private fun persistWebSessionResult(
        passed: Boolean,
        failureReason: String,
        expectedSessionId: String,
        pcCompletionMessage: String?,
    ) {
        diagnosticLog.record(
            if (passed) "WEB_SESSION_COMPLETE" else "WEB_SESSION_FAILED",
            if (passed) null else failureReason,
        )
        ioExecutor.execute {
            val before = runCatching { studentRepository.currentSession() }.getOrNull()
            if (before?.sessionId != expectedSessionId) {
                diagnosticLog.record("STALE_WEB_SESSION_RESULT")
                return@execute
            }
            val outcome = WebSessionResultPersistence.persist(
                passed = passed,
                persistTransition = {
                    if (passed) {
                        studentRepository.transitionSession(
                            expectedState = KioskState.PRELOGIN_CHECK,
                            state = KioskState.QR_READY,
                            expectedSessionId = expectedSessionId,
                        )
                    } else {
                        studentRepository.transitionSession(
                            expectedState = KioskState.PRELOGIN_CHECK,
                            state = KioskState.LOCKED,
                            expectedSessionId = expectedSessionId,
                            lockedReason = failureReason,
                        )
                    }
                },
                loadSession = { studentRepository.currentSession() },
            )
            val sessionAfterFailure = outcome.exceptionOrNull()
                ?.let { runCatching { studentRepository.currentSession() }.getOrNull() }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                outcome.fold(
                    onSuccess = { persisted ->
                        currentSession = persisted.session
                        if (persisted.passed) {
                            provideFeedback(success = true)
                            pcCompletionMessage?.let { message ->
                                reportPcStatus(
                                    state = message,
                                    studentName = activeStudentDisplayName,
                                    notify = true,
                                )
                            }
                            activeStudentDisplayName = null
                            showScanner()
                        } else {
                            provideFeedback(success = false)
                            reportPcStatus(
                                state = "복구 필요",
                                studentName = activeStudentDisplayName,
                                notify = true,
                            )
                            statusText.text = KioskState.LOCKED.name
                            showAuthentication(enrollment = false)
                            authError.text = "화면이 잠겼습니다 · $failureReason"
                        }
                    },
                    onFailure = {
                        if (sessionAfterFailure?.sessionId != expectedSessionId) {
                            currentSession = sessionAfterFailure
                            return@fold
                        }
                        reportPcStatus(
                            state = "복구 필요",
                            studentName = activeStudentDisplayName,
                            notify = true,
                        )
                        currentSession = null
                        statusText.text = KioskState.LOCKED.name
                        showAuthentication(enrollment = false)
                        authError.text =
                            "세션 결과를 확인하지 못했습니다. 관리자 PIN으로 상태를 확인하세요."
                    },
                )
            }
        }
    }

    private val webRecoveryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val requestedAction = pendingRecoveryAction
        pendingRecoveryAction = PendingRecoveryAction.None
        val failureReason = result.data
            ?.getStringExtra(CredentialBridgeContract.EXTRA_FAILURE_REASON)
            ?.take(80)
        if (result.resultCode == Activity.RESULT_OK) {
            when (requestedAction) {
                PendingRecoveryAction.None -> {
                    finishWebRecoveryOperation()
                    refreshAdminData("Web 로그인 상태를 안전하게 정리했습니다.")
                }
                is PendingRecoveryAction.StartSession ->
                    completeSessionStart(requestedAction)
                PendingRecoveryAction.EndSession ->
                    completeSessionEnd()
            }
        } else {
            finishWebRecoveryOperation()
            val message = "Web 세션 정리에 실패해 수업 상태를 변경하지 않았습니다" +
                (failureReason?.let { " · $it" } ?: "")
            refreshAdminData(message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingRecoveryAction = restorePendingRecoveryAction(savedInstanceState)
        pendingWebSessionId = savedInstanceState?.getString(KEY_PENDING_WEB_SESSION_ID)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        )
        remoteSupportStore = RemoteSupportStore(this)
        remoteSupportWindowController = RemoteSupportWindowController(
            activity = this,
            handler = mainHandler,
            store = remoteSupportStore,
            onStateChanged = ::updateRemoteSupportButton,
        )
        setContentView(R.layout.activity_main)
        bindViews()
        configureSensitiveViews()
        configureBackNavigation()
        lockTaskController = KioskLockTaskController(this)
        configureDedicatedDevice()

        database = KioskDatabase.get(this)
        authRepository = AdminAuthRepository(database)
        studentRepository = StudentRepository(
            database = database,
            cipher = AndroidKeystoreCredentialCipher(),
            appVersion = applicationVersion(),
        )
        pcPairingStore = PcPairingStore(this)
        diagnosticLog = PrivateDiagnosticLog(this)
        pcStatusDispatcher = LatestValueDispatcher(
            threadName = "pc-status-latest",
            preserve = { status -> status.notify },
        ) { status ->
            runCatching {
                withReachablePairedPc { pairing ->
                    pcControlClient.sendStatus(
                        pairing = pairing,
                        state = status.state,
                        studentName = status.studentName,
                        notify = status.notify,
                    )
                }
            }
        }
        RemoteQrTestBridge.register(this, ::handleRemoteQrTest)
        QrPdfExporter.cleanupExpired(this)
        configureActions()
        remoteSupportWindowController.start()
        loadInitialState()
    }

    private fun bindViews() {
        appHeader = findViewById(R.id.app_header)
        statusText = findViewById(R.id.status_text)
        deviceModeText = findViewById(R.id.device_mode_text)
        authPanel = findViewById(R.id.auth_panel)
        authTitle = findViewById(R.id.auth_title)
        authDescription = findViewById(R.id.auth_description)
        pinInput = findViewById(R.id.pin_input)
        pinConfirmInput = findViewById(R.id.pin_confirm_input)
        authError = findViewById(R.id.auth_error)
        authSubmit = findViewById(R.id.auth_submit)
        adminPanel = findViewById(R.id.admin_panel)
        classNameInput = findViewById(R.id.class_name_input)
        createClassButton = findViewById(R.id.create_class_button)
        classSpinner = findViewById(R.id.class_spinner)
        quickClassGrid = findViewById(R.id.quick_class_grid)
        classRosterText = findViewById(R.id.class_roster_text)
        manageClassMembersButton = findViewById(R.id.manage_class_members_button)
        deleteClassButton = findViewById(R.id.delete_class_button)
        studentSpinner = findViewById(R.id.student_spinner)
        registerStudentButton = findViewById(R.id.register_student_button)
        importStudentCsvButton = findViewById(R.id.import_student_csv_button)
        reissueQrButton = findViewById(R.id.reissue_qr_button)
        updateProfileButton = findViewById(R.id.update_profile_button)
        updateCredentialsButton = findViewById(R.id.update_credentials_button)
        deactivateStudentButton = findViewById(R.id.deactivate_student_button)
        reusableCardsButton = findViewById(R.id.reusable_cards_button)
        addTemporaryButton = findViewById(R.id.add_temporary_button)
        startSessionButton = findViewById(R.id.start_session_button)
        resumeSessionButton = findViewById(R.id.resume_session_button)
        selfTestButton = findViewById(R.id.self_test_button)
        feedbackSettingsButton = findViewById(R.id.feedback_settings_button)
        keypadLayoutButton = findViewById(R.id.keypad_layout_button)
        remoteSupportButton = findViewById(R.id.remote_support_button)
        recoverSessionButton = findViewById(R.id.recover_session_button)
        adminMessage = findViewById(R.id.admin_message)
        undoAdminButton = findViewById(R.id.undo_admin_button)
        qrCardName = findViewById(R.id.qr_card_name)
        qrImage = findViewById(R.id.qr_image)
        exportQrPdfButton = findViewById(R.id.export_qr_pdf_button)
        batchQrButton = findViewById(R.id.batch_qr_button)
        pendingCardsPdfButton = findViewById(R.id.pending_cards_pdf_button)
        cardStatusButton = findViewById(R.id.card_status_button)
        pairPcButton = findViewById(R.id.pair_pc_button)
        sendPcPdfButton = findViewById(R.id.send_pc_pdf_button)
        scannerPanel = findViewById(R.id.scanner_panel)
        scannerCenterContent = findViewById(R.id.scanner_center_content)
        scannerActionControls = findViewById(R.id.scanner_action_controls)
        scannerInstruction = findViewById(R.id.scanner_lens_instruction)
        scannerLensPointer = findViewById(R.id.scanner_lens_pointer)
        scannerMessage = findViewById(R.id.scanner_message)
        cancelQrLoginButton = findViewById(R.id.cancel_qr_login_button)
        switchCameraButton = findViewById(R.id.switch_camera_button)
        sessionAdminButton = findViewById(R.id.session_admin_button)
        scannerHelpButton = findViewById(R.id.scanner_help_button)
        scannerHelpPanel = findViewById(R.id.scanner_help_panel)
        scannerHelpCloseButton = findViewById(R.id.scanner_help_close_button)
        (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager)
            .registerDisplayListener(scannerDisplayListener, mainHandler)
    }

    private fun configureSensitiveViews() {
        listOf(pinInput, pinConfirmInput).forEach {
            it.isSaveEnabled = false
            it.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            it.filterTouchesWhenObscured = true
        }
        listOf(
            authSubmit,
            findViewById<Button>(R.id.create_class_button),
            registerStudentButton,
            importStudentCsvButton,
            manageClassMembersButton,
            deleteClassButton,
            reissueQrButton,
            updateProfileButton,
            updateCredentialsButton,
            deactivateStudentButton,
            reusableCardsButton,
            exportQrPdfButton,
            batchQrButton,
            pendingCardsPdfButton,
            cardStatusButton,
            undoAdminButton,
            pairPcButton,
            sendPcPdfButton,
            addTemporaryButton,
            startSessionButton,
            resumeSessionButton,
            selfTestButton,
            feedbackSettingsButton,
            keypadLayoutButton,
            remoteSupportButton,
            recoverSessionButton,
            cancelQrLoginButton,
            switchCameraButton,
            sessionAdminButton,
            scannerHelpButton,
            scannerHelpCloseButton,
        ).forEach { it.filterTouchesWhenObscured = true }
        pinConfirmInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitAuthentication()
                true
            } else {
                false
            }
        }
        pinInput.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    automaticAuthenticationGeneration += 1
                    mainHandler.removeCallbacks(automaticAuthenticationRunnable)
                    if (
                        !authEnrollmentMode && !authBusy &&
                        authPanel.visibility == View.VISIBLE &&
                        (s?.length ?: 0) == enrolledAdminPinLength
                    ) {
                        mainHandler.postDelayed(automaticAuthenticationRunnable, 300L)
                    }
                }

                override fun afterTextChanged(s: Editable?) = Unit
            },
        )
    }

    private fun configureActions() {
        authSubmit.setOnClickListener {
            if (initialStateLoadFailed) {
                loadInitialState()
            } else {
                submitAuthentication()
            }
        }
        createClassButton.setOnClickListener { createClass() }
        configureQuickClassButtons()
        registerStudentButton.setOnClickListener { showRegisterStudentDialog() }
        importStudentCsvButton.setOnClickListener { fetchStudentCsvFromPc() }
        manageClassMembersButton.setOnClickListener { showClassMembershipDialog() }
        deleteClassButton.setOnClickListener { confirmDeleteClass() }
        reissueQrButton.setOnClickListener { confirmReissueQr() }
        updateProfileButton.setOnClickListener { showUpdateStudentNameDialog() }
        updateCredentialsButton.setOnClickListener { showUpdateCredentialsDialog() }
        deactivateStudentButton.setOnClickListener { confirmDeactivateStudent() }
        reusableCardsButton.setOnClickListener { showReusableCardMenu() }
        exportQrPdfButton.setOnClickListener { confirmQrPdfExport() }
        batchQrButton.setOnClickListener { confirmBatchQrPrint() }
        pendingCardsPdfButton.setOnClickListener { showPendingCardsDialog() }
        cardStatusButton.setOnClickListener { showCardStatusDialog() }
        undoAdminButton.setOnClickListener { performPendingAdminUndo() }
        pairPcButton.setOnClickListener { startPcPairingScanner() }
        sendPcPdfButton.setOnClickListener { confirmPcPdfTransfer() }
        addTemporaryButton.setOnClickListener { showTemporaryStudentDialog() }
        startSessionButton.setOnClickListener { startOrEndSession() }
        resumeSessionButton.setOnClickListener { showScanner() }
        selfTestButton.setOnClickListener { runOperationalSelfTest() }
        feedbackSettingsButton.setOnClickListener { showFeedbackSettings() }
        keypadLayoutButton.setOnClickListener { showKeypadLayoutSettings() }
        remoteSupportButton.setOnClickListener { toggleRemoteSupport() }
        recoverSessionButton.setOnClickListener { confirmOneButtonRecovery() }
        cancelQrLoginButton.setOnClickListener { cancelPendingQrLogin() }
        switchCameraButton.setOnClickListener { switchCamera() }
        scannerHelpButton.setOnClickListener {
            if (scannerHelpPanel.visibility == View.VISIBLE) {
                hideScannerHelp(resumeAnalyzer = true)
            } else {
                showScannerHelp()
            }
        }
        scannerHelpCloseButton.setOnClickListener { hideScannerHelp(resumeAnalyzer = true) }
        sessionAdminButton.setOnClickListener {
            if (pcPairingMode) {
                returnToAdminAfterPcPairing("PC 페어링을 취소했습니다.")
            } else if (studentLaunchGate.isActive) {
                scannerMessage.text = "학생 로그인을 준비하고 있습니다. 잠시 기다리세요"
            } else {
                qrAcceptanceGeneration += 1
                studentFlowGeneration += 1
                cancelQrLoginButton.visibility = View.GONE
                activeStudentDisplayName = null
                requestSessionAdminAuthentication()
            }
        }
        classSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                if (!suppressClassSelectionCallback) {
                    val selectedClassId = classes.getOrNull(position)?.id
                    classRosterState.select(selectedClassId)?.let { request ->
                        pendingTemporaryStudentIds = emptySet()
                        updateClassRosterUi()
                        refreshSelectedClassDetails(request)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                classRosterState.resolve(classId = null, studentIds = emptySet())
                pendingTemporaryStudentIds = emptySet()
                updateClassRosterUi()
            }
        }
        studentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                if (!suppressStudentSelectionCallback) {
                    studentSelectionState.select(students.getOrNull(position)?.id)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                if (!suppressStudentSelectionCallback) {
                    studentSelectionState.select(null)
                }
            }
        }
    }

    private fun configureBackNavigation() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (scannerHelpPanel.visibility == View.VISIBLE) {
                        hideScannerHelp(resumeAnalyzer = true)
                    }
                    // The administrator can deliberately leave Lock Task to use Recents, but
                    // system Back must not discard the current admin form or reopen PIN entry.
                }
            },
        )
    }

    private fun loadInitialState() {
        showInitialStateLoading()
        ioExecutor.execute {
            var stage = "DATABASE_OPEN"
            val result = runCatching {
                database.openHelper.writableDatabase
                stage = "AUDIT_MAINTENANCE"
                studentRepository.maintainAuditRetention()
                stage = "ADMIN_ENROLLMENT"
                val enrolled = authRepository.isEnrolled()
                stage = "ADMIN_PIN_LENGTH"
                val pinLength = authRepository.enrolledPinLength()
                stage = "SESSION_RECOVERY"
                val recoveredState = studentRepository.applyRestartPolicy()
                val preparedReusableCards = run {
                    stage = "REUSABLE_CARD_BOOTSTRAP"
                    studentRepository.ensureReusableCardSlots()
                }
                val reusableCardDelivery = deliverReusableQrCards(preparedReusableCards)
                InitialStateSnapshot(
                    enrolled = enrolled,
                    pinLength = pinLength,
                    recoveredState = recoveredState,
                    reusableCardBootstrapMessage = when {
                        preparedReusableCards.isEmpty() -> null
                        reusableCardDelivery.isSuccess ->
                            "신규용 더미 QR ${preparedReusableCards.size}장을 지정 PC에 저장했습니다."
                        else ->
                            "신규용 더미 데이터 ${preparedReusableCards.size}장은 준비됐지만 " +
                                "QR PDF 전송은 실패했습니다. 관리자 화면에서 다시 준비하세요."
                    },
                )
            }
            result.exceptionOrNull()?.let {
                diagnosticLog.record("INITIALIZATION_FAILED", stage)
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { snapshot ->
                        enrolledAdminPinLength = snapshot.pinLength
                        reusableCardBootstrapMessage = snapshot.reusableCardBootstrapMessage
                        statusText.text = snapshot.recoveredState.name
                        showAuthentication(enrollment = !snapshot.enrolled)
                    },
                    onFailure = {
                        showInitialStateFailure(stage)
                    },
                )
            }
        }
    }

    private fun showInitialStateLoading() {
        stopCamera()
        pcPairingMode = false
        initialStateLoadFailed = false
        authEnrollmentMode = false
        authPanel.visibility = View.VISIBLE
        adminPanel.visibility = View.GONE
        scannerPanel.visibility = View.GONE
        scannerVisible = false
        statusText.text = "초기 상태 확인 중"
        authTitle.text = "초기 상태 확인 중"
        authDescription.text = "저장된 관리자 설정과 수업 복구 상태를 안전하게 확인하고 있습니다."
        pinInput.text.clear()
        pinConfirmInput.text.clear()
        pinInput.visibility = View.GONE
        pinConfirmInput.visibility = View.GONE
        authError.text = ""
        authSubmit.text = "확인 중"
        authSubmit.isEnabled = false
        enterDedicatedMode()
    }

    private fun showInitialStateFailure(stage: String) {
        stopCamera()
        pcPairingMode = false
        initialStateLoadFailed = true
        authEnrollmentMode = false
        authPanel.visibility = View.VISIBLE
        adminPanel.visibility = View.GONE
        scannerPanel.visibility = View.GONE
        scannerVisible = false
        statusText.text = "INITIALIZATION_FAILED"
        authTitle.text = "초기 상태 복구 실패"
        authDescription.text =
            "저장된 관리자 설정과 수업 상태를 확인하지 못했습니다. 다른 작업은 차단했습니다."
        pinInput.text.clear()
        pinConfirmInput.text.clear()
        pinInput.visibility = View.GONE
        pinConfirmInput.visibility = View.GONE
        authError.text =
            "기기 데이터는 변경하지 않았습니다. 잠시 후 다시 시도하세요. · $stage"
        authSubmit.text = "다시 시도"
        authSubmit.isEnabled = true
        enterDedicatedMode()
    }

    private fun showAuthentication(enrollment: Boolean) {
        remoteSupportWindowController.setSensitiveScreen(true)
        qrAcceptanceGeneration += 1
        studentFlowGeneration += 1
        activeStudentDisplayName = null
        stopCamera()
        pcPairingMode = false
        initialStateLoadFailed = false
        authEnrollmentMode = enrollment
        automaticAuthenticationGeneration += 1
        mainHandler.removeCallbacks(automaticAuthenticationRunnable)
        appHeader.visibility = View.VISIBLE
        authPanel.visibility = View.VISIBLE
        adminPanel.visibility = View.GONE
        scannerPanel.visibility = View.GONE
        scannerVisible = false
        authTitle.text = if (enrollment) "관리자 PIN 설정" else "관리자 인증"
        authDescription.text = if (enrollment) {
            "숫자 6~12자리 PIN을 이 기기에서 설정하세요. 기본 PIN과 복구 PIN은 없습니다."
        } else {
            "관리자 PIN을 입력하세요."
        }
        pinInput.visibility = View.VISIBLE
        pinConfirmInput.visibility = if (enrollment) View.VISIBLE else View.GONE
        authSubmit.text = if (enrollment) "설정" else "인증"
        authError.text = ""
        pinInput.text.clear()
        pinConfirmInput.text.clear()
        setAuthBusy(false)
        pinInput.requestFocus()
        enterDedicatedMode()
        reportPcStatus(
            state = if (enrollment) "관리자 PIN 설정 필요" else "관리자 인증 필요",
            studentName = null,
            notify = false,
        )
    }

    private fun submitAuthentication() {
        if (authBusy) return
        automaticAuthenticationGeneration += 1
        mainHandler.removeCallbacks(automaticAuthenticationRunnable)
        val pin = pinInput.text.toSensitiveCharArray()
        val attemptedPinLength = pin.size
        val confirmation = if (authEnrollmentMode) {
            pinConfirmInput.text.toSensitiveCharArray()
        } else {
            null
        }
        if (authEnrollmentMode && confirmation?.contentEquals(pin) != true) {
            pin.fill('\u0000')
            confirmation?.fill('\u0000')
            authError.text = "PIN 확인값이 일치하지 않습니다."
            return
        }
        confirmation?.fill('\u0000')
        pinInput.text.clear()
        pinConfirmInput.text.clear()
        setAuthBusy(true)
        executeSensitive(
            cleanup = { pin.fill('\u0000') },
        ) {
            val result = runCatching {
                if (authEnrollmentMode) {
                    authRepository.enroll(pin)
                    AdminAuthResult.Success
                } else {
                    authRepository.authenticate(pin)
                }
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                setAuthBusy(false)
                result.fold(
                    onSuccess = {
                        if (it == AdminAuthResult.Success) {
                            enrolledAdminPinLength = attemptedPinLength
                        }
                        handleAuthResult(it)
                    },
                    onFailure = {
                        pin.fill('\u0000')
                        authError.text = it.message ?: "관리자 인증 처리에 실패했습니다."
                    },
                )
            }
        }
    }

    private fun handleAuthResult(result: AdminAuthResult) {
        when (result) {
            AdminAuthResult.Success -> showAdmin()
            AdminAuthResult.NotEnrolled -> showAuthentication(enrollment = true)
            is AdminAuthResult.Rejected -> {
                val seconds = (result.retryAfterMillis + 999) / 1_000
                authError.text = "PIN이 올바르지 않습니다. ${seconds}초 후 다시 시도하세요."
            }
        }
    }

    private fun setAuthBusy(busy: Boolean) {
        authBusy = busy
        authSubmit.isEnabled = !busy
        pinInput.isEnabled = !busy
        pinConfirmInput.isEnabled = !busy
    }

    private fun showAdmin(message: String? = null) {
        remoteSupportWindowController.setSensitiveScreen(false)
        stopCamera()
        pcPairingMode = false
        setSessionControlMode(admin = true)
        appHeader.visibility = View.VISIBLE
        authPanel.visibility = View.GONE
        scannerPanel.visibility = View.GONE
        adminPanel.visibility = View.VISIBLE
        scannerVisible = false
        suppressNextAdminStopRelock = true
        exitDedicatedModeForAdministrator()
        mainHandler.postDelayed(
            { suppressNextAdminStopRelock = false },
            LOCK_TASK_EXIT_LIFECYCLE_GRACE_MS,
        )
        statusText.text = "ADMIN_LOADING"
        val startupMessage = reusableCardBootstrapMessage
        reusableCardBootstrapMessage = null
        refreshAdminData(message ?: startupMessage)
        refreshPcPairingState()
        reportPcStatus("관리자 화면", null, notify = false)
    }

    private fun reportPcStatus(
        state: String,
        studentName: String?,
        notify: Boolean,
    ) {
        if (!::pcStatusDispatcher.isInitialized) return
        pcStatusDispatcher.submit(PcStatusUpdate(state, studentName, notify))
    }

    private fun <T> withReachablePairedPc(
        operation: (PcReceiverPairing) -> T,
    ): T {
        val original = requireNotNull(pcPairingStore.load()) {
            "저장된 PC 페어링이 없습니다."
        }
        var recovered: PcReceiverPairing? = null
        try {
            return try {
                operation(original)
            } catch (firstFailure: Exception) {
                val candidateHosts = pcRecoveryCandidateHosts(original)
                val resolved = try {
                    pcEndpointResolver.resolve(original, candidateHosts)
                } catch (recoveryFailure: Exception) {
                    firstFailure.addSuppressed(recoveryFailure)
                    throw firstFailure
                }
                recovered = resolved
                pcPairingStore.save(resolved)
                diagnosticLog.record("PC_ENDPOINT_RECOVERED")
                try {
                    operation(resolved)
                } catch (retryFailure: Exception) {
                    retryFailure.addSuppressed(firstFailure)
                    throw retryFailure
                }
            }
        } finally {
            original.clearSensitiveData()
            recovered?.clearSensitiveData()
        }
    }

    private fun pcRecoveryCandidateHosts(pairing: PcReceiverPairing): List<String> {
        val connectivity = getSystemService(ConnectivityManager::class.java)
        val network = requireNotNull(connectivity?.activeNetwork) {
            "활성 네트워크가 없습니다."
        }
        val capabilities = requireNotNull(connectivity.getNetworkCapabilities(network)) {
            "활성 네트워크 상태를 확인하지 못했습니다."
        }
        require(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            "PC 주소 자동 복구는 같은 Wi-Fi에서만 수행합니다."
        }
        val link = requireNotNull(
            connectivity.getLinkProperties(network)
                ?.linkAddresses
                ?.firstOrNull { address ->
                    address.address is Inet4Address &&
                        address.address.isSiteLocalAddress
                },
        ) {
            "Wi-Fi 사설 IPv4 주소를 확인하지 못했습니다."
        }
        return PcSubnetCandidates.samePrivateSubnet(
            localAddress = requireNotNull(link.address.hostAddress),
            prefixLength = link.prefixLength,
            previousHost = pairing.host,
        )
    }

    private fun configureDedicatedDevice() {
        val configured = lockTaskController.configureIfDeviceOwner()
        dedicatedDevicePolicyFailed = configured.isFailure
        if (configured.getOrDefault(false)) {
            enterDedicatedMode()
        } else {
            updateDedicatedDeviceStatus(administratorUnlocked = false)
        }
    }

    private fun enterDedicatedMode() {
        val entered = lockTaskController.enterRestrictedMode()
        dedicatedDevicePolicyFailed =
            dedicatedDevicePolicyFailed || entered.isFailure || !entered.getOrDefault(false)
        updateDedicatedDeviceStatus(administratorUnlocked = false)
        mainHandler.postDelayed(
            {
                if (
                    !destroyed &&
                    adminPanel.visibility != View.VISIBLE
                ) {
                    updateDedicatedDeviceStatus(administratorUnlocked = false)
                }
            },
            LOCK_TASK_STATUS_REFRESH_MS,
        )
    }

    private fun exitDedicatedModeForAdministrator() {
        val exited = lockTaskController.exitForAdministrator()
        dedicatedDevicePolicyFailed = dedicatedDevicePolicyFailed || exited.isFailure
        updateDedicatedDeviceStatus(administratorUnlocked = true)
    }

    private fun updateDedicatedDeviceStatus(administratorUnlocked: Boolean) {
        deviceModeText.text = if (dedicatedDevicePolicyFailed) {
            "보안 정책 오류"
        } else {
            DedicatedDevicePolicy.statusLabel(
                status = lockTaskController.status(),
                administratorUnlocked = administratorUnlocked,
            )
        }
    }

    private fun refreshAdminData(
        message: String? = null,
        preferredClassId: String? = classes.getOrNull(classSpinner.selectedItemPosition)?.id,
        preferredStudentId: String? = studentSelectionState.selectedId,
        completeAdminDataOperationAfterLoad: Boolean = false,
    ) {
        val classSelectionSnapshot = classRosterState.snapshotSelection()
        val studentSelectionSnapshot = studentSelectionState.snapshotSelection()
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.ensureClasses(FixedClassSlots.names)
                val loadedClasses = studentRepository.listClasses()
                    .map { Choice(it.classId, it.className) }
                val loadedStudents = studentRepository.listStudents()
                    .map {
                        StudentChoice(
                            id = it.studentId,
                            label = it.displayNameExact,
                            reusableCardLabel = it.reusableCardLabel,
                        )
                    }
                val loadedReusableCardSlots = studentRepository.listReusableCardSlots()
                val session = studentRepository.currentSession()
                val resolvedClassId = session?.classId
                    ?: preferredClassId?.takeIf { candidate ->
                        loadedClasses.any { it.id == candidate }
                    }
                    ?: loadedClasses.firstOrNull()?.id
                AdminDataSnapshot(
                    classes = loadedClasses,
                    students = loadedStudents,
                    reusableCardSlots = loadedReusableCardSlots,
                    session = session,
                    resolvedClassId = resolvedClassId,
                    membershipStudentIds = resolvedClassId
                        ?.let(studentRepository::membershipStudentIds)
                        .orEmpty(),
                )
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { snapshot ->
                        applyAdminDataSnapshot(
                            snapshot = snapshot,
                            classSelectionSnapshot = classSelectionSnapshot,
                            studentSelectionSnapshot = studentSelectionSnapshot,
                            preferredStudentId = preferredStudentId,
                            completeAdminDataOperationAfterLoad = completeAdminDataOperationAfterLoad,
                            message = message,
                        )
                    },
                    onFailure = {
                        if (completeAdminDataOperationAfterLoad) adminDataOperationGate.finish()
                        updateSessionAdminControls(currentSession)
                        updateStudentManagementControls()
                        updateClassRosterUi()
                        adminMessage.text = adminRefreshFailureMessage(message)
                    },
                )
            }
        }
    }

    private fun applyAdminDataSnapshot(
        snapshot: AdminDataSnapshot,
        classSelectionSnapshot: ClassRosterSelectionState.SelectionSnapshot,
        studentSelectionSnapshot: RefreshableSelectionState.SelectionSnapshot,
        preferredStudentId: String?,
        completeAdminDataOperationAfterLoad: Boolean,
        message: String?,
    ) {
        classes = snapshot.classes
        students = snapshot.students
        reusableCardSlots = snapshot.reusableCardSlots
        currentSession = snapshot.session
        classRosterState.resolveRefresh(
            snapshot = classSelectionSnapshot,
            loadedClassId = snapshot.resolvedClassId,
            loadedStudentIds = snapshot.membershipStudentIds,
            availableClassIds = snapshot.classes.mapTo(mutableSetOf(), Choice::id),
            forceLoadedSelection = snapshot.session != null,
        )
        val displayedClassId = classRosterState.selectedClassId
        val displayedStudentId = studentSelectionState.resolveRefresh(
            snapshot = studentSelectionSnapshot,
            preferredId = preferredStudentId,
            availableIds = snapshot.students.map(StudentChoice::id),
        )
        pendingTemporaryStudentIds = pendingTemporaryStudentIds
            .intersect(students.mapTo(mutableSetOf(), StudentChoice::id))
        suppressClassSelectionCallback = true
        suppressStudentSelectionCallback = true
        classSpinner.adapter = choiceAdapter(classes, "먼저 반을 생성하세요")
        studentSpinner.adapter = studentChoiceAdapter(students, "등록 학생이 없습니다")
        classes.indexOfFirst { it.id == displayedClassId }
            .takeIf { it >= 0 }
            ?.let(classSpinner::setSelection)
        students.indexOfFirst { it.id == displayedStudentId }
            .takeIf { it >= 0 }
            ?.let(studentSpinner::setSelection)
        suppressClassSelectionCallback = false
        suppressStudentSelectionCallback = false
        if (completeAdminDataOperationAfterLoad) adminDataOperationGate.finish()
        updateSessionAdminControls(snapshot.session)
        updateStudentManagementControls()
        updateClassRosterUi()
        updateQuickClassButtons()
        adminMessage.text = message.orEmpty()
    }

    private fun configureQuickClassButtons() {
        quickClassGrid.removeAllViews()
        quickClassButtons.clear()
        FixedClassSlots.names.forEach { className ->
            val button = Button(this).apply {
                text = className
                minHeight = dp(52)
                textSize = 16f
                stateListAnimator = null
                filterTouchesWhenObscured = true
                setOnClickListener { selectQuickClass(className) }
            }
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(3), dp(3), dp(3), dp(3))
            }
            quickClassGrid.addView(button, params)
            quickClassButtons[className] = button
        }
    }

    private fun selectQuickClass(className: String) {
        val index = classes.indexOfFirst { it.label == className }
        if (index < 0) {
            adminMessage.text = "$className 반을 준비하고 있습니다. 잠시 후 다시 누르세요."
            refreshAdminData()
            return
        }
        classSpinner.setSelection(index)
    }

    private fun updateQuickClassButtons() {
        val selectedName = classes.getOrNull(classSpinner.selectedItemPosition)?.label
        quickClassButtons.forEach { (className, button) ->
            val isSelected = className == selectedName
            button.isEnabled = !adminDataOperationGate.isActive &&
                !webRecoveryGate.isActive &&
                (currentSession?.sessionId == null || className == selectedName)
            button.alpha = if (isSelected) 1f else 0.72f
        }
    }

    private fun adminRefreshFailureMessage(completedMessage: String?): String =
        if (completedMessage.isNullOrBlank()) {
            "관리자 목록을 불러오지 못했습니다. 관리자 화면을 다시 열어 재시도하세요."
        } else {
            "$completedMessage 다만 최신 목록을 불러오지 못했습니다. " +
                "관리자 화면을 다시 열어 재시도하세요."
        }

    private fun choiceAdapter(choices: List<Choice>, emptyLabel: String): ArrayAdapter<String> =
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            choices.map(Choice::label).ifEmpty { listOf(emptyLabel) },
        )

    private fun studentChoiceAdapter(
        choices: List<StudentChoice>,
        emptyLabel: String,
    ): ArrayAdapter<String> =
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            choices.map { choice ->
                choice.reusableCardLabel?.let { slotLabel ->
                    "${choice.label} · $slotLabel"
                } ?: choice.label
            }.ifEmpty { listOf(emptyLabel) },
        )

    private fun createClass() {
        val name = classNameInput.text.toString().trim()
        if (name.isEmpty()) {
            adminMessage.text = "반 이름을 입력하세요."
            return
        }
        if (!beginAdminDataOperation("반 생성 중")) return
        ioExecutor.execute {
            val result = runCatching { studentRepository.createClass(name) }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { createdClassId ->
                        classNameInput.text.clear()
                            refreshAdminData(
                                message = "반을 생성했습니다.",
                                preferredClassId = createdClassId,
                                completeAdminDataOperationAfterLoad = true,
                            )
                        },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text = it.message ?: "반 생성 실패"
                    },
                )
            }
        }
    }

    private fun confirmDeleteClass() {
        val selected = classes.getOrNull(classSpinner.selectedItemPosition)
        if (selected == null) {
            adminMessage.text = "삭제할 반을 선택하세요."
            return
        }
        if (FixedClassSlots.contains(selected.label)) {
            adminMessage.text =
                "${selected.label}은 고정 빠른선택 반이라 삭제할 수 없습니다. 테스트반만 삭제할 수 있습니다."
            return
        }
        AlertDialog.Builder(this)
            .setTitle("반 삭제")
            .setMessage(
                "${selected.label} 반과 소속 관계를 삭제합니다.\n" +
                    "학생 정보와 QR은 삭제되지 않으며 다른 반에서는 그대로 사용할 수 있습니다.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("반 삭제") { _, _ -> deleteClass(selected) }
            .show()
    }

    private fun deleteClass(selected: Choice) {
        if (!beginAdminDataOperation("반 삭제 중")) return
        val previousMembers = classRosterState.membershipStudentIds.toSet()
        ioExecutor.execute {
            val result = runCatching { studentRepository.deleteClass(selected.id) }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        pendingTemporaryStudentIds = emptySet()
                        offerAdminUndo(
                            PendingAdminUndo.RestoreClass(
                                selected.id,
                                selected.label,
                                previousMembers,
                            ),
                        )
                        refreshAdminData(
                            message = "${selected.label} 반을 삭제했습니다.",
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text = it.message ?: "반 삭제 실패"
                    },
                )
            }
        }
    }

    private fun showClassMembershipDialog() {
        val selectedClass = classes.getOrNull(classSpinner.selectedItemPosition)
        if (selectedClass == null) {
            adminMessage.text = "반을 먼저 선택하세요."
            return
        }
        if (students.isEmpty()) {
            adminMessage.text = "먼저 학생을 등록하세요."
            return
        }
        val chosen = classRosterState.membershipStudentIds.toMutableSet()
        val checked = BooleanArray(students.size) { index -> students[index].id in chosen }
        AlertDialog.Builder(this)
            .setTitle("${selectedClass.label} 학생 구성")
            .setMultiChoiceItems(
                students.map(StudentChoice::label).toTypedArray(),
                checked,
            ) { _, which, isChecked ->
                val studentId = students[which].id
                if (isChecked) chosen += studentId else chosen -= studentId
            }
            .setNegativeButton("취소", null)
            .setPositiveButton("저장") { _, _ ->
                replaceClassMemberships(selectedClass, chosen)
            }
            .show()
    }

    private fun replaceClassMemberships(selectedClass: Choice, studentIds: Set<String>) {
        if (!beginAdminDataOperation("반 학생 구성 저장 중")) return
        val previousStudentIds = classRosterState.membershipStudentIds.toSet()
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.replaceClassMemberships(selectedClass.id, studentIds)
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        if (classRosterState.replaceIfSelected(selectedClass.id, studentIds)) {
                            pendingTemporaryStudentIds -= studentIds
                            updateClassRosterUi()
                        }
                        offerAdminUndo(
                            PendingAdminUndo.RestoreMemberships(
                                selectedClass.id,
                                selectedClass.label,
                                previousStudentIds,
                            ),
                        )
                        refreshAdminData(
                            message = "${selectedClass.label} 반 학생 ${studentIds.size}명을 저장했습니다.",
                            preferredClassId = selectedClass.id,
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text = it.message ?: "반 학생 구성 저장 실패"
                    },
                )
            }
        }
    }

    private fun refreshSelectedClassDetails(
        request: ClassRosterSelectionState.LoadRequest,
    ) {
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.membershipStudentIds(request.classId)
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { membershipIds ->
                        if (!classRosterState.apply(request, membershipIds)) {
                            return@runOnUiThread
                        }
                        pendingTemporaryStudentIds -= membershipIds
                        updateClassRosterUi()
                    },
                    onFailure = {
                        if (!classRosterState.fail(request)) {
                            return@runOnUiThread
                        }
                        pendingTemporaryStudentIds = emptySet()
                        updateClassRosterUi()
                        adminMessage.text =
                            "반 소속 학생을 불러오지 못했습니다. 관리자 화면을 다시 열어 재시도하세요."
                    },
                )
            }
        }
    }

    private fun updateClassRosterUi() {
        val selectedClass = classes.getOrNull(classSpinner.selectedItemPosition)
        val memberNames = students
            .filter { it.id in classRosterState.membershipStudentIds }
            .map(StudentChoice::label)
        classRosterText.text = when {
            selectedClass == null -> "반을 먼저 생성하세요."
            classRosterState.isLoading -> "소속 학생 불러오는 중"
            classRosterState.hasLoadFailure -> "소속 학생을 불러오지 못했습니다."
            memberNames.isEmpty() -> "소속 학생 없음"
            else -> "소속 ${memberNames.size}명 · ${memberNames.joinToString(", ")}"
        }
        val activeClassId = currentSession?.classId
        val operationAvailable =
            !adminDataOperationGate.isActive && !webRecoveryGate.isActive
        val classAvailable = selectedClass != null
        val classReady = classAvailable &&
            !classRosterState.isLoading &&
            !classRosterState.hasLoadFailure
        classNameInput.isEnabled = operationAvailable
        createClassButton.isEnabled = operationAvailable && currentSession?.sessionId == null
        manageClassMembersButton.isEnabled = operationAvailable && classReady &&
            currentSession?.sessionId == null
        deleteClassButton.isEnabled = operationAvailable && classReady &&
            selectedClass?.label?.let(FixedClassSlots::contains) == false &&
            (currentSession?.sessionId == null || activeClassId != selectedClass?.id)
        addTemporaryButton.isEnabled = operationAvailable && classReady && students.any {
            it.id !in classRosterState.membershipStudentIds
        }
        startSessionButton.isEnabled = operationAvailable && !webRecoveryGate.isActive &&
            (currentSession?.sessionId != null || classReady)
        resumeSessionButton.isEnabled = operationAvailable && !webRecoveryGate.isActive
        val pendingCount = pendingTemporaryStudentIds.size
        addTemporaryButton.text = if (currentSession?.sessionId == null) {
            "이번 수업 보강 학생 선택" + if (pendingCount > 0) " (${pendingCount}명)" else ""
        } else {
            "현재 수업 보강 학생 추가"
        }
        batchQrButton.isEnabled = operationAvailable && classReady &&
            currentSession?.sessionId == null &&
            classRosterState.membershipStudentIds.isNotEmpty() &&
            pairedPcDisplayName != null
        updateQuickClassButtons()
    }

    private fun showRegisterStudentDialog() {
        val nameInput = dialogTextInput(
            hint = "학생 전체 이름",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
        )
        val usernameInput = dialogTextInput(
            hint = "학습 계정 아이디",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            sensitive = true,
        )
        val passwordInput = dialogTextInput(
            hint = "학습 계정 비밀번호",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            sensitive = true,
        )
        val dialog = AlertDialog.Builder(this)
            .setTitle("새 학생 등록")
            .setMessage("학생은 먼저 한 번만 등록한 뒤 여러 반에 소속시킬 수 있습니다.")
            .setView(dialogForm(nameInput, usernameInput, passwordInput))
            .setNegativeButton("취소", null)
            .setPositiveButton("등록 및 QR 발급", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val exactName = nameInput.text.toString().trim()
                val username = usernameInput.text.toSensitiveCharArray()
                val password = passwordInput.text.toSensitiveCharArray()
                usernameInput.text.clear()
                passwordInput.text.clear()
                if (exactName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    username.fill('\u0000')
                    password.fill('\u0000')
                    nameInput.error = "이름, 아이디와 비밀번호를 모두 입력하세요."
                    return@setOnClickListener
                }
                dialog.dismiss()
                registerStudent(exactName, username, password)
            }
        }
        remoteSupportWindowController.setSensitiveScreen(true)
        dialog.setOnDismissListener { restoreRemoteSupportScreenPolicy() }
        dialog.show()
    }

    private fun registerStudent(
        exactName: String,
        username: CharArray,
        password: CharArray,
    ) {
        if (!beginAdminDataOperation("학생 정보 암호화 등록 중")) {
            username.fill('\u0000')
            password.fill('\u0000')
            return
        }
        executeSensitive(
            cleanup = {
                username.fill('\u0000')
                password.fill('\u0000')
            },
        ) {
            val result = runCatching {
                val registered = studentRepository.registerStudent(
                    exactName,
                    username,
                    password,
                )
                QrPreview(
                    studentId = registered.studentId,
                    exactName = exactName,
                    bitmap = QrImageRenderer.render(
                        registered.issuedQr.payload,
                        QR_SIZE_PIXELS,
                    ),
                )
            }
            runOnUiThread {
                if (destroyed) {
                    result.getOrNull()?.let(::wipeQrPreview)
                    return@runOnUiThread
                }
                result.fold(
                    onSuccess = { preview ->
                        showQrPreview(preview)
                        refreshAdminData(
                            message = "학생을 등록하고 QR을 발급했습니다. 반 학생 구성에서 소속 반을 선택하세요.",
                            preferredStudentId = preview.studentId,
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        username.fill('\u0000')
                        password.fill('\u0000')
                        adminMessage.text = it.message ?: "학생 등록 실패"
                    },
                )
            }
        }
    }

    private fun confirmReissueQr() {
        val selected = students.getOrNull(studentSpinner.selectedItemPosition)
        if (selected == null) {
            adminMessage.text = "학생을 선택하세요."
            return
        }
        if (selected.reusableCardLabel != null) {
            confirmMoveReusableCard(selected)
            return
        }
        AlertDialog.Builder(this)
            .setTitle("QR 폐기·재발급")
            .setMessage(
                "${selected.label} 학생의 기존 QR은 즉시 사용할 수 없게 됩니다.\n" +
                    "새 QR을 안전하게 저장하거나 인쇄하기 전에는 되돌릴 수 없습니다.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("기존 QR 폐기·재발급") { _, _ -> reissueQr(selected) }
            .show()
    }

    private fun reissueQr(selected: StudentChoice) {
        if (!beginAdminDataOperation("기존 QR 폐기 및 재발급 중")) return
        ioExecutor.execute {
            val result = runCatching {
                val issued = studentRepository.reissueQr(selected.id)
                QrPreview(
                    studentId = selected.id,
                    exactName = selected.label,
                    bitmap = QrImageRenderer.render(issued.payload, QR_SIZE_PIXELS),
                )
            }
            runOnUiThread {
                if (destroyed) {
                    result.getOrNull()?.let(::wipeQrPreview)
                    return@runOnUiThread
                }
                result.fold(
                    onSuccess = { preview ->
                        finishAdminDataOperation()
                        showQrPreview(preview)
                        adminMessage.text = "기존 QR을 폐기하고 새 QR을 발급했습니다."
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text = it.message ?: "QR 재발급 실패"
                    },
                )
            }
        }
    }

    private fun showUpdateStudentNameDialog() {
        val selected = students.getOrNull(studentSpinner.selectedItemPosition)
        if (selected == null) {
            adminMessage.text = "학생을 선택하세요."
            return
        }
        val nameInput = dialogTextInput(
            hint = "학생 전체 이름",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
        ).apply {
            setText(selected.label)
            selectAll()
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("학생 이름 변경")
            .setView(dialogForm(nameInput))
            .setNegativeButton("취소", null)
            .setPositiveButton("변경", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val exactName = nameInput.text.toString().trim()
                if (exactName.isEmpty()) {
                    nameInput.error = "학생 이름을 입력하세요."
                    return@setOnClickListener
                }
                dialog.dismiss()
                updateStudentName(selected, exactName)
            }
        }
        remoteSupportWindowController.setSensitiveScreen(true)
        dialog.setOnDismissListener { restoreRemoteSupportScreenPolicy() }
        dialog.show()
    }

    private fun updateStudentName(selected: StudentChoice, exactName: String) {
        if (!beginAdminDataOperation("학생 표시명 수정 중")) return
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.updateStudentProfile(
                    selected.id,
                    exactName,
                )
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        if (issuedQrPreview?.studentId == selected.id) clearQrPreview()
                        offerAdminUndo(
                            PendingAdminUndo.RestoreStudentName(
                                selected.id,
                                selected.label,
                            ),
                        )
                        refreshAdminData(
                            message = "학생 표시명을 수정했습니다. 이름이 적힌 카드는 QR을 재발급해 다시 인쇄하세요.",
                            preferredStudentId = selected.id,
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text = it.message ?: "학생 표시명 수정 실패"
                    },
                )
            }
        }
    }

    private fun offerAdminUndo(action: PendingAdminUndo) {
        pendingAdminUndo = action
        val generation = ++adminUndoGeneration
        undoAdminButton.visibility = View.VISIBLE
        undoAdminButton.isEnabled = !adminDataOperationGate.isActive
        mainHandler.postDelayed({
            if (generation == adminUndoGeneration) clearPendingAdminUndo()
        }, ADMIN_UNDO_WINDOW_MS)
    }

    private fun clearPendingAdminUndo() {
        adminUndoGeneration += 1
        pendingAdminUndo = null
        if (::undoAdminButton.isInitialized) {
            undoAdminButton.visibility = View.GONE
            undoAdminButton.isEnabled = false
        }
    }

    private fun performPendingAdminUndo() {
        val action = pendingAdminUndo ?: return
        if (!beginAdminDataOperation("방금 관리자 작업을 되돌리는 중")) return
        clearPendingAdminUndo()
        ioExecutor.execute {
            val result = runCatching {
                when (action) {
                    is PendingAdminUndo.RestoreMemberships ->
                        studentRepository.replaceClassMemberships(
                            action.classId,
                            action.studentIds,
                        )
                    is PendingAdminUndo.RestoreStudentName ->
                        studentRepository.updateStudentProfile(
                            action.studentId,
                            action.previousName,
                        )
                    is PendingAdminUndo.RestoreClass ->
                        studentRepository.restoreClass(
                            action.classId,
                            action.className,
                            action.studentIds,
                        )
                }
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        refreshAdminData(
                            message = "방금 관리자 작업을 되돌렸습니다.",
                            preferredClassId = when (action) {
                                is PendingAdminUndo.RestoreClass -> action.classId
                                is PendingAdminUndo.RestoreMemberships -> action.classId
                                is PendingAdminUndo.RestoreStudentName -> null
                            },
                            preferredStudentId = when (action) {
                                is PendingAdminUndo.RestoreStudentName -> action.studentId
                                else -> null
                            },
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text =
                            it.message ?: "관리자 작업 실행취소에 실패했습니다."
                    },
                )
            }
        }
    }

    private fun showUpdateCredentialsDialog() {
        val selected = students.getOrNull(studentSpinner.selectedItemPosition)
        if (selected == null) {
            adminMessage.text = "학생을 선택하세요."
            return
        }
        val usernameInput = dialogTextInput(
            hint = "새 학습 계정 아이디",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            sensitive = true,
        )
        val passwordInput = dialogTextInput(
            hint = "새 학습 계정 비밀번호",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            sensitive = true,
        )
        val passwordConfirmInput = dialogTextInput(
            hint = "새 비밀번호 확인",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            sensitive = true,
        )
        val dialog = AlertDialog.Builder(this)
            .setTitle("${selected.label} 로그인 정보 변경")
            .setMessage("아이디와 비밀번호를 모두 새 값으로 교체합니다. 기존 QR은 그대로 유지됩니다.")
            .setView(dialogForm(usernameInput, passwordInput, passwordConfirmInput))
            .setNegativeButton("취소", null)
            .setPositiveButton("변경", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val username = usernameInput.text.toSensitiveCharArray()
                val password = passwordInput.text.toSensitiveCharArray()
                val confirmation = passwordConfirmInput.text.toSensitiveCharArray()
                usernameInput.text.clear()
                passwordInput.text.clear()
                passwordConfirmInput.text.clear()
                if (
                    username.isEmpty() ||
                    password.isEmpty() ||
                    confirmation.isEmpty() ||
                    !password.contentEquals(confirmation)
                ) {
                    username.fill('\u0000')
                    password.fill('\u0000')
                    confirmation.fill('\u0000')
                    passwordConfirmInput.error = "아이디·비밀번호를 모두 입력하고 확인값을 맞추세요."
                    return@setOnClickListener
                }
                confirmation.fill('\u0000')
                dialog.dismiss()
                updateStudentCredentials(selected, username, password)
            }
        }
        remoteSupportWindowController.setSensitiveScreen(true)
        dialog.setOnDismissListener { restoreRemoteSupportScreenPolicy() }
        dialog.show()
    }

    private fun updateStudentCredentials(
        selected: StudentChoice,
        username: CharArray,
        password: CharArray,
    ) {
        if (!beginAdminDataOperation("학생 계정정보 재암호화 중")) {
            username.fill('\u0000')
            password.fill('\u0000')
            return
        }
        executeSensitive(
            cleanup = {
                username.fill('\u0000')
                password.fill('\u0000')
            },
        ) {
            val result = runCatching {
                studentRepository.updateStudentCredentials(
                    selected.id,
                    username,
                    password,
                )
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        refreshAdminData(
                            message = "학생 계정정보를 새 IV로 암호화해 갱신했습니다. 기존 QR은 그대로 유효합니다.",
                            preferredStudentId = selected.id,
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        username.fill('\u0000')
                        password.fill('\u0000')
                        adminMessage.text = it.message ?: "학생 계정정보 갱신 실패"
                    },
                )
            }
        }
    }

    private fun showReusableCardMenu() {
        val slots = reusableCardSlots
        val available = slots.filterNot(ReusableCardSlotSummary::isAssigned)
        val assigned = slots.filter(ReusableCardSlotSummary::isAssigned)
        val pendingPrint = available.count(ReusableCardSlotSummary::needsCardPdf)
        var actionIndex = 0
        val assignIndex = actionIndex++
        val replacePendingIndex = if (pendingPrint > 0) actionIndex++ else -1
        val moveIndex = if (assigned.isNotEmpty()) actionIndex++ else -1
        val releaseIndex = if (assigned.isNotEmpty()) actionIndex++ else -1
        val actions = buildList {
            add("무료 카드 학생에게 배정")
            if (pendingPrint > 0) add("저장 미확인 카드 QR 폐기·새 QR 전송")
            if (assigned.isNotEmpty()) add("실제 QR 카드로 전환·더미 초기화")
            if (assigned.isNotEmpty()) add("사용 중 카드 회수·초기화")
        }
        AlertDialog.Builder(this)
            .setTitle(
                "신규용 QR 카드 · 전체 ${slots.size}장 · " +
                    "무료 ${available.size}장 · 사용 중 ${assigned.size}장",
            )
            .setItems(actions.toTypedArray()) { _, which ->
                when {
                    which == assignIndex -> showAssignReusableCardDialog()
                    which == replacePendingIndex -> confirmReplacePendingReusableCards(pendingPrint)
                    which == moveIndex -> showMoveReusableCardDialog()
                    which == releaseIndex -> showReleaseReusableCardDialog()
                }
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun confirmReplacePendingReusableCards(pendingCount: Int) {
        if (currentSession?.sessionId != null) {
            adminMessage.text = "수업을 종료한 뒤 더미 QR을 다시 준비하세요."
            return
        }
        if (pairedPcDisplayName == null) {
            adminMessage.text = "새 더미 QR PDF를 전송하려면 먼저 지정 PC를 페어링하세요."
            return
        }
        AlertDialog.Builder(this)
            .setTitle("저장 미확인 QR ${pendingCount}장 폐기·교체")
            .setMessage(
                "앱은 QR 원문을 보관하지 않아 같은 QR을 다시 전송할 수 없습니다.\n\n" +
                    "계속하면 저장 확인이 안 된 무료 카드의 기존 QR은 즉시 사용할 수 없게 되고 " +
                    "새 QR PDF를 지정 PC로 전송합니다. PC에 이전 PDF가 있거나 이미 출력했다면 " +
                    "이전 파일과 인쇄물을 폐기하고 새 QR만 사용하세요.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("기존 QR 폐기·새 QR 전송") { _, _ ->
                prepareReusableCards()
            }
            .show()
    }

    private fun prepareReusableCards() {
        if (currentSession?.sessionId != null) {
            adminMessage.text = "수업을 종료한 뒤 더미 QR을 다시 준비하세요."
            return
        }
        if (pairedPcDisplayName == null) {
            adminMessage.text = "더미 QR PDF를 전송하려면 먼저 지정 PC를 페어링하세요."
            return
        }
        if (!beginAdminDataOperation("저장 미확인 QR을 폐기하고 새 QR을 전송하는 중")) return
        ioExecutor.execute {
            val result = runCatching {
                val issued = studentRepository.prepareReusableCardSlots()
                deliverReusableQrCards(issued).getOrThrow()
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { sentCount ->
                        refreshAdminData(
                            message = if (sentCount > 0) {
                                "신규카드 더미 QR ${sentCount}장을 지정 PC에 저장했습니다."
                            } else {
                                "다시 전송할 미출력 더미 QR이 없습니다."
                            },
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text =
                            (it.message ?: "신규용 QR 카드 준비 실패") +
                                " 더미 QR은 미출력 상태로 유지됩니다."
                    },
                )
            }
        }
    }

    private fun deliverReusableQrCards(
        issued: List<BatchIssuedQr>,
    ): Result<Int> {
        if (issued.isEmpty()) return Result.success(0)
        var output: File? = null
        val cards = mutableListOf<BatchQrCard>()
        val deliveryRequestId = ByteArray(PcTransferProtocol.REQUEST_ID_BYTES)
            .also(SecureRandom()::nextBytes)
        return try {
            runCatching {
                issued.forEach { item ->
                    cards += BatchQrCard(
                        displayName = item.displayNameExact,
                        qrBitmap = QrImageRenderer.render(
                            payload = item.issuedQr.payload,
                            sizePixels = QR_SIZE_PIXELS,
                        ),
                    )
                }
                output = BatchQrPdfExporter.export(this, cards)
                val cardLabels = issued.map(BatchIssuedQr::displayNameExact).sorted()
                val filename = if (
                    cardLabels == (1..4).map { index -> "신규카드$index" }
                ) {
                    "신규카드1-4 더미 QR.pdf"
                } else {
                    "${cardLabels.joinToString("-")} 더미 QR.pdf"
                }
                withReachablePairedPc { pairing ->
                    pcPdfSender.send(
                        pairing = pairing,
                        pdfFile = requireNotNull(output),
                        filename = filename,
                        requestId = deliveryRequestId,
                    )
                }
                studentRepository.markCardPdfsSavedToPc(
                    issued.mapTo(mutableSetOf(), BatchIssuedQr::studentId),
                )
                issued.size
            }
        } finally {
            deliveryRequestId.fill(0)
            output?.delete()
            cards.forEach { card ->
                if (!card.qrBitmap.isRecycled) {
                    QrPdfExporter.releaseSensitiveBitmap(card.qrBitmap)
                }
            }
        }
    }

    private fun showAssignReusableCardDialog() {
        val candidates = reusableCardSlots.filter {
            !it.isAssigned && !it.needsCardPdf
        }
        if (candidates.isEmpty()) {
            adminMessage.text = if (
                reusableCardSlots.any { !it.isAssigned && it.needsCardPdf }
            ) {
                "먼저 신규용 카드를 지정 PC에 저장하고 출력하세요."
            } else {
                "현재 무료 신규용 카드가 없습니다. 사용 중 카드를 실제 QR 카드로 전환하거나 회수·초기화하세요."
            }
            return
        }
        val slotSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                candidates.map(ReusableCardSlotSummary::slotLabel),
            )
            filterTouchesWhenObscured = true
        }
        val nameInput = dialogTextInput(
            hint = "학생 전체 이름",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
        )
        val usernameInput = dialogTextInput(
            hint = "학습 계정 아이디",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            sensitive = true,
        )
        val passwordInput = dialogTextInput(
            hint = "학습 계정 비밀번호",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            sensitive = true,
        )
        val passwordConfirmInput = dialogTextInput(
            hint = "비밀번호 확인",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            sensitive = true,
        )
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(8), dp(24), 0)
            addView(
                TextView(this@MainActivity).apply {
                    text = "사용할 카드"
                    setTextColor(android.graphics.Color.rgb(72, 101, 129))
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ),
            )
            addView(
                slotSpinner,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = dp(4) },
            )
            listOf(nameInput, usernameInput, passwordInput, passwordConfirmInput)
                .forEach { input ->
                    addView(
                        input,
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                        ).apply { topMargin = dp(8) },
                    )
                }
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("신규 학생에게 카드 배정")
            .setMessage(
                "선택한 카드의 QR은 변경하지 않습니다. " +
                    "학생 이름·ID·PW만 저장하며 현재 수업의 보충 학생으로 자동 추가하지 않습니다.",
            )
            .setView(form)
            .setNegativeButton("취소", null)
            .setPositiveButton("QR 유지·배정", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val exactName = nameInput.text.toString().trim()
                val username = usernameInput.text.toSensitiveCharArray()
                val password = passwordInput.text.toSensitiveCharArray()
                val confirmation = passwordConfirmInput.text.toSensitiveCharArray()
                usernameInput.text.clear()
                passwordInput.text.clear()
                passwordConfirmInput.text.clear()
                if (
                    exactName.isEmpty() ||
                    username.isEmpty() ||
                    password.isEmpty() ||
                    confirmation.isEmpty() ||
                    !password.contentEquals(confirmation)
                ) {
                    username.fill('\u0000')
                    password.fill('\u0000')
                    confirmation.fill('\u0000')
                    passwordConfirmInput.error =
                        "이름·아이디·비밀번호를 입력하고 확인값을 맞추세요."
                    return@setOnClickListener
                }
                confirmation.fill('\u0000')
                dialog.dismiss()
                assignReusableCard(
                    slot = candidates[slotSpinner.selectedItemPosition],
                    exactName = exactName,
                    username = username,
                    password = password,
                )
            }
        }
        remoteSupportWindowController.setSensitiveScreen(true)
        dialog.setOnDismissListener { restoreRemoteSupportScreenPolicy() }
        dialog.show()
    }

    private fun assignReusableCard(
        slot: ReusableCardSlotSummary,
        exactName: String,
        username: CharArray,
        password: CharArray,
    ) {
        if (!beginAdminDataOperation("${slot.slotLabel} 학생 계정 배정 중")) {
            username.fill('\u0000')
            password.fill('\u0000')
            return
        }
        executeSensitive(
            cleanup = {
                username.fill('\u0000')
                password.fill('\u0000')
            },
        ) {
            val result = runCatching {
                val assigned = studentRepository.assignReusableCardSlot(
                    slotStudentId = slot.studentId,
                    displayNameExact = exactName,
                    username = username,
                    password = password,
                )
                assigned
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { assigned ->
                        refreshAdminData(
                            message =
                                "${assigned.slotLabel}를 $exactName 학생에게 배정했습니다. " +
                                    "QR은 그대로 유지하며 현재 수업 보강에는 자동 추가하지 않습니다.",
                            preferredStudentId = assigned.studentId,
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text = it.message ?: "신규용 카드 배정 실패"
                    },
                )
            }
        }
    }

    private fun confirmMoveReusableCard(selected: StudentChoice) {
        val slotLabel = selected.reusableCardLabel
        if (slotLabel == null) {
            adminMessage.text = "신규용 카드 슬롯을 선택하세요."
            return
        }
        if (currentSession?.sessionId != null) {
            adminMessage.text = "수업을 종료한 뒤 실제 QR 카드로 전환하세요."
            return
        }
        if (pairedPcDisplayName == null) {
            adminMessage.text = "새 실제 QR 카드를 저장하려면 먼저 지정 PC를 페어링하세요."
            return
        }
        AlertDialog.Builder(this)
            .setTitle("$slotLabel 실제 QR 카드로 전환")
            .setMessage(
                "현재 더미 QR은 폐기하지 않고 $slotLabel 빈 더미로 초기화합니다.\n" +
                    "학생 정보와 반 소속은 새 QR의 일반 학생 카드로 옮기며, 새 QR PDF를 지정 PC에 저장합니다.\n" +
                    "전환 후에는 새 QR 카드를 사용하세요.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("새 QR 발급·전환") { _, _ -> moveReusableCard(selected) }
            .show()
    }

    private fun showMoveReusableCardDialog() {
        val assigned = students.filter { it.reusableCardLabel != null }
        if (assigned.isEmpty()) {
            adminMessage.text = "사용 중인 신규용 카드가 없습니다."
            return
        }
        AlertDialog.Builder(this)
            .setTitle("실제 QR 카드로 전환할 학생 선택")
            .setItems(
                assigned.map { selected ->
                    "${selected.reusableCardLabel} · ${selected.label}"
                }.toTypedArray(),
            ) { _, which -> confirmMoveReusableCard(assigned[which]) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun moveReusableCard(selected: StudentChoice) {
        val slotLabel = requireNotNull(selected.reusableCardLabel)
        if (!beginAdminDataOperation("$slotLabel 실제 QR 카드로 전환 중")) return
        ioExecutor.execute {
            var output: File? = null
            val cards = mutableListOf<BatchQrCard>()
            val requestId = ByteArray(PcTransferProtocol.REQUEST_ID_BYTES)
                .also(SecureRandom()::nextBytes)
            val result = runCatching {
                val moved = studentRepository.moveReusableCardToRegularStudent(selected.id)
                var deliveryFailure: Throwable? = null
                try {
                    cards += BatchQrCard(
                        displayName = moved.displayNameExact,
                        qrBitmap = QrImageRenderer.render(
                            payload = moved.issuedQr.payload,
                            sizePixels = QR_SIZE_PIXELS,
                        ),
                    )
                    output = BatchQrPdfExporter.export(this, cards)
                    withReachablePairedPc { pairing ->
                        pcPdfSender.send(
                            pairing = pairing,
                            pdfFile = requireNotNull(output),
                            filename = "실제 학생 QR 카드.pdf",
                            requestId = requestId,
                        )
                    }
                    studentRepository.markCardPdfsSavedToPc(setOf(moved.studentId))
                } catch (failure: Throwable) {
                    deliveryFailure = failure
                }
                moved to deliveryFailure
            }
            requestId.fill(0)
            output?.delete()
            cards.forEach { card ->
                if (!card.qrBitmap.isRecycled) {
                    QrPdfExporter.releaseSensitiveBitmap(card.qrBitmap)
                }
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { (moved, deliveryFailure) ->
                        showQrPreview(
                            QrPreview(
                                studentId = moved.studentId,
                                exactName = moved.displayNameExact,
                                bitmap = QrImageRenderer.render(
                                    payload = moved.issuedQr.payload,
                                    sizePixels = QR_SIZE_PIXELS,
                                ),
                            ),
                        )
                        refreshAdminData(
                            message = if (deliveryFailure == null) {
                                "실제 학생 QR 카드로 전환했고 기존 ${moved.slotLabel}은 빈 더미로 초기화했습니다. " +
                                    "새 QR 카드를 사용하세요."
                            } else {
                                "실제 학생 데이터와 새 QR을 만들고 기존 ${moved.slotLabel}을 빈 더미로 초기화했습니다. " +
                                    "새 QR PDF 전송 실패이므로 카드 상태에서 다시 전송하세요."
                            },
                            preferredStudentId = moved.studentId,
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text = it.message ?: "실제 QR 카드 전환 실패"
                    },
                )
            }
        }
    }

    private fun showReleaseReusableCardDialog() {
        val assigned = reusableCardSlots.filter(ReusableCardSlotSummary::isAssigned)
        if (assigned.isEmpty()) {
            adminMessage.text = "사용 중인 신규용 카드가 없습니다."
            return
        }
        AlertDialog.Builder(this)
            .setTitle("사용 중 카드 선택")
            .setItems(
                assigned.map {
                    "${it.slotLabel} · ${it.displayNameExact}"
                }.toTypedArray(),
            ) { _, which ->
                val selected = assigned[which]
                AlertDialog.Builder(this)
                    .setTitle("${selected.slotLabel} 회수·초기화")
                    .setMessage(
                        "${selected.displayNameExact} 학생의 계정정보와 반 소속을 이 카드 슬롯에서 제거하고 " +
                            "같은 QR을 다시 무료 카드로 돌립니다. 수업이 끝난 뒤에만 실행할 수 있습니다.",
                    )
                    .setNegativeButton("취소", null)
                    .setPositiveButton("회수·초기화") { _, _ ->
                        releaseReusableCard(selected)
                    }
                    .show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun releaseReusableCard(slot: ReusableCardSlotSummary) {
        if (!beginAdminDataOperation("${slot.slotLabel} 카드 회수·초기화 중")) return
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.releaseReusableCardSlot(slot.studentId)
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        refreshAdminData(
                            message = "${slot.slotLabel}를 회수·초기화했습니다. 같은 QR이 다시 무료 슬롯이 되었습니다.",
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text = it.message ?: "신규용 카드 회수·초기화 실패"
                    },
                )
            }
        }
    }

    private fun confirmDeactivateStudent() {
        val selected = students.getOrNull(studentSpinner.selectedItemPosition)
        if (selected == null) {
            adminMessage.text = "학생을 선택하세요."
            return
        }
        AlertDialog.Builder(this)
            .setTitle("학생 비활성화")
            .setMessage(
                "${selected.label} 학생을 비활성화하고 현재 QR을 폐기합니다.\n" +
                    "관리 화면에서 사라지며 다시 사용하려면 새로 등록해야 합니다.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("비활성화") { _, _ -> deactivateStudent(selected) }
            .show()
    }

    private fun deactivateStudent(selected: StudentChoice) {
        if (!beginAdminDataOperation("학생 비활성화 및 QR 폐기 중")) return
        ioExecutor.execute {
            val result = runCatching { studentRepository.deactivateStudent(selected.id) }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        if (issuedQrPreview?.studentId == selected.id) clearQrPreview()
                        refreshAdminData(
                            message = "${selected.label} 학생을 비활성화하고 QR을 폐기했습니다.",
                            preferredStudentId = null,
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text = it.message ?: "학생 비활성화 실패"
                    },
                )
            }
        }
    }

    private fun beginAdminDataOperation(message: String): Boolean {
        if (webRecoveryGate.isActive) {
            adminMessage.text = "Web 로그인 상태 정리가 끝날 때까지 기다리세요."
            return false
        }
        if (!adminDataOperationGate.tryStart()) {
            adminMessage.text = "다른 학생·반 작업이 끝날 때까지 기다리세요."
            return false
        }
        clearPendingAdminUndo()
        adminMessage.text = message
        updateStudentManagementControls()
        updateClassRosterUi()
        updateSessionAdminControls(currentSession)
        return true
    }

    private fun finishAdminDataOperation() {
        adminDataOperationGate.finish()
        updateStudentManagementControls()
        updateClassRosterUi()
        updateSessionAdminControls(currentSession)
    }

    private fun updateStudentManagementControls() {
        val available = !adminDataOperationGate.isActive && !webRecoveryGate.isActive
        val hasStudents = students.isNotEmpty()
        studentSpinner.isEnabled = available && hasStudents
        registerStudentButton.isEnabled = available
        importStudentCsvButton.isEnabled =
            available && currentSession?.sessionId == null && pairedPcDisplayName != null
        reissueQrButton.isEnabled = available && hasStudents
        updateProfileButton.isEnabled = available && hasStudents
        updateCredentialsButton.isEnabled = available && hasStudents
        deactivateStudentButton.isEnabled = available && hasStudents
        val reusableAvailableCount = reusableCardSlots.count { !it.isAssigned }
        reusableCardsButton.text = "신규용 카드 관리 · 무료 ${reusableAvailableCount}장"
        reusableCardsButton.isEnabled = available
        pendingCardsPdfButton.isEnabled =
            available && hasStudents && currentSession?.sessionId == null &&
            pairedPcDisplayName != null
        cardStatusButton.isEnabled = available && hasStudents
        if (pendingAdminUndo != null) undoAdminButton.isEnabled = available
        val previewAvailable = issuedQrPreview?.bitmap?.isRecycled == false
        exportQrPdfButton.isEnabled = available && previewAvailable
        sendPcPdfButton.isEnabled =
            available && previewAvailable && pairedPcDisplayName != null
    }

    private fun fetchStudentCsvFromPc() {
        if (adminDataOperationGate.isActive) {
            adminMessage.text = "다른 학생·반 작업이 끝날 때까지 기다리세요."
            return
        }
        if (currentSession?.sessionId != null) {
            adminMessage.text = "수업 중에는 학생 CSV를 가져올 수 없습니다."
            return
        }
        if (pairedPcDisplayName == null) {
            adminMessage.text = "먼저 지정 PC를 페어링하세요."
            return
        }
        importStudentCsvButton.isEnabled = false
        adminMessage.text = "지정 PC에서 암호화된 학생 CSV를 가져오는 중"
        runCatching {
            pcControlExecutor.execute {
                val download = try {
                    withReachablePairedPc(pcControlClient::fetchStudentCsv)
                } catch (failure: Throwable) {
                    runOnUiThread {
                        importStudentCsvButton.isEnabled = true
                        adminMessage.text = failure.message ?: "PC에서 CSV를 가져오지 못했습니다."
                    }
                    return@execute
                }
                if (download == null) {
                    runOnUiThread {
                        importStudentCsvButton.isEnabled = true
                        adminMessage.text = "PC 도우미에서 먼저 학생 CSV를 선택하세요."
                    }
                    return@execute
                }
                val parsed = try {
                    StudentCsvParser.parse(download.payload)
                } catch (failure: Throwable) {
                    download.payload.fill(0)
                    runOnUiThread {
                        importStudentCsvButton.isEnabled = true
                        adminMessage.text = failure.message ?: "학생 CSV 형식을 확인하지 못했습니다."
                    }
                    return@execute
                } finally {
                    download.payload.fill(0)
                }
                val previewTask = SensitiveHandoffTask(parsed::clearSensitiveData) {
                    val preview = runCatching {
                        studentRepository.previewStudentImport(parsed.rows)
                    }
                    mainHandler.post {
                        importStudentCsvButton.isEnabled = true
                        if (destroyed) {
                            parsed.clearSensitiveData()
                            return@post
                        }
                        preview.fold(
                            onSuccess = {
                                showStudentCsvPreview(download, parsed, it)
                            },
                            onFailure = {
                                parsed.clearSensitiveData()
                                adminMessage.text =
                                    it.message ?: "학생 CSV 변경 내용을 확인하지 못했습니다."
                            },
                        )
                    }
                }
                try {
                    ioExecutor.execute(previewTask)
                } catch (_: RuntimeException) {
                    previewTask.discard()
                    runOnUiThread {
                        if (!destroyed) {
                            importStudentCsvButton.isEnabled = true
                            adminMessage.text = "학생 CSV 미리보기를 시작하지 못했습니다."
                        }
                    }
                }
            }
        }.onFailure {
            importStudentCsvButton.isEnabled = true
            adminMessage.text = "PC CSV 가져오기를 시작하지 못했습니다."
        }
    }

    private fun showStudentCsvPreview(
        download: PcCsvDownload,
        parsed: ParsedStudentCsv,
        preview: StudentCsvImportPreview,
    ) {
        var applying = false
        val dialog = AlertDialog.Builder(this)
            .setTitle("학생 CSV 변경 미리보기")
            .setMessage(
                "${download.filename}\n\n" +
                    "신규 등록: ${preview.created}명\n" +
                    "기존 계정 갱신: ${preview.updated}명\n" +
                    "이름 변경: ${preview.renamed}명\n" +
                    "반 소속: CSV 내용으로 교체\n" +
                    "적용 후 카드 PDF 생성 필요 예상: ${preview.cardsNeedingPdfAfterImport}명\n\n" +
                    "아이디가 같은 기존 학생은 이름·비밀번호·반 소속을 갱신합니다.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("변경 적용") { _, _ ->
                applying = true
                applyStudentCsv(download.deliveryId, parsed)
            }
            .create()
        dialog.setOnDismissListener {
            if (!applying) parsed.clearSensitiveData()
        }
        dialog.show()
    }

    private fun applyStudentCsv(deliveryId: String, parsed: ParsedStudentCsv) {
        if (!beginAdminDataOperation("학생 CSV를 암호화해 적용하는 중")) {
            parsed.clearSensitiveData()
            return
        }
        runCatching {
            executeSensitive(cleanup = parsed::clearSensitiveData) {
                val result = runCatching {
                    val imported = studentRepository.importStudents(parsed.rows)
                    val confirmationFailure = runCatching {
                        withReachablePairedPc { pairing ->
                            pcControlClient.confirmStudentCsv(pairing, deliveryId)
                        }
                    }.exceptionOrNull()
                    imported to confirmationFailure
                }
                runOnUiThread {
                    if (destroyed) return@runOnUiThread
                    result.fold(
                        onSuccess = { (imported, confirmationFailure) ->
                            refreshAdminData(
                                "학생 CSV 적용 완료 · 신규 ${imported.created}명, " +
                                    "갱신 ${imported.updated}명, " +
                                    "카드 PDF 생성 필요 ${imported.cardsNeedingPdf}명" +
                                    if (confirmationFailure == null) {
                                        ""
                                    } else {
                                        " · PC 적용 확인이 남아 있습니다. " +
                                            "연결 복구 후 같은 CSV를 다시 가져오면 안전하게 재적용됩니다."
                                    },
                                completeAdminDataOperationAfterLoad = true,
                            )
                        },
                        onFailure = {
                            finishAdminDataOperation()
                            adminMessage.text = it.message ?: "학생 CSV 적용 실패"
                        },
                    )
                }
            }
        }.onFailure {
            finishAdminDataOperation()
            if (!destroyed) {
                adminMessage.text = "학생 CSV 적용 작업을 시작하지 못했습니다."
            }
        }
    }

    private fun showPendingCardsDialog() {
        adminMessage.text = "카드 PDF 생성 필요 학생 확인 중"
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.listQrCardStatuses().filter { it.needsCardPdf }
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { pending ->
                        if (pending.isEmpty()) {
                            adminMessage.text = "신규·변경으로 카드 출력이 필요한 학생이 없습니다."
                            return@fold
                        }
                        val chosen = pending.mapTo(mutableSetOf()) { it.studentId }
                        val checked = BooleanArray(pending.size) { true }
                        AlertDialog.Builder(this)
                            .setTitle("신규·변경 카드 선택")
                            .setMultiChoiceItems(
                                pending.map { it.displayNameExact }.toTypedArray(),
                                checked,
                            ) { _, index, enabled ->
                                if (enabled) {
                                    chosen += pending[index].studentId
                                } else {
                                    chosen -= pending[index].studentId
                                }
                            }
                            .setNegativeButton("취소", null)
                            .setPositiveButton("영향 확인") { _, _ ->
                                confirmPendingCardsPdf(chosen)
                            }
                            .show()
                    },
                    onFailure = {
                        adminMessage.text = it.message ?: "QR 카드 상태를 확인하지 못했습니다."
                    },
                )
            }
        }
    }

    private fun confirmPendingCardsPdf(studentIds: Set<String>) {
        if (studentIds.isEmpty()) {
            adminMessage.text = "카드를 만들 학생을 한 명 이상 선택하세요."
            return
        }
        val pages = (studentIds.size + 8) / 9
        AlertDialog.Builder(this)
            .setTitle("QR 재발급 영향 확인")
            .setMessage(
                "선택 학생 ${studentIds.size}명의 기존 QR을 무효화합니다.\n" +
                    "새 카드 PDF: $pages 페이지 · 3×3 배치 · 카드 사이 절단 여백 5mm\n" +
                    "지정 PC의 저장 응답이 확인된 학생만 PDF 생성 완료로 기록합니다.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("재발급·PC 전송") { _, _ ->
                preparePendingCardsPdf(studentIds)
            }
            .show()
    }

    private fun preparePendingCardsPdf(studentIds: Set<String>) {
        if (!beginAdminDataOperation("선택 학생 QR 재발급·PDF 암호화 전송 중")) return
        ioExecutor.execute {
            var output: File? = null
            val cards = mutableListOf<BatchQrCard>()
            val deliveryRequestId = ByteArray(PcTransferProtocol.REQUEST_ID_BYTES)
                .also(SecureRandom()::nextBytes)
            val result = runCatching {
                val issued = studentRepository.reissueQrBatch(studentIds)
                issued.forEach { item ->
                    cards += BatchQrCard(
                        displayName = item.displayNameExact,
                        qrBitmap = QrImageRenderer.render(
                            payload = item.issuedQr.payload,
                            sizePixels = QR_SIZE_PIXELS,
                        ),
                    )
                }
                output = BatchQrPdfExporter.export(this, cards)
                withReachablePairedPc { pairing ->
                    pcPdfSender.send(
                        pairing = pairing,
                        pdfFile = requireNotNull(output),
                        filename = "신규 변경 학생 QR.pdf",
                        requestId = deliveryRequestId,
                    )
                }
                studentRepository.markCardPdfsSavedToPc(studentIds)
            }
            deliveryRequestId.fill(0)
            output?.delete()
            cards.forEach { card ->
                if (!card.qrBitmap.isRecycled) {
                    QrPdfExporter.releaseSensitiveBitmap(card.qrBitmap)
                }
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        refreshAdminData(
                            "새 QR ${studentIds.size}장을 지정 PC에 저장했습니다. 기존 QR은 무효화되었습니다.",
                            completeAdminDataOperationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text =
                            (it.message ?: "선택 카드 PDF 전송 실패") +
                                " 기존 QR이 이미 무효화되었을 수 있으므로 카드 상태를 확인하세요."
                    },
                )
            }
        }
    }

    private fun showCardStatusDialog() {
        adminMessage.text = "QR 카드 상태·이력 확인 중"
        ioExecutor.execute {
            val result = runCatching { studentRepository.listQrCardStatuses() }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { statuses ->
                        val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.KOREA)
                        val message = statuses.joinToString("\n\n") { status ->
                            val issued = formatter.format(Date(status.issuedAtEpochMs))
                            val used = status.lastUsedAtEpochMs
                                ?.let { formatter.format(Date(it)) }
                                ?: "사용 기록 없음"
                            val pdfSaved = status.lastPdfSavedAtEpochMs
                                ?.let { formatter.format(Date(it)) }
                                ?: "PC 저장 기록 없음"
                            "${status.displayNameExact} · " +
                                (if (status.needsCardPdf) {
                                    "카드 PDF 생성 필요"
                                } else {
                                    "카드 PDF 지정 PC 저장 완료"
                                }) +
                                "\n발급 $issued · 최근 사용 $used · 최근 PC 저장 $pdfSaved"
                        }
                        AlertDialog.Builder(this)
                            .setTitle("QR 카드 상태·이력")
                            .setMessage(message.ifEmpty { "등록 학생이 없습니다." })
                            .setPositiveButton("확인", null)
                            .show()
                        adminMessage.text = "QR 카드 상태를 확인했습니다."
                    },
                    onFailure = {
                        adminMessage.text = it.message ?: "QR 카드 상태를 불러오지 못했습니다."
                    },
                )
            }
        }
    }

    private fun confirmBatchQrPrint() {
        val selectedClass = classes.getOrNull(classSpinner.selectedItemPosition)
        val studentIds = classRosterState.membershipStudentIds.toSet()
        if (selectedClass == null || studentIds.isEmpty()) {
            adminMessage.text = "학생이 소속된 반을 선택하세요."
            return
        }
        if (pairedPcDisplayName == null) {
            adminMessage.text = "먼저 지정 PC를 페어링하세요."
            return
        }
        AlertDialog.Builder(this)
            .setTitle("${selectedClass.label} QR 전체 재발급")
            .setMessage(
                "선택 반 학생 전원의 기존 QR 카드가 즉시 무효화되고 새 카드가 발급됩니다.\n\n" +
                    "A4 한 장에 55×80mm 카드가 5mm 절단 여백을 두고 최대 9장씩 배치됩니다. " +
                    "새 PDF는 페어링된 지정 PC에 암호화해 저장됩니다.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("전체 재발급·PC 전송") { _, _ ->
                preparePendingCardsPdf(studentIds)
            }
            .show()
    }

    private fun confirmQrPdfExport() {
        val preview = issuedQrPreview
        if (preview == null || preview.bitmap.isRecycled) {
            adminMessage.text = "먼저 QR을 발급하거나 재발급하세요."
            exportQrPdfButton.isEnabled = false
            return
        }
        AlertDialog.Builder(this)
            .setTitle("QR 카드 PDF 전송·저장")
            .setMessage(
                "55×80mm 세로 카드에 40×40mm QR과 학생 전체 이름을 넣습니다.\n" +
                    "학생 이름은 QR 아래에 표시됩니다.\n" +
                    "PDF에는 로그인 가능한 QR이 포함되므로 신뢰하는 PC나 저장 위치만 선택하세요.",
            )
            .setNegativeButton("취소", null)
            .setNeutralButton("다른 앱·저장") { _, _ ->
                prepareQrPdfExport(preview, preferQuickShare = false)
            }
            .setPositiveButton("Quick Share") { _, _ ->
                prepareQrPdfExport(preview, preferQuickShare = true)
            }
            .show()
    }

    private fun prepareQrPdfExport(preview: QrPreview, preferQuickShare: Boolean) {
        if (issuedQrPreview !== preview || preview.bitmap.isRecycled) {
            adminMessage.text = "QR 미리보기가 만료되었습니다. 다시 발급하세요."
            return
        }
        val exportBitmap = runCatching {
            requireNotNull(preview.bitmap.copy(Bitmap.Config.ARGB_8888, true))
        }.getOrElse {
            adminMessage.text = "PDF용 QR 복사 실패"
            return
        }
        if (!beginAdminDataOperation("카드 크기 PDF 생성 중")) {
            QrPdfExporter.releaseSensitiveBitmap(exportBitmap)
            return
        }
        executeSensitive(
            cleanup = { QrPdfExporter.releaseSensitiveBitmap(exportBitmap) },
        ) {
            val result = runCatching {
                QrPdfExporter.consumeSensitiveBitmap(exportBitmap) { ownedBitmap ->
                    studentRepository.recordQrExportRequested(preview.studentId)
                    QrPdfExporter.export(
                        context = this,
                        displayName = preview.exactName,
                        qrBitmap = ownedBitmap,
                    )
                }
            }
            runOnUiThread {
                if (destroyed) {
                    result.getOrNull()?.delete()
                    return@runOnUiThread
                }
                result.fold(
                    onSuccess = { file ->
                        finishAdminDataOperation()
                        shareQrPdf(file, preview, preferQuickShare)
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text = it.message ?: "QR 카드 PDF 생성 실패"
                    },
                )
            }
        }
    }

    private fun shareQrPdf(
        file: File,
        preview: QrPreview,
        preferQuickShare: Boolean,
    ) {
        val uri = FileProvider.getUriForFile(this, "$packageName.files", file)
        val share = QrPdfShareIntentFactory.create(uri, preview.exactName)
        pendingSharedPdf = file
        suppressNextAdminStopRelock = true
        runCatching {
            QrPdfExporter.scheduleSharedFileExpiry(this, file)
            if (preferQuickShare) {
                runCatching {
                    startActivity(
                        QrPdfShareIntentFactory.createQuickShare(
                            uri,
                            preview.exactName,
                        ),
                    )
                }.getOrElse {
                    startActivity(
                        Intent.createChooser(share, "PDF를 PC로 보내거나 저장"),
                    )
                }
            } else {
                startActivity(Intent.createChooser(share, "PDF를 PC로 보내거나 저장"))
            }
        }.onSuccess {
            clearQrPreview("QR 카드 PDF를 전달해 화면 표시를 지웠습니다")
        }.onFailure {
            suppressNextAdminStopRelock = false
            pendingSharedPdf = null
            file.delete()
            adminMessage.text = "PDF 공유 화면을 열지 못했습니다."
        }
    }

    private fun confirmPcPdfTransfer() {
        val preview = issuedQrPreview
        val pcName = pairedPcDisplayName
        if (preview == null || preview.bitmap.isRecycled) {
            adminMessage.text = "먼저 QR을 발급하거나 재발급하세요."
            sendPcPdfButton.isEnabled = false
            return
        }
        if (pcName == null) {
            adminMessage.text = "먼저 PC 수신기의 페어링 QR을 촬영하세요."
            sendPcPdfButton.isEnabled = false
            return
        }
        AlertDialog.Builder(this)
            .setTitle("지정 PC로 카드 PDF 보내기")
            .setMessage(
                "$pcName PC의 전용 수신 폴더로 암호화해 전송합니다.\n" +
                    "PDF에는 로그인 가능한 QR과 학생 전체 이름이 포함됩니다.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("PC로 보내기") { _, _ ->
                preparePcPdfTransfer(preview)
            }
            .show()
    }

    private fun preparePcPdfTransfer(preview: QrPreview) {
        if (issuedQrPreview !== preview || preview.bitmap.isRecycled) {
            adminMessage.text = "QR 미리보기가 만료되었습니다. 다시 발급하세요."
            return
        }
        val exportBitmap = runCatching {
            requireNotNull(preview.bitmap.copy(Bitmap.Config.ARGB_8888, true))
        }.getOrElse {
            adminMessage.text = "PC 전송용 QR 복사 실패"
            return
        }
        if (!beginAdminDataOperation("지정 PC로 카드 PDF를 암호화해 보내는 중")) {
            QrPdfExporter.releaseSensitiveBitmap(exportBitmap)
            return
        }
        executeSensitive(
            cleanup = { QrPdfExporter.releaseSensitiveBitmap(exportBitmap) },
        ) {
            var exportFile: File? = null
            val result = runCatching {
                studentRepository.recordQrExportRequested(preview.studentId)
                exportFile = QrPdfExporter.consumeSensitiveBitmap(exportBitmap) { ownedBitmap ->
                    QrPdfExporter.export(
                        context = this,
                        displayName = preview.exactName,
                        qrBitmap = ownedBitmap,
                    )
                }
                val deliveryRequestId = ByteArray(PcTransferProtocol.REQUEST_ID_BYTES)
                    .also(SecureRandom()::nextBytes)
                val pcName = try {
                    withReachablePairedPc { pairing ->
                        pcPdfSender.send(
                            pairing = pairing,
                            pdfFile = requireNotNull(exportFile),
                            filename = "${preview.exactName} QR.pdf",
                            requestId = deliveryRequestId,
                        )
                        pairing.displayName
                    }
                } finally {
                    deliveryRequestId.fill(0)
                }
                studentRepository.markCardPdfsSavedToPc(setOf(preview.studentId))
                pcName
            }
            exportFile?.delete()
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { pcName ->
                        finishAdminDataOperation()
                        clearQrPreview("$pcName PC에 카드 PDF를 안전하게 저장했습니다")
                    },
                    onFailure = {
                        finishAdminDataOperation()
                        adminMessage.text =
                            it.message ?: "지정 PC로 카드 PDF를 보내지 못했습니다."
                    },
                )
            }
        }
    }

    private fun showTemporaryStudentDialog() {
        val session = currentSession
        val selectedClass = classes.getOrNull(classSpinner.selectedItemPosition)
        if (selectedClass == null) {
            adminMessage.text = "수업 반을 먼저 선택하세요."
            return
        }
        val candidates = students.filter { it.id !in classRosterState.membershipStudentIds }
        if (candidates.isEmpty()) {
            adminMessage.text = "이 반 밖에서 추가할 활성 학생이 없습니다."
            return
        }
        val chosen = if (session?.sessionId == null) {
            pendingTemporaryStudentIds.toMutableSet()
        } else {
            mutableSetOf()
        }
        val checked = BooleanArray(candidates.size) { index -> candidates[index].id in chosen }
        AlertDialog.Builder(this)
            .setTitle(
                if (session?.sessionId == null) {
                    "${selectedClass.label} 이번 수업 보강 학생"
                } else {
                    "${selectedClass.label} 현재 수업 보강 학생 추가"
                },
            )
            .setMultiChoiceItems(
                candidates.map(StudentChoice::label).toTypedArray(),
                checked,
            ) { _, which, isChecked ->
                val studentId = candidates[which].id
                if (isChecked) chosen += studentId else chosen -= studentId
            }
            .setNegativeButton("취소", null)
            .setPositiveButton(
                if (session?.sessionId == null) "선택 저장" else "현재 수업에 추가",
            ) { _, _ ->
                if (session?.sessionId == null) {
                    pendingTemporaryStudentIds = chosen
                    updateClassRosterUi()
                    adminMessage.text = "이번 수업 보강 학생 ${chosen.size}명을 선택했습니다."
                } else {
                    addTemporaryStudents(session, chosen)
                }
            }
            .show()
    }

    private fun addTemporaryStudents(session: ActiveSessionEntity, studentIds: Set<String>) {
        if (studentIds.isEmpty()) {
            adminMessage.text = "추가할 학생을 선택하세요."
            return
        }
        if (!beginAdminDataOperation("현재 수업 보강 학생 추가 중")) return
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.addTemporaryStudents(
                    requireNotNull(session.sessionId),
                    studentIds,
                )
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                finishAdminDataOperation()
                adminMessage.text = result.fold(
                    onSuccess = { "현재 수업에 보강 학생 ${studentIds.size}명을 추가했습니다." },
                    onFailure = { it.message ?: "보강 학생 추가 실패" },
                )
            }
        }
    }

    private fun launchWebSessionRecovery(action: PendingRecoveryAction) {
        if (adminDataOperationGate.isActive) {
            adminMessage.text = "다른 학생·반 작업이 끝날 때까지 기다리세요."
            return
        }
        if (!webRecoveryGate.tryStart()) {
            adminMessage.text = "Web 로그인 상태를 이미 안전하게 정리하고 있습니다."
            return
        }
        clearPendingAdminUndo()
        updateStudentManagementControls()
        pendingRecoveryAction = action
        adminMessage.text = when (action) {
            PendingRecoveryAction.None -> "Web 로그인 상태 안전 정리 중"
            is PendingRecoveryAction.StartSession ->
                "공식 웹 접속·화면 구조·로그인 상태 사전점검 중"
            PendingRecoveryAction.EndSession -> "Web 상태 정리 후 수업 안전 종료 중"
        }
        updateSessionAdminControls(currentSession)
        suppressNextAdminStopRelock = true
        val intent = Intent(CredentialBridgeContract.ACTION_RECOVER_WEB_SESSION)
            .setComponent(
                ComponentName(
                    CredentialBridgeContract.TRUSTED_CONSUMER_PACKAGE,
                    "com.local.matholickiosk.webpoc.MainActivity",
                ),
            )
        runCatching { webRecoveryLauncher.launch(intent) }
            .onFailure {
                suppressNextAdminStopRelock = false
                pendingRecoveryAction = PendingRecoveryAction.None
                finishWebRecoveryOperation()
                updateSessionAdminControls(currentSession)
                adminMessage.text = "Web 세션 정리 화면을 열지 못했습니다."
            }
    }

    private fun startOrEndSession() {
        if (adminDataOperationGate.isActive) {
            adminMessage.text = "다른 학생·반 작업이 끝날 때까지 기다리세요."
            return
        }
        val active = currentSession?.sessionId != null
        if (active) {
            AlertDialog.Builder(this)
                .setTitle("현재 수업 안전 종료")
                .setMessage(
                    "남은 Web 로그인을 먼저 안전하게 정리한 뒤 현재 수업과 일회성 보강 명단을 종료합니다.",
                )
                .setNegativeButton("취소", null)
                .setPositiveButton("안전 종료") { _, _ ->
                    launchWebSessionRecovery(PendingRecoveryAction.EndSession)
                }
                .show()
            return
        }
        val selectedClass = classes.getOrNull(classSpinner.selectedItemPosition)
        if (selectedClass == null) {
            adminMessage.text = "수업 반을 선택하세요."
            return
        }
        if (
            classRosterState.membershipStudentIds.isEmpty() &&
            pendingTemporaryStudentIds.isEmpty()
        ) {
            adminMessage.text = "반 학생 또는 이번 수업 보강 학생을 한 명 이상 선택하세요."
            return
        }
        runSessionPreflight(
            PendingRecoveryAction.StartSession(
                classId = selectedClass.id,
                temporaryStudentIds = pendingTemporaryStudentIds,
            ),
        )
    }

    private fun runSessionPreflight(action: PendingRecoveryAction.StartSession) {
        val entered = lockTaskController.enterRestrictedMode()
        dedicatedDevicePolicyFailed =
            entered.isFailure || !entered.getOrDefault(false)
        updateDedicatedDeviceStatus(administratorUnlocked = false)
        val status = lockTaskController.status()
        val batteryIntent = registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        )
        val batteryLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val batteryScale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPercent = if (batteryLevel >= 0 && batteryScale > 0) {
            (batteryLevel * 100 / batteryScale).coerceIn(0, 100)
        } else {
            null
        }
        val cameraHardware = packageManager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_ANY,
        )
        val result = SessionPreflightPolicy.evaluate(
            SessionPreflightInput(
                deviceOwner = status.isDeviceOwner,
                kioskPackagePermitted = status.isKioskPackagePermitted,
                webAppProtected = status.isWebPocUninstallBlocked,
                lockTaskMode = status.mode,
                policyConfigurationFailed = dedicatedDevicePolicyFailed,
                cameraPermissionGranted =
                    checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED,
                cameraHardwareAvailable = cameraHardware,
                batteryPercent = batteryPercent,
                usableStorageBytes = filesDir.usableSpace,
            ),
        )
        if (!result.canStart) {
            exitDedicatedModeForAdministrator()
            adminMessage.text = "수업 시작 차단 · ${result.blockingReasons.joinToString(" ")}"
            AlertDialog.Builder(this)
                .setTitle("수업 사전점검 실패")
                .setMessage(result.blockingReasons.joinToString("\n"))
                .setPositiveButton("확인", null)
                .show()
            return
        }
        val checks = buildString {
            append("필수 보안 정책: 정상\n")
            append("공식 웹 접속·화면 구조: 시작 직전 안전검사\n")
            append("로그인 잔여 상태: 시작 직전 안전정리\n")
            if (result.warnings.isNotEmpty()) {
                append("\n주의\n")
                append(result.warnings.joinToString("\n") { "• $it" })
            }
        }
        AlertDialog.Builder(this)
            .setTitle("수업 시작 사전점검")
            .setMessage(checks)
            .setNegativeButton("취소") { _, _ ->
                exitDedicatedModeForAdministrator()
            }
            .setPositiveButton("웹 검사 후 시작") { _, _ ->
                manualStudentSelectionOnly = result.manualStudentSelectionRequired
                launchWebSessionRecovery(action)
            }
            .show()
    }

    private fun runOperationalSelfTest() {
        selfTestButton.isEnabled = false
        adminMessage.text = "운영 준비 상태를 점검하는 중"
        val deviceStatus = lockTaskController.status()
        val cameraPermission =
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val cameraHardware = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        val connected = getSystemService(ConnectivityManager::class.java)
            ?.activeNetwork
            ?.let { network ->
                getSystemService(ConnectivityManager::class.java)
                    ?.getNetworkCapabilities(network)
            }
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        val localChecks = listOf(
            "Device Owner" to deviceStatus.isDeviceOwner,
            "전용기기 잠금 허용" to deviceStatus.isKioskPackagePermitted,
            "학습 앱 보호" to deviceStatus.isWebPocUninstallBlocked,
            "카메라 권한" to cameraPermission,
            "카메라 장치" to cameraHardware,
            "인터넷 연결" to connected,
            "저장공간 100MB 이상" to (filesDir.usableSpace >= 100L * 1024L * 1024L),
        )
        runCatching {
            pcControlExecutor.execute {
                val pcReachable = runCatching {
                    withReachablePairedPc { pairing ->
                        pcControlClient.sendStatus(
                            pairing,
                            state = "자가진단 중",
                            studentName = null,
                            notify = false,
                        )
                    }
                }.isSuccess
                runOnUiThread {
                    if (destroyed) return@runOnUiThread
                    selfTestButton.isEnabled = true
                    val checks = localChecks + ("지정 PC 연결" to pcReachable)
                    val failed = checks.filterNot { it.second }
                    AlertDialog.Builder(this)
                        .setTitle(
                            if (failed.isEmpty()) "운영 준비 자가진단 정상"
                            else "운영 준비 자가진단 확인 필요",
                        )
                        .setMessage(
                            checks.joinToString("\n") { (label, passed) ->
                                "${if (passed) "✓" else "!"} $label"
                            } +
                                "\n\n공식 Web 화면 구조와 로그인 잔여 상태는 수업 시작 직전에 " +
                                "기존 안전검사로 확인합니다.",
                        )
                        .setPositiveButton("확인", null)
                        .show()
                    adminMessage.text = if (failed.isEmpty()) {
                        "자가진단 정상 · 수업 시작 직전 Web 안전검사를 계속 사용합니다."
                    } else {
                        "자가진단 확인 필요 · ${failed.joinToString { it.first }}"
                    }
                }
            }
        }.onFailure {
            selfTestButton.isEnabled = true
            adminMessage.text = "자가진단을 시작하지 못했습니다."
        }
    }

    private fun showFeedbackSettings() {
        val preferences = getSharedPreferences(FEEDBACK_PREFERENCES, Context.MODE_PRIVATE)
        val selected = booleanArrayOf(
            preferences.getBoolean(KEY_VIBRATION_ENABLED, true),
            preferences.getBoolean(KEY_SOUND_ENABLED, false),
        )
        AlertDialog.Builder(this)
            .setTitle("소리·진동 피드백")
            .setMultiChoiceItems(
                arrayOf("진동", "짧은 확인음"),
                selected,
            ) { _, index, enabled -> selected[index] = enabled }
            .setNegativeButton("취소", null)
            .setPositiveButton("저장") { _, _ ->
                preferences.edit()
                    .putBoolean(KEY_VIBRATION_ENABLED, selected[0])
                    .putBoolean(KEY_SOUND_ENABLED, selected[1])
                    .apply()
                adminMessage.text =
                    "피드백 설정 저장 · 진동 ${onOff(selected[0])}, 소리 ${onOff(selected[1])}"
                provideFeedback(success = true)
            }
            .show()
    }

    private fun showKeypadLayoutSettings() {
        val choices = arrayOf("오른손 배치 (기본)", "왼손 배치", "하단 중앙 배치")
        val values = arrayOf(KEYPAD_PRESET_RIGHT, KEYPAD_PRESET_LEFT, KEYPAD_PRESET_CENTER)
        val preferences = getSharedPreferences(STUDENT_UI_PREFERENCES, Context.MODE_PRIVATE)
        val current = preferences.getString(KEY_KEYPAD_PRESET, KEYPAD_PRESET_RIGHT)
        var selected = values.indexOf(current).takeIf { it >= 0 } ?: 0
        AlertDialog.Builder(this)
            .setTitle("수식 키패드 배치")
            .setSingleChoiceItems(choices, selected) { _, index -> selected = index }
            .setNegativeButton("취소", null)
            .setPositiveButton("저장") { _, _ ->
                preferences.edit().putString(KEY_KEYPAD_PRESET, values[selected]).apply()
                adminMessage.text = "수식 키패드 배치 저장 · ${choices[selected]}"
            }
            .show()
    }

    private fun toggleRemoteSupport() {
        if (remoteSupportStore.activeUntilEpochMillis() != null) {
            setRemoteSupportEnabled(enabled = false)
            return
        }
        AlertDialog.Builder(this)
            .setTitle("원격 점검 30분 시작")
            .setMessage(
                "원격 점검 중에는 이 PC의 승인된 USB 디버깅 연결로 " +
                    "QR·학생 이름·학습 화면을 캡처하고 태블릿을 조작할 수 있습니다.\n\n" +
                    "화면 오른쪽 위에 상태가 표시되며 30분 뒤 자동으로 다시 보호됩니다.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("시작") { _, _ ->
                setRemoteSupportEnabled(enabled = true)
            }
            .show()
    }

    private fun setRemoteSupportEnabled(enabled: Boolean) {
        remoteSupportWindowController.setSensitiveScreen(true)
        val duration = RemoteSupportPolicy.DEFAULT_DURATION_MILLIS
        val localStored = runCatching {
            if (enabled) {
                remoteSupportStore.enable(duration)
            } else {
                remoteSupportStore.disable()
            }
        }.isSuccess
        if (!localStored) {
            remoteSupportWindowController.setSensitiveScreen(true)
            diagnosticLog.record("REMOTE_SUPPORT_PERSIST_FAILED")
            adminMessage.text = "원격 점검 상태를 저장하지 못했습니다. 화면 캡처는 차단 상태를 유지합니다."
            remoteSupportWindowController.refresh()
            return
        }
        remoteSupportButton.isEnabled = false
        notifyWebRemoteSupport(
            enabled = enabled,
            durationSeconds = (duration / 1_000L).toInt(),
        ) { webNotified ->
            remoteSupportButton.isEnabled = true
            if (enabled && !webNotified) {
                runCatching { remoteSupportStore.disable() }
                remoteSupportWindowController.setSensitiveScreen(true)
                remoteSupportWindowController.refresh()
                diagnosticLog.record("REMOTE_SUPPORT_ENABLE_FAILED")
                adminMessage.text =
                    "학습 앱에 원격 점검 상태를 적용하지 못해 시작을 취소했습니다."
                return@notifyWebRemoteSupport
            }
            remoteSupportWindowController.setSensitiveScreen(false)
            remoteSupportWindowController.refresh()
            diagnosticLog.record(
                if (enabled) "REMOTE_SUPPORT_ENABLED" else "REMOTE_SUPPORT_DISABLED",
            )
            adminMessage.text = if (enabled) {
                "원격 점검을 시작했습니다. 30분 뒤 화면 캡처가 자동으로 다시 차단됩니다."
            } else if (!webNotified) {
                "관리 화면 캡처를 차단했습니다. 학습 화면은 기존 만료 시각에 자동 차단됩니다."
            } else {
                "원격 점검을 종료하고 화면 캡처를 다시 차단했습니다."
            }
        }
    }

    private fun notifyWebRemoteSupport(
        enabled: Boolean,
        durationSeconds: Int,
        onResult: (Boolean) -> Unit,
    ) {
        val intent = Intent(ACTION_SET_WEB_REMOTE_SUPPORT)
            .setComponent(ComponentName(WEB_PACKAGE, WEB_REMOTE_SUPPORT_RECEIVER))
            .putExtra(EXTRA_REMOTE_SUPPORT_ENABLED, enabled)
            .putExtra(EXTRA_REMOTE_SUPPORT_DURATION_SECONDS, durationSeconds)
        val resultReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                onResult(resultCode == Activity.RESULT_OK)
            }
        }
        runCatching {
            sendOrderedBroadcast(
                intent,
                null,
                resultReceiver,
                mainHandler,
                Activity.RESULT_CANCELED,
                null,
                null,
            )
        }.onFailure { onResult(false) }
    }

    private fun updateRemoteSupportButton(active: Boolean) {
        if (!::remoteSupportButton.isInitialized) return
        remoteSupportButton.text = if (active) {
            "원격 점검 종료"
        } else {
            "원격 점검 30분 시작"
        }
    }

    private fun selectedKeypadPreset(): String =
        getSharedPreferences(STUDENT_UI_PREFERENCES, Context.MODE_PRIVATE)
            .getString(KEY_KEYPAD_PRESET, KEYPAD_PRESET_RIGHT)
            ?.takeIf {
                it == KEYPAD_PRESET_RIGHT ||
                    it == KEYPAD_PRESET_LEFT ||
                    it == KEYPAD_PRESET_CENTER
            }
            ?: KEYPAD_PRESET_RIGHT

    private fun provideFeedback(success: Boolean) {
        val preferences = getSharedPreferences(FEEDBACK_PREFERENCES, Context.MODE_PRIVATE)
        if (preferences.getBoolean(KEY_VIBRATION_ENABLED, true)) {
            runCatching {
                getSystemService(VibratorManager::class.java)
                    ?.defaultVibrator
                    ?.vibrate(
                        VibrationEffect.createOneShot(
                            if (success) 45L else 110L,
                            VibrationEffect.DEFAULT_AMPLITUDE,
                        ),
                    )
            }
        }
        if (preferences.getBoolean(KEY_SOUND_ENABLED, false)) {
            runCatching {
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55).apply {
                    startTone(
                        if (success) ToneGenerator.TONE_PROP_ACK
                        else ToneGenerator.TONE_PROP_NACK,
                        120,
                    )
                    mainHandler.postDelayed({ release() }, 200L)
                }
            }
        }
    }

    private fun onOff(enabled: Boolean): String = if (enabled) "켬" else "끔"

    private fun confirmOneButtonRecovery() {
        val session = currentSession
        if (session?.sessionId == null || session.state == KioskState.QR_READY.name) {
            adminMessage.text = "원버튼 복구가 필요한 수업 상태가 아닙니다."
            return
        }
        AlertDialog.Builder(this)
            .setTitle("오류 상태 원버튼 복구")
            .setMessage(
                "현재 상태: ${session.state}\n\n" +
                    "남은 Web 로그인을 안전하게 정리하고 현재 수업과 보강 명단을 종료합니다. " +
                    "학생·반·QR 데이터는 삭제하지 않습니다.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("안전 복구") { _, _ ->
                launchWebSessionRecovery(PendingRecoveryAction.EndSession)
            }
            .show()
    }

    private fun completeSessionStart(action: PendingRecoveryAction.StartSession) {
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.startSession(
                    classId = action.classId,
                    temporaryStudentIds = action.temporaryStudentIds,
                )
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                finishWebRecoveryOperation()
                result.fold(
                    onSuccess = {
                        currentSession = it
                        pendingTemporaryStudentIds = emptySet()
                        showScanner()
                    },
                    onFailure = {
                        updateSessionAdminControls(currentSession)
                        adminMessage.text = it.message ?: "수업 시작 실패"
                    },
                )
            }
        }
    }

    private fun completeSessionEnd() {
        ioExecutor.execute {
            val result = runCatching { studentRepository.endSession() }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                finishWebRecoveryOperation()
                result.fold(
                    onSuccess = { idleSession ->
                        currentSession = idleSession
                        pendingTemporaryStudentIds = emptySet()
                        updateStudentManagementControls()
                        updateClassRosterUi()
                        updateSessionAdminControls(currentSession)
                        refreshAdminData("Web 로그인과 현재 수업을 안전하게 종료했습니다.")
                    },
                    onFailure = {
                        updateSessionAdminControls(currentSession)
                        adminMessage.text = it.message ?: "수업 종료 실패"
                    },
                )
            }
        }
    }

    private fun updateSessionAdminControls(session: ActiveSessionEntity?) {
        val active = session?.sessionId != null
        val resumable = active && session.state == KioskState.QR_READY.name
        addTemporaryButton.visibility = if (!active || resumable) View.VISIBLE else View.GONE
        resumeSessionButton.visibility = if (resumable) View.VISIBLE else View.GONE
        recoverSessionButton.visibility = if (active && !resumable) View.VISIBLE else View.GONE
        startSessionButton.visibility = if (active && !resumable) View.GONE else View.VISIBLE
        startSessionButton.text = if (active) {
            "현재 수업 안전 종료"
        } else {
            "선택한 반 수업 안전 시작"
        }
        val operationAvailable =
            !adminDataOperationGate.isActive && !webRecoveryGate.isActive
        classSpinner.isEnabled = operationAvailable && !active && !webRecoveryGate.isActive
        selfTestButton.isEnabled = operationAvailable && !webRecoveryGate.isActive
        statusText.text = session?.state ?: KioskState.ADMIN_IDLE.name
        updateClassRosterUi()
        if (active && !resumable) {
            adminMessage.text = "재시작 또는 민감 상태 종료가 감지되어 ${session.state} 상태입니다. 기존 수업을 안전 종료하세요."
        }
    }

    private fun finishWebRecoveryOperation() {
        webRecoveryGate.finish()
        updateStudentManagementControls()
        updateSessionAdminControls(currentSession)
    }

    private fun showQrPreview(preview: QrPreview) {
        clearQrPreview()
        issuedQrPreview = preview
        qrImage.setImageBitmap(preview.bitmap)
        qrCardName.text = preview.exactName
        val operationAvailable =
            !adminDataOperationGate.isActive && !webRecoveryGate.isActive
        exportQrPdfButton.isEnabled = operationAvailable
        sendPcPdfButton.isEnabled = operationAvailable && pairedPcDisplayName != null
    }

    private fun clearQrPreview(
        cardMessage: String = "발급한 QR은 이 화면에 한 번만 표시됩니다",
    ) {
        qrImage.setImageDrawable(null)
        issuedQrPreview?.let(::wipeQrPreview)
        issuedQrPreview = null
        exportQrPdfButton.isEnabled = false
        sendPcPdfButton.isEnabled = false
        qrCardName.text = cardMessage
    }

    private fun wipeQrPreview(preview: QrPreview) {
        if (!preview.bitmap.isRecycled) {
            if (preview.bitmap.isMutable) {
                preview.bitmap.eraseColor(android.graphics.Color.WHITE)
            }
            preview.bitmap.recycle()
        }
    }

    private fun refreshPcPairingState() {
        ioExecutor.execute {
            val result = runCatching {
                pcPairingStore.load()?.let { pairing ->
                    try {
                        pairing.displayName
                    } finally {
                        pairing.clearSensitiveData()
                    }
                }
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { displayName ->
                        pairedPcDisplayName = displayName
                        pairPcButton.text = if (displayName == null) {
                            "PC 무선 전송 페어링"
                        } else {
                            "지정 PC 다시 페어링 · $displayName"
                        }
                        sendPcPdfButton.isEnabled =
                            displayName != null && issuedQrPreview != null
                        updateStudentManagementControls()
                    },
                    onFailure = {
                        pairedPcDisplayName = null
                        pairPcButton.text = "PC 무선 전송 페어링"
                        sendPcPdfButton.isEnabled = false
                        updateStudentManagementControls()
                        adminMessage.text =
                            "저장된 PC 페어링을 확인하지 못했습니다. PC QR로 다시 페어링하세요."
                    },
                )
            }
        }
    }

    private fun startPcPairingScanner() {
        pcPairingMode = true
        hideScannerHelp(resumeAnalyzer = false)
        scannerHelpButton.visibility = View.GONE
        appHeader.visibility = View.GONE
        authPanel.visibility = View.GONE
        adminPanel.visibility = View.GONE
        scannerPanel.visibility = View.VISIBLE
        scannerVisible = true
        updateCameraSwitchLabel()
        scannerMessage.text = "PC 페어링 QR을 기다리고 있습니다"
        setSessionControlMode(admin = false)
        statusText.text = "PC_PAIRING"
        enterDedicatedMode()
        ensureCamera()
    }

    private fun handleRawQr(rawValue: String): Boolean {
        if (!pcPairingMode) return false
        if (!rawValue.startsWith(PcReceiverPairing.PREFIX)) {
            runOnUiThread {
                if (pcPairingMode && !destroyed) {
                    scannerMessage.text = "PC 수신기에 표시된 페어링 QR이 아닙니다"
                }
            }
            return true
        }
        val pairing = runCatching { PcReceiverPairing.decode(rawValue) }
            .getOrElse {
                runOnUiThread {
                    if (pcPairingMode && !destroyed) {
                        scannerMessage.text = "PC 페어링 QR을 확인하지 못했습니다. 다시 보여주세요"
                    }
                }
                return true
            }
        qrAnalyzer?.setEnabled(false)
        runOnUiThread {
            if (pcPairingMode && !destroyed) {
                scannerMessage.text = "지정 PC의 주소와 암호 응답을 확인하고 있습니다"
                savePcPairing(pairing)
            } else {
                pairing.clearSensitiveData()
            }
        }
        return true
    }

    private fun savePcPairing(pairing: PcReceiverPairing) {
        executeSensitive(cleanup = pairing::clearSensitiveData) {
            val result = runCatching {
                requireInitialPcPairingNetwork(pairing)
                pcControlClient.sendStatus(
                    pairing = pairing,
                    state = "PC 초기 페어링 확인",
                    studentName = null,
                    notify = false,
                )
                pcPairingStore.save(pairing)
                pairing.displayName
            }
            runOnUiThread {
                if (destroyed || !pcPairingMode) return@runOnUiThread
                result.fold(
                    onSuccess = { displayName ->
                        pairedPcDisplayName = displayName
                        returnToAdminAfterPcPairing(
                            "$displayName PC와 암호화 무선 전송을 페어링했습니다.",
                        )
                    },
                    onFailure = {
                        scannerMessage.text =
                            "PC 페어링 QR을 확인하지 못했습니다. 다시 보여주세요"
                        qrAnalyzer?.setEnabled(true)
                    },
                )
            }
        }
    }

    private fun requireInitialPcPairingNetwork(pairing: PcReceiverPairing) {
        val connectivity = getSystemService(ConnectivityManager::class.java)
        val network = requireNotNull(connectivity?.activeNetwork) {
            "활성 네트워크가 없습니다."
        }
        val capabilities = requireNotNull(connectivity.getNetworkCapabilities(network)) {
            "활성 네트워크 상태를 확인하지 못했습니다."
        }
        require(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            "PC 페어링은 같은 사설 Wi-Fi에서만 할 수 있습니다."
        }
        val link = requireNotNull(
            connectivity.getLinkProperties(network)
                ?.linkAddresses
                ?.firstOrNull { address ->
                    address.address is Inet4Address && address.address.isSiteLocalAddress
                },
        ) {
            "Wi-Fi 사설 IPv4 주소를 확인하지 못했습니다."
        }
        PcSubnetCandidates.requireSamePrivateSubnet(
            localAddress = requireNotNull(link.address.hostAddress),
            prefixLength = link.prefixLength,
            candidateHost = pairing.host,
        )
    }

    private fun returnToAdminAfterPcPairing(message: String) {
        pcPairingMode = false
        scannerVisible = false
        showAdmin(message)
    }

    private fun showScanner() {
        val session = currentSession
        if (session?.sessionId == null || session.state != KioskState.QR_READY.name) {
            adminMessage.text = "QR 대기로 복귀할 수 있는 수업 상태가 아닙니다."
            return
        }
        remoteSupportWindowController.setSensitiveScreen(false)
        clearQrPreview()
        pcPairingMode = false
        hideScannerHelp(resumeAnalyzer = false)
        scannerHelpButton.visibility = View.VISIBLE
        setSessionControlMode(admin = true)
        updateCameraSwitchLabel()
        appHeader.visibility = View.GONE
        authPanel.visibility = View.GONE
        adminPanel.visibility = View.GONE
        scannerPanel.visibility = View.VISIBLE
        scannerVisible = true
        studentLaunchGate.finish()
        manualStudentSelectionFlowActive = false
        sessionAdminActionFlowActive = false
        studentFlowGeneration += 1
        qrAcceptanceGeneration += 1
        cancelQrLoginButton.visibility = View.GONE
        activeStudentDisplayName = null
        qrGuidanceGeneration += 1
        scannerNoticeGate.invalidate()
        scannerMessage.text = ""
        statusText.text = KioskState.QR_READY.name
        reportPcStatus("QR 대기", null, notify = false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterDedicatedMode()
        if (manualStudentSelectionOnly) {
            scannerMessage.text =
                "QR 카메라를 사용할 수 없습니다\n오른쪽 아래 관리자 버튼에서 학생을 수동 선택하세요"
            statusText.text = "MANUAL_STUDENT_SELECTION"
        } else {
            ensureCamera()
        }
    }

    private fun handleRemoteQrTest(tokenHash: ByteArray) {
        if (destroyed || remoteSupportStore.activeUntilEpochMillis() == null) {
            tokenHash.fill(0)
            return
        }
        try {
            ioExecutor.execute {
                val session = runCatching { studentRepository.currentSession() }.getOrNull()
                val posted = mainHandler.post {
                    if (
                        destroyed ||
                        remoteSupportStore.activeUntilEpochMillis() == null ||
                        session?.sessionId == null ||
                        session.state != KioskState.QR_READY.name
                    ) {
                        tokenHash.fill(0)
                        diagnosticLog.record("REMOTE_QR_TEST_REJECTED")
                        return@post
                    }
                    currentSession = session
                    showScanner()
                    if (!scannerVisible) {
                        tokenHash.fill(0)
                        diagnosticLog.record("REMOTE_QR_TEST_REJECTED")
                        return@post
                    }
                    stopCamera()
                    diagnosticLog.record("REMOTE_QR_TEST_ACCEPTED")
                    validateQr(
                        tokenHash,
                        requiredDisplayNameExact =
                            RemoteQrTestAccountPolicy.REQUIRED_DISPLAY_NAME_EXACT,
                    )
                }
                if (!posted) tokenHash.fill(0)
            }
        } catch (_: RuntimeException) {
            tokenHash.fill(0)
            if (!destroyed) {
                diagnosticLog.record("REMOTE_QR_TEST_REJECTED")
            }
        }
    }

    private fun ensureCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            bindCamera()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun bindCamera() {
        if (!scannerVisible || destroyed) return
        val generation = ++cameraBindGeneration
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            if (!scannerVisible || destroyed || generation != cameraBindGeneration) {
                return@addListener
            }
            try {
                val provider = future.get()
                cameraProvider = provider
                val facing = CameraFacingPolicy.choose(
                    preferred = preferredCameraFacing,
                    frontAvailable = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA),
                    backAvailable = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA),
                )
                if (facing == null) {
                    scannerMessage.text = "사용 가능한 카메라가 없습니다\n선생님 확인이 필요합니다"
                    statusText.text = "CAMERA_UNAVAILABLE"
                    return@addListener
                }
                activeCameraFacing = facing
                preferredCameraFacing = facing
                updateCameraSwitchLabel()
                val analyzer = qrAnalyzer ?: QrImageAnalyzer(
                    onDecision = ::handleQrDecision,
                    onGuidance = ::handleQrGuidance,
                    onQuality = ::handleQrQuality,
                    onRawQr = ::handleRawQr,
                )
                    .also { qrAnalyzer = it }
                analyzer.setFrontFacing(facing == CameraFacing.FRONT)
                analyzer.setEnabled(scannerHelpPanel.visibility != View.VISIBLE)
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(ioExecutor, analyzer) }
                provider.unbindAll()
                provider.bindToLifecycle(
                    this,
                    if (facing == CameraFacing.FRONT) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    },
                    analysis,
                )
            } catch (_: Exception) {
                if (scannerVisible && !destroyed && generation == cameraBindGeneration) {
                    handleCameraBindingFailure()
                }
            }
        }, mainExecutor)
    }

    private fun handleCameraBindingFailure() {
        diagnosticLog.record("CAMERA_BIND_FAILURE")
        showAuthentication(enrollment = false)
        statusText.text = "CAMERA_ERROR"
        authError.text = "카메라를 시작하지 못했습니다. 관리자 PIN으로 상태를 확인하세요."
    }

    private fun switchCamera() {
        preferredCameraFacing = CameraFacingPolicy.opposite(activeCameraFacing)
        bindCamera()
    }

    private fun updateCameraSwitchLabel() {
        val activeCameraLabel = if (activeCameraFacing == CameraFacing.FRONT) {
            "전면"
        } else {
            "후면"
        }
        scannerInstruction.text = if (pcPairingMode) {
            "PC의 QR PDF 수신기에 표시된\n" +
                "페어링 QR을 ${activeCameraLabel} 카메라 렌즈에 보여주세요"
        } else {
            "QR 카드를 ${activeCameraLabel} 카메라 렌즈에 보여주세요"
        }
        val label = if (activeCameraFacing == CameraFacing.FRONT) {
            "후면 카메라로 전환"
        } else {
            "전면 카메라로 전환"
        }
        switchCameraButton.contentDescription = label
        switchCameraButton.tooltipText = label
        updateScannerLensPointer()
    }

    private fun updateScannerLensPointer() {
        if (!::scannerLensPointer.isInitialized) return
        if (activeCameraFacing != CameraFacing.FRONT) {
            scannerLensPointer.visibility = View.GONE
            return
        }
        val rotation = scannerPanel.display?.rotation ?: Surface.ROTATION_90
        val lensOnLeft = rotation != Surface.ROTATION_270
        val layoutParams = scannerLensPointer.layoutParams as FrameLayout.LayoutParams
        val horizontalGravity = if (lensOnLeft) Gravity.START else Gravity.END
        val targetGravity = Gravity.CENTER_VERTICAL or horizontalGravity
        if (layoutParams.gravity != targetGravity) {
            layoutParams.gravity = targetGravity
            scannerLensPointer.layoutParams = layoutParams
        }
        scannerLensPointer.text = if (lensOnLeft) {
            "←  전면 카메라 렌즈"
        } else {
            "전면 카메라 렌즈  →"
        }
        scannerLensPointer.contentDescription = if (lensOnLeft) {
            "왼쪽 전면 카메라 렌즈 위치"
        } else {
            "오른쪽 전면 카메라 렌즈 위치"
        }
        scannerLensPointer.visibility = View.VISIBLE
    }

    private fun setSessionControlMode(admin: Boolean) {
        val icon = if (admin) {
            R.drawable.ic_admin_panel_settings
        } else {
            R.drawable.ic_close
        }
        val label = if (admin) "관리자 인증" else "PC 페어링 취소"
        sessionAdminButton.setImageResource(icon)
        sessionAdminButton.contentDescription = label
        sessionAdminButton.tooltipText = label
    }

    private fun handleQrGuidance(guidance: QrFrameGuidance) {
        runOnUiThread {
            if (
                !scannerVisible ||
                destroyed ||
                qrAnalyzer?.isEnabled() != true ||
                statusText.text != KioskState.QR_READY.name
            ) {
                return@runOnUiThread
            }
            if (scannerNoticeGate.suppressesPassiveGuidance()) return@runOnUiThread
            val message = when (guidance) {
                QrFrameGuidance.MOVE_LEFT ->
                    "QR이 오른쪽에 있습니다\n카드를 왼쪽으로 옮겨주세요"
                QrFrameGuidance.MOVE_RIGHT ->
                    "QR이 왼쪽에 있습니다\n카드를 오른쪽으로 옮겨주세요"
                QrFrameGuidance.MOVE_UP ->
                    "QR이 아래쪽에 있습니다\n카드를 위로 올려주세요"
                QrFrameGuidance.MOVE_DOWN ->
                    "QR이 위쪽에 있습니다\n카드를 아래로 내려주세요"
                QrFrameGuidance.MOVE_CLOSER ->
                    "QR이 너무 작게 보입니다\n카드를 렌즈에 가까이 해주세요"
                QrFrameGuidance.MOVE_FARTHER ->
                    "QR이 너무 크게 보입니다\n카드를 렌즈에서 조금 떼어주세요"
                QrFrameGuidance.CENTERED ->
                    "QR 위치가 맞습니다\n카드를 잠시 그대로 유지해주세요"
            }
            val generation = ++qrGuidanceGeneration
            scannerMessage.text = message
            mainHandler.postDelayed({
                if (
                    generation == qrGuidanceGeneration &&
                    scannerVisible &&
                    !destroyed &&
                    qrAnalyzer?.isEnabled() == true &&
                    statusText.text == KioskState.QR_READY.name
                ) {
                    scannerMessage.text = ""
                }
            }, QR_GUIDANCE_STALE_MS)
        }
    }

    private fun handleQrQuality(quality: QrFrameQuality) {
        runOnUiThread {
            if (
                !scannerVisible ||
                destroyed ||
                qrAnalyzer?.isEnabled() != true ||
                statusText.text != KioskState.QR_READY.name
            ) {
                return@runOnUiThread
            }
            if (scannerNoticeGate.suppressesPassiveGuidance()) return@runOnUiThread
            if (quality == QrFrameQuality.GLARE) return@runOnUiThread
            val generation = ++qrGuidanceGeneration
            scannerMessage.text = when (quality) {
                QrFrameQuality.TOO_DARK -> "QR이 보이지 않습니다\n카드에 빛이 닿게 해주세요"
                QrFrameQuality.GLARE -> return@runOnUiThread
                QrFrameQuality.LOW_CONTRAST -> "QR이 흐리게 보입니다\n카드를 렌즈에 가까이 해주세요"
            }
            mainHandler.postDelayed({
                if (
                    generation == qrGuidanceGeneration &&
                    scannerVisible &&
                    !destroyed &&
                    qrAnalyzer?.isEnabled() == true
                ) {
                    scannerMessage.text = ""
                }
            }, QR_GUIDANCE_STALE_MS)
        }
    }

    private fun handleQrDecision(decision: QrFrameDecision) {
        runOnUiThread {
            if (
                !scannerVisible ||
                destroyed ||
                qrAnalyzer?.isEnabled() != true
            ) {
                decision.clearSensitiveData()
                return@runOnUiThread
            }
            if (pcPairingMode) {
                decision.clearSensitiveData()
                scannerMessage.text = "PC 페어링 QR은 한 장만 보여주세요"
                qrAnalyzer?.setEnabled(true)
                return@runOnUiThread
            }
            qrAnalyzer?.setEnabled(false)
            qrGuidanceGeneration += 1
            when (decision) {
                QrFrameDecision.Ignore -> qrAnalyzer?.setEnabled(true)
                is QrFrameDecision.Reject -> {
                    scannerNoticeGate.invalidate()
                    scannerHelpButton.visibility = View.GONE
                    val reason = when (decision.reason) {
                        QrFrameRejection.MULTIPLE_QR -> {
                            scannerMessage.text = "QR카드는 한 장만 보여주세요"
                            "MULTIPLE_QR"
                        }
                        QrFrameRejection.INVALID_QR -> {
                            scannerMessage.text = "사용할 수 없는 카드입니다\n선생님에게 문의하세요"
                            "INVALID_QR"
                        }
                    }
                    recordQrRejection(reason)
                }
                is QrFrameDecision.Accept -> {
                    scannerNoticeGate.invalidate()
                    validateQr(decision.tokenHash)
                }
            }
        }
    }

    private fun recordQrRejection(reason: String) {
        diagnosticLog.record("QR_REJECTED", reason)
        try {
            ioExecutor.execute {
                val result = runCatching { studentRepository.recordQrRejection(reason) }
                runOnUiThread {
                    if (!scannerVisible || destroyed) return@runOnUiThread
                    result.fold(
                        onSuccess = {
                            resumeScannerAfterCooldown()
                        },
                        onFailure = {
                            currentSession = null
                            statusText.text = KioskState.LOCKED.name
                            showAuthentication(enrollment = false)
                            authError.text =
                                "QR 거부 기록 중 오류가 발생했습니다. 관리자 PIN으로 상태를 확인하세요."
                        },
                    )
                }
            }
        } catch (_: RuntimeException) {
            if (!destroyed) {
                currentSession = null
                statusText.text = KioskState.LOCKED.name
                showAuthentication(enrollment = false)
                authError.text =
                    "QR 거부 기록을 시작하지 못했습니다. 관리자 PIN으로 상태를 확인하세요."
            }
        }
    }

    private fun validateQr(
        tokenHash: ByteArray,
        requiredDisplayNameExact: String? = null,
    ) {
        val expectedSessionId = currentSession?.sessionId
        if (expectedSessionId == null) {
            tokenHash.fill(0)
            return
        }
        val flowGeneration = studentFlowGeneration
        hideScannerHelp(resumeAnalyzer = false)
        scannerHelpButton.visibility = View.GONE
        qrGuidanceGeneration += 1
        scannerMessage.text = "확인되었습니다"
        statusText.text = KioskState.QR_VALIDATING.name
        executeSensitive(
            cleanup = { tokenHash.fill(0) },
        ) {
            val result = runCatching {
                studentRepository.validateForActiveSession(
                    tokenHash = tokenHash,
                    requiredDisplayNameExact = requiredDisplayNameExact,
                    expectedSessionId = expectedSessionId,
                )
            }
            runOnUiThread {
                if (
                    !scannerVisible || destroyed ||
                    flowGeneration != studentFlowGeneration ||
                    currentSession?.sessionId != expectedSessionId
                ) return@runOnUiThread
                result.fold(
                    onSuccess = { student ->
                        if (student == null) {
                            cancelQrLoginButton.visibility = View.GONE
                            scannerMessage.text = "현재 수업에서 사용할 수 없는 카드입니다\n선생님에게 문의하세요"
                            resumeScannerAfterCooldown()
                        } else {
                            val generation = ++qrAcceptanceGeneration
                            activeStudentDisplayName = student.displayNameExact
                            cancelQrLoginButton.visibility = View.VISIBLE
                            provideFeedback(success = true)
                            scannerMessage.text =
                                "${student.displayNameExact}\nQR 인증이 완료되었습니다"
                            reportPcStatus(
                                state = "학생 확인",
                                studentName = student.displayNameExact,
                                notify = StudentLoginPcNotificationPolicy.shouldNotify(
                                    StudentLoginPcStage.QR_VERIFIED,
                                ),
                            )
                            mainHandler.postDelayed({
                                if (
                                    !destroyed &&
                                    scannerVisible &&
                                    generation == qrAcceptanceGeneration &&
                                    flowGeneration == studentFlowGeneration &&
                                    currentSession?.sessionId == expectedSessionId &&
                                    studentLaunchGate.tryStart()
                                ) {
                                    cancelQrLoginButton.visibility = View.GONE
                                    scannerMessage.text =
                                        "${student.displayNameExact}\n로그인 중입니다"
                                    launchSecureWebSession(student, expectedSessionId)
                                }
                            }, QR_ACCEPTED_DISPLAY_MS)
                        }
                    },
                    onFailure = {
                        currentSession = null
                        statusText.text = KioskState.LOCKED.name
                        showAuthentication(enrollment = false)
                        authError.text =
                            "QR 확인 중 오류가 발생했습니다. 관리자 PIN으로 상태를 확인하세요."
                    },
                )
            }
        }
    }

    private fun launchSecureWebSession(
        student: ValidatedStudent,
        expectedSessionId: String,
    ) {
        cancelQrLoginButton.visibility = View.GONE
        activeStudentDisplayName = student.displayNameExact
        reportPcStatus(
            state = "로그인 중",
            studentName = student.displayNameExact,
            notify = StudentLoginPcNotificationPolicy.shouldNotify(
                StudentLoginPcStage.LOGIN_IN_PROGRESS,
            ),
        )
        statusText.text = KioskState.PRELOGIN_CHECK.name
        ioExecutor.execute {
            val prepared = runCatching {
                studentRepository.transitionSession(
                    expectedState = KioskState.QR_READY,
                    state = KioskState.PRELOGIN_CHECK,
                    expectedSessionId = expectedSessionId,
                    currentStudentId = student.studentId,
                    automationStep = "CREDENTIAL_BRIDGE",
                )
                studentRepository.decryptCredentials(student.studentId).use { credentials ->
                    OneTimeCredentialBroker.publish(
                        student.displayNameExact,
                        credentials.username,
                        credentials.password,
                    )
                }
            }
            runOnUiThread {
                prepared.fold(
                    onSuccess = { handle ->
                        when (
                            PreparedWebSessionPolicy.decide(
                                destroyed = destroyed,
                                scannerVisible = scannerVisible,
                            )
                        ) {
                            PreparedWebSessionDisposition.REVOKE_ONLY -> {
                                OneTimeCredentialBroker.revoke(handle.id)
                                studentLaunchGate.finish()
                            }
                            PreparedWebSessionDisposition.CANCEL_AND_RESTORE -> {
                                OneTimeCredentialBroker.revoke(handle.id)
                                studentLaunchGate.finish()
                                restoreQrReadyAfterCancelledWebLaunch(expectedSessionId)
                            }
                            PreparedWebSessionDisposition.LAUNCH -> {
                                pendingCredentialBridgeId = handle.id
                                pendingWebSessionId = expectedSessionId
                                scannerVisible = false
                                stopCamera()
                                val intent = Intent(
                                    CredentialBridgeContract.ACTION_START_SECURE_SESSION,
                                )
                                    .setComponent(
                                        ComponentName(
                                            CredentialBridgeContract.TRUSTED_CONSUMER_PACKAGE,
                                            "com.local.matholickiosk.webpoc.MainActivity",
                                        ),
                                    )
                                    .putExtra(
                                        CredentialBridgeContract.EXTRA_CREDENTIAL_HANDLE,
                                        handle.id,
                                    )
                                    .putExtra(
                                        CredentialBridgeContract.EXTRA_KEYPAD_PRESET,
                                        selectedKeypadPreset(),
                                    )
                                runCatching { webSessionLauncher.launch(intent) }
                                    .onFailure {
                                        OneTimeCredentialBroker.revoke(handle.id)
                                        pendingCredentialBridgeId = null
                                        pendingWebSessionId = null
                                        studentLaunchGate.finish()
                                        lockAfterBridgeFailure(
                                            "WEBPOC_NOT_AVAILABLE",
                                            expectedSessionId,
                                        )
                                    }
                            }
                        }
                    },
                    onFailure = {
                        studentLaunchGate.finish()
                        if (!destroyed) {
                            lockAfterBridgeFailure("CREDENTIAL_PREPARATION", expectedSessionId)
                        }
                    },
                )
            }
        }
    }

    private fun restoreQrReadyAfterCancelledWebLaunch(expectedSessionId: String) {
        pendingWebSessionId = null
        ioExecutor.execute {
            val restored = runCatching {
                studentRepository.transitionSession(
                    expectedState = KioskState.PRELOGIN_CHECK,
                    state = KioskState.QR_READY,
                    expectedSessionId = expectedSessionId,
                )
                checkNotNull(studentRepository.currentSession()) {
                    "Prepared Web session was restored without an active session"
                }.also { session ->
                    check(
                        session.sessionId != null &&
                            session.state == KioskState.QR_READY.name,
                    ) {
                        "Prepared Web session did not return to QR_READY"
                    }
                }
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                restored.fold(
                    onSuccess = { session ->
                        currentSession = session
                        statusText.text = session.state
                    },
                    onFailure = {
                        currentSession = null
                        statusText.text = KioskState.LOCKED.name
                        showAuthentication(enrollment = false)
                        authError.text =
                            "준비된 로그인 상태를 복구하지 못했습니다. " +
                            "관리자 PIN으로 상태를 확인하세요."
                    },
                )
            }
        }
    }

    private fun lockAfterBridgeFailure(reason: String, expectedSessionId: String) {
        pendingWebSessionId = null
        studentLaunchGate.finish()
        diagnosticLog.record("BRIDGE_FAILURE", reason)
        ioExecutor.execute {
            runCatching {
                studentRepository.transitionSession(
                    expectedState = KioskState.PRELOGIN_CHECK,
                    state = KioskState.LOCKED,
                    expectedSessionId = expectedSessionId,
                    lockedReason = reason,
                )
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                reportPcStatus(
                    state = "복구 필요",
                    studentName = activeStudentDisplayName,
                    notify = true,
                )
                statusText.text = KioskState.LOCKED.name
                showAuthentication(enrollment = false)
                authError.text = "화면이 잠겼습니다 · $reason"
            }
        }
    }

    private fun resumeScannerAfterCooldown() {
        qrAcceptanceGeneration += 1
        cancelQrLoginButton.visibility = View.GONE
        scannerHelpButton.visibility = View.GONE
        mainHandler.postDelayed({
            if (!scannerVisible || destroyed) return@postDelayed
            qrGuidanceGeneration += 1
            scannerMessage.text = ""
            statusText.text = KioskState.QR_READY.name
            scannerHelpButton.visibility = View.VISIBLE
            when (
                ScannerCameraResumePolicy.decide(
                    manualStudentSelectionOnly = manualStudentSelectionOnly,
                    cameraBound = cameraProvider != null,
                    activityStarted = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED),
                )
            ) {
                ScannerCameraResumeAction.REBIND_CAMERA -> ensureCamera()
                ScannerCameraResumeAction.ENABLE_ANALYZER -> qrAnalyzer?.setEnabled(true)
                ScannerCameraResumeAction.NONE -> Unit
            }
        }, SCAN_COOLDOWN_MS)
    }

    private fun cancelPendingQrLogin() {
        if (!scannerVisible || cancelQrLoginButton.visibility != View.VISIBLE) return
        qrAcceptanceGeneration += 1
        studentFlowGeneration += 1
        cancelQrLoginButton.visibility = View.GONE
        scannerHelpButton.visibility = View.GONE
        activeStudentDisplayName = null
        scannerMessage.text = "로그인을 취소했습니다\n다른 QR 카드를 보여주세요"
        statusText.text = KioskState.QR_READY.name
        reportPcStatus("QR 대기", null, notify = false)
        mainHandler.postDelayed({
            if (!destroyed && scannerVisible) {
                scannerMessage.text = ""
                scannerHelpButton.visibility = View.VISIBLE
                qrAnalyzer?.setEnabled(true)
            }
        }, 700L)
    }

    private fun showScannerHelp() {
        if (
            !scannerVisible ||
            pcPairingMode ||
            studentLaunchGate.isActive ||
            statusText.text != KioskState.QR_READY.name
        ) return
        scannerHelpPausedAnalyzer = true
        qrAnalyzer?.setEnabled(false)
        qrGuidanceGeneration += 1
        setScannerHelpBackgroundAccessibility(hidden = true)
        scannerHelpPanel.visibility = View.VISIBLE
        scannerHelpPanel.bringToFront()
        scannerHelpButton.bringToFront()
        scannerHelpButton.contentDescription = "QR 카드 화면 도움말 닫기"
        scannerHelpPanel.requestFocus()
    }

    private fun hideScannerHelp(resumeAnalyzer: Boolean) {
        if (!::scannerHelpPanel.isInitialized) return
        val shouldResume = resumeAnalyzer &&
            scannerHelpPausedAnalyzer &&
            scannerVisible &&
            !pcPairingMode &&
            !studentLaunchGate.isActive &&
            statusText.text == KioskState.QR_READY.name
        scannerHelpPausedAnalyzer = false
        scannerHelpPanel.visibility = View.GONE
        scannerHelpButton.contentDescription = "QR 카드 화면 도움말"
        setScannerHelpBackgroundAccessibility(hidden = false)
        if (shouldResume) qrAnalyzer?.setEnabled(true)
    }

    private fun setScannerHelpBackgroundAccessibility(hidden: Boolean) {
        val groupImportance = if (hidden) {
            View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        } else {
            View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        }
        scannerCenterContent.importantForAccessibility = groupImportance
        scannerActionControls.importantForAccessibility = groupImportance
        scannerHelpButton.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
    }

    private fun requestSessionAdminAuthentication() {
        qrAnalyzer?.setEnabled(false)
        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            maxLines = 1
            isSaveEnabled = false
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            hint = "관리자 PIN"
        }
        var submitting = false
        lateinit var submitPin: () -> Unit
        val automaticSubmit = Runnable {
            if (
                !submitting && input.isAttachedToWindow &&
                input.text.length == enrolledAdminPinLength
            ) {
                submitPin()
            }
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("관리자 인증")
            .setView(input)
            .setNegativeButton("취소") { _, _ -> qrAnalyzer?.setEnabled(true) }
            .setPositiveButton("인증", null)
            .create()
        dialog.setOnShowListener {
            submitPin = submit@{
                if (submitting) return@submit
                mainHandler.removeCallbacks(automaticSubmit)
                val pin = input.text.toSensitiveCharArray()
                val attemptedPinLength = pin.size
                input.text.clear()
                submitting = true
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                executeSensitive(
                    cleanup = { pin.fill('\u0000') },
                ) {
                    val result = authRepository.authenticate(pin)
                    runOnUiThread {
                        if (destroyed) return@runOnUiThread
                        when (result) {
                            AdminAuthResult.Success -> {
                                enrolledAdminPinLength = attemptedPinLength
                                dialog.dismiss()
                                showAuthenticatedSessionActions()
                            }
                            AdminAuthResult.NotEnrolled -> {
                                dialog.dismiss()
                                showAuthentication(enrollment = true)
                            }
                            is AdminAuthResult.Rejected -> {
                                submitting = false
                                val seconds = (result.retryAfterMillis + 999) / 1_000
                                input.error = "PIN 오류 · ${seconds}초 후 재시도"
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                            }
                        }
                    }
                }
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                submitPin()
            }
            input.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submitPin()
                    true
                } else {
                    false
                }
            }
            input.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) = Unit

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {
                        mainHandler.removeCallbacks(automaticSubmit)
                        if (!submitting && (s?.length ?: 0) == enrolledAdminPinLength) {
                            mainHandler.postDelayed(automaticSubmit, 300L)
                        }
                    }

                    override fun afterTextChanged(s: Editable?) = Unit
                },
            )
            input.requestFocus()
        }
        dialog.setOnDismissListener {
            mainHandler.removeCallbacks(automaticSubmit)
            if (scannerVisible) qrAnalyzer?.setEnabled(true)
            restoreRemoteSupportScreenPolicy()
        }
        remoteSupportWindowController.setSensitiveScreen(true)
        dialog.show()
    }

    private fun restoreRemoteSupportScreenPolicy() {
        remoteSupportWindowController.setSensitiveScreen(
            ::authPanel.isInitialized && authPanel.visibility == View.VISIBLE,
        )
    }

    private fun showAuthenticatedSessionActions() {
        if (!scannerVisible || currentSession?.sessionId == null) {
            showAdmin()
            return
        }
        val className = classes.firstOrNull { it.id == currentSession?.classId }?.label
            ?: "현재 수업"
        qrAnalyzer?.setEnabled(false)
        AlertDialog.Builder(this)
            .setTitle("$className · 관리자 작업")
            .setItems(
                arrayOf(
                    "다음 수업 반으로 바로 변경",
                    "현재 반에 보충 인원 추가 (이번 수업만)",
                    "학생 수동 선택",
                    "관리자 화면 열기",
                ),
            ) { _, which ->
                when (which) {
                    0 -> {
                        sessionAdminActionFlowActive = true
                        showQuickClassSwitchDialog()
                    }
                    1 -> {
                        sessionAdminActionFlowActive = true
                        loadQuickTemporaryStudentChoices()
                    }
                    2 -> {
                        manualStudentSelectionFlowActive = true
                        val expectedSessionId = currentSession?.sessionId
                        if (expectedSessionId == null) {
                            finishManualStudentSelectionFlow()
                        } else {
                            loadManualStudentChoices(
                                className,
                                studentFlowGeneration,
                                expectedSessionId,
                            )
                        }
                    }
                    else -> showAdmin()
                }
            }
            .setNegativeButton("취소", null)
            .setOnDismissListener {
                if (
                    scannerVisible &&
                    !manualStudentSelectionFlowActive &&
                    !sessionAdminActionFlowActive
                ) {
                    qrAnalyzer?.setEnabled(!manualStudentSelectionOnly)
                }
            }
            .show()
    }

    private fun showQuickClassSwitchDialog() {
        val expectedSessionId = currentSession?.sessionId
        val currentClassId = currentSession?.classId
        if (expectedSessionId == null || currentClassId == null || !scannerVisible) {
            scannerMessage.text = "현재 수업을 확인하지 못했습니다"
            finishSessionAdminActionFlow()
            return
        }
        val targets = FixedClassSlots.names.mapNotNull { className ->
            classes.firstOrNull { it.label == className }
        }.filterNot { it.id == currentClassId }
        if (targets.isEmpty()) {
            scannerMessage.text = "변경할 수 있는 다른 고정 반이 없습니다"
            finishSessionAdminActionFlow()
            return
        }

        var selectionMade = false
        AlertDialog.Builder(this)
            .setTitle("다음 수업 반 선택 · QR 대기로 바로 변경")
            .setItems(targets.map(Choice::label).toTypedArray()) { _, which ->
                selectionMade = true
                confirmQuickClassSwitch(expectedSessionId, targets[which])
            }
            .setNegativeButton("취소", null)
            .setOnDismissListener {
                if (!selectionMade) finishSessionAdminActionFlow()
            }
            .show()
    }

    private fun confirmQuickClassSwitch(expectedSessionId: String, target: Choice) {
        var submitted = false
        AlertDialog.Builder(this)
            .setTitle("${target.label}으로 변경")
            .setMessage(
                "현재 수업을 끝내고 ${target.label} 수업을 시작합니다.\n" +
                    "현재 수업의 임시 보충 명단은 함께 종료됩니다.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("반 변경") { _, _ ->
                submitted = true
                performQuickClassSwitch(expectedSessionId, target)
            }
            .setOnDismissListener {
                if (!submitted) finishSessionAdminActionFlow()
            }
            .show()
    }

    private fun performQuickClassSwitch(expectedSessionId: String, target: Choice) {
        scannerMessage.text = "${target.label} 수업으로 변경하고 있습니다"
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.switchSessionClass(expectedSessionId, target.id)
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { replacement ->
                        currentSession = replacement
                        pendingTemporaryStudentIds = emptySet()
                        finishSessionAdminActionFlow(resumeAnalyzer = false)
                        showScanner()
                        showTransientScannerMessage(
                            "${target.label} 수업으로 변경했습니다\nQR 카드를 보여주세요",
                        )
                    },
                    onFailure = {
                        scannerMessage.text = it.message ?: "반을 변경하지 못했습니다"
                        finishSessionAdminActionFlow()
                    },
                )
            }
        }
    }

    private fun loadQuickTemporaryStudentChoices() {
        val expectedSessionId = currentSession?.sessionId
        val className = classes.firstOrNull { it.id == currentSession?.classId }?.label
            ?: "현재 반"
        if (expectedSessionId == null || !scannerVisible) {
            scannerMessage.text = "현재 수업을 확인하지 못했습니다"
            finishSessionAdminActionFlow()
            return
        }
        scannerMessage.text = "추가할 보충 학생을 확인하고 있습니다"
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.listTemporaryStudentCandidatesForActiveSession(
                    expectedSessionId,
                )
            }
            runOnUiThread {
                if (
                    destroyed || !scannerVisible ||
                    currentSession?.sessionId != expectedSessionId
                ) return@runOnUiThread
                result.fold(
                    onSuccess = { candidates ->
                        if (candidates.isEmpty()) {
                            scannerMessage.text = "추가할 수 있는 다른 활성 학생이 없습니다"
                            finishSessionAdminActionFlow()
                        } else {
                            scannerMessage.text = ""
                            showQuickTemporaryStudentDialog(
                                className,
                                expectedSessionId,
                                candidates,
                            )
                        }
                    },
                    onFailure = {
                        scannerMessage.text =
                            it.message ?: "보충 학생 명단을 불러오지 못했습니다"
                        finishSessionAdminActionFlow()
                    },
                )
            }
        }
    }

    private fun showQuickTemporaryStudentDialog(
        className: String,
        expectedSessionId: String,
        candidates: List<ValidatedStudent>,
    ) {
        val chosen = mutableSetOf<String>()
        var submitted = false
        AlertDialog.Builder(this)
            .setTitle("$className · 임시 보충 인원 (이번 수업만)")
            .setMultiChoiceItems(
                candidates.map(ValidatedStudent::displayNameExact).toTypedArray(),
                BooleanArray(candidates.size),
            ) { _, which, isChecked ->
                val studentId = candidates[which].studentId
                if (isChecked) chosen += studentId else chosen -= studentId
            }
            .setNegativeButton("취소", null)
            .setPositiveButton("현재 수업에 추가") { _, _ ->
                submitted = true
                if (chosen.isEmpty()) {
                    scannerMessage.text = "추가할 학생을 선택하세요"
                    finishSessionAdminActionFlow()
                } else {
                    performQuickTemporaryStudentAdd(expectedSessionId, chosen)
                }
            }
            .setOnDismissListener {
                if (!submitted) finishSessionAdminActionFlow()
            }
            .show()
    }

    private fun performQuickTemporaryStudentAdd(
        expectedSessionId: String,
        studentIds: Set<String>,
    ) {
        scannerMessage.text = "보충 학생 ${studentIds.size}명을 추가하고 있습니다"
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.addTemporaryStudents(expectedSessionId, studentIds)
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        showTransientScannerMessage(
                            "보충 학생 ${studentIds.size}명을 추가했습니다\n" +
                                "이제 QR 카드를 사용할 수 있습니다",
                        )
                    },
                    onFailure = {
                        scannerMessage.text =
                            it.message ?: "보충 학생을 추가하지 못했습니다"
                    },
                )
                finishSessionAdminActionFlow()
            }
        }
    }

    private fun showTransientScannerMessage(
        message: String,
        durationMillis: Long = SCANNER_NOTICE_DURATION_MS,
    ) {
        qrGuidanceGeneration += 1
        val generation = scannerNoticeGate.begin()
        scannerMessage.text = message
        mainHandler.postDelayed({
            val noticeIsCurrent = scannerNoticeGate.finish(generation)
            if (
                !destroyed && scannerVisible &&
                noticeIsCurrent &&
                scannerMessage.text.toString() == message
            ) {
                scannerMessage.text = ""
            }
        }, durationMillis)
    }

    private fun finishSessionAdminActionFlow(resumeAnalyzer: Boolean = true) {
        sessionAdminActionFlowActive = false
        if (
            resumeAnalyzer && scannerVisible &&
            currentSession?.state == KioskState.QR_READY.name
        ) {
            qrAnalyzer?.setEnabled(!manualStudentSelectionOnly)
        }
    }

    private fun loadManualStudentChoices(
        className: String,
        flowGeneration: Int,
        expectedSessionId: String,
    ) {
        scannerMessage.text = "현재 수업 학생 명단을 확인하고 있습니다"
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.listEligibleStudentsForActiveSession()
            }
            runOnUiThread {
                if (
                    destroyed || !scannerVisible ||
                    flowGeneration != studentFlowGeneration ||
                    currentSession?.sessionId != expectedSessionId
                ) return@runOnUiThread
                result.fold(
                    onSuccess = { choices ->
                        if (choices.isEmpty()) {
                            scannerMessage.text = "수동 선택할 수 있는 학생이 없습니다"
                            finishManualStudentSelectionFlow()
                        } else {
                            showManualStudentDialog(
                                className,
                                choices,
                                flowGeneration,
                                expectedSessionId,
                            )
                        }
                    },
                    onFailure = {
                        scannerMessage.text =
                            it.message ?: "현재 수업 학생 명단을 불러오지 못했습니다"
                        finishManualStudentSelectionFlow()
                    },
                )
            }
        }
    }

    private fun showManualStudentDialog(
        className: String,
        choices: List<ValidatedStudent>,
        flowGeneration: Int,
        expectedSessionId: String,
    ) {
        var selectionMade = false
        AlertDialog.Builder(this)
            .setTitle("$className · 학생 선택")
            .setItems(choices.map(ValidatedStudent::displayNameExact).toTypedArray()) { _, which ->
                selectionMade = true
                validateManualStudent(
                    choices[which].studentId,
                    flowGeneration,
                    expectedSessionId,
                )
            }
            .setNegativeButton("취소", null)
            .setOnDismissListener {
                if (!selectionMade) {
                    finishManualStudentSelectionFlow()
                    if (scannerMessage.text == "현재 수업 학생 명단을 확인하고 있습니다") {
                        scannerMessage.text = ""
                    }
                }
            }
            .show()
    }

    private fun validateManualStudent(
        studentId: String,
        flowGeneration: Int,
        expectedSessionId: String,
    ) {
        scannerMessage.text = "선택한 학생을 확인하고 있습니다"
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.validateManualStudentForActiveSession(
                    studentId,
                    expectedSessionId,
                )
            }
            runOnUiThread {
                if (
                    destroyed || !scannerVisible ||
                    flowGeneration != studentFlowGeneration ||
                    currentSession?.sessionId != expectedSessionId
                ) return@runOnUiThread
                result.fold(
                    onSuccess = { student ->
                        if (student == null) {
                            scannerMessage.text =
                                "현재 수업에서 선택할 수 없는 학생입니다\n명단을 다시 확인하세요"
                            finishManualStudentSelectionFlow()
                        } else {
                            scannerMessage.text =
                                "${student.displayNameExact}\n수동 인증이 완료되었습니다"
                            mainHandler.postDelayed({
                                if (
                                    !destroyed && scannerVisible &&
                                    flowGeneration == studentFlowGeneration &&
                                    currentSession?.sessionId == expectedSessionId &&
                                    studentLaunchGate.tryStart()
                                ) {
                                    launchSecureWebSession(student, expectedSessionId)
                                }
                            }, QR_ACCEPTED_DISPLAY_MS)
                        }
                    },
                    onFailure = {
                        scannerMessage.text = "학생 수동 인증에 실패했습니다"
                        finishManualStudentSelectionFlow()
                    },
                )
            }
        }
    }

    private fun finishManualStudentSelectionFlow() {
        manualStudentSelectionFlowActive = false
        studentFlowGeneration += 1
        if (scannerVisible) {
            qrAnalyzer?.setEnabled(!manualStudentSelectionOnly)
        }
    }

    private fun stopCamera() {
        cameraBindGeneration += 1
        qrAnalyzer?.setEnabled(false)
        val provider = cameraProvider
        cameraProvider = null
        try {
            provider?.unbindAll()
        } catch (_: Exception) {
            // Camera teardown must not prevent the fail-closed authentication screen.
        }
    }

    override fun onStop() {
        if (adminPanel.visibility == View.VISIBLE) {
            if (suppressNextAdminStopRelock) {
                suppressNextAdminStopRelock = false
            } else {
                relockAdminOnStart = true
            }
        }
        super.onStop()
        stopCamera()
        clearQrPreview("보안을 위해 QR 표시를 지웠습니다")
        pinInput.text.clear()
        pinConfirmInput.text.clear()
        activeStudentDisplayName = null
    }

    override fun onStart() {
        super.onStart()
        pendingSharedPdf?.let { shared ->
            pendingSharedPdf = null
            QrPdfExporter.scheduleSharedFileCleanup(this, shared)
        }
        when {
            relockAdminOnStart && ::authRepository.isInitialized -> {
                relockAdminOnStart = false
                showAuthentication(enrollment = false)
            }
            scannerVisible && ::studentRepository.isInitialized -> {
                enterDedicatedMode()
                ensureCamera()
            }
            ::lockTaskController.isInitialized &&
                ::authPanel.isInitialized &&
                authPanel.visibility == View.VISIBLE -> enterDedicatedMode()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pendingWebSessionId?.let { outState.putString(KEY_PENDING_WEB_SESSION_ID, it) }
        when (val action = pendingRecoveryAction) {
            PendingRecoveryAction.None -> outState.putString(
                KEY_PENDING_RECOVERY_ACTION,
                PENDING_RECOVERY_NONE,
            )
            is PendingRecoveryAction.StartSession -> {
                outState.putString(KEY_PENDING_RECOVERY_ACTION, PENDING_RECOVERY_START)
                outState.putString(KEY_PENDING_RECOVERY_CLASS_ID, action.classId)
                outState.putStringArrayList(
                    KEY_PENDING_RECOVERY_TEMPORARY_STUDENT_IDS,
                    ArrayList(action.temporaryStudentIds),
                )
            }
            PendingRecoveryAction.EndSession -> outState.putString(
                KEY_PENDING_RECOVERY_ACTION,
                PENDING_RECOVERY_END,
            )
        }
    }

    private fun restorePendingRecoveryAction(savedState: Bundle?): PendingRecoveryAction =
        when (savedState?.getString(KEY_PENDING_RECOVERY_ACTION)) {
            PENDING_RECOVERY_START -> savedState.getString(KEY_PENDING_RECOVERY_CLASS_ID)
                ?.let { classId ->
                    PendingRecoveryAction.StartSession(
                        classId = classId,
                        temporaryStudentIds = savedState
                            .getStringArrayList(KEY_PENDING_RECOVERY_TEMPORARY_STUDENT_IDS)
                            ?.toSet()
                            .orEmpty(),
                    )
                }
                ?: PendingRecoveryAction.None
            PENDING_RECOVERY_END -> PendingRecoveryAction.EndSession
            else -> PendingRecoveryAction.None
        }

    override fun onDestroy() {
        destroyed = true
        if (::scannerPanel.isInitialized) {
            (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager)
                .unregisterDisplayListener(scannerDisplayListener)
        }
        RemoteQrTestBridge.unregister(this)
        pendingCredentialBridgeId?.let(OneTimeCredentialBroker::revoke)
        pendingCredentialBridgeId = null
        if (::remoteSupportWindowController.isInitialized) {
            remoteSupportWindowController.stop()
        }
        mainHandler.removeCallbacksAndMessages(null)
        stopCamera()
        qrAnalyzer?.close()
        ioExecutor.shutdownNow()
            .filterIsInstance<DiscardableSensitiveTask>()
            .forEach(DiscardableSensitiveTask::discard)
        pcControlExecutor.shutdownNow()
        if (::pcStatusDispatcher.isInitialized) pcStatusDispatcher.close()
        super.onDestroy()
    }

    private fun executeSensitive(
        cleanup: () -> Unit,
        operation: () -> Unit,
    ) {
        val task = SensitiveTask(cleanup, operation)
        try {
            ioExecutor.execute(task)
        } catch (failure: RuntimeException) {
            task.discard()
            if (!destroyed) throw failure
        }
    }

    private fun dialogTextInput(
        hint: String,
        inputType: Int,
        sensitive: Boolean = false,
    ): EditText =
        EditText(this).apply {
            this.hint = hint
            this.inputType = inputType
            maxLines = 1
            if (sensitive) {
                isSaveEnabled = false
                importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
                filterTouchesWhenObscured = true
            }
        }

    private fun dialogForm(vararg inputs: EditText): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(8), dp(24), 0)
            inputs.forEach { input ->
                addView(
                    input,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        topMargin = dp(8)
                    },
                )
            }
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun applicationVersion(): String = runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
    }.getOrDefault("unknown")

    private fun Editable.toSensitiveCharArray(): CharArray =
        CharArray(length) { index -> this[index] }

    private data class Choice(val id: String, val label: String)

    private data class StudentChoice(
        val id: String,
        val label: String,
        val reusableCardLabel: String? = null,
    )

    private data class PcStatusUpdate(
        val state: String,
        val studentName: String?,
        val notify: Boolean,
    )

    private data class AdminDataSnapshot(
        val classes: List<Choice>,
        val students: List<StudentChoice>,
        val reusableCardSlots: List<ReusableCardSlotSummary>,
        val session: ActiveSessionEntity?,
        val resolvedClassId: String?,
        val membershipStudentIds: Set<String>,
    )

    private data class InitialStateSnapshot(
        val enrolled: Boolean,
        val pinLength: Int?,
        val recoveredState: KioskState,
        val reusableCardBootstrapMessage: String?,
    )

    private data class QrPreview(
        val studentId: String,
        val exactName: String,
        val bitmap: Bitmap,
    )

    private sealed interface PendingRecoveryAction {
        data object None : PendingRecoveryAction
        data class StartSession(
            val classId: String,
            val temporaryStudentIds: Set<String>,
        ) : PendingRecoveryAction
        data object EndSession : PendingRecoveryAction
    }

    private sealed interface PendingAdminUndo {
        data class RestoreMemberships(
            val classId: String,
            val className: String,
            val studentIds: Set<String>,
        ) : PendingAdminUndo

        data class RestoreStudentName(
            val studentId: String,
            val previousName: String,
        ) : PendingAdminUndo

        data class RestoreClass(
            val classId: String,
            val className: String,
            val studentIds: Set<String>,
        ) : PendingAdminUndo
    }

    companion object {
        private const val QR_SIZE_PIXELS = 720
        private const val SCAN_COOLDOWN_MS = 2_000L
        private const val QR_GUIDANCE_STALE_MS = 900L
        private const val QR_ACCEPTED_DISPLAY_MS = 2_000L
        private const val SCANNER_NOTICE_DURATION_MS = 3_000L
        private const val LOCK_TASK_EXIT_LIFECYCLE_GRACE_MS = 1_500L
        private const val LOCK_TASK_STATUS_REFRESH_MS = 250L
        private const val ADMIN_UNDO_WINDOW_MS = 30_000L
        private const val KEY_PENDING_RECOVERY_ACTION = "pending_recovery_action"
        private const val KEY_PENDING_WEB_SESSION_ID = "pending_web_session_id"
        private const val KEY_PENDING_RECOVERY_CLASS_ID = "pending_recovery_class_id"
        private const val KEY_PENDING_RECOVERY_TEMPORARY_STUDENT_IDS =
            "pending_recovery_temporary_student_ids"
        private const val PENDING_RECOVERY_NONE = "none"
        private const val PENDING_RECOVERY_START = "start"
        private const val PENDING_RECOVERY_END = "end"
        private const val FEEDBACK_PREFERENCES = "operator_feedback"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val STUDENT_UI_PREFERENCES = "student_ui"
        private const val KEY_KEYPAD_PRESET = "keypad_preset"
        private const val KEYPAD_PRESET_RIGHT = "right"
        private const val KEYPAD_PRESET_LEFT = "left"
        private const val KEYPAD_PRESET_CENTER = "center"
        private const val WEB_PACKAGE = "com.local.matholickiosk.webpoc"
        private const val WEB_REMOTE_SUPPORT_RECEIVER =
            "com.local.matholickiosk.webpoc.KioskRemoteSupportReceiver"
        private const val ACTION_SET_WEB_REMOTE_SUPPORT =
            "com.local.matholickiosk.action.SET_WEB_REMOTE_SUPPORT"
        private const val EXTRA_REMOTE_SUPPORT_ENABLED = "enabled"
        private const val EXTRA_REMOTE_SUPPORT_DURATION_SECONDS = "duration_seconds"
    }
}
