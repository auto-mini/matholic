package com.local.matholickiosk.webpoc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebFailurePolicyTest {
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
}
