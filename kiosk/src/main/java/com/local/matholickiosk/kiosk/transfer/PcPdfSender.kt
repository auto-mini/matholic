package com.local.matholickiosk.kiosk.transfer

import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

class PcPdfSender(
    private val connectTimeoutMs: Int = 5_000,
    private val readTimeoutMs: Int = 10_000,
) {
    fun send(
        pairing: PcReceiverPairing,
        pdfFile: File,
        filename: String,
    ) {
        require(pdfFile.isFile && pdfFile.length() in 1..PcTransferProtocol.MAX_PDF_BYTES) {
            "전송할 PDF가 올바르지 않습니다."
        }
        val pdf = pdfFile.readBytes()
        try {
            val transfer = PcTransferProtocol.encodeRequest(pairing, filename, pdf)
            try {
                Socket().use { socket ->
                    socket.soTimeout = readTimeoutMs
                    socket.connect(
                        InetSocketAddress(pairing.host, pairing.port),
                        connectTimeoutMs,
                    )
                    socket.getOutputStream().apply {
                        write(transfer.frame)
                        flush()
                    }
                    val ack = socket.getInputStream().readExact(PcTransferProtocol.ACK_BYTES)
                    try {
                        PcTransferProtocol.verifyAck(
                            pairing = pairing,
                            frame = ack,
                            expectedRequestId = transfer.requestId,
                            expectedPdfSha256 = transfer.pdfSha256,
                        )
                    } finally {
                        ack.fill(0)
                    }
                }
            } finally {
                transfer.frame.fill(0)
                transfer.requestId.fill(0)
                transfer.pdfSha256.fill(0)
            }
        } finally {
            pdf.fill(0)
        }
    }

    private fun java.io.InputStream.readExact(size: Int): ByteArray {
        val result = ByteArray(size)
        var offset = 0
        try {
            while (offset < size) {
                val read = read(result, offset, size - offset)
                require(read >= 0) { "PC 응답이 중간에 종료되었습니다." }
                offset += read
            }
            return result
        } catch (failure: Throwable) {
            result.fill(0)
            throw failure
        }
    }
}
