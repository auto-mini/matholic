package com.local.matholickiosk.kiosk.domain

import com.local.matholickiosk.kiosk.bridge.CredentialBridgeContract

internal object PcGradingCompletionMessage {
    private const val MAX_PROBLEM_NUMBER = 9_999
    private const val MAX_REPORTED_WRONG_PROBLEMS = 200
    private const val MAX_VISIBLE_WRONG_PROBLEMS = 8
    private const val DETAIL_UNAVAILABLE = "채점 완료 · 상세 결과 확인 필요"

    fun fromWire(outcome: String?, wrongProblemNumbers: IntArray?): String? = when (outcome) {
        CredentialBridgeContract.GRADING_RESULT_COMPLETE ->
            completeMessage(wrongProblemNumbers)
        CredentialBridgeContract.GRADING_RESULT_DETAIL_UNAVAILABLE -> DETAIL_UNAVAILABLE
        else -> null
    }

    private fun completeMessage(wrongProblemNumbers: IntArray?): String {
        if (wrongProblemNumbers == null || wrongProblemNumbers.size > MAX_REPORTED_WRONG_PROBLEMS) {
            return DETAIL_UNAVAILABLE
        }
        val sanitized = wrongProblemNumbers
            .asSequence()
            .filter { it in 1..MAX_PROBLEM_NUMBER }
            .distinct()
            .sorted()
            .toList()
        if (sanitized.isEmpty()) return "채점 완료 · 틀린 문제 없음"

        val visible = sanitized.take(MAX_VISIBLE_WRONG_PROBLEMS)
        val remaining = sanitized.size - visible.size
        return buildString {
            append("채점 완료 · 오답 ${sanitized.size}개: ")
            append(visible.joinToString(", "))
            append("번")
            if (remaining > 0) append(" 외 ${remaining}개")
        }
    }
}
