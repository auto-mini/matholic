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
import com.local.matholickiosk.kiosk.domain.SingleFlightGate
import com.local.matholickiosk.kiosk.qr.QrFrameDecision
import com.local.matholickiosk.kiosk.qr.QrFrameRejection
import com.local.matholickiosk.kiosk.qr.QrImageAnalyzer
import com.local.matholickiosk.kiosk.security.AndroidKeystoreCredentialCipher
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {
    @Test
    fun scannerInstructionReferencesPhysicalLensWithoutScreenTarget() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val instruction = activity
                    .findViewById<android.widget.TextView>(R.id.scanner_lens_instruction)
                    .text
                    .toString()
                assertTrue(instruction.contains("카메라 렌즈"))
                assertFalse(instruction.contains("목표 영역"))
                assertFalse(instruction.contains("가운데"))
            }
        }
    }

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
            scenario.onActivity { activity ->
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.admin_panel).visibility)
                assertEquals(View.GONE, activity.findViewById<View>(R.id.auth_panel).visibility)
            }
        }
        database.clearAllTables()
    }

    @Test
    fun failedInitialStateLoadShowsRetryWithoutOfferingPinEnrollment() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.auth_panel).visibility == View.VISIBLE &&
                    activity.findViewById<android.widget.TextView>(R.id.auth_title)
                        .text
                        .toString() == "관리자 PIN 설정"
            }

            val repositoryField = MainActivity::class.java
                .getDeclaredField("authRepository")
                .apply { isAccessible = true }
            val loadMethod = MainActivity::class.java
                .getDeclaredMethod("loadInitialState")
                .apply { isAccessible = true }
            lateinit var originalRepository: AdminAuthRepository
            try {
                scenario.onActivity { activity ->
                    originalRepository = repositoryField.get(activity) as AdminAuthRepository
                    repositoryField.set(activity, null)
                    loadMethod.invoke(activity)
                }

                waitUntil(scenario) { activity ->
                    activity.findViewById<android.widget.TextView>(R.id.status_text)
                        .text
                        .toString() == "INITIALIZATION_FAILED"
                }
                scenario.onActivity { activity ->
                    assertEquals(
                        "초기 상태 복구 실패",
                        activity.findViewById<android.widget.TextView>(R.id.auth_title)
                            .text
                            .toString(),
                    )
                    assertEquals(
                        View.GONE,
                        activity.findViewById<View>(R.id.pin_input).visibility,
                    )
                    assertEquals(
                        View.GONE,
                        activity.findViewById<View>(R.id.pin_confirm_input).visibility,
                    )
                    val retryButton = activity.findViewById<android.widget.Button>(R.id.auth_submit)
                    assertEquals("다시 시도", retryButton.text.toString())
                    assertTrue(retryButton.isEnabled)
                    assertEquals(View.GONE, activity.findViewById<View>(R.id.admin_panel).visibility)
                    assertEquals(
                        View.GONE,
                        activity.findViewById<View>(R.id.scanner_panel).visibility,
                    )
                }
                scenario.onActivity { activity ->
                    repositoryField.set(activity, originalRepository)
                    activity.findViewById<View>(R.id.auth_submit).performClick()
                }
                waitUntil(scenario) { activity ->
                    activity.findViewById<android.widget.TextView>(R.id.auth_title)
                        .text
                        .toString() == "관리자 PIN 설정" &&
                        activity.findViewById<View>(R.id.pin_input).visibility == View.VISIBLE
                }
            } finally {
                scenario.onActivity { activity ->
                    repositoryField.set(activity, originalRepository)
                }
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

    @Test
    fun failedAdminRefreshReleasesStudentMutationAndShowsRetryGuidance() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())
        StudentRepository(
            database = database,
            cipher = AndroidKeystoreCredentialCipher(),
            appVersion = "instrumented-test",
        ).registerStudent(
            displayNameExact = "가상학생-새로고침",
            username = "synthetic-refresh-user".toCharArray(),
            password = "synthetic-refresh-password".toCharArray(),
        )

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

            val repositoryField = MainActivity::class.java
                .getDeclaredField("studentRepository")
                .apply { isAccessible = true }
            val mutationGateField = MainActivity::class.java
                .getDeclaredField("studentMutationGate")
                .apply { isAccessible = true }
            val refreshMethod = MainActivity::class.java
                .getDeclaredMethod(
                    "refreshAdminData",
                    String::class.java,
                    String::class.java,
                    String::class.java,
                    Boolean::class.javaPrimitiveType,
                )
                .apply { isAccessible = true }
            lateinit var originalRepository: StudentRepository
            lateinit var mutationGate: SingleFlightGate
            try {
                scenario.onActivity { activity ->
                    originalRepository = repositoryField.get(activity) as StudentRepository
                    mutationGate = mutationGateField.get(activity) as SingleFlightGate
                    assertTrue(mutationGate.tryStart())
                    repositoryField.set(activity, null)
                    refreshMethod.invoke(
                        activity,
                        "학생 변경은 저장됐습니다.",
                        null,
                        null,
                        true,
                    )
                }

                waitUntil(scenario, timeoutMillis = 3_000) {
                    !mutationGate.isActive
                }
                scenario.onActivity { activity ->
                    assertFalse(mutationGate.isActive)
                    assertEquals(
                        "학생 변경은 저장됐습니다. 다만 최신 목록을 불러오지 못했습니다. " +
                            "관리자 화면을 다시 열어 재시도하세요.",
                        activity.findViewById<android.widget.TextView>(R.id.admin_message)
                            .text
                            .toString(),
                    )
                    assertTrue(
                        activity.findViewById<View>(R.id.register_student_button).isEnabled,
                    )
                }
            } finally {
                scenario.onActivity { activity ->
                    repositoryField.set(activity, originalRepository)
                }
            }
        }
        database.clearAllTables()
    }

    @Test
    fun failedRosterRefreshIsNotRenderedAsEmptyAndDisablesClassActions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())
        val repository = StudentRepository(
            database = database,
            cipher = AndroidKeystoreCredentialCipher(),
            appVersion = "instrumented-test",
        )
        repository.createClass("가상반-소속-A")
        repository.createClass("가상반-소속-B")

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
                    activity.findViewById<android.widget.Spinner>(R.id.class_spinner).count == 2 &&
                    activity.findViewById<android.widget.TextView>(R.id.class_roster_text)
                        .text
                        .toString() != "소속 학생 불러오는 중"
            }

            val repositoryField = MainActivity::class.java
                .getDeclaredField("studentRepository")
                .apply { isAccessible = true }
            lateinit var originalRepository: StudentRepository
            try {
                scenario.onActivity { activity ->
                    originalRepository = repositoryField.get(activity) as StudentRepository
                    repositoryField.set(activity, null)
                    val spinner =
                        activity.findViewById<android.widget.Spinner>(R.id.class_spinner)
                    spinner.setSelection(if (spinner.selectedItemPosition == 0) 1 else 0)
                }

                waitUntil(scenario) { activity ->
                    activity.findViewById<android.widget.TextView>(R.id.class_roster_text)
                        .text
                        .toString() == "소속 학생을 불러오지 못했습니다."
                }
                scenario.onActivity { activity ->
                    assertFalse(
                        activity.findViewById<View>(R.id.manage_class_members_button).isEnabled,
                    )
                    assertFalse(activity.findViewById<View>(R.id.delete_class_button).isEnabled)
                    assertFalse(activity.findViewById<View>(R.id.add_temporary_button).isEnabled)
                    assertFalse(activity.findViewById<View>(R.id.start_session_button).isEnabled)
                    assertEquals(
                        "반 소속 학생을 불러오지 못했습니다. 관리자 화면을 다시 열어 재시도하세요.",
                        activity.findViewById<android.widget.TextView>(R.id.admin_message)
                            .text
                            .toString(),
                    )
                }
            } finally {
                scenario.onActivity { activity ->
                    repositoryField.set(activity, originalRepository)
                }
            }
        }
        database.clearAllTables()
    }

    @Test
    fun failedWebSessionResultPersistenceShowsClosedRecoveryUi() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.auth_panel).visibility == View.VISIBLE
            }

            val repositoryField = MainActivity::class.java
                .getDeclaredField("studentRepository")
                .apply { isAccessible = true }
            val persistMethod = MainActivity::class.java
                .getDeclaredMethod(
                    "persistWebSessionResult",
                    Boolean::class.javaPrimitiveType,
                    String::class.java,
                )
                .apply { isAccessible = true }
            lateinit var originalRepository: StudentRepository
            try {
                scenario.onActivity { activity ->
                    originalRepository = repositoryField.get(activity) as StudentRepository
                    repositoryField.set(activity, null)
                    persistMethod.invoke(activity, true, "synthetic-result")
                }

                waitUntil(scenario) { activity ->
                    activity.findViewById<android.widget.TextView>(R.id.status_text)
                        .text
                        .toString() == "LOCKED" &&
                        activity.findViewById<android.widget.TextView>(R.id.auth_error)
                            .text
                            .toString()
                            .contains("관리자 PIN으로 상태를 확인하세요")
                }
                scenario.onActivity { activity ->
                    assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.auth_panel).visibility)
                    assertEquals(View.GONE, activity.findViewById<View>(R.id.admin_panel).visibility)
                    assertEquals(View.GONE, activity.findViewById<View>(R.id.scanner_panel).visibility)
                }
            } finally {
                scenario.onActivity { activity ->
                    repositoryField.set(activity, originalRepository)
                }
            }
        }
        database.clearAllTables()
    }

    @Test
    fun failedPreparedWebSessionRestoreShowsClosedRecoveryUi() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.auth_panel).visibility == View.VISIBLE
            }

            val repositoryField = MainActivity::class.java
                .getDeclaredField("studentRepository")
                .apply { isAccessible = true }
            val restoreMethod = MainActivity::class.java
                .getDeclaredMethod("restoreQrReadyAfterCancelledWebLaunch")
                .apply { isAccessible = true }
            lateinit var originalRepository: StudentRepository
            try {
                scenario.onActivity { activity ->
                    originalRepository = repositoryField.get(activity) as StudentRepository
                    repositoryField.set(activity, null)
                    restoreMethod.invoke(activity)
                }

                waitUntil(scenario, timeoutMillis = 3_000) { activity ->
                    activity.findViewById<android.widget.TextView>(R.id.auth_error)
                        .text
                        .toString()
                        .contains("준비된 로그인 상태를 복구하지 못했습니다")
                }
                scenario.onActivity { activity ->
                    assertEquals(
                        "LOCKED",
                        activity.findViewById<android.widget.TextView>(R.id.status_text)
                            .text
                            .toString(),
                    )
                    assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.auth_panel).visibility)
                    assertEquals(View.GONE, activity.findViewById<View>(R.id.admin_panel).visibility)
                    assertEquals(View.GONE, activity.findViewById<View>(R.id.scanner_panel).visibility)
                }
            } finally {
                scenario.onActivity { activity ->
                    repositoryField.set(activity, originalRepository)
                }
            }
        }
        database.clearAllTables()
    }

    @Test
    fun failedQrValidationDoesNotResumeScannerAfterCooldown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())
        val repository = StudentRepository(
            database = database,
            cipher = AndroidKeystoreCredentialCipher(),
            appVersion = "instrumented-test",
        )
        val classId = repository.createClass("가상반-검증실패")
        val registered = repository.registerStudent(
            displayNameExact = "가상학생-검증실패",
            username = "synthetic-validation-user".toCharArray(),
            password = "synthetic-validation-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(registered.studentId))

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
                activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE
            }
            val activeSession = repository.startSession(classId)
            val currentSessionField = MainActivity::class.java
                .getDeclaredField("currentSession")
                .apply { isAccessible = true }
            val showScannerMethod = MainActivity::class.java
                .getDeclaredMethod("showScanner")
                .apply { isAccessible = true }
            scenario.onActivity { activity ->
                currentSessionField.set(activity, activeSession)
                showScannerMethod.invoke(activity)
            }
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.scanner_panel).visibility == View.VISIBLE &&
                    activity.findViewById<android.widget.TextView>(R.id.status_text)
                        .text
                        .toString() == "QR_READY"
            }

            val repositoryField = MainActivity::class.java
                .getDeclaredField("studentRepository")
                .apply { isAccessible = true }
            val validateMethod = MainActivity::class.java
                .getDeclaredMethod("validateQr", ByteArray::class.java)
                .apply { isAccessible = true }
            lateinit var originalRepository: StudentRepository
            try {
                scenario.onActivity { activity ->
                    originalRepository = repositoryField.get(activity) as StudentRepository
                    repositoryField.set(activity, null)
                    validateMethod.invoke(activity, ByteArray(32) { 0x5A })
                }

                waitUntil(scenario, timeoutMillis = 3_000) { activity ->
                    activity.findViewById<android.widget.TextView>(R.id.auth_error)
                        .text
                        .toString()
                        .contains("QR 확인 중 오류가 발생했습니다")
                }
                Thread.sleep(2_000)
                scenario.onActivity { activity ->
                    assertEquals(
                        "LOCKED",
                        activity.findViewById<android.widget.TextView>(R.id.status_text)
                            .text
                            .toString(),
                    )
                    assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.auth_panel).visibility)
                    assertEquals(View.GONE, activity.findViewById<View>(R.id.scanner_panel).visibility)
                }
            } finally {
                scenario.onActivity { activity ->
                    repositoryField.set(activity, originalRepository)
                }
            }
        }
        database.clearAllTables()
    }

    @Test
    fun cameraBindingFailureClosesScannerAndRequiresAdminAuthentication() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())
        val repository = StudentRepository(
            database = database,
            cipher = AndroidKeystoreCredentialCipher(),
            appVersion = "instrumented-test",
        )
        val classId = repository.createClass("가상반-카메라오류")
        val registered = repository.registerStudent(
            displayNameExact = "가상학생-카메라오류",
            username = "synthetic-camera-user".toCharArray(),
            password = "synthetic-camera-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(registered.studentId))

        try {
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
                    activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE
                }
                val activeSession = repository.startSession(classId)
                val currentSessionField = MainActivity::class.java
                    .getDeclaredField("currentSession")
                    .apply { isAccessible = true }
                val showScannerMethod = MainActivity::class.java
                    .getDeclaredMethod("showScanner")
                    .apply { isAccessible = true }
                val failureMethod = MainActivity::class.java
                    .getDeclaredMethod("handleCameraBindingFailure")
                    .apply { isAccessible = true }

                scenario.onActivity { activity ->
                    currentSessionField.set(activity, activeSession)
                    showScannerMethod.invoke(activity)
                    failureMethod.invoke(activity)
                }

                waitUntil(scenario) { activity ->
                    activity.findViewById<android.widget.TextView>(R.id.status_text)
                        .text
                        .toString() == "CAMERA_ERROR"
                }
                scenario.onActivity { activity ->
                    assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.auth_panel).visibility)
                    assertEquals(View.GONE, activity.findViewById<View>(R.id.scanner_panel).visibility)
                    assertEquals(
                        "카메라를 시작하지 못했습니다. 관리자 PIN으로 상태를 확인하세요.",
                        activity.findViewById<android.widget.TextView>(R.id.auth_error)
                            .text
                            .toString(),
                    )
                }
            }
        } finally {
            database.clearAllTables()
        }
    }

    @Test
    fun failedQrRejectionAuditDoesNotResumeScanner() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())
        val repository = StudentRepository(
            database = database,
            cipher = AndroidKeystoreCredentialCipher(),
            appVersion = "instrumented-test",
        )
        val classId = repository.createClass("가상반-거부기록오류")
        val registered = repository.registerStudent(
            displayNameExact = "가상학생-거부기록오류",
            username = "synthetic-rejection-user".toCharArray(),
            password = "synthetic-rejection-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(registered.studentId))

        try {
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
                    activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE
                }
                val activeSession = repository.startSession(classId)
                val currentSessionField = MainActivity::class.java
                    .getDeclaredField("currentSession")
                    .apply { isAccessible = true }
                val scannerVisibleField = MainActivity::class.java
                    .getDeclaredField("scannerVisible")
                    .apply { isAccessible = true }
                val analyzerField = MainActivity::class.java
                    .getDeclaredField("qrAnalyzer")
                    .apply { isAccessible = true }
                val repositoryField = MainActivity::class.java
                    .getDeclaredField("studentRepository")
                    .apply { isAccessible = true }
                val decisionMethod = MainActivity::class.java
                    .getDeclaredMethod("handleQrDecision", QrFrameDecision::class.java)
                    .apply { isAccessible = true }
                val analyzer = QrImageAnalyzer(onDecision = {})
                    .apply { setEnabled(true) }

                scenario.onActivity { activity ->
                    currentSessionField.set(activity, activeSession)
                    scannerVisibleField.setBoolean(activity, true)
                    analyzerField.set(activity, analyzer)
                    activity.findViewById<View>(R.id.admin_panel).visibility = View.GONE
                    activity.findViewById<View>(R.id.scanner_panel).visibility = View.VISIBLE
                    activity.findViewById<android.widget.TextView>(R.id.status_text).text = "QR_READY"
                }

                lateinit var originalRepository: StudentRepository
                try {
                    scenario.onActivity { activity ->
                        originalRepository = repositoryField.get(activity) as StudentRepository
                        repositoryField.set(activity, null)
                        decisionMethod.invoke(
                            activity,
                            QrFrameDecision.Reject(QrFrameRejection.INVALID_QR),
                        )
                    }

                    waitUntil(scenario, timeoutMillis = 3_000) { activity ->
                        activity.findViewById<android.widget.TextView>(R.id.auth_error)
                            .text
                            .toString()
                            .contains("QR 거부 기록 중 오류가 발생했습니다")
                    }
                    Thread.sleep(2_000)
                    scenario.onActivity { activity ->
                        assertEquals(
                            "LOCKED",
                            activity.findViewById<android.widget.TextView>(R.id.status_text)
                                .text
                                .toString(),
                        )
                        assertEquals(
                            View.VISIBLE,
                            activity.findViewById<View>(R.id.auth_panel).visibility,
                        )
                        assertEquals(
                            View.GONE,
                            activity.findViewById<View>(R.id.scanner_panel).visibility,
                        )
                    }
                } finally {
                    scenario.onActivity { activity ->
                        repositoryField.set(activity, originalRepository)
                    }
                }
            }
        } finally {
            database.clearAllTables()
        }
    }

    private fun waitUntil(
        scenario: ActivityScenario<MainActivity>,
        timeoutMillis: Long = 15_000,
        condition: (MainActivity) -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            var matched = false
            scenario.onActivity { matched = condition(it) }
            if (matched) return
            Thread.sleep(100)
        }
        throw AssertionError("UI condition was not met before timeout")
    }
}
