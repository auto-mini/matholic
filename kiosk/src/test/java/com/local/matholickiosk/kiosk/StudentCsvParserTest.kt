package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.data.StudentCsvParser
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class StudentCsvParserTest {
    @Test
    fun `parses quoted Korean CSV and multiple classes`() {
        val parsed = StudentCsvParser.parse(
            (
                "\uFEFF이름,아이디,비밀번호,소속 반들\r\n" +
                    "\"테스트, 학생\",test01,\"pw\"\"123\",월1|화2\r\n"
                ).toByteArray(),
        )
        try {
            assertEquals(1, parsed.rows.size)
            val row = parsed.rows.single()
            assertEquals("테스트, 학생", row.displayNameExact)
            assertArrayEquals("test01".toCharArray(), row.username)
            assertArrayEquals("pw\"123".toCharArray(), row.password)
            assertEquals(setOf("월1", "화2"), row.classNames)
        } finally {
            parsed.clearSensitiveData()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects duplicate usernames`() {
        StudentCsvParser.parse(
            (
                "이름,아이디,비밀번호,소속 반들\n" +
                    "학생1,same,pw1,월1\n" +
                    "학생2,same,pw2,월2\n"
                ).toByteArray(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects unexpected headers`() {
        StudentCsvParser.parse(
            "name,id,password,class\nx,y,z,월1\n".toByteArray(),
        )
    }
}
