package com.local.matholickiosk.webpoc

object CredentialBridgeLaunchPolicy {
    private val handlePattern = Regex("[A-Za-z0-9_-]{32}")

    fun isValidHandleId(value: String?): Boolean =
        value != null && handlePattern.matches(value)
}
