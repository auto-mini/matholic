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
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
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
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import com.local.matholickiosk.kiosk.domain.DedicatedDevicePolicy
import com.local.matholickiosk.kiosk.domain.KioskState
import com.local.matholickiosk.kiosk.print.QrPrintDocumentAdapter
import com.local.matholickiosk.kiosk.qr.QrFrameDecision
import com.local.matholickiosk.kiosk.qr.QrFrameRejection
import com.local.matholickiosk.kiosk.qr.QrImageAnalyzer
import com.local.matholickiosk.kiosk.qr.QrImageRenderer
import com.local.matholickiosk.kiosk.security.AndroidKeystoreCredentialCipher
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
    private lateinit var studentNameInput: EditText
    private lateinit var studentMaskedInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var studentSpinner: Spinner
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
    private lateinit var scannerPanel: FrameLayout
    private lateinit var cameraPreview: PreviewView
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
    private var issuedQrPreview: QrPreview? = null
    private var currentSession: ActiveSessionEntity? = null
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
                    studentRepository.transitionSession(KioskState.QR_READY)
                    true
                } else {
                    studentRepository.transitionSession(
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
        studentNameInput = findViewById(R.id.student_name_input)
        studentMaskedInput = findViewById(R.id.student_masked_input)
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        studentSpinner = findViewById(R.id.student_spinner)
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
        scannerPanel = findViewById(R.id.scanner_panel)
        cameraPreview = findViewById(R.id.camera_preview)
        scannerMessage = findViewById(R.id.scanner_message)
        switchCameraButton = findViewById(R.id.switch_camera_button)
    }

    private fun configureSensitiveViews() {
        listOf(pinInput, pinConfirmInput, usernameInput, passwordInput).forEach {
            it.isSaveEnabled = false
            it.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            it.filterTouchesWhenObscured = true
        }
        listOf(
            authSubmit,
            findViewById<Button>(R.id.create_class_button),
            findViewById<Button>(R.id.register_student_button),
            reissueQrButton,
            updateProfileButton,
            updateCredentialsButton,
            deactivateStudentButton,
            printQrButton,
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
        findViewById<Button>(R.id.register_student_button).setOnClickListener { registerStudent() }
        reissueQrButton.setOnClickListener { reissueQr() }
        updateProfileButton.setOnClickListener { updateStudentProfile() }
        updateCredentialsButton.setOnClickListener { updateStudentCredentials() }
        deactivateStudentButton.setOnClickListener { confirmDeactivateStudent() }
        printQrButton.setOnClickListener { confirmQrPrint() }
        addTemporaryButton.setOnClickListener { addTemporaryStudent() }
        startSessionButton.setOnClickListener { startOrEndSession() }
        resumeSessionButton.setOnClickListener { showScanner() }
        switchCameraButton.setOnClickListener { switchCamera() }
        findViewById<Button>(R.id.session_admin_button).setOnClickListener {
            requestSessionAdminAuthentication()
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
        ioExecutor.execute {
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
    ) {
        ioExecutor.execute {
            val loadedClasses = studentRepository.listClasses()
                .map { Choice(it.classId, it.className) }
            val loadedStudents = studentRepository.listStudents()
                .map {
                    StudentChoice(
                        id = it.studentId,
                        label = it.displayNameExact,
                        maskedLabel = it.displayNameMasked,
                    )
                }
            val session = studentRepository.currentSession()
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                classes = loadedClasses
                students = loadedStudents
                currentSession = session
                classSpinner.adapter = choiceAdapter(classes, "먼저 반을 생성하세요")
                studentSpinner.adapter = studentChoiceAdapter(students, "등록 학생이 없습니다")
                classes.indexOfFirst { it.id == preferredClassId }
                    .takeIf { it >= 0 }
                    ?.let(classSpinner::setSelection)
                students.indexOfFirst { it.id == preferredStudentId }
                    .takeIf { it >= 0 }
                    ?.let(studentSpinner::setSelection)
                updateSessionAdminControls(session)
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

    private fun registerStudent() {
        val selectedClass = classes.getOrNull(classSpinner.selectedItemPosition)
        if (selectedClass == null) {
            adminMessage.text = "학생을 등록할 반을 먼저 생성하세요."
            return
        }
        val exactName = studentNameInput.text.toString().trim()
        val maskedName = studentMaskedInput.text.toString().trim()
        val username = usernameInput.text.toSensitiveCharArray()
        val password = passwordInput.text.toSensitiveCharArray()
        usernameInput.text.clear()
        passwordInput.text.clear()
        if (exactName.isEmpty() || maskedName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            username.fill('\u0000')
            password.fill('\u0000')
            adminMessage.text = "학생 표시명, 마스킹명, 아이디와 비밀번호를 모두 입력하세요."
            return
        }
        adminMessage.text = "Keystore 암호화 등록 중"
        ioExecutor.execute {
            val result = runCatching {
                val registered = studentRepository.registerStudent(
                    selectedClass.id,
                    exactName,
                    maskedName,
                    username,
                    password,
                )
                QrPreview(
                    studentId = registered.studentId,
                    exactName = exactName,
                    maskedName = maskedName,
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
                        studentNameInput.text.clear()
                        studentMaskedInput.text.clear()
                        refreshAdminData(
                            message = "학생을 암호화 등록하고 QR을 발급했습니다.",
                            preferredClassId = selectedClass.id,
                            preferredStudentId = preview.studentId,
                        )
                    },
                    onFailure = {
                        username.fill('\u0000')
                        password.fill('\u0000')
                        adminMessage.text = it.message ?: "학생 등록 실패"
                    },
                )
            }
        }
    }

    private fun reissueQr() {
        val selected = students.getOrNull(studentSpinner.selectedItemPosition)
        if (selected == null) {
            adminMessage.text = "학생을 선택하세요."
            return
        }
        adminMessage.text = "기존 QR 폐기 및 재발급 중"
        ioExecutor.execute {
            val result = runCatching {
                val issued = studentRepository.reissueQr(selected.id)
                QrPreview(
                    studentId = selected.id,
                    exactName = selected.label,
                    maskedName = selected.maskedLabel,
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
                        showQrPreview(preview)
                        adminMessage.text = "기존 QR을 폐기하고 새 QR을 발급했습니다."
                    },
                    onFailure = { adminMessage.text = it.message ?: "QR 재발급 실패" },
                )
            }
        }
    }

    private fun updateStudentProfile() {
        val selected = students.getOrNull(studentSpinner.selectedItemPosition)
        if (selected == null) {
            adminMessage.text = "학생을 선택하세요."
            return
        }
        val exactName = studentNameInput.text.toString().trim()
        val maskedName = studentMaskedInput.text.toString().trim()
        if (exactName.isEmpty() || maskedName.isEmpty()) {
            adminMessage.text = "변경할 정확한 표시명과 마스킹명을 모두 입력하세요."
            return
        }
        adminMessage.text = "학생 표시명 수정 중"
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.updateStudentProfile(
                    selected.id,
                    exactName,
                    maskedName,
                )
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        studentNameInput.text.clear()
                        studentMaskedInput.text.clear()
                        if (issuedQrPreview?.studentId == selected.id) clearQrPreview()
                        refreshAdminData(
                            message = "학생 표시명을 수정했습니다. 이름이 적힌 카드는 QR을 재발급해 다시 인쇄하세요.",
                            preferredStudentId = selected.id,
                        )
                    },
                    onFailure = { adminMessage.text = it.message ?: "학생 표시명 수정 실패" },
                )
            }
        }
    }

    private fun updateStudentCredentials() {
        val selected = students.getOrNull(studentSpinner.selectedItemPosition)
        if (selected == null) {
            adminMessage.text = "학생을 선택하세요."
            return
        }
        val username = usernameInput.text.toSensitiveCharArray()
        val password = passwordInput.text.toSensitiveCharArray()
        usernameInput.text.clear()
        passwordInput.text.clear()
        if (username.isEmpty() || password.isEmpty()) {
            username.fill('\u0000')
            password.fill('\u0000')
            adminMessage.text = "새 매쓰홀릭 아이디와 비밀번호를 모두 입력하세요."
            return
        }
        adminMessage.text = "학생 계정정보 재암호화 중"
        ioExecutor.execute {
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
                        )
                    },
                    onFailure = {
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
        adminMessage.text = "학생 비활성화 및 QR 폐기 중"
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
                        )
                    },
                    onFailure = { adminMessage.text = it.message ?: "학생 비활성화 실패" },
                )
            }
        }
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
                            maskedDisplayName = preview.maskedName,
                            qrBitmap = printable,
                        )
                        val attributes = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
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

    private fun addTemporaryStudent() {
        val session = currentSession
        val selected = students.getOrNull(studentSpinner.selectedItemPosition)
        if (session?.sessionId == null || selected == null) {
            adminMessage.text = "활성 수업과 보강 학생을 확인하세요."
            return
        }
        ioExecutor.execute {
            val result = runCatching {
                studentRepository.addTemporaryStudent(session.sessionId, selected.id)
            }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                adminMessage.text = result.fold(
                    onSuccess = { "${selected.label} 학생을 이번 수업에만 추가했습니다." },
                    onFailure = { it.message ?: "보강 학생 추가 실패" },
                )
            }
        }
    }

    private fun startOrEndSession() {
        val active = currentSession?.sessionId != null
        if (active) {
            ioExecutor.execute {
                val result = runCatching { studentRepository.endSession() }
                runOnUiThread {
                    if (destroyed) return@runOnUiThread
                    result.fold(
                        onSuccess = { refreshAdminData("현재 수업을 종료했습니다.") },
                        onFailure = { adminMessage.text = it.message ?: "수업 종료 실패" },
                    )
                }
            }
            return
        }
        val selectedClass = classes.getOrNull(classSpinner.selectedItemPosition)
        if (selectedClass == null) {
            adminMessage.text = "수업 반을 선택하세요."
            return
        }
        ioExecutor.execute {
            val result = runCatching { studentRepository.startSession(selectedClass.id) }
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                result.fold(
                    onSuccess = {
                        currentSession = it
                        showScanner()
                    },
                    onFailure = { adminMessage.text = it.message ?: "수업 시작 실패" },
                )
            }
        }
    }

    private fun updateSessionAdminControls(session: ActiveSessionEntity?) {
        val active = session?.sessionId != null
        val resumable = active && session.state == KioskState.QR_READY.name
        addTemporaryButton.visibility = if (resumable) View.VISIBLE else View.GONE
        resumeSessionButton.visibility = if (resumable) View.VISIBLE else View.GONE
        startSessionButton.text = if (active) "현재 수업 안전 종료" else "선택한 반 수업 시작"
        statusText.text = session?.state ?: KioskState.ADMIN_IDLE.name
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
    }

    private fun clearQrPreview(
        cardMessage: String = "발급한 QR은 이 화면에 한 번만 표시됩니다",
    ) {
        qrImage.setImageDrawable(null)
        issuedQrPreview?.let(::wipeQrPreview)
        issuedQrPreview = null
        printQrButton.isEnabled = false
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
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = cameraPreview.surfaceProvider
            }
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
                preview,
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
            if (!scannerVisible || destroyed) return@runOnUiThread
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
        ioExecutor.execute {
            val result = runCatching { studentRepository.validateForActiveSession(tokenHash) }
            tokenHash.fill(0)
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
                if (destroyed) {
                    prepared.getOrNull()?.let { OneTimeCredentialBroker.revoke(it.id) }
                    return@runOnUiThread
                }
                prepared.fold(
                    onSuccess = { handle ->
                        pendingCredentialBridgeId = handle.id
                        scannerVisible = false
                        stopCamera()
                        val intent = Intent(CredentialBridgeContract.ACTION_START_SECURE_SESSION)
                            .setComponent(
                                ComponentName(
                                    CredentialBridgeContract.TRUSTED_CONSUMER_PACKAGE,
                                    "com.local.matholickiosk.webpoc.MainActivity",
                                ),
                            )
                            .setData(handle.uri)
                        runCatching { webSessionLauncher.launch(intent) }
                            .onFailure {
                                OneTimeCredentialBroker.revoke(handle.id)
                                pendingCredentialBridgeId = null
                                lockAfterBridgeFailure("WEBPOC_NOT_AVAILABLE")
                            }
                    },
                    onFailure = { lockAfterBridgeFailure("CREDENTIAL_PREPARATION") },
                )
            }
        }
    }

    private fun lockAfterBridgeFailure(reason: String) {
        ioExecutor.execute {
            runCatching {
                studentRepository.transitionSession(
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
                ioExecutor.execute {
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
        usernameInput.text.clear()
        passwordInput.text.clear()
        pinInput.text.clear()
        pinConfirmInput.text.clear()
    }

    override fun onStart() {
        super.onStart()
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
        super.onDestroy()
    }

    private fun applicationVersion(): String = runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
    }.getOrDefault("unknown")

    private fun Editable.toSensitiveCharArray(): CharArray =
        CharArray(length) { index -> this[index] }

    private data class Choice(val id: String, val label: String)

    private data class StudentChoice(
        val id: String,
        val label: String,
        val maskedLabel: String,
    )

    private data class QrPreview(
        val studentId: String,
        val exactName: String,
        val maskedName: String,
        val bitmap: Bitmap,
    )

    companion object {
        private const val QR_SIZE_PIXELS = 720
        private const val SCAN_COOLDOWN_MS = 2_000L
        private const val LOCK_TASK_EXIT_LIFECYCLE_GRACE_MS = 1_500L
        private const val LOCK_TASK_STATUS_REFRESH_MS = 250L
    }
}
