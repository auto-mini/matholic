package com.local.matholickiosk.kiosk

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.Editable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
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
import com.local.matholickiosk.kiosk.admin.KioskLockTaskController
import com.local.matholickiosk.kiosk.bridge.CredentialBridgeContract
import com.local.matholickiosk.kiosk.bridge.OneTimeCredentialBroker
import com.local.matholickiosk.kiosk.data.ActiveSessionEntity
import com.local.matholickiosk.kiosk.data.AdminAuthRepository
import com.local.matholickiosk.kiosk.data.AdminAuthResult
import com.local.matholickiosk.kiosk.data.KioskDatabase
import com.local.matholickiosk.kiosk.data.StudentRepository
import com.local.matholickiosk.kiosk.data.ValidatedStudent
import com.local.matholickiosk.kiosk.domain.CameraFacing
import com.local.matholickiosk.kiosk.domain.CameraFacingPolicy
import com.local.matholickiosk.kiosk.domain.ClassRosterSelectionState
import com.local.matholickiosk.kiosk.domain.DedicatedDevicePolicy
import com.local.matholickiosk.kiosk.domain.KioskState
import com.local.matholickiosk.kiosk.domain.SensitiveTask
import com.local.matholickiosk.kiosk.domain.SingleFlightGate
import com.local.matholickiosk.kiosk.print.QrPdfExporter
import com.local.matholickiosk.kiosk.print.QrPdfShareIntentFactory
import com.local.matholickiosk.kiosk.print.QrPrintDocumentAdapter
import com.local.matholickiosk.kiosk.qr.QrFrameDecision
import com.local.matholickiosk.kiosk.qr.QrFrameRejection
import com.local.matholickiosk.kiosk.qr.QrImageAnalyzer
import com.local.matholickiosk.kiosk.qr.QrImageRenderer
import com.local.matholickiosk.kiosk.qr.clearSensitiveData
import com.local.matholickiosk.kiosk.security.AndroidKeystoreCredentialCipher
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
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
    private lateinit var classSpinner: Spinner
    private lateinit var classRosterText: TextView
    private lateinit var manageClassMembersButton: Button
    private lateinit var deleteClassButton: Button
    private lateinit var studentSpinner: Spinner
    private lateinit var registerStudentButton: Button
    private lateinit var reissueQrButton: Button
    private lateinit var updateProfileButton: Button
    private lateinit var updateCredentialsButton: Button
    private lateinit var deactivateStudentButton: Button
    private lateinit var addTemporaryButton: Button
    private lateinit var startSessionButton: Button
    private lateinit var resumeSessionButton: Button
    private lateinit var adminMessage: TextView
    private lateinit var qrCardName: TextView
    private lateinit var qrImage: ImageView
    private lateinit var printQrButton: Button
    private lateinit var exportQrPdfButton: Button
    private lateinit var scannerPanel: FrameLayout
    private lateinit var scannerMessage: TextView
    private lateinit var switchCameraButton: Button

    private val mainHandler = Handler(Looper.getMainLooper())
    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var database: KioskDatabase
    private lateinit var authRepository: AdminAuthRepository
    private lateinit var studentRepository: StudentRepository
    private lateinit var lockTaskController: KioskLockTaskController

    private var authEnrollmentMode = false
    private var authBusy = false
    private var classes: List<Choice> = emptyList()
    private var students: List<StudentChoice> = emptyList()
    private val classRosterState = ClassRosterSelectionState()
    private val webRecoveryGate = SingleFlightGate()
    private val studentMutationGate = SingleFlightGate()
    private var issuedQrPreview: QrPreview? = null
    private var currentSession: ActiveSessionEntity? = null
    private var pendingTemporaryStudentIds: Set<String> = emptySet()
    private var suppressClassSelectionCallback = false
    private var cameraProvider: ProcessCameraProvider? = null
    private var qrAnalyzer: QrImageAnalyzer? = null
    private var scannerVisible = false
    private var preferredCameraFacing = CameraFacing.FRONT
    private var activeCameraFacing = CameraFacing.FRONT
    private var cameraBindGeneration = 0
    private var destroyed = false
    private var pendingCredentialBridgeId: String? = null
    private var relockAdminOnStart = false
    private var suppressNextAdminStopRelock = false
    private var dedicatedDevicePolicyFailed = false
    private var pendingRecoveryAction: PendingRecoveryAction = PendingRecoveryAction.None
    private var pendingSharedPdf: File? = null

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
        pendingCredentialBridgeId?.let(OneTimeCredentialBroker::revoke)
        pendingCredentialBridgeId = null
        val failureReason = result.data
            ?.getStringExtra(CredentialBridgeContract.EXTRA_FAILURE_REASON)
            ?.take(80)
            ?: "WEB_SESSION_FAILED"
        ioExecutor.execute {
            val outcome = runCatching {
                if (result.resultCode == Activity.RESULT_OK) {
                    studentRepository.transitionSession(
                        expectedState = KioskState.PRELOGIN_CHECK,
                        state = KioskState.QR_READY,
                    )
                    true
                } else {
                    studentRepository.transitionSession(
                        expectedState = KioskState.PRELOGIN_CHECK,
                        state = KioskState.LOCKED,
                        lockedReason = failureReason,
                    )
                    false
                }
            }
            val session = runCatching { studentRepository.currentSession() }.getOrNull()
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                currentSession = session
                outcome.fold(
                    onSuccess = { passed ->
                        if (passed) {
                            showScanner()
                        } else {
                            statusText.text = KioskState.LOCKED.name
                            showAuthentication(enrollment = false)
                            authError.text = "채점기가 잠겼습니다 · $failureReason"
                        }
                    },
                    onFailure = {
                        statusText.text = KioskState.LOCKED.name
                        showAuthentication(enrollment = false)
                        authError.text = "세션 결과 저장 실패"
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
                    webRecoveryGate.finish()
                    refreshAdminData("Web 로그인 상태를 안전하게 정리했습니다.")
                }
                is PendingRecoveryAction.StartSession ->
                    completeSessionStart(requestedAction)
                PendingRecoveryAction.EndSession ->
                    completeSessionEnd()
            }
        } else {
            webRecoveryGate.finish()
            val message = "Web 세션 정리에 실패해 수업 상태를 변경하지 않았습니다" +
                (failureReason?.let { " · $it" } ?: "")
            refreshAdminData(message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
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
        QrPdfExporter.cleanupExpired(this)
        configureActions()
        loadInitialState()
    }

    private fun bindViews() {
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
        classSpinner = findViewById(R.id.class_spinner)
        classRosterText = findViewById(R.id.class_roster_text)
        manageClassMembersButton = findViewById(R.id.manage_class_members_button)
        deleteClassButton = findViewById(R.id.delete_class_button)
        studentSpinner = findViewById(R.id.student_spinner)
        registerStudentButton = findViewById(R.id.register_student_button)
        reissueQrButton = findViewById(R.id.reissue_qr_button)
        updateProfileButton = findViewById(R.id.update_profile_button)
        updateCredentialsButton = findViewById(R.id.update_credentials_button)
        deactivateStudentButton = findViewById(R.id.deactivate_student_button)
        addTemporaryButton = findViewById(R.id.add_temporary_button)
        startSessionButton = findViewById(R.id.start_session_button)
        resumeSessionButton = findViewById(R.id.resume_session_button)
        adminMessage = findViewById(R.id.admin_message)
        qrCardName = findViewById(R.id.qr_card_name)
        qrImage = findViewById(R.id.qr_image)
        printQrButton = findViewById(R.id.print_qr_button)
        exportQrPdfButton = findViewById(R.id.export_qr_pdf_button)
        scannerPanel = findViewById(R.id.scanner_panel)
        scannerMessage = findViewById(R.id.scanner_message)
        switchCameraButton = findViewById(R.id.switch_camera_button)
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
            manageClassMembersButton,
            deleteClassButton,
            reissueQrButton,
            updateProfileButton,
            updateCredentialsButton,
            deactivateStudentButton,
            printQrButton,
            exportQrPdfButton,
            addTemporaryButton,
            startSessionButton,
            resumeSessionButton,
            switchCameraButton,
            findViewById<Button>(R.id.session_admin_button),
        ).forEach { it.filterTouchesWhenObscured = true }
        pinConfirmInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitAuthentication()
                true
            } else {
                false
            }
        }
    }

    private fun configureActions() {
        authSubmit.setOnClickListener { submitAuthentication() }
        findViewById<Button>(R.id.create_class_button).setOnClickListener { createClass() }
        registerStudentButton.setOnClickListener { showRegisterStudentDialog() }
        manageClassMembersButton.setOnClickListener { showClassMembershipDialog() }
        deleteClassButton.setOnClickListener { confirmDeleteClass() }
        reissueQrButton.setOnClickListener { confirmReissueQr() }
        updateProfileButton.setOnClickListener { showUpdateStudentNameDialog() }
        updateCredentialsButton.setOnClickListener { showUpdateCredentialsDialog() }
        deactivateStudentButton.setOnClickListener { confirmDeactivateStudent() }
        printQrButton.setOnClickListener { confirmQrPrint() }
        exportQrPdfButton.setOnClickListener { confirmQrPdfExport() }
        addTemporaryButton.setOnClickListener { showTemporaryStudentDialog() }
        startSessionButton.setOnClickListener { startOrEndSession() }
        resumeSessionButton.setOnClickListener { showScanner() }
        switchCameraButton.setOnClickListener { switchCamera() }
        findViewById<Button>(R.id.session_admin_button).setOnClickListener {
            requestSessionAdminAuthentication()
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
    }

    private fun configureBackNavigation() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (
                        ::adminPanel.isInitialized &&
                        adminPanel.visibility == View.VISIBLE
                    ) {
                        showAuthentication(enrollment = false)
                    }
                }
            },
        )
    }

    private fun loadInitialState() {
        statusText.text = "초기 상태 확인 중"
        ioExecutor.execute {
            val enrolled = authRepository.isEnrolled()
            val recoveredState = studentRepository.applyRestartPolicy()
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                statusText.text = recoveredState.name
                showAuthentication(enrollment = !enrolled)
            }
        }
    }

    private fun showAuthentication(enrollment: Boolean) {
        stopCamera()
        authEnrollmentMode = enrollment
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
        pinConfirmInput.visibility = if (enrollment) View.VISIBLE else View.GONE
        authSubmit.text = if (enrollment) "설정" else "인증"
        authError.text = ""
        pinInput.text.clear()
        pinConfirmInput.text.clear()
        pinInput.requestFocus()
        enterDedicatedMode()
    }

    private fun submitAuthentication() {
        if (authBusy) return
        val pin = pinInput.text.toSensitiveCharArray()
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
                    onSuccess = { handleAuthResult(it) },
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

    private fun showAdmin() {
        stopCamera()
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
        refreshAdminData()
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
        dedicatedDevicePolicyFailed = dedicatedDevicePolicyFailed || entered.isFailure
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
            "전용기기 정책 오류"
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
        preferredStudentId: String? = students.getOrNull(studentSpinner.selectedItemPosition)?.id,
        completeStudentMutationAfterLoad: Boolean = false,
    ) {
        ioExecutor.execute {
            val loadedClasses = studentRepository.listClasses()
                .map { Choice(it.classId, it.className) }
            val loadedStudents = studentRepository.listStudents()
                .map {
                    StudentChoice(
                        id = it.studentId,
                        label = it.displayNameExact,
                    )
                }
            val session = studentRepository.currentSession()
            val resolvedClassId = session?.classId
                ?: preferredClassId?.takeIf { candidate ->
                    loadedClasses.any { it.id == candidate }
                }
                ?: loadedClasses.firstOrNull()?.id
            val loadedMembershipIds = resolvedClassId
                ?.let(studentRepository::membershipStudentIds)
                .orEmpty()
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                classes = loadedClasses
                students = loadedStudents
                currentSession = session
                classRosterState.resolve(resolvedClassId, loadedMembershipIds)
                pendingTemporaryStudentIds = pendingTemporaryStudentIds
                    .intersect(students.mapTo(mutableSetOf(), StudentChoice::id))
                suppressClassSelectionCallback = true
                classSpinner.adapter = choiceAdapter(classes, "먼저 반을 생성하세요")
                studentSpinner.adapter = studentChoiceAdapter(students, "등록 학생이 없습니다")
                classes.indexOfFirst { it.id == resolvedClassId }
                    .takeIf { it >= 0 }
                    ?.let(classSpinner::setSelection)
                students.indexOfFirst { it.id == preferredStudentId }
                    .takeIf { it >= 0 }
                    ?.let(studentSpinner::setSelection)
                suppressClassSelectionCallback = false
                if (completeStudentMutationAfterLoad) studentMutationGate.finish()
                updateSessionAdminControls(session)
                updateStudentManagementControls()
                updateClassRosterUi()
                adminMessage.text = message.orEmpty()
            }
        }
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
            choices.map(StudentChoice::label).ifEmpty { listOf(emptyLabel) },
        )

    private fun createClass() {
        val name = classNameInput.text.toString().trim()
        if (name.isEmpty()) {
            adminMessage.text = "반 이름을 입력하세요."
            return
        }
        adminMessage.text = "반 생성 중"
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
                        )
                    },
                    onFailure = { adminMessage.text = it.message ?: "반 생성 실패" },
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
        adminMessage.text = "반 삭제 중"
        ioExecutor.execute {
            val result = runCatching { studentRepository.deleteClass(selected.id) }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        pendingTemporaryStudentIds = emptySet()
                        refreshAdminData("${selected.label} 반을 삭제했습니다.")
                    },
                    onFailure = { adminMessage.text = it.message ?: "반 삭제 실패" },
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
        adminMessage.text = "반 학생 구성 저장 중"
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
                        adminMessage.text = "${selectedClass.label} 반 학생 ${studentIds.size}명을 저장했습니다."
                    },
                    onFailure = { adminMessage.text = it.message ?: "반 학생 구성 저장 실패" },
                )
            }
        }
    }

    private fun refreshSelectedClassDetails(
        request: ClassRosterSelectionState.LoadRequest,
    ) {
        ioExecutor.execute {
            val membershipIds = runCatching {
                studentRepository.membershipStudentIds(request.classId)
            }.getOrDefault(emptySet())
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                if (!classRosterState.apply(request, membershipIds)) {
                    return@runOnUiThread
                }
                pendingTemporaryStudentIds -= membershipIds
                updateClassRosterUi()
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
            memberNames.isEmpty() -> "소속 학생 없음"
            else -> "소속 ${memberNames.size}명 · ${memberNames.joinToString(", ")}"
        }
        val activeClassId = currentSession?.classId
        val classAvailable = selectedClass != null
        val classReady = classAvailable && !classRosterState.isLoading
        manageClassMembersButton.isEnabled = classReady && currentSession?.sessionId == null
        deleteClassButton.isEnabled = classReady &&
            (currentSession?.sessionId == null || activeClassId != selectedClass?.id)
        addTemporaryButton.isEnabled = classReady && students.any {
            it.id !in classRosterState.membershipStudentIds
        }
        startSessionButton.isEnabled = !webRecoveryGate.isActive &&
            (currentSession?.sessionId != null || classReady)
        resumeSessionButton.isEnabled = !webRecoveryGate.isActive
        val pendingCount = pendingTemporaryStudentIds.size
        addTemporaryButton.text = if (currentSession?.sessionId == null) {
            "이번 수업 보강 학생 선택" + if (pendingCount > 0) " (${pendingCount}명)" else ""
        } else {
            "현재 수업 보강 학생 추가"
        }
    }

    private fun showRegisterStudentDialog() {
        val nameInput = dialogTextInput(
            hint = "학생 전체 이름",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
        )
        val usernameInput = dialogTextInput(
            hint = "매쓰홀릭 아이디",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            sensitive = true,
        )
        val passwordInput = dialogTextInput(
            hint = "매쓰홀릭 비밀번호",
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
        dialog.show()
    }

    private fun registerStudent(
        exactName: String,
        username: CharArray,
        password: CharArray,
    ) {
        if (!beginStudentMutation("학생 정보 암호화 등록 중")) {
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
                            completeStudentMutationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishStudentMutation()
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
        if (!beginStudentMutation("기존 QR 폐기 및 재발급 중")) return
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
                        finishStudentMutation()
                        showQrPreview(preview)
                        adminMessage.text = "기존 QR을 폐기하고 새 QR을 발급했습니다."
                    },
                    onFailure = {
                        finishStudentMutation()
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
        dialog.show()
    }

    private fun updateStudentName(selected: StudentChoice, exactName: String) {
        if (!beginStudentMutation("학생 표시명 수정 중")) return
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
                        refreshAdminData(
                            message = "학생 표시명을 수정했습니다. 이름이 적힌 카드는 QR을 재발급해 다시 인쇄하세요.",
                            preferredStudentId = selected.id,
                            completeStudentMutationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishStudentMutation()
                        adminMessage.text = it.message ?: "학생 표시명 수정 실패"
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
            hint = "새 매쓰홀릭 아이디",
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            sensitive = true,
        )
        val passwordInput = dialogTextInput(
            hint = "새 매쓰홀릭 비밀번호",
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
        dialog.show()
    }

    private fun updateStudentCredentials(
        selected: StudentChoice,
        username: CharArray,
        password: CharArray,
    ) {
        if (!beginStudentMutation("학생 계정정보 재암호화 중")) {
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
                            completeStudentMutationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishStudentMutation()
                        username.fill('\u0000')
                        password.fill('\u0000')
                        adminMessage.text = it.message ?: "학생 계정정보 갱신 실패"
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
        if (!beginStudentMutation("학생 비활성화 및 QR 폐기 중")) return
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
                            completeStudentMutationAfterLoad = true,
                        )
                    },
                    onFailure = {
                        finishStudentMutation()
                        adminMessage.text = it.message ?: "학생 비활성화 실패"
                    },
                )
            }
        }
    }

    private fun beginStudentMutation(message: String): Boolean {
        if (!studentMutationGate.tryStart()) {
            adminMessage.text = "다른 학생 정보 변경을 처리하고 있습니다."
            return false
        }
        adminMessage.text = message
        updateStudentManagementControls()
        return true
    }

    private fun finishStudentMutation() {
        studentMutationGate.finish()
        updateStudentManagementControls()
    }

    private fun updateStudentManagementControls() {
        val available = !studentMutationGate.isActive
        val hasStudents = students.isNotEmpty()
        studentSpinner.isEnabled = available && hasStudents
        registerStudentButton.isEnabled = available
        reissueQrButton.isEnabled = available && hasStudents
        updateProfileButton.isEnabled = available && hasStudents
        updateCredentialsButton.isEnabled = available && hasStudents
        deactivateStudentButton.isEnabled = available && hasStudents
    }

    private fun confirmQrPrint() {
        val preview = issuedQrPreview
        if (preview == null || preview.bitmap.isRecycled) {
            adminMessage.text = "먼저 QR을 발급하거나 재발급하세요."
            printQrButton.isEnabled = false
            return
        }
        AlertDialog.Builder(this)
            .setTitle("현재 표시 QR 인쇄")
            .setMessage(
                "QR 토큰이 Android 인쇄 서비스와 선택한 프린터로 전달됩니다.\n" +
                    "신뢰하는 로컬 프린터만 선택하고 인쇄 대기열의 작업도 확인하세요.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("인쇄 화면 열기") { _, _ -> prepareQrPrint(preview) }
            .show()
    }

    private fun prepareQrPrint(preview: QrPreview) {
        adminMessage.text = "QR 인쇄 요청 기록 중"
        ioExecutor.execute {
            val audited = runCatching {
                studentRepository.recordQrPrintRequested(preview.studentId)
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                audited.fold(
                    onSuccess = {
                        if (
                            issuedQrPreview !== preview ||
                            preview.bitmap.isRecycled
                        ) {
                            adminMessage.text = "QR 미리보기가 만료되었습니다. 다시 발급하세요."
                            return@fold
                        }
                        val printable = runCatching {
                            requireNotNull(
                                preview.bitmap.copy(Bitmap.Config.ARGB_8888, true),
                            )
                        }.getOrElse {
                            adminMessage.text = "인쇄용 QR 복사 실패"
                            return@fold
                        }
                        val adapter = QrPrintDocumentAdapter(
                            context = this,
                            displayName = preview.exactName,
                            qrBitmap = printable,
                        )
                        val attributes = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
                            .setResolution(
                                PrintAttributes.Resolution("print", "print", 300, 300),
                            )
                            .setMinMargins(PrintAttributes.Margins(500, 500, 500, 500))
                            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                            .build()
                        runCatching {
                            getSystemService(PrintManager::class.java).print(
                                "매쓰홀릭 QR 카드",
                                adapter,
                                attributes,
                            )
                        }.onSuccess {
                            clearQrPreview(
                                "QR을 인쇄 서비스로 전달해 화면 표시를 지웠습니다",
                            )
                        }.onFailure {
                            if (!printable.isRecycled) {
                                printable.eraseColor(android.graphics.Color.WHITE)
                                printable.recycle()
                            }
                            adminMessage.text = "Android 인쇄 화면을 열지 못했습니다."
                        }
                    },
                    onFailure = { adminMessage.text = it.message ?: "QR 인쇄 감사기록 실패" },
                )
            }
        }
    }

    private fun confirmQrPdfExport() {
        val preview = issuedQrPreview
        if (preview == null || preview.bitmap.isRecycled) {
            adminMessage.text = "먼저 QR을 발급하거나 재발급하세요."
            exportQrPdfButton.isEnabled = false
            return
        }
        AlertDialog.Builder(this)
            .setTitle("QR 카드 PDF 공유·저장")
            .setMessage(
                "65×90mm 세로 카드에 30×30mm QR과 학생 전체 이름을 넣습니다.\n" +
                    "PDF에는 로그인 가능한 QR이 포함되므로 신뢰하는 PC나 저장 위치만 선택하세요.",
            )
            .setNegativeButton("취소", null)
            .setPositiveButton("PDF 만들기") { _, _ -> prepareQrPdfExport(preview) }
            .show()
    }

    private fun prepareQrPdfExport(preview: QrPreview) {
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
        adminMessage.text = "카드 크기 PDF 생성 중"
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
                    onSuccess = { file -> shareQrPdf(file, preview) },
                    onFailure = {
                        adminMessage.text = it.message ?: "QR 카드 PDF 생성 실패"
                    },
                )
            }
        }
    }

    private fun shareQrPdf(file: File, preview: QrPreview) {
        val uri = FileProvider.getUriForFile(this, "$packageName.files", file)
        val share = QrPdfShareIntentFactory.create(uri, preview.exactName)
        pendingSharedPdf = file
        suppressNextAdminStopRelock = true
        runCatching {
            startActivity(Intent.createChooser(share, "PDF를 PC로 보내거나 저장"))
        }.onSuccess {
            clearQrPreview("QR 카드 PDF를 전달해 화면 표시를 지웠습니다")
        }.onFailure {
            suppressNextAdminStopRelock = false
            pendingSharedPdf = null
            file.delete()
            adminMessage.text = "PDF 공유 화면을 열지 못했습니다."
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
        adminMessage.text = "현재 수업 보강 학생 추가 중"
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.addTemporaryStudents(
                    requireNotNull(session.sessionId),
                    studentIds,
                )
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                adminMessage.text = result.fold(
                    onSuccess = { "현재 수업에 보강 학생 ${studentIds.size}명을 추가했습니다." },
                    onFailure = { it.message ?: "보강 학생 추가 실패" },
                )
            }
        }
    }

    private fun launchWebSessionRecovery(action: PendingRecoveryAction) {
        if (!webRecoveryGate.tryStart()) {
            adminMessage.text = "Web 로그인 상태를 이미 안전하게 정리하고 있습니다."
            return
        }
        pendingRecoveryAction = action
        adminMessage.text = when (action) {
            PendingRecoveryAction.None -> "Web 로그인 상태 안전 정리 중"
            is PendingRecoveryAction.StartSession -> "Web 상태 확인 후 수업 시작 준비 중"
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
                webRecoveryGate.finish()
                updateSessionAdminControls(currentSession)
                adminMessage.text = "Web 세션 정리 화면을 열지 못했습니다."
            }
    }

    private fun startOrEndSession() {
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
        launchWebSessionRecovery(
            PendingRecoveryAction.StartSession(
                classId = selectedClass.id,
                temporaryStudentIds = pendingTemporaryStudentIds,
            ),
        )
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
                webRecoveryGate.finish()
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
                webRecoveryGate.finish()
                result.fold(
                    onSuccess = {
                        pendingTemporaryStudentIds = emptySet()
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
        startSessionButton.text = if (active) {
            "현재 수업 안전 종료"
        } else {
            "선택한 반 수업 안전 시작"
        }
        classSpinner.isEnabled = !active && !webRecoveryGate.isActive
        statusText.text = session?.state ?: KioskState.ADMIN_IDLE.name
        updateClassRosterUi()
        if (active && !resumable) {
            adminMessage.text = "재시작 또는 민감 상태 종료가 감지되어 ${session.state} 상태입니다. 기존 수업을 안전 종료하세요."
        }
    }

    private fun showQrPreview(preview: QrPreview) {
        clearQrPreview()
        issuedQrPreview = preview
        qrImage.setImageBitmap(preview.bitmap)
        qrCardName.text = preview.exactName
        printQrButton.isEnabled = true
        exportQrPdfButton.isEnabled = true
    }

    private fun clearQrPreview(
        cardMessage: String = "발급한 QR은 이 화면에 한 번만 표시됩니다",
    ) {
        qrImage.setImageDrawable(null)
        issuedQrPreview?.let(::wipeQrPreview)
        issuedQrPreview = null
        printQrButton.isEnabled = false
        exportQrPdfButton.isEnabled = false
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

    private fun showScanner() {
        val session = currentSession
        if (session?.sessionId == null || session.state != KioskState.QR_READY.name) {
            adminMessage.text = "QR 대기로 복귀할 수 있는 수업 상태가 아닙니다."
            return
        }
        clearQrPreview()
        authPanel.visibility = View.GONE
        adminPanel.visibility = View.GONE
        scannerPanel.visibility = View.VISIBLE
        scannerVisible = true
        scannerMessage.text = "QR카드를 카메라에 보여주세요"
        statusText.text = KioskState.QR_READY.name
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterDedicatedMode()
        ensureCamera()
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
            val analyzer = qrAnalyzer ?: QrImageAnalyzer(onDecision = ::handleQrDecision)
                .also { qrAnalyzer = it }
            analyzer.setEnabled(true)
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
        }, mainExecutor)
    }

    private fun switchCamera() {
        preferredCameraFacing = CameraFacingPolicy.opposite(activeCameraFacing)
        bindCamera()
    }

    private fun updateCameraSwitchLabel() {
        switchCameraButton.text = if (activeCameraFacing == CameraFacing.FRONT) {
            "후면으로"
        } else {
            "전면으로"
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
            qrAnalyzer?.setEnabled(false)
            when (decision) {
                QrFrameDecision.Ignore -> qrAnalyzer?.setEnabled(true)
                is QrFrameDecision.Reject -> {
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
                    ioExecutor.execute { studentRepository.recordQrRejection(reason) }
                    resumeScannerAfterCooldown()
                }
                is QrFrameDecision.Accept -> validateQr(decision.tokenHash)
            }
        }
    }

    private fun validateQr(tokenHash: ByteArray) {
        scannerMessage.text = "확인되었습니다"
        statusText.text = KioskState.QR_VALIDATING.name
        executeSensitive(
            cleanup = { tokenHash.fill(0) },
        ) {
            val result = runCatching { studentRepository.validateForActiveSession(tokenHash) }
            runOnUiThread {
                if (!scannerVisible || destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = { student ->
                        if (student == null) {
                            scannerMessage.text = "현재 수업에서 사용할 수 없는 카드입니다\n선생님에게 문의하세요"
                            resumeScannerAfterCooldown()
                        } else {
                            scannerMessage.text = "확인되었습니다\n로그인 중입니다"
                            launchSecureWebSession(student)
                        }
                    },
                    onFailure = {
                        scannerMessage.text = "채점기가 잠겼습니다\n선생님 확인이 필요합니다"
                        statusText.text = KioskState.LOCKED.name
                        resumeScannerAfterCooldown()
                    },
                )
            }
        }
    }

    private fun launchSecureWebSession(student: ValidatedStudent) {
        statusText.text = KioskState.PRELOGIN_CHECK.name
        ioExecutor.execute {
            val prepared = runCatching {
                studentRepository.transitionSession(
                    expectedState = KioskState.QR_READY,
                    state = KioskState.PRELOGIN_CHECK,
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
                            }
                            PreparedWebSessionDisposition.CANCEL_AND_RESTORE -> {
                                OneTimeCredentialBroker.revoke(handle.id)
                                restoreQrReadyAfterCancelledWebLaunch()
                            }
                            PreparedWebSessionDisposition.LAUNCH -> {
                                pendingCredentialBridgeId = handle.id
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
                                runCatching { webSessionLauncher.launch(intent) }
                                    .onFailure {
                                        OneTimeCredentialBroker.revoke(handle.id)
                                        pendingCredentialBridgeId = null
                                        lockAfterBridgeFailure("WEBPOC_NOT_AVAILABLE")
                                    }
                            }
                        }
                    },
                    onFailure = {
                        if (!destroyed) lockAfterBridgeFailure("CREDENTIAL_PREPARATION")
                    },
                )
            }
        }
    }

    private fun restoreQrReadyAfterCancelledWebLaunch() {
        ioExecutor.execute {
            val restored = runCatching {
                studentRepository.transitionSession(
                    expectedState = KioskState.PRELOGIN_CHECK,
                    state = KioskState.QR_READY,
                )
                studentRepository.currentSession()
            }.getOrNull()
            runOnUiThread {
                if (destroyed || restored == null) return@runOnUiThread
                currentSession = restored
                statusText.text = restored.state
            }
        }
    }

    private fun lockAfterBridgeFailure(reason: String) {
        ioExecutor.execute {
            runCatching {
                studentRepository.transitionSession(
                    expectedState = KioskState.PRELOGIN_CHECK,
                    state = KioskState.LOCKED,
                    lockedReason = reason,
                )
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                statusText.text = KioskState.LOCKED.name
                showAuthentication(enrollment = false)
                authError.text = "채점기가 잠겼습니다 · $reason"
            }
        }
    }

    private fun resumeScannerAfterCooldown() {
        mainHandler.postDelayed({
            if (!scannerVisible || destroyed) return@postDelayed
            scannerMessage.text = "QR카드를 카메라에 보여주세요"
            statusText.text = KioskState.QR_READY.name
            qrAnalyzer?.setEnabled(true)
        }, SCAN_COOLDOWN_MS)
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
        val dialog = AlertDialog.Builder(this)
            .setTitle("관리자 인증")
            .setView(input)
            .setNegativeButton("취소") { _, _ -> qrAnalyzer?.setEnabled(true) }
            .setPositiveButton("인증", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pin = input.text.toSensitiveCharArray()
                input.text.clear()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                executeSensitive(
                    cleanup = { pin.fill('\u0000') },
                ) {
                    val result = authRepository.authenticate(pin)
                    runOnUiThread {
                        if (destroyed) return@runOnUiThread
                        when (result) {
                            AdminAuthResult.Success -> {
                                dialog.dismiss()
                                showAdmin()
                            }
                            AdminAuthResult.NotEnrolled -> {
                                dialog.dismiss()
                                showAuthentication(enrollment = true)
                            }
                            is AdminAuthResult.Rejected -> {
                                val seconds = (result.retryAfterMillis + 999) / 1_000
                                input.error = "PIN 오류 · ${seconds}초 후 재시도"
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                            }
                        }
                    }
                }
            }
        }
        dialog.setOnDismissListener {
            if (scannerVisible) qrAnalyzer?.setEnabled(true)
        }
        dialog.show()
    }

    private fun stopCamera() {
        cameraBindGeneration += 1
        qrAnalyzer?.setEnabled(false)
        cameraProvider?.unbindAll()
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

    override fun onDestroy() {
        destroyed = true
        pendingCredentialBridgeId?.let(OneTimeCredentialBroker::revoke)
        pendingCredentialBridgeId = null
        mainHandler.removeCallbacksAndMessages(null)
        stopCamera()
        qrAnalyzer?.close()
        ioExecutor.shutdownNow()
            .filterIsInstance<SensitiveTask>()
            .forEach(SensitiveTask::discard)
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

    companion object {
        private const val QR_SIZE_PIXELS = 720
        private const val SCAN_COOLDOWN_MS = 2_000L
        private const val LOCK_TASK_EXIT_LIFECYCLE_GRACE_MS = 1_500L
        private const val LOCK_TASK_STATUS_REFRESH_MS = 250L
    }
}
