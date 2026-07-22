package com.local.matholickiosk.webpoc

import java.net.URI
import java.text.Normalizer
import java.util.Locale

object WebSecurityPolicy {
    const val LOGIN_URL = "https://login.matholic.com/"
    const val COURSE_URL = "https://im.matholic.com/course"

    private val allowedHosts = setOf(
        "login.matholic.com",
        "auth.matholic.com",
        "im.matholic.com",
    )

    fun isAllowedTopLevelUrl(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        val uri = runCatching { URI(value) }.getOrNull() ?: return false
        val host = uri.host?.lowercase(Locale.ROOT) ?: return false
        return uri.scheme.equals("https", ignoreCase = true) &&
            host in allowedHosts &&
            uri.userInfo == null &&
            (uri.port == -1 || uri.port == 443)
    }

    fun isLoginUrl(value: String?): Boolean = hostOf(value) == "login.matholic.com"

    fun isPortalUrl(value: String?): Boolean = hostOf(value) == "im.matholic.com"

    fun normalizeDisplayName(value: String): String = Normalizer
        .normalize(value, Normalizer.Form.NFKC)
        .trim()
        .replace(Regex("\\s+"), " ")

    fun displayNamesMatch(expected: String, actual: String): Boolean {
        val normalizedExpected = normalizeDisplayName(expected)
        val normalizedActual = normalizeDisplayName(actual)
        return normalizedExpected.isNotEmpty() && normalizedExpected == normalizedActual
    }

    private fun hostOf(value: String?): String? {
        if (!isAllowedTopLevelUrl(value)) return null
        return URI(value).host.lowercase(Locale.ROOT)
    }
}
