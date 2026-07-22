package com.local.matholickiosk.webpoc

class EphemeralCredentials(
    username: CharSequence,
    password: CharSequence,
) {
    private val usernameChars = CharArray(username.length) { username[it] }
    private val passwordChars = CharArray(password.length) { password[it] }

    fun usernameForImmediateUse(): String = String(usernameChars)

    fun passwordForImmediateUse(): String = String(passwordChars)

    fun wipe() {
        usernameChars.fill('\u0000')
        passwordChars.fill('\u0000')
    }
}
