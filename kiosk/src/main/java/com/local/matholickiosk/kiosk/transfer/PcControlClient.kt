package com.local.matholickiosk.kiosk.transfer

import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

data class PcCsvDownload(
    val deliveryId: String,
    val filename: String,
    val payload: ByteArray,
)

class PcControlClient(
    private val connectTimeoutMs: Int = 2_000,
    private val readTimeoutMs: Int = 4_000,
) {
    fun sendStatus(
        pairing: PcReceiverPairing,
        state: String,
        studentName: String?,
        notify: Boolean,
    ) {
        require(state.isNotBlank() && state.length <= 80) { "PC status is invalid" }
        require(studentName == null || studentName.length <= 80) {
            "PC student display name is invalid"
        }
        val payload = buildString {
            append("{\"state\":\"")
            append(state.jsonEscaped())
            append("\",\"studentName\":")
            if (studentName == null) {
                append("null")
            } else {
                append('"')
                append(studentName.jsonEscaped())
                append('"')
            }
            append(",\"notify\":")
            append(notify)
            append('}')
        }.toByteArray(StandardCharsets.UTF_8)
        try {
            val response = exchange(
                pairing,
                PcControlProtocol.OP_STATUS,
                state,
                payload,
            )
            response.payload.fill(0)
            require(response.accepted) { "PC가 상태 정보를 거부했습니다." }
        } finally {
            payload.fill(0)
        }
    }

    fun fetchStudentCsv(pairing: PcReceiverPairing): PcCsvDownload? {
        val response = exchange(
            pairing,
            PcControlProtocol.OP_FETCH_CSV,
            "FETCH",
            ByteArray(0),
        )
        if (!response.accepted) {
            response.payload.fill(0)
            return null
        }
        val labelParts = response.label.split('|', limit = 2)
        require(
            labelParts.size == 2 &&
                labelParts[0].matches(Regex("^[0-9a-f]{32}$")) &&
                labelParts[1].lowercase().endsWith(".csv") &&
                response.payload.isNotEmpty()
        ) {
            response.payload.fill(0)
            "PC의 CSV 응답이 올바르지 않습니다."
        }
        return PcCsvDownload(labelParts[0], labelParts[1], response.payload)
    }

    fun confirmStudentCsv(pairing: PcReceiverPairing, deliveryId: String) {
        require(deliveryId.matches(Regex("^[0-9a-f]{32}$"))) {
            "PC CSV 전달 식별자가 올바르지 않습니다."
        }
        val response = exchange(
            pairing,
            PcControlProtocol.OP_CONFIRM_CSV,
            deliveryId,
            ByteArray(0),
        )
        try {
            require(response.accepted) { "PC가 CSV 적용 확인을 거부했습니다." }
        } finally {
            response.payload.fill(0)
        }
    }

    private fun exchange(
        pairing: PcReceiverPairing,
        operation: Int,
        label: String,
        payload: ByteArray,
    ): DecodedPcControlResponse {
        val request = PcControlProtocol.encodeRequest(pairing, operation, label, payload)
        try {
            Socket().use { socket ->
                socket.soTimeout = readTimeoutMs
                socket.connect(
                    InetSocketAddress(pairing.host, pairing.port),
                    connectTimeoutMs,
                )
                socket.getOutputStream().apply {
                    write(request.frame)
                    flush()
                }
                val input = socket.getInputStream()
                val header = input.readExact(PcControlProtocol.HEADER_BYTES)
                val fullLength = PcControlProtocol.responseFrameLength(header)
                val frame = ByteArray(fullLength)
                header.copyInto(frame)
                input.readExactInto(frame, header.size, fullLength - header.size)
                return try {
                    val response = PcControlProtocol.decodeResponse(
                        pairing,
                        frame,
                        request.requestId,
                    )
                    require(response.operation == operation) {
                        response.payload.fill(0)
                        "PC control response operation does not match"
                    }
                    response
                } finally {
                    frame.fill(0)
                    header.fill(0)
                }
            }
        } finally {
            request.frame.fill(0)
            request.requestId.fill(0)
        }
    }

    private fun InputStream.readExact(size: Int): ByteArray =
        ByteArray(size).also { readExactInto(it, 0, size) }

    private fun InputStream.readExactInto(target: ByteArray, start: Int, size: Int) {
        var offset = start
        val end = start + size
        while (offset < end) {
            val read = read(target, offset, end - offset)
            require(read >= 0) { "PC 응답이 중간에 종료되었습니다." }
            offset += read
        }
    }

    private fun String.jsonEscaped(): String = buildString(length) {
        this@jsonEscaped.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\b' -> append("\\b")
                '\u000c' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (character.code < 0x20) {
                    append("\\u%04x".format(character.code))
                } else {
                    append(character)
                }
            }
        }
    }
}
