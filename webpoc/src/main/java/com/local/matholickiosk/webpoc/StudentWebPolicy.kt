package com.local.matholickiosk.webpoc

import java.net.URI
import java.util.Locale

object StudentWebPolicy {
    const val WORKBOOK_PATH = "/workbook"
    const val DIAGNOSTIC_PATH = "/diagnostic"
    const val LEARNING_PATH_PREFIX = "/learningV2/"

    fun isAllowedUrl(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        val uri = runCatching { URI(value) }.getOrNull() ?: return false
        val host = uri.host?.lowercase(Locale.ROOT) ?: return false
        if (
            !uri.scheme.equals("https", ignoreCase = true) ||
            host != "im.matholic.com" ||
            uri.userInfo != null ||
            (uri.port != -1 && uri.port != 443)
        ) return false

        return isAllowedPath(uri.path.orEmpty())
    }

    fun isAllowedPath(path: String): Boolean =
        path == WORKBOOK_PATH ||
            path.startsWith("$WORKBOOK_PATH/") ||
            path == DIAGNOSTIC_PATH ||
            path.startsWith("$DIAGNOSTIC_PATH/") ||
            path.startsWith(LEARNING_PATH_PREFIX)

    fun isListPath(path: String?): Boolean =
        path == WORKBOOK_PATH ||
            path?.startsWith("$WORKBOOK_PATH/") == true ||
            path == DIAGNOSTIC_PATH ||
            path?.startsWith("$DIAGNOSTIC_PATH/") == true

    fun isLearningPath(path: String?): Boolean =
        path?.startsWith(LEARNING_PATH_PREFIX) == true

    fun pathOf(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return runCatching { URI(value).path }.getOrNull()
    }
}
