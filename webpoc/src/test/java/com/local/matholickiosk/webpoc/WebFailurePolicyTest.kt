package com.local.matholickiosk.webpoc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebFailurePolicyTest {
    @Test
    fun `portal verification callback requires the current login probe generation`() {
        assertTrue(
            WebFailurePolicy.shouldProcessStateGenerationCallback(
                state = WebPocState.LOGIN_VERIFY,
                expectedState = WebPocState.LOGIN_VERIFY,
                callbackGeneration = 4,
                currentGeneration = 4,
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldProcessStateGenerationCallback(
                state = WebPocState.LOGIN_VERIFY,
                expectedState = WebPocState.LOGIN_VERIFY,
                callbackGeneration = 3,
                currentGeneration = 4,
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldProcessStateGenerationCallback(
                state = WebPocState.ACTIVE,
                expectedState = WebPocState.LOGIN_VERIFY,
                callbackGeneration = 4,
                currentGeneration = 4,
            ),
        )
    }

    @Test
    fun `logout callback requires both the expected state and current attempt generation`() {
        assertTrue(
            WebFailurePolicy.shouldProcessLogoutCallback(
                state = WebPocState.LOGOUT_NAVIGATE,
                expectedState = WebPocState.LOGOUT_NAVIGATE,
                callbackGeneration = 2,
                currentGeneration = 2,
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldProcessLogoutCallback(
                state = WebPocState.LOGOUT_NAVIGATE,
                expectedState = WebPocState.LOGOUT_NAVIGATE,
                callbackGeneration = 1,
                currentGeneration = 2,
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldProcessLogoutCallback(
                state = WebPocState.LOGOUT_SUBMIT,
                expectedState = WebPocState.LOGOUT_NAVIGATE,
                callbackGeneration = 2,
                currentGeneration = 2,
            ),
        )
    }

    @Test
    fun `page finish is processed only for the current main document`() {
        assertTrue(
            WebFailurePolicy.shouldProcessPageFinished(
                callbackUrl = "https://im.matholic.com/course",
                currentUrl = "https://im.matholic.com/course",
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldProcessPageFinished(
                callbackUrl = "https://login.matholic.com/",
                currentUrl = "https://im.matholic.com/course",
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldProcessPageFinished(
                callbackUrl = "https://im.matholic.com/course",
                currentUrl = "https://im.matholic.com/workbook",
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldProcessPageFinished(
                callbackUrl = null,
                currentUrl = "https://im.matholic.com/course",
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldProcessPageFinished(
                callbackUrl = "https://im.matholic.com/course",
                currentUrl = null,
            ),
        )
    }

    @Test
    fun `error page finish is ignored only while preflight DNS retry is pending`() {
        assertTrue(
            WebFailurePolicy.shouldIgnorePageFinishedWhilePreflightRetryPending(
                WebPocState.PREFLIGHT,
                retryScheduled = true,
                retryStarted = false,
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldIgnorePageFinishedWhilePreflightRetryPending(
                WebPocState.PREFLIGHT,
                retryScheduled = true,
                retryStarted = true,
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldIgnorePageFinishedWhilePreflightRetryPending(
                WebPocState.LOGIN_FILL,
                retryScheduled = true,
                retryStarted = false,
            ),
        )
    }

    @Test
    fun `only first preflight login DNS failure is retryable`() {
        assertTrue(
            WebFailurePolicy.canRetryPreflightLoginDns(
                isForMainFrame = true,
                state = WebPocState.PREFLIGHT,
                failureReason = "NETWORK_DNS_LOGIN",
                retryStarted = false,
            ),
        )
        assertFalse(
            WebFailurePolicy.canRetryPreflightLoginDns(
                isForMainFrame = true,
                state = WebPocState.PREFLIGHT,
                failureReason = "NETWORK_DNS_LOGIN",
                retryStarted = true,
            ),
        )
    }

    @Test
    fun `preflight retry never applies to other failures or states`() {
        assertFalse(
            WebFailurePolicy.canRetryPreflightLoginDns(
                isForMainFrame = false,
                state = WebPocState.PREFLIGHT,
                failureReason = "NETWORK_DNS_LOGIN",
                retryStarted = false,
            ),
        )
        assertFalse(
            WebFailurePolicy.canRetryPreflightLoginDns(
                isForMainFrame = true,
                state = WebPocState.LOGIN_FILL,
                failureReason = "NETWORK_DNS_LOGIN",
                retryStarted = false,
            ),
        )
        assertFalse(
            WebFailurePolicy.canRetryPreflightLoginDns(
                isForMainFrame = true,
                state = WebPocState.PREFLIGHT,
                failureReason = "NETWORK_CONNECT_LOGIN",
                retryStarted = false,
            ),
        )
    }

    @Test
    fun `subframe failures never lock`() {
        WebPocState.entries.forEach { state ->
            assertFalse(WebFailurePolicy.shouldLockForMainFrameFailure(false, state))
        }
    }

    @Test
    fun `late main frame failures do not relock idle or terminal states`() {
        listOf(
            WebPocState.IDLE,
            WebPocState.LOCKED,
            WebPocState.MAINTENANCE_REQUIRED,
        ).forEach { state ->
            assertFalse(WebFailurePolicy.shouldLockForMainFrameFailure(true, state))
        }
    }

    @Test
    fun `main frame failures lock every web active state`() {
        WebPocState.entries
            .filterNot {
                it in setOf(
                    WebPocState.IDLE,
                    WebPocState.LOCKED,
                    WebPocState.MAINTENANCE_REQUIRED,
                )
            }
            .forEach { state ->
                assertTrue(WebFailurePolicy.shouldLockForMainFrameFailure(true, state))
            }
    }

    @Test
    fun `secure session locks only for a real background transition`() {
        assertTrue(
            WebFailurePolicy.shouldLockSecureSessionOnStop(
                secureKioskSession = true,
                isFinishing = false,
                isChangingConfigurations = false,
                recoveryRecreatePending = false,
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldLockSecureSessionOnStop(
                secureKioskSession = true,
                isFinishing = false,
                isChangingConfigurations = false,
                recoveryRecreatePending = true,
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldLockSecureSessionOnStop(
                secureKioskSession = true,
                isFinishing = false,
                isChangingConfigurations = true,
                recoveryRecreatePending = false,
            ),
        )
        assertFalse(
            WebFailurePolicy.shouldLockSecureSessionOnStop(
                secureKioskSession = false,
                isFinishing = false,
                isChangingConfigurations = false,
                recoveryRecreatePending = false,
            ),
        )
    }
}
