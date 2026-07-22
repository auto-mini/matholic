package com.local.matholickiosk.webpoc

import android.webkit.WebViewClient

object NetworkFailureReason {
    fun fromWebViewErrorCode(errorCode: Int?, host: String?): String {
        val category = when (errorCode) {
        WebViewClient.ERROR_HOST_LOOKUP -> "NETWORK_DNS"
        WebViewClient.ERROR_CONNECT -> "NETWORK_CONNECT"
        WebViewClient.ERROR_TIMEOUT -> "NETWORK_TIMEOUT"
        WebViewClient.ERROR_TOO_MANY_REQUESTS -> "NETWORK_RATE_LIMIT"
        WebViewClient.ERROR_FAILED_SSL_HANDSHAKE -> "NETWORK_TLS_HANDSHAKE"
        else -> "NETWORK_ERROR"
        }
        val hostCategory = when (host?.lowercase()) {
            "login.matholic.com" -> "LOGIN"
            "auth.matholic.com" -> "AUTH"
            "im.matholic.com" -> "PORTAL"
            else -> "OTHER"
        }
        return "${category}_$hostCategory"
    }
}
