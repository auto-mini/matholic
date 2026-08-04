package com.local.matholickiosk.webpoc

internal enum class StudentHelpContext {
    NONE,
    WORKBOOK,
    DIAGNOSTIC,
    PROBLEM,
    PROBLEM_OBJECTIVE,
    PROBLEM_SUBJECTIVE,
    PROBLEM_MAP,
    REVIEW,
    RESULT,
    ;

    companion object {
        fun fromContract(value: String?): StudentHelpContext =
            entries.firstOrNull { it.name == value?.trim()?.uppercase() } ?: NONE

        fun fromPath(path: String?): StudentHelpContext = when {
            path == StudentWebPolicy.WORKBOOK_PATH ||
                path?.startsWith("${StudentWebPolicy.WORKBOOK_PATH}/") == true -> WORKBOOK
            path == StudentWebPolicy.DIAGNOSTIC_PATH ||
                path?.startsWith("${StudentWebPolicy.DIAGNOSTIC_PATH}/") == true -> DIAGNOSTIC
            path?.startsWith("/learningV2/") == true -> PROBLEM
            else -> NONE
        }
    }
}

internal data class StudentHelpCopy(
    val eyebrow: String,
    val title: String,
    val lead: String,
    val steps: String,
    val caution: String,
)

internal object StudentHelpContent {
    fun forContext(context: StudentHelpContext): StudentHelpCopy? = when (context) {
        StudentHelpContext.NONE -> null
        StudentHelpContext.WORKBOOK -> StudentHelpCopy(
            eyebrow = "학습지",
            title = "채점할 학습지를 고르는 화면",
            lead = "제목과 단원을 확인한 뒤 해당 줄의 ‘학습하기’를 누르세요.",
            steps = "1. 선생님이 안내한 학습지의 제목과 단원을 찾습니다.\n" +
                "2. 같은 줄의 ‘학습하기’를 누르면 문제 화면으로 이동합니다.",
            caution = "내 이름이나 학습지가 다르면 더 누르지 말고 선생님을 불러주세요.",
        )
        StudentHelpContext.DIAGNOSTIC -> StudentHelpCopy(
            eyebrow = "진단평가",
            title = "응시할 진단평가를 고르는 화면",
            lead = "평가 이름을 확인하고 해당 줄의 시작 버튼을 누르세요.",
            steps = "1. 선생님이 안내한 평가 이름을 찾습니다.\n" +
                "2. 같은 줄의 시작 버튼을 누르면 문제 화면으로 이동합니다.",
            caution = "안내받은 평가가 없거나 다른 평가만 보이면 선생님을 불러주세요.",
        )
        StudentHelpContext.PROBLEM -> StudentHelpCopy(
            eyebrow = "문제 풀이",
            title = "답을 입력하고 문제를 이동하는 화면",
            lead = "답을 고르거나 입력하면 저장됩니다. 제출 전까지 다시 바꿀 수 있습니다.",
            steps = "1. 객관식은 답 번호를, 주관식은 답 입력칸을 누릅니다.\n" +
                "2. 답을 모르겠으면 ‘모름’을 누릅니다.\n" +
                "3. ‘이전 문제’와 ‘다음 문제’로 이동합니다.\n" +
                "4. ‘답안 현황’에서 빠진 문제를 확인합니다.\n" +
                "5. 모두 풀었으면 오른쪽 위 ‘답안제출’을 누릅니다.",
            caution = "빨간 ‘채점 끝내기’는 답안 제출이 아니라 현재 학생의 채점을 중단하는 버튼입니다.",
        )
        StudentHelpContext.PROBLEM_OBJECTIVE -> StudentHelpCopy(
            eyebrow = "객관식 문제",
            title = "보기에서 정답을 하나 고르는 화면",
            lead = "고른 보기는 바로 저장되며 제출 전까지 다시 바꿀 수 있습니다.",
            steps = "1. 정답이라고 생각하는 보기 하나를 누릅니다.\n" +
                "2. 모르겠으면 ‘모름’을 누릅니다.\n" +
                "3. ‘다음 문제’로 이동하고 ‘답안 현황’에서 빠진 답을 확인합니다.",
            caution = "‘답안제출’을 누르기 전까지는 최종 채점되지 않습니다.",
        )
        StudentHelpContext.PROBLEM_SUBJECTIVE -> StudentHelpCopy(
            eyebrow = "주관식 문제",
            title = "숫자나 수식을 직접 입력하는 화면",
            lead = "답 입력칸을 누르면 ‘답안 현황’ 바로 아래에 수식 키패드가 열립니다.",
            steps = "1. 답 입력칸을 누르고 숫자·소수점·분수·루트·파이를 입력합니다.\n" +
                "2. 문제가 분수나 소수 형식을 지정하면 그 형식에 맞춥니다.\n" +
                "3. ‘다음 문제’로 이동하고 ‘답안 현황’에서 빠진 답을 확인합니다.",
            caution = "키오스크는 입력값을 바꾸지 않고 매쓰홀릭 채점 서버에 전달합니다.",
        )
        StudentHelpContext.PROBLEM_MAP -> StudentHelpCopy(
            eyebrow = "답안 현황",
            title = "입력한 답과 빠진 문제를 확인하는 화면",
            lead = "답안 현황이 열려 있을 때는 현황판 안의 문제 번호만 안내합니다.",
            steps = "1. 답변·모름·현재 문제 표시를 확인합니다.\n" +
                "2. 이동할 문제 번호를 누르면 해당 문제로 이동합니다.\n" +
                "3. 현황판 밖을 누르면 현황판만 닫히며, 뒤의 버튼은 눌리지 않습니다.",
            caution = "답안 현황은 확인과 이동용입니다. 실제 제출은 오른쪽 위 ‘답안제출’에서 시작합니다.",
        )
        StudentHelpContext.REVIEW -> StudentHelpCopy(
            eyebrow = "전체답안 확인",
            title = "제출 전에 모든 답을 확인하는 화면",
            lead = "여기서는 아직 최종 제출되지 않았습니다. 빠진 답과 잘못 누른 답을 확인하세요.",
            steps = "1. 1번부터 마지막 문제까지 답이 있는지 확인합니다.\n" +
                "2. 수정할 답이 있으면 ‘닫기’를 눌러 문제로 돌아갑니다.\n" +
                "3. 수정할 내용이 없으면 아래의 큰 ‘답안 제출’을 누릅니다.",
            caution = "아래의 ‘답안 제출’을 누르면 실제 채점이 시작되며 답을 다시 바꿀 수 없습니다.",
        )
        StudentHelpContext.RESULT -> StudentHelpCopy(
            eyebrow = "채점 결과",
            title = "틀린 문제를 확인하는 화면",
            lead = "틀린 문제 번호를 확인한 뒤 계속 채점하거나 학생 채점을 끝내세요.",
            steps = "1. 표시된 틀린 문제 번호를 확인합니다.\n" +
                "2. 다른 학습지도 채점하려면 ‘다른 학습지 계속 채점’을 누릅니다.\n" +
                "3. 모두 끝났으면 ‘확인하고 채점 끝내기’를 누릅니다.",
            caution = "결과를 정확히 확인하지 못했다는 안내가 나오면 선생님을 불러주세요.",
        )
    }
}
