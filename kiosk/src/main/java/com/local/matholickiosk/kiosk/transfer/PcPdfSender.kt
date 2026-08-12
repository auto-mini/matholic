package com.local.matholickiosk.kiosk.transfer

import java.io.File
import java.io.InterruptedIOException
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class PcPdfSender(
    private val connectTimeoutMs: Int = 5_000,
    private val readTimeoutMs: Int = 10_000,
    private val writeTimeoutMs: Int = readTimeoutMs,
) {
    fun send(
        pairing: PcReceiverPairing,
        pdfFile: File,
        filename: String,
        requestId: ByteArray? = null,
    ) {
        require(pdfFile.isFile && pdfFile.length() in 1..PcTransferProtocol.MAX_PDF_BYTES) {
            "전송할 PDF가 올바르지 않습니다."
        }
        val pdf = pdfFile.readBytes()
        try {
            val transfer = if (requestId == null) {
                PcTransferProtocol.encodeRequest(pairing, filename, pdf)
            } else {
                PcTransferProtocol.encodeRequest(
                    pairing = pairing,
                    filename = filename,
                    pdf = pdf,
                    requestId = requestId,
                )
            }
            try {
                SocketChannel.open().use { channel ->
                    val socket = channel.socket()
                    socket.soTimeout = readTimeoutMs
                    socket.connect(
                        InetSocketAddress(pairing.host, pairing.port),
                        connectTimeoutMs,
                    )
                    channel.writeExact(transfer.frame, writeTimeoutMs)
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

    private fun SocketChannel.writeExact(frame: ByteArray, timeoutMs: Int) {
        require(timeoutMs > 0) { "PC PDF write timeout must be positive" }
        val finished = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>()
        val writer = Thread(
            {
                try {
                    val buffer = ByteBuffer.wrap(frame)
                    while (buffer.hasRemaining()) {
                        check(write(buffer) > 0) { "PC PDF write made no progress" }
                    }
                } catch (writeFailure: Throwable) {
                    failure.set(writeFailure)
                } finally {
                    finished.countDown()
                }
            },
            "pc-pdf-write",
        ).apply { isDaemon = true }
        writer.start()
        var writeCompleted = false
        try {
            writeCompleted = finished.await(timeoutMs.toLong(), TimeUnit.MILLISECONDS)
            if (!writeCompleted) {
                throw SocketTimeoutException(
                    "지정 PC가 PDF 수신을 시작하지 않아 전송 시간이 초과되었습니다.",
                )
            }
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
            throw InterruptedIOException("PC PDF 전송 대기가 중단되었습니다.").apply {
                initCause(interrupted)
            }
        } finally {
            stopWriter(writer, closeFirst = !writeCompleted)
        }
        failure.get()?.let { throw it }
    }

    private fun SocketChannel.stopWriter(writer: Thread, closeFirst: Boolean) {
        if (!writer.isAlive) return
        if (closeFirst) {
            runCatching { close() }
            writer.interrupt()
        }
        var restoreInterrupt = Thread.interrupted()
        try {
            writer.join(WRITER_STOP_TIMEOUT_MS)
        } catch (_: InterruptedException) {
            restoreInterrupt = true
        }
        if (writer.isAlive) {
            runCatching { close() }
            writer.interrupt()
        }
        if (restoreInterrupt) Thread.currentThread().interrupt()
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

    private companion object {
        const val WRITER_STOP_TIMEOUT_MS = 1_000L
    }
}
