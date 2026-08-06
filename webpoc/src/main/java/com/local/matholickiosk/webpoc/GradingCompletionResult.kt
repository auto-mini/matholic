package com.local.matholickiosk.webpoc

internal enum class GradingCompletionOutcome(val wireValue: String) {
    COMPLETE("complete"),
    DETAIL_UNAVAILABLE("detail_unavailable"),
}

internal data class GradingCompletionResult(
    val outcome: GradingCompletionOutcome,
    val wrongProblemNumbers: List<Int>,
) {
    companion object {
        private const val MAX_PROBLEM_NUMBER = 9_999
        private const val MAX_REPORTED_WRONG_PROBLEMS = 200

        fun complete(wrongProblemNumbers: List<Int>): GradingCompletionResult {
            val sanitized = wrongProblemNumbers
                .asSequence()
                .filter { it in 1..MAX_PROBLEM_NUMBER }
                .distinct()
                .sorted()
                .toList()
            return if (sanitized.size <= MAX_REPORTED_WRONG_PROBLEMS) {
                GradingCompletionResult(GradingCompletionOutcome.COMPLETE, sanitized)
            } else {
                unavailable()
            }
        }

        fun unavailable(): GradingCompletionResult = GradingCompletionResult(
            outcome = GradingCompletionOutcome.DETAIL_UNAVAILABLE,
            wrongProblemNumbers = emptyList(),
        )
    }
}
