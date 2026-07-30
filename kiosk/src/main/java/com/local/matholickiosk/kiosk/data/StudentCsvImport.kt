package com.local.matholickiosk.kiosk.data

import java.nio.charset.StandardCharsets

data class StudentCsvRow(
    val displayNameExact: String,
    val username: CharArray,
    val password: CharArray,
    val classNames: Set<String>,
) {
    fun clearSensitiveData() {
        username.fill('\u0000')
        password.fill('\u0000')
    }
}

data class ParsedStudentCsv(
    val rows: List<StudentCsvRow>,
) {
    fun clearSensitiveData() {
        rows.forEach(StudentCsvRow::clearSensitiveData)
    }
}

data class StudentCsvImportResult(
    val created: Int,
    val updated: Int,
    val cardsNeedingPrint: Int,
)

data class StudentCsvImportPreview(
    val created: Int,
    val updated: Int,
    val renamed: Int,
    val cardsNeedingPrintAfterImport: Int,
)

object StudentCsvParser {
    private val expectedHeaders = listOf("이름", "아이디", "비밀번호", "소속 반들")

    fun parse(payload: ByteArray): ParsedStudentCsv {
        require(payload.isNotEmpty() && payload.size <= 1024 * 1024) {
            "CSV 파일은 1MB 이하여야 합니다."
        }
        val text = String(payload, StandardCharsets.UTF_8).removePrefix("\uFEFF")
        val records = parseRecords(text)
            .filterNot { record -> record.all(String::isBlank) }
        require(records.isNotEmpty()) { "CSV가 비어 있습니다." }
        require(records.first().map(String::trim) == expectedHeaders) {
            "CSV 첫 줄은 이름,아이디,비밀번호,소속 반들 순서여야 합니다."
        }
        require(records.size in 2..1001) { "학생은 한 번에 1~1000명까지 처리할 수 있습니다." }
        val usernames = mutableSetOf<String>()
        val rows = records.drop(1).mapIndexed { index, values ->
            require(values.size == expectedHeaders.size) {
                "CSV ${index + 2}행의 열 개수가 올바르지 않습니다."
            }
            val name = values[0].trim()
            val username = values[1].trim()
            val password = values[2]
            val classes = values[3]
                .split('|')
                .map(String::trim)
                .filter(String::isNotEmpty)
                .toSet()
            require(name.isNotEmpty() && name.length <= 80) {
                "CSV ${index + 2}행의 이름이 올바르지 않습니다."
            }
            require(username.isNotEmpty() && username.length <= 200) {
                "CSV ${index + 2}행의 아이디가 올바르지 않습니다."
            }
            require(password.isNotEmpty() && password.length <= 200) {
                "CSV ${index + 2}행의 비밀번호가 올바르지 않습니다."
            }
            require(classes.isNotEmpty()) {
                "CSV ${index + 2}행에는 소속 반이 하나 이상 필요합니다."
            }
            require(usernames.add(username)) {
                "CSV에 같은 아이디가 두 번 포함되어 있습니다."
            }
            StudentCsvRow(
                displayNameExact = name,
                username = username.toCharArray(),
                password = password.toCharArray(),
                classNames = classes,
            )
        }
        return ParsedStudentCsv(rows)
    }

    private fun parseRecords(text: String): List<List<String>> {
        val records = mutableListOf<List<String>>()
        var record = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var index = 0
        while (index < text.length) {
            val character = text[index]
            when {
                quoted && character == '"' && index + 1 < text.length &&
                    text[index + 1] == '"' -> {
                    field.append('"')
                    index += 1
                }
                character == '"' -> quoted = !quoted
                !quoted && character == ',' -> {
                    record += field.toString()
                    field.setLength(0)
                }
                !quoted && (character == '\n' || character == '\r') -> {
                    if (character == '\r' && index + 1 < text.length && text[index + 1] == '\n') {
                        index += 1
                    }
                    record += field.toString()
                    field.setLength(0)
                    records += record
                    record = mutableListOf()
                }
                else -> field.append(character)
            }
            index += 1
        }
        require(!quoted) { "CSV 따옴표가 닫히지 않았습니다." }
        if (field.isNotEmpty() || record.isNotEmpty()) {
            record += field.toString()
            records += record
        }
        return records
    }
}
