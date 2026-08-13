package com.local.matholickiosk.kiosk

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inspector.WindowInspector
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.local.matholickiosk.kiosk.data.AdminAuthRepository
import com.local.matholickiosk.kiosk.data.ActiveSessionEntity
import com.local.matholickiosk.kiosk.data.KioskDatabase
import com.local.matholickiosk.kiosk.data.StudentRepository
import com.local.matholickiosk.kiosk.domain.CameraFacing
import com.local.matholickiosk.kiosk.domain.SingleFlightGate
import com.local.matholickiosk.kiosk.qr.QrFrameDecision
import com.local.matholickiosk.kiosk.qr.QrFrameRejection
import com.local.matholickiosk.kiosk.qr.QrImageAnalyzer
import com.local.matholickiosk.kiosk.qr.QrParseResult
import com.local.matholickiosk.kiosk.qr.QrTokenCodec
import com.local.matholickiosk.kiosk.security.AndroidKeystoreCredentialCipher
import com.local.matholickiosk.kiosk.transfer.PcPairingStore
import com.local.matholickiosk.kiosk.transfer.PcReceiverPairing
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {
    @Before
    fun clearAutomaticScheduleStateBeforeTest() {
        ApplicationProvider.getApplicationContext<Context>()
            .deleteSharedPreferences(AUTOMATIC_SCHEDULE_PREFERENCES)
    }

    @After
    fun clearAutomaticScheduleStateAfterTest() {
        ApplicationProvider.getApplicationContext<Context>()
            .deleteSharedPreferences(AUTOMATIC_SCHEDULE_PREFERENCES)
    }

    @Test
    fun destroyedActivityDiscardsQueuedPcPairingAndWipesMutableSecrets() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        val pairingStore = PcPairingStore(context)
        val receiverId = ByteArray(16) { index -> (index + 1).toByte() }
        val secret = ByteArray(32) { index -> (index + 33).toByte() }
        val pairing = PcReceiverPairing(
            receiverId = receiverId,
            secret = secret,
            host = "192.168.1.42",
            port = 48129,
            displayName = "대기열 폐기 시험 PC",
        )
        val blockerStarted = CountDownLatch(1)
        val blockerInterrupted = CountDownLatch(1)
        var scenario: ActivityScenario<MainActivity>? = null

        try {
            database.clearAllTables()
            pairingStore.clear()
            val launched = ActivityScenario.launch(MainActivity::class.java)
            scenario = launched
            val executorField = MainActivity::class.java
                .getDeclaredField("ioExecutor")
                .apply { isAccessible = true }
            val savePairingMethod = MainActivity::class.java
                .getDeclaredMethod("savePcPairing", PcReceiverPairing::class.java)
                .apply { isAccessible = true }

            launched.onActivity { activity ->
                val executor = executorField.get(activity) as ExecutorService
                executor.execute {
                    blockerStarted.countDown()
                    try {
                        CountDownLatch(1).await()
                    } catch (_: InterruptedException) {
                        blockerInterrupted.countDown()
                        Thread.currentThread().interrupt()
                    }
                }
            }
            assertTrue(
                "Synthetic blocker did not occupy the Activity executor",
                blockerStarted.await(10, TimeUnit.SECONDS),
            )
            launched.onActivity { activity -> savePairingMethod.invoke(activity, pairing) }

            launched.close()
            scenario = null

            assertTrue(
                "Activity shutdown did not interrupt the running executor task",
                blockerInterrupted.await(5, TimeUnit.SECONDS),
            )
            assertArrayEquals(ByteArray(receiverId.size), receiverId)
            assertArrayEquals(ByteArray(secret.size), secret)
            assertNull(pairingStore.load())
        } finally {
            scenario?.close()
            pairing.clearSensitiveData()
            pairingStore.clear()
            database.clearAllTables()
        }
    }

    @Test
    fun failedLockTaskEntryIsRenderedAsPolicyError() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
        assertFalse(
            "This regression requires a non-Device-Owner test package",
            devicePolicyManager.isDeviceOwnerApp(context.packageName),
        )
        database.clearAllTables()

        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                waitUntil(scenario) { activity ->
                    activity.findViewById<View>(R.id.auth_panel).visibility == View.VISIBLE &&
                        activity.findViewById<android.widget.TextView>(R.id.device_mode_text)
                            .text
                            .toString() == "보안 정책 오류"
                }
                val policyFailedField = MainActivity::class.java
                    .getDeclaredField("dedicatedDevicePolicyFailed")
                    .apply { isAccessible = true }
                scenario.onActivity { activity ->
                    assertTrue(policyFailedField.getBoolean(activity))
                    assertEquals(
                        "보안 정책 오류",
                        activity.findViewById<android.widget.TextView>(R.id.device_mode_text)
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
    fun unresponsivePairedPcDoesNotDelayAdminAuthenticationAtStartup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        val pairingStore = PcPairingStore(context)
        val accepted = CountDownLatch(1)
        val releaseConnection = CountDownLatch(1)
        val serverExecutor = Executors.newSingleThreadExecutor()
        val server = ServerSocket(0, 1, InetAddress.getLoopbackAddress())
        val pairing = PcReceiverPairing(
            receiverId = ByteArray(16) { index -> index.toByte() },
            secret = ByteArray(32) { index -> (index + 16).toByte() },
            host = requireNotNull(InetAddress.getLoopbackAddress().hostAddress),
            port = server.localPort,
            displayName = "무응답 시험 PC",
        )
        try {
            database.clearAllTables()
            AdminAuthRepository(database).enroll("654321".toCharArray())
            pairingStore.save(pairing)
            serverExecutor.execute {
                server.accept().use {
                    accepted.countDown()
                    try {
                        releaseConnection.await(20, TimeUnit.SECONDS)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }
            }

            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                assertTrue("Startup PDF transfer did not reach the test PC", accepted.await(5, TimeUnit.SECONDS))
                waitUntil(scenario, timeoutMillis = 2_000) { activity ->
                    activity.findViewById<android.widget.TextView>(R.id.auth_title)
                        .text
                        .toString() == "관리자 인증" &&
                        activity.findViewById<View>(R.id.pin_input).visibility == View.VISIBLE
                }
                scenario.onActivity { activity ->
                    activity.findViewById<android.widget.EditText>(R.id.pin_input)
                        .setText("654321")
                }
                waitUntil(scenario, timeoutMillis = 8_000) { activity ->
                    activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE &&
                        (activity.findViewById<android.widget.Spinner>(R.id.class_spinner)
                            .adapter
                            ?.count ?: 0) > 0
                }
            }
        } finally {
            releaseConnection.countDown()
            server.close()
            serverExecutor.shutdownNow()
            pairing.clearSensitiveData()
            pairingStore.clear()
            database.clearAllTables()
        }
    }

    @Test
    fun studentCsvFetchHoldsAdminGateUntilThePcRequestFinishes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        val pairingStore = PcPairingStore(context)
        val accepted = CountDownLatch(1)
        val releaseConnection = CountDownLatch(1)
        val serverExecutor = Executors.newSingleThreadExecutor()
        val server = ServerSocket(0, 1, InetAddress.getLoopbackAddress())
        val pairing = PcReceiverPairing(
            receiverId = ByteArray(16) { index -> (index + 32).toByte() },
            secret = ByteArray(32) { index -> (index + 64).toByte() },
            host = requireNotNull(InetAddress.getLoopbackAddress().hostAddress),
            port = server.localPort,
            displayName = "지연 CSV 시험 PC",
        )
        try {
            database.clearAllTables()
            AdminAuthRepository(database).enroll("654321".toCharArray())
            serverExecutor.execute {
                server.accept().use {
                    accepted.countDown()
                    try {
                        releaseConnection.await(20, TimeUnit.SECONDS)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }
            }

            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                waitForAdminAuthentication(scenario)
                scenario.onActivity { activity ->
                    activity.findViewById<android.widget.EditText>(R.id.pin_input)
                        .setText("654321")
                }
                waitUntil(scenario) { activity ->
                    activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE &&
                        activity.findViewById<View>(R.id.create_class_button).isEnabled
                }

                pairingStore.save(pairing)
                val pairedPcDisplayNameField = MainActivity::class.java
                    .getDeclaredField("pairedPcDisplayName")
                    .apply { isAccessible = true }
                val mutationGateField = MainActivity::class.java
                    .getDeclaredField("adminDataOperationGate")
                    .apply { isAccessible = true }
                val webRecoveryGateField = MainActivity::class.java
                    .getDeclaredField("webRecoveryGate")
                    .apply { isAccessible = true }
                val updateStudentControlsMethod = MainActivity::class.java
                    .getDeclaredMethod("updateStudentManagementControls")
                    .apply { isAccessible = true }
                val fetchStudentCsvMethod = MainActivity::class.java
                    .getDeclaredMethod("fetchStudentCsvFromPc")
                    .apply { isAccessible = true }
                val startOrEndSessionMethod = MainActivity::class.java
                    .getDeclaredMethod("startOrEndSession")
                    .apply { isAccessible = true }

                scenario.onActivity { activity ->
                    pairedPcDisplayNameField.set(activity, pairing.displayName)
                    updateStudentControlsMethod.invoke(activity)
                    assertTrue(
                        activity.findViewById<View>(R.id.import_student_csv_button).isEnabled,
                    )
                    fetchStudentCsvMethod.invoke(activity)
                }
                assertTrue(
                    "CSV fetch did not reach the delayed test PC",
                    accepted.await(5, TimeUnit.SECONDS),
                )
                scenario.onActivity { activity ->
                    val mutationGate = mutationGateField.get(activity) as SingleFlightGate
                    assertTrue(
                        "CSV fetch did not hold the common admin operation gate",
                        mutationGate.isActive,
                    )
                    assertFalse(
                        activity.findViewById<View>(R.id.register_student_button).isEnabled,
                    )
                    assertFalse(activity.findViewById<View>(R.id.create_class_button).isEnabled)
                    assertFalse(activity.findViewById<View>(R.id.start_session_button).isEnabled)
                    startOrEndSessionMethod.invoke(activity)
                    assertFalse(
                        (webRecoveryGateField.get(activity) as SingleFlightGate).isActive,
                    )
                    assertEquals(
                        "다른 학생·반 작업이 끝날 때까지 기다리세요.",
                        activity.findViewById<android.widget.TextView>(R.id.admin_message)
                            .text
                            .toString(),
                    )
                }

                releaseConnection.countDown()
                server.close()
                waitUntil(scenario, timeoutMillis = 10_000) { activity ->
                    !(mutationGateField.get(activity) as SingleFlightGate).isActive
                }
                scenario.onActivity { activity ->
                    assertTrue(activity.findViewById<View>(R.id.register_student_button).isEnabled)
                    assertTrue(activity.findViewById<View>(R.id.create_class_button).isEnabled)
                    assertTrue(activity.findViewById<View>(R.id.start_session_button).isEnabled)
                }
            }
        } finally {
            releaseConnection.countDown()
            runCatching { server.close() }
            serverExecutor.shutdownNow()
            pairing.clearSensitiveData()
            pairingStore.clear()
            database.clearAllTables()
        }
    }

    @Test
    fun sessionRecoveryInvalidatesPendingMembershipUndoBeforeWebLaunch() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())

        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                waitForAdminAuthentication(scenario)
                scenario.onActivity { activity ->
                    activity.findViewById<android.widget.EditText>(R.id.pin_input)
                        .setText("654321")
                }
                waitUntil(scenario) { activity ->
                    activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE &&
                        activity.findViewById<View>(R.id.create_class_button).isEnabled
                }

                val undoType = Class.forName(
                    "${MainActivity::class.java.name}\$PendingAdminUndo",
                )
                val restoreMembershipsType = Class.forName(
                    "${MainActivity::class.java.name}\$PendingAdminUndo\$RestoreMemberships",
                )
                val restoreMemberships = restoreMembershipsType
                    .getDeclaredConstructor(
                        String::class.java,
                        String::class.java,
                        Set::class.java,
                    )
                    .apply { isAccessible = true }
                    .newInstance("synthetic-class", "월1", setOf("synthetic-student"))
                val offerUndoMethod = MainActivity::class.java
                    .getDeclaredMethod("offerAdminUndo", undoType)
                    .apply { isAccessible = true }
                val pendingUndoField = MainActivity::class.java
                    .getDeclaredField("pendingAdminUndo")
                    .apply { isAccessible = true }
                val webRecoveryGateField = MainActivity::class.java
                    .getDeclaredField("webRecoveryGate")
                    .apply { isAccessible = true }
                val webRecoveryLauncherField = MainActivity::class.java
                    .getDeclaredField("webRecoveryLauncher")
                    .apply { isAccessible = true }
                val pendingRecoveryType = Class.forName(
                    "${MainActivity::class.java.name}\$PendingRecoveryAction",
                )
                val startSessionType = Class.forName(
                    "${MainActivity::class.java.name}\$PendingRecoveryAction\$StartSession",
                )
                val startSession = startSessionType
                    .getDeclaredConstructor(
                        String::class.java,
                        Set::class.java,
                        Boolean::class.javaPrimitiveType,
                        String::class.java,
                    )
                    .apply { isAccessible = true }
                    .newInstance("synthetic-class", emptySet<String>(), false, null)
                val launchRecoveryMethod = MainActivity::class.java
                    .getDeclaredMethod("launchWebSessionRecovery", pendingRecoveryType)
                    .apply { isAccessible = true }

                scenario.onActivity { activity ->
                    offerUndoMethod.invoke(activity, restoreMemberships)
                    val undoButton = activity.findViewById<View>(R.id.undo_admin_button)
                    assertTrue(pendingUndoField.get(activity) != null)
                    assertEquals(View.VISIBLE, undoButton.visibility)
                    assertTrue(undoButton.isEnabled)

                    @Suppress("UNCHECKED_CAST")
                    val launcher = webRecoveryLauncherField.get(activity) as
                        androidx.activity.result.ActivityResultLauncher<android.content.Intent>
                    launcher.unregister()
                    launchRecoveryMethod.invoke(activity, startSession)

                    assertNull(pendingUndoField.get(activity))
                    assertEquals(View.GONE, undoButton.visibility)
                    assertFalse(undoButton.isEnabled)
                    assertFalse(
                        (webRecoveryGateField.get(activity) as SingleFlightGate).isActive,
                    )
                    assertEquals(
                        "Web 세션 정리 화면을 열지 못했습니다.",
                        activity.findViewById<android.widget.TextView>(R.id.admin_message)
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
    fun creatingClassInvalidatesPendingNameUndoBeforeMutation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())
        val repository = StudentRepository(
            database = database,
            cipher = AndroidKeystoreCredentialCipher(),
            appVersion = "instrumented-test",
        )
        val student = repository.registerStudent(
            displayNameExact = "가상학생-현재이름",
            username = "synthetic-undo-user".toCharArray(),
            password = "synthetic-undo-password".toCharArray(),
        )

        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                waitForAdminAuthentication(scenario)
                scenario.onActivity { activity ->
                    activity.findViewById<android.widget.EditText>(R.id.pin_input)
                        .setText("654321")
                }
                waitUntil(scenario) { activity ->
                    activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE &&
                        activity.findViewById<View>(R.id.create_class_button).isEnabled
                }

                val undoType = Class.forName(
                    "${MainActivity::class.java.name}\$PendingAdminUndo",
                )
                val restoreNameType = Class.forName(
                    "${MainActivity::class.java.name}\$PendingAdminUndo\$RestoreStudentName",
                )
                val restoreName = restoreNameType
                    .getDeclaredConstructor(String::class.java, String::class.java)
                    .apply { isAccessible = true }
                    .newInstance(student.studentId, "가상학생-이전이름")
                val offerUndoMethod = MainActivity::class.java
                    .getDeclaredMethod("offerAdminUndo", undoType)
                    .apply { isAccessible = true }
                val performUndoMethod = MainActivity::class.java
                    .getDeclaredMethod("performPendingAdminUndo")
                    .apply { isAccessible = true }
                val pendingUndoField = MainActivity::class.java
                    .getDeclaredField("pendingAdminUndo")
                    .apply { isAccessible = true }
                val mutationGateField = MainActivity::class.java
                    .getDeclaredField("adminDataOperationGate")
                    .apply { isAccessible = true }

                scenario.onActivity { activity ->
                    offerUndoMethod.invoke(activity, restoreName)
                    val undoButton = activity.findViewById<View>(R.id.undo_admin_button)
                    assertTrue(pendingUndoField.get(activity) != null)
                    assertEquals(View.VISIBLE, undoButton.visibility)
                    assertTrue(undoButton.isEnabled)

                    activity.findViewById<android.widget.EditText>(R.id.class_name_input)
                        .setText("가상반-실행취소무효화")
                    activity.findViewById<View>(R.id.create_class_button).performClick()

                    assertNull(pendingUndoField.get(activity))
                    assertEquals(View.GONE, undoButton.visibility)
                    assertFalse(undoButton.isEnabled)
                    assertTrue(
                        (mutationGateField.get(activity) as SingleFlightGate).isActive,
                    )
                }

                waitUntil(scenario) { activity ->
                    !(mutationGateField.get(activity) as SingleFlightGate).isActive &&
                        activity.findViewById<android.widget.TextView>(R.id.admin_message)
                            .text
                            .toString() == "반을 생성했습니다."
                }
                scenario.onActivity { activity ->
                    val undoButton = activity.findViewById<View>(R.id.undo_admin_button)
                    assertNull(pendingUndoField.get(activity))
                    assertEquals(View.GONE, undoButton.visibility)
                    assertFalse(undoButton.isEnabled)
                    performUndoMethod.invoke(activity)
                    assertNull(pendingUndoField.get(activity))
                }
                assertEquals(
                    "가상학생-현재이름",
                    repository.listStudents()
                        .single { it.studentId == student.studentId }
                        .displayNameExact,
                )
                assertTrue(
                    repository.listClasses().any { it.className == "가상반-실행취소무효화" },
                )
            }
        } finally {
            database.clearAllTables()
        }
    }

    @Test
    fun pendingRecoveryActionsSurviveActivityRecreation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()

        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                val pendingRecoveryField = MainActivity::class.java
                    .getDeclaredField("pendingRecoveryAction")
                    .apply { isAccessible = true }
                val startSessionType = Class.forName(
                    "${MainActivity::class.java.name}\$PendingRecoveryAction\$StartSession",
                )
                val startSession = startSessionType
                    .getDeclaredConstructor(
                        String::class.java,
                        Set::class.java,
                        Boolean::class.javaPrimitiveType,
                        String::class.java,
                    )
                    .apply { isAccessible = true }
                    .newInstance(
                        "synthetic-class",
                        setOf("synthetic-temporary-a", "synthetic-temporary-b"),
                        false,
                        null,
                    )
                val endSessionType = Class.forName(
                    "${MainActivity::class.java.name}\$PendingRecoveryAction\$EndSession",
                )
                val endSession = endSessionType
                    .getDeclaredConstructor(Boolean::class.javaPrimitiveType)
                    .apply { isAccessible = true }
                    .newInstance(false)

                scenario.onActivity { activity ->
                    pendingRecoveryField.set(activity, startSession)
                }
                scenario.recreate()
                scenario.onActivity { activity ->
                    val restored = pendingRecoveryField.get(activity)
                    assertEquals(startSessionType, restored.javaClass)
                    assertEquals(
                        "synthetic-class",
                        startSessionType.getMethod("getClassId").invoke(restored),
                    )
                    assertEquals(
                        setOf("synthetic-temporary-a", "synthetic-temporary-b"),
                        startSessionType.getMethod("getTemporaryStudentIds").invoke(restored),
                    )
                    pendingRecoveryField.set(activity, endSession)
                }

                scenario.recreate()
                scenario.onActivity { activity ->
                    assertEquals(endSessionType, pendingRecoveryField.get(activity).javaClass)
                }
            }
        } finally {
            database.clearAllTables()
        }
    }

    @Test
    fun correctAdminPinOpensAdminWithoutDoneOrSubmitTap() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitForAdminAuthentication(scenario)
            scenario.onActivity { activity ->
                activity.findViewById<android.widget.EditText>(R.id.pin_input)
                    .setText("654321")
            }
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE
            }
        }
    }

    @Test
    fun longerAdminPinDoesNotSubmitOrRecordFailureAtSixDigitPrefix() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        val repository = AdminAuthRepository(database)
        repository.enroll("7654321".toCharArray())
        assertEquals(7, repository.enrolledPinLength())

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitForAdminAuthentication(scenario)
            scenario.onActivity { activity ->
                activity.findViewById<android.widget.EditText>(R.id.pin_input)
                    .setText("765432")
            }
            Thread.sleep(600)
            scenario.onActivity { activity ->
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.auth_panel).visibility)
            }
            assertEquals(0, database.adminDao().get()?.consecutiveFailures)
            scenario.onActivity { activity ->
                activity.findViewById<android.widget.EditText>(R.id.pin_input)
                    .setText("7654321")
            }
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE
            }
        }
    }

    @Test
    fun scannerInstructionReferencesPhysicalLensWithoutScreenTarget() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val instructionView = activity
                    .findViewById<android.widget.TextView>(R.id.scanner_lens_instruction)
                val instruction = instructionView.text.toString()
                assertTrue(instruction.contains("카메라 렌즈"))
                assertTrue(instruction.contains("전면 카메라 렌즈"))
                assertFalse(instruction.contains("선택한 카메라"))
                assertFalse(instruction.contains("목표 영역"))
                assertFalse(instruction.contains("가운데"))
                assertFalse(instruction.contains("화면 아래"))
                assertEquals(
                    activity.findViewById<View>(R.id.scanner_center_content),
                    activity.findViewById<View>(R.id.scanner_message).parent,
                )
                assertTrue(
                    activity.findViewById<View>(R.id.switch_camera_button) is
                        android.widget.ImageButton,
                )
                assertTrue(
                    activity.findViewById<View>(R.id.session_admin_button) is
                        android.widget.ImageButton,
                )
            }
        }
    }

    @Test
    fun qrHelpFitsTheScannerViewportAndUsesTheBadgeSimulation() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val deviceAContext = baseContext.createConfigurationContext(
            Configuration(baseContext.resources.configuration).apply {
                densityDpi = 240
                fontScale = 1.1f
                orientation = Configuration.ORIENTATION_LANDSCAPE
                screenWidthDp = 1_333
                screenHeightDp = 752
                smallestScreenWidthDp = 800
            },
        )

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val root = LayoutInflater.from(deviceAContext)
                .inflate(R.layout.activity_main, null, false)
            root.findViewById<View>(R.id.app_header).visibility = View.GONE
            root.findViewById<View>(R.id.auth_panel).visibility = View.GONE
            root.findViewById<View>(R.id.admin_panel).visibility = View.GONE
            root.findViewById<View>(R.id.scanner_panel).visibility = View.VISIBLE
            val panel = root.findViewById<ViewGroup>(R.id.scanner_help_panel).apply {
                visibility = View.VISIBLE
            }
            root.measure(
                View.MeasureSpec.makeMeasureSpec(2_000, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1_128, View.MeasureSpec.EXACTLY),
            )
            root.layout(0, 0, root.measuredWidth, root.measuredHeight)

            val caution = root.findViewById<View>(R.id.scanner_help_caution)
            val cautionBounds = Rect(0, 0, caution.width, caution.height).also {
                panel.offsetDescendantRectToMyCoords(caution, it)
            }
            val simulation = root.findViewById<android.widget.ImageView>(
                R.id.scanner_help_simulation,
            )
            assertTrue(caution.width > 0 && caution.height > 0)
            assertTrue(simulation.width > 0 && simulation.height > 0)
            assertTrue(simulation.drawable != null)
            assertTrue(simulation.contentDescription.contains("QR 명찰"))
            assertTrue(cautionBounds.bottom <= panel.height)
            assertEquals(
                11f * deviceAContext.resources.displayMetrics.density,
                root.findViewById<View>(R.id.scanner_lens_pointer).translationY,
                0.6f,
            )
        }
    }

    @Test
    fun scannerHidesHeaderAndUsesAccessibleIconControls() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())
        val repository = StudentRepository(
            database = database,
            cipher = AndroidKeystoreCredentialCipher(),
            appVersion = "instrumented-test",
        )
        val classId = repository.createClass("가상반-스캐너화면")
        val registered = repository.registerStudent(
            displayNameExact = "가상학생-스캐너화면",
            username = "synthetic-scanner-ui-user".toCharArray(),
            password = "synthetic-scanner-ui-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(registered.studentId))

        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                waitForAdminAuthentication(scenario)
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
                val showAuthenticationMethod = MainActivity::class.java
                    .getDeclaredMethod("showAuthentication", Boolean::class.javaPrimitiveType!!)
                    .apply { isAccessible = true }
                val activeCameraFacingField = MainActivity::class.java
                    .getDeclaredField("activeCameraFacing")
                    .apply { isAccessible = true }
                val updateCameraSwitchLabelMethod = MainActivity::class.java
                    .getDeclaredMethod("updateCameraSwitchLabel")
                    .apply { isAccessible = true }

                scenario.onActivity { activity ->
                    currentSessionField.set(activity, activeSession)
                    showScannerMethod.invoke(activity)
                }
                waitUntil(scenario) { activity ->
                    activity.findViewById<View>(R.id.scanner_panel).visibility == View.VISIBLE
                }
                scenario.onActivity { activity ->
                    assertEquals(View.GONE, activity.findViewById<View>(R.id.app_header).visibility)
                    assertEquals(
                        "",
                        activity.findViewById<android.widget.TextView>(R.id.scanner_message)
                            .text
                            .toString(),
                    )
                    assertEquals(
                        "후면 카메라로 전환",
                        activity.findViewById<View>(R.id.switch_camera_button)
                            .contentDescription
                            .toString(),
                    )
                    assertEquals(
                        "관리자 인증",
                        activity.findViewById<View>(R.id.session_admin_button)
                            .contentDescription
                            .toString(),
                    )
                    assertEquals(
                        "QR 카드를 전면 카메라 렌즈에 보여주세요",
                        activity.findViewById<android.widget.TextView>(
                            R.id.scanner_lens_instruction,
                        ).text.toString(),
                    )
                    activeCameraFacingField.set(activity, CameraFacing.BACK)
                    updateCameraSwitchLabelMethod.invoke(activity)
                    assertEquals(
                        "QR 카드를 후면 카메라 렌즈에 보여주세요",
                        activity.findViewById<android.widget.TextView>(
                            R.id.scanner_lens_instruction,
                        ).text.toString(),
                    )
                    assertEquals(
                        "전면 카메라로 전환",
                        activity.findViewById<View>(R.id.switch_camera_button)
                            .contentDescription
                            .toString(),
                    )
                    val helpButton = activity.findViewById<View>(R.id.scanner_help_button)
                    val helpPanel = activity.findViewById<View>(R.id.scanner_help_panel)
                    helpButton.performClick()
                    assertEquals(View.VISIBLE, helpPanel.visibility)
                    assertEquals("QR 카드 화면 도움말 닫기", helpButton.contentDescription)
                    assertTrue(
                        (helpButton.parent as ViewGroup).indexOfChild(helpButton) >
                            (helpPanel.parent as ViewGroup).indexOfChild(helpPanel),
                    )
                    helpButton.performClick()
                    assertEquals(View.GONE, helpPanel.visibility)
                    assertEquals("QR 카드 화면 도움말", helpButton.contentDescription)
                    showAuthenticationMethod.invoke(activity, false)
                    assertEquals(
                        View.VISIBLE,
                        activity.findViewById<View>(R.id.app_header).visibility,
                    )
                }
            }
        } finally {
            database.clearAllTables()
        }
    }

    @Test
    fun activeRemoteSupportKeepsAdminPinSecureAndAllowsAdminCapture() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        val remoteSupportStore = RemoteSupportStore(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())
        remoteSupportStore.disable()
        remoteSupportStore.enable(RemoteSupportPolicy.DEFAULT_DURATION_MILLIS)

        try {
            assertTrue(remoteSupportStore.activeUntilEpochMillis() != null)
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                waitForAdminAuthentication(scenario)
                scenario.onActivity { activity ->
                    assertEquals(
                        View.VISIBLE,
                        activity.findViewById<View>(R.id.remote_support_badge).visibility,
                    )
                    assertTrue(
                        (activity.window.attributes.flags and
                            WindowManager.LayoutParams.FLAG_SECURE) != 0,
                    )
                    activity.findViewById<android.widget.EditText>(R.id.pin_input)
                        .setText("654321")
                    activity.findViewById<View>(R.id.auth_submit).performClick()
                }
                waitUntil(scenario) { activity ->
                    activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE &&
                        activity.findViewById<View>(R.id.remote_support_badge).visibility ==
                            View.VISIBLE &&
                        (activity.window.attributes.flags and
                            WindowManager.LayoutParams.FLAG_SECURE) == 0
                }
                scenario.onActivity { activity ->
                    MainActivity::class.java
                        .getDeclaredMethod("requestSessionAdminAuthentication")
                        .apply { isAccessible = true }
                        .invoke(activity)

                    assertTrue(
                        (activity.window.attributes.flags and
                            WindowManager.LayoutParams.FLAG_SECURE) != 0,
                    )
                    val dialogRoot = WindowInspector.getGlobalWindowViews().single {
                        it !== activity.window.decorView
                    }
                    val dialogWindowAttributes =
                        dialogRoot.layoutParams as WindowManager.LayoutParams
                    assertTrue(
                        (dialogWindowAttributes.flags and
                            WindowManager.LayoutParams.FLAG_SECURE) != 0,
                    )
                    dialogRoot.findViewById<View>(android.R.id.button2).performClick()
                }
                waitUntil(scenario) { activity ->
                    (activity.window.attributes.flags and
                        WindowManager.LayoutParams.FLAG_SECURE) == 0
                }
            }
        } finally {
            remoteSupportStore.disable()
            database.clearAllTables()
        }
    }

    @Test
    fun adminPinUnlocksUiAndWindowRemainsSecure() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitForAdminAuthentication(scenario)
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
        val originalHash = (
            QrTokenCodec().parse(registered.issuedQr.payload) as QrParseResult.Valid
        ).hash

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitForAdminAuthentication(scenario)
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
        originalHash.fill(0)
        database.clearAllTables()
    }

    @Test
    fun adminAndWebRecoveryOperationsBlockStudentAndClassChanges() {
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
            waitForAdminAuthentication(scenario)
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
                .getDeclaredField("adminDataOperationGate")
                .apply { isAccessible = true }
            val webRecoveryGateField = MainActivity::class.java
                .getDeclaredField("webRecoveryGate")
                .apply { isAccessible = true }
            val beginOperationMethod = MainActivity::class.java
                .getDeclaredMethod("beginAdminDataOperation", String::class.java)
                .apply { isAccessible = true }
            val updateStudentControlsMethod = MainActivity::class.java
                .getDeclaredMethod("updateStudentManagementControls")
                .apply { isAccessible = true }
            val updateClassControlsMethod = MainActivity::class.java
                .getDeclaredMethod("updateClassRosterUi")
                .apply { isAccessible = true }
            val finishWebRecoveryMethod = MainActivity::class.java
                .getDeclaredMethod("finishWebRecoveryOperation")
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
                    assertTrue(
                        beginOperationMethod.invoke(activity, "일괄 작업 시험 중") as Boolean,
                    )
                    assertFalse(activity.findViewById<View>(R.id.register_student_button).isEnabled)
                    assertFalse(activity.findViewById<View>(R.id.create_class_button).isEnabled)
                    assertFalse(activity.findViewById<View>(R.id.import_student_csv_button).isEnabled)
                    assertFalse(activity.findViewById<View>(R.id.start_session_button).isEnabled)
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
                    val webRecoveryGate =
                        webRecoveryGateField.get(activity) as SingleFlightGate
                    assertTrue(webRecoveryGate.tryStart())
                    updateStudentControlsMethod.invoke(activity)
                    updateClassControlsMethod.invoke(activity)
                    assertFalse(
                        activity.findViewById<View>(R.id.register_student_button).isEnabled,
                    )
                    assertFalse(activity.findViewById<View>(R.id.create_class_button).isEnabled)
                    assertFalse(
                        beginOperationMethod.invoke(activity, "차단 확인") as Boolean,
                    )
                    finishWebRecoveryMethod.invoke(activity)
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
    fun sessionEndCommitKeepsIdleUiWhenSnapshotRefreshFails() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())
        val repository = StudentRepository(
            database = database,
            cipher = AndroidKeystoreCredentialCipher(),
            appVersion = "instrumented-test",
        )
        val classId = repository.createClass("가상반-종료새로고침실패")
        val registered = repository.registerStudent(
            displayNameExact = "가상학생-종료새로고침실패",
            username = "synthetic-end-refresh-user".toCharArray(),
            password = "synthetic-end-refresh-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(registered.studentId))

        val endCommitted = CountDownLatch(1)
        val commitWatcher = Executors.newSingleThreadExecutor()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                waitForAdminAuthentication(scenario)
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
                val repositoryField = MainActivity::class.java
                    .getDeclaredField("studentRepository")
                    .apply { isAccessible = true }
                val updateSessionControlsMethod = MainActivity::class.java
                    .getDeclaredMethod(
                        "updateSessionAdminControls",
                        ActiveSessionEntity::class.java,
                    )
                    .apply { isAccessible = true }
                val endSessionType = Class.forName(
                    "${MainActivity::class.java.name}\$PendingRecoveryAction\$EndSession",
                )
                val endSessionAction = endSessionType
                    .getDeclaredConstructor(Boolean::class.javaPrimitiveType)
                    .apply { isAccessible = true }
                    .newInstance(false)
                val completeSessionEndMethod = MainActivity::class.java
                    .getDeclaredMethod("completeSessionEnd", endSessionType)
                    .apply { isAccessible = true }
                lateinit var originalRepository: StudentRepository

                commitWatcher.execute {
                    val deadline = System.currentTimeMillis() + 5_000
                    while (System.currentTimeMillis() < deadline) {
                        if (database.sessionDao().get()?.sessionId == null) {
                            endCommitted.countDown()
                            return@execute
                        }
                        Thread.sleep(10)
                    }
                }

                try {
                    scenario.onActivity { activity ->
                        originalRepository = repositoryField.get(activity) as StudentRepository
                        currentSessionField.set(activity, activeSession)
                        updateSessionControlsMethod.invoke(activity, activeSession)
                        assertEquals(
                            "현재 수업 안전 종료",
                            activity.findViewById<android.widget.Button>(R.id.start_session_button)
                                .text
                                .toString(),
                        )
                        assertEquals(
                            View.VISIBLE,
                            activity.findViewById<View>(R.id.resume_session_button).visibility,
                        )

                        completeSessionEndMethod.invoke(activity, endSessionAction)
                        assertTrue(
                            "Session end transaction did not commit",
                            endCommitted.await(5, TimeUnit.SECONDS),
                        )
                        repositoryField.set(activity, null)
                    }

                    waitUntil(scenario, timeoutMillis = 5_000) { activity ->
                        activity.findViewById<android.widget.TextView>(R.id.status_text)
                            .text
                            .toString() == "ADMIN_IDLE" &&
                            activity.findViewById<android.widget.TextView>(R.id.admin_message)
                                .text
                                .toString()
                                .contains("최신 목록을 불러오지 못했습니다")
                    }
                    scenario.onActivity { activity ->
                        val projected = currentSessionField.get(activity) as ActiveSessionEntity
                        assertNull(projected.sessionId)
                        assertEquals(
                            "선택한 반 수업 안전 시작",
                            activity.findViewById<android.widget.Button>(R.id.start_session_button)
                                .text
                                .toString(),
                        )
                        assertEquals(
                            View.GONE,
                            activity.findViewById<View>(R.id.resume_session_button).visibility,
                        )
                        assertEquals(
                            View.GONE,
                            activity.findViewById<View>(R.id.recover_session_button).visibility,
                        )
                        assertEquals(
                            View.GONE,
                            activity.findViewById<View>(R.id.scanner_panel).visibility,
                        )
                    }
                    assertNull(repository.currentSession()?.sessionId)
                } finally {
                    scenario.onActivity { activity ->
                        repositoryField.set(activity, originalRepository)
                    }
                }
            }
        } finally {
            commitWatcher.shutdownNow()
            database.clearAllTables()
        }
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
            waitForAdminAuthentication(scenario)
            scenario.onActivity { activity ->
                activity.findViewById<android.widget.EditText>(R.id.pin_input)
                    .setText("654321")
                activity.findViewById<View>(R.id.auth_submit).performClick()
            }
            waitUntil(scenario) { activity ->
                activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE &&
                    activity.findViewById<android.widget.Spinner>(R.id.class_spinner).count == 14 &&
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
    fun quickClassButtonsKeepStableGeometryAndTypographyAfterSelectionChanges() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())

        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                waitForAdminAuthentication(scenario)
                scenario.onActivity { activity ->
                    activity.findViewById<android.widget.EditText>(R.id.pin_input)
                        .setText("654321")
                }
                waitUntil(scenario) { activity ->
                    activity.findViewById<View>(R.id.admin_panel).visibility == View.VISIBLE &&
                        activity.findViewById<android.widget.GridLayout>(R.id.quick_class_grid)
                            .childCount == 12 &&
                        (activity.findViewById<android.widget.Spinner>(R.id.class_spinner)
                            .adapter
                            ?.count ?: 0) == 12 &&
                        quickClassButton(activity, "월1").isEnabled
                }

                lateinit var initialBounds: Map<String, android.graphics.Rect>
                scenario.onActivity { activity ->
                    initialBounds = quickClassButtonBounds(activity)
                    quickClassButton(activity, "월1").performClick()
                }
                waitUntil(scenario) { activity ->
                    activity.findViewById<android.widget.Spinner>(R.id.class_spinner)
                        .selectedItem
                        ?.toString() == "월1"
                }
                scenario.onActivity { activity ->
                    val selected = quickClassButton(activity, "월1")
                    assertFalse(selected.typeface.isBold)
                    assertNull(selected.stateListAnimator)
                    assertEquals(1f, selected.alpha, 0.001f)
                    assertEquals(initialBounds, quickClassButtonBounds(activity))
                    quickClassButton(activity, "월2").performClick()
                }
                waitUntil(scenario) { activity ->
                    activity.findViewById<android.widget.Spinner>(R.id.class_spinner)
                        .selectedItem
                        ?.toString() == "월2"
                }
                scenario.onActivity { activity ->
                    val previous = quickClassButton(activity, "월1")
                    val selected = quickClassButton(activity, "월2")
                    assertFalse(previous.typeface.isBold)
                    assertNull(previous.stateListAnimator)
                    assertEquals(0.72f, previous.alpha, 0.001f)
                    assertFalse(selected.typeface.isBold)
                    assertNull(selected.stateListAnimator)
                    assertEquals(1f, selected.alpha, 0.001f)
                    assertEquals(initialBounds, quickClassButtonBounds(activity))
                }
            }
        } finally {
            database.clearAllTables()
        }
    }

    @Test
    fun failedWebSessionResultPersistenceShowsClosedRecoveryUi() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = KioskDatabase.get(context)
        database.clearAllTables()
        AdminAuthRepository(database).enroll("654321".toCharArray())
        val repository = StudentRepository(
            database = database,
            cipher = AndroidKeystoreCredentialCipher(),
            appVersion = "instrumented-test",
        )
        val classId = repository.createClass("가상반-결과저장실패")
        val registered = repository.registerStudent(
            displayNameExact = "가상학생-결과저장실패",
            username = "synthetic-result-user".toCharArray(),
            password = "synthetic-result-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(registered.studentId))

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitForAdminAuthentication(scenario)

            val activeSession = repository.startSession(classId)
            val persistMethod = MainActivity::class.java
                .getDeclaredMethod(
                    "persistWebSessionResult",
                    Boolean::class.javaPrimitiveType,
                    String::class.java,
                    String::class.java,
                    String::class.java,
                )
                .apply { isAccessible = true }
            scenario.onActivity { activity ->
                persistMethod.invoke(
                    activity,
                    true,
                    "synthetic-result",
                    checkNotNull(activeSession.sessionId),
                    null,
                )
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
            waitForAdminAuthentication(scenario)

            val repositoryField = MainActivity::class.java
                .getDeclaredField("studentRepository")
                .apply { isAccessible = true }
            val restoreMethod = MainActivity::class.java
                .getDeclaredMethod(
                    "restoreQrReadyAfterCancelledWebLaunch",
                    String::class.java,
                )
                .apply { isAccessible = true }
            lateinit var originalRepository: StudentRepository
            try {
                scenario.onActivity { activity ->
                    originalRepository = repositoryField.get(activity) as StudentRepository
                    repositoryField.set(activity, null)
                    restoreMethod.invoke(activity, "synthetic-session")
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
            waitForAdminAuthentication(scenario)
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
                .getDeclaredMethod(
                    "validateQr",
                    ByteArray::class.java,
                    String::class.java,
                )
                .apply { isAccessible = true }
            lateinit var originalRepository: StudentRepository
            try {
                scenario.onActivity { activity ->
                    originalRepository = repositoryField.get(activity) as StudentRepository
                    repositoryField.set(activity, null)
                    validateMethod.invoke(activity, ByteArray(32) { 0x5A }, null)
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
                waitForAdminAuthentication(scenario)
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
                waitForAdminAuthentication(scenario)
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

    private fun waitForAdminAuthentication(scenario: ActivityScenario<MainActivity>) {
        waitUntil(scenario) { activity ->
            activity.findViewById<View>(R.id.auth_panel).visibility == View.VISIBLE &&
                activity.findViewById<android.widget.TextView>(R.id.auth_title)
                    .text
                    .toString() == "관리자 인증" &&
                activity.findViewById<View>(R.id.pin_input).visibility == View.VISIBLE &&
                activity.findViewById<View>(R.id.pin_input).isEnabled
        }
    }

    private companion object {
        const val AUTOMATIC_SCHEDULE_PREFERENCES = "automatic_class_schedule"
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

    private fun quickClassButton(
        activity: MainActivity,
        label: String,
    ): android.widget.Button {
        val grid = activity.findViewById<android.widget.GridLayout>(R.id.quick_class_grid)
        return (0 until grid.childCount)
            .map(grid::getChildAt)
            .filterIsInstance<android.widget.Button>()
            .first { it.text.toString() == label }
    }

    private fun quickClassButtonBounds(activity: MainActivity): Map<String, android.graphics.Rect> {
        val grid = activity.findViewById<android.widget.GridLayout>(R.id.quick_class_grid)
        return (0 until grid.childCount)
            .map(grid::getChildAt)
            .filterIsInstance<android.widget.Button>()
            .associate { button ->
                button.text.toString() to android.graphics.Rect(
                    button.left,
                    button.top,
                    button.right,
                    button.bottom,
                )
            }
    }
}
