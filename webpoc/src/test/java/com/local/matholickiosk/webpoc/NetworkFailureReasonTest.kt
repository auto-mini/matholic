package com.local.matholickiosk.webpoc

import android.webkit.WebViewClient
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkFailureReasonTest {
    @Test
    fun `maps bounded transport categories without raw messages`() {
        assertEquals(
            "NETWORK_DNS",
            NetworkFailureReason.fromWebViewErrorCode(
                WebViewClient.ERROR_HOST_LOOKUP,
                "login.matholic.com",
            ).substringBeforeLast('_'),
        )
        assertEquals(
            "NETWORK_CONNECT",
            NetworkFailureReason.fromWebViewErrorCode(WebViewClient.ERROR_CONNECT, null)
                .substringBeforeLast('_'),
        )
        assertEquals(
            "NETWORK_TIMEOUT",
            NetworkFailureReason.fromWebViewErrorCode(WebViewClient.ERROR_TIMEOUT, null)
                .substringBeforeLast('_'),
        )
        assertEquals(
            "NETWORK_RATE_LIMIT",
            NetworkFailureReason.fromWebViewErrorCode(WebViewClient.ERROR_TOO_MANY_REQUESTS, null)
                .substringBeforeLast('_'),
        )
        assertEquals(
            "NETWORK_TLS_HANDSHAKE",
            NetworkFailureReason.fromWebViewErrorCode(WebViewClient.ERROR_FAILED_SSL_HANDSHAKE, null)
                .substringBeforeLast('_'),
        )
        assertEquals("NETWORK_ERROR_OTHER", NetworkFailureReason.fromWebViewErrorCode(null, null))
        assertEquals("NETWORK_ERROR_OTHER", NetworkFailureReason.fromWebViewErrorCode(-999, null))
        assertEquals(
            "NETWORK_DNS_AUTH",
            NetworkFailureReason.fromWebViewErrorCode(
                WebViewClient.ERROR_HOST_LOOKUP,
                "auth.matholic.com",
            ),
        )
        assertEquals(
            "NETWORK_DNS_PORTAL",
            NetworkFailureReason.fromWebViewErrorCode(
                WebViewClient.ERROR_HOST_LOOKUP,
                "im.matholic.com",
            ),
        )
        assertEquals(
            "NETWORK_DNS_OTHER",
            NetworkFailureReason.fromWebViewErrorCode(
                WebViewClient.ERROR_HOST_LOOKUP,
                "unapproved.invalid",
            ),
        )
    }
}
