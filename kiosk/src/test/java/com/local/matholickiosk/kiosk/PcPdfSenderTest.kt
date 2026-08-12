package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.transfer.PcPdfSender
import com.local.matholickiosk.kiosk.transfer.PcReceiverPairing
import com.local.matholickiosk.kiosk.transfer.PcTransferProtocol
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.util.Base64
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PcPdfSenderTest {
    @Test
    fun `sender writes complete frame and accepts authenticated acknowledgement`() {
        val server = ServerSocket(0, 1, InetAddress.getLoopbackAddress())
        val serverExecutor = Executors.newSingleThreadExecutor()
        val pdfFile = Files.createTempFile("pc-pdf-sender-success", ".pdf")
        val pdf = "%PDF-1.4\nmatholic-test\n%%EOF\n".toByteArray()
        Files.write(pdfFile, pdf)
        pdf.fill(0)
        val requestId = "102132435465768798a9babbdcddedef".hex()
        val pairing = PcReceiverPairing(
            receiverId = "00112233445566778899aabbccddeeff".hex(),
            secret = ByteArray(32) { index -> index.toByte() },
            host = requireNotNull(InetAddress.getLoopbackAddress().hostAddress),
            port = server.localPort,
            displayName = "정상 시험 PC",
        )
        val ack = Base64.getDecoder().decode(
            "TUFUSEFDSzEBECEyQ1RldoeYqbq73N3t77hn2ZnGMDRqIz6OwjGjo9+xIydW+JCkzl5i" +
                "5IwFcuk2uBbydtZjUVSJ+X5VFCu7IF2NsAAeNCCKBQkELJUQWU4=",
        )
        val serverTask = serverExecutor.submit {
            server.accept().use { connection ->
                val header = connection.getInputStream().readExact(REQUEST_HEADER_BYTES)
                val ciphertextBytes = ByteBuffer.wrap(header)
                    .order(ByteOrder.BIG_ENDIAN)
                    .getInt(REQUEST_HEADER_BYTES - Int.SIZE_BYTES)
                val body = connection.getInputStream().readExact(ciphertextBytes)
                try {
                    connection.getOutputStream().apply {
                        write(ack)
                        flush()
                    }
                } finally {
                    header.fill(0)
                    body.fill(0)
                }
            }
        }

        try {
            PcPdfSender(
                connectTimeoutMs = 500,
                readTimeoutMs = 500,
            ).send(
                pairing = pairing,
                pdfFile = pdfFile.toFile(),
                filename = "normal-write-test.pdf",
                requestId = requestId,
            )
            serverTask.get(2, TimeUnit.SECONDS)
        } finally {
            runCatching { server.close() }
            serverTask.cancel(true)
            serverExecutor.shutdownNow()
            assertTrue(serverExecutor.awaitTermination(2, TimeUnit.SECONDS))
            pairing.clearSensitiveData()
            requestId.fill(0)
            ack.fill(0)
            Files.deleteIfExists(pdfFile)
        }
    }

    @Test
    fun `sender is bounded when connected receiver does not read pdf`() {
        val server = ServerSocket().apply {
            receiveBufferSize = 1_024
            bind(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 1)
        }
        val accepted = CompletableFuture<Socket>()
        val serverExecutor = Executors.newSingleThreadExecutor()
        val senderExecutor = Executors.newSingleThreadExecutor()
        val pdfFile = Files.createTempFile("pc-pdf-sender-stall", ".pdf")
        val pairing = PcReceiverPairing(
            receiverId = ByteArray(16) { index -> index.toByte() },
            secret = ByteArray(32) { index -> (index + 16).toByte() },
            host = requireNotNull(InetAddress.getLoopbackAddress().hostAddress),
            port = server.localPort,
            displayName = "무응답 시험 PC",
        )
        val pdf = ByteArray(PcTransferProtocol.MAX_PDF_BYTES).also { bytes ->
            "%PDF-1.4\n".toByteArray().copyInto(bytes)
        }
        Files.write(pdfFile, pdf)
        pdf.fill(0)

        var connection: Socket? = null
        var sendTask: java.util.concurrent.Future<Throwable?>? = null
        try {
            serverExecutor.execute {
                runCatching {
                    server.accept().apply { receiveBufferSize = 1_024 }
                }.fold(accepted::complete, accepted::completeExceptionally)
            }
            sendTask = senderExecutor.submit<Throwable?> {
                runCatching {
                    PcPdfSender(
                        connectTimeoutMs = 500,
                        readTimeoutMs = 250,
                    ).send(
                        pairing = pairing,
                        pdfFile = pdfFile.toFile(),
                        filename = "bounded-write-test.pdf",
                    )
                }.exceptionOrNull()
            }
            connection = accepted.get(2, TimeUnit.SECONDS)

            val failure = try {
                sendTask.get(2, TimeUnit.SECONDS)
            } catch (_: TimeoutException) {
                null
            }
            assertNotNull("PDF send exceeded its bounded I/O timeout", failure)
            assertTrue(failure is SocketTimeoutException)
        } finally {
            runCatching { connection?.close() }
            runCatching { server.close() }
            sendTask?.cancel(true)
            senderExecutor.shutdownNow()
            serverExecutor.shutdownNow()
            assertTrue(senderExecutor.awaitTermination(2, TimeUnit.SECONDS))
            assertTrue(serverExecutor.awaitTermination(2, TimeUnit.SECONDS))
            pairing.clearSensitiveData()
            Files.deleteIfExists(pdfFile)
        }
    }

    private fun java.io.InputStream.readExact(size: Int): ByteArray {
        val result = ByteArray(size)
        var offset = 0
        while (offset < size) {
            val read = read(result, offset, size - offset)
            require(read >= 0) { "Test receiver frame ended early" }
            offset += read
        }
        return result
    }

    private fun String.hex(): ByteArray =
        chunked(2).map { pair -> pair.toInt(16).toByte() }.toByteArray()

    private companion object {
        const val REQUEST_HEADER_BYTES = 65
    }
}
