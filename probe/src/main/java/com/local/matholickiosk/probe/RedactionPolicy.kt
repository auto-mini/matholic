package com.local.matholickiosk.probe

import java.security.MessageDigest
import java.security.SecureRandom

class RedactionPolicy(
    private val sessionSalt: ByteArray = ByteArray(32).also(SecureRandom()::nextBytes),
) {
    private val safeUiText = setOf(
        "로그인",
        "로그아웃",
        "아이디",
        "비밀번호",
        "로그인 정보 기억하기",
        "계정",
        "설정",
        "프로필",
        "과제",
        "과제목록",
        "문제풀이",
        "대시보드",
        "학생",
        "확인",
        "취소",
        "닫기",
        "뒤로",
        "Login",
        "Logout",
        "ID",
        "Password",
        "Remember me",
        "Settings",
        "Profile",
        "Close",
        "Back",
    )

    fun redact(value: CharSequence?, sensitive: Boolean = false): String? {
        val raw = value?.toString() ?: return null
        if (raw.isEmpty()) return ""
        val normalized = raw.trim()
        if (!sensitive && normalized in safeUiText) return normalized

        val category = if (sensitive) "SENSITIVE" else "TEXT"
        return "<REDACTED_${category} len=${raw.length} fp=${fingerprint(raw)}>"
    }

    private fun fingerprint(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(sessionSalt)
        val bytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.take(8).joinToString("") { "%02x".format(it) }
    }
}
