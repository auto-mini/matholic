package com.local.matholickiosk.kiosk

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.transfer.PcPdfSender
import com.local.matholickiosk.kiosk.transfer.PcReceiverPairing
import com.local.matholickiosk.kiosk.transfer.PcTransferProtocol
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PcPdfSenderInstrumentedTest {
    @Test
    fun maxPdfWriteTimesOutWhenConnectedReceiverDoesNotRead() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val server = ServerSocket().apply {
            receiveBufferSize = 1_024
            bind(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 1)
        }
        val accepted = CompletableFuture<Socket>()
        val serverExecutor = Executors.newSingleThreadExecutor()
        val pdfFile = java.io.File.createTempFile("pc-pdf-write-stall", ".pdf", context.cacheDir)
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
        pdfFile.writeBytes(pdf)
        pdf.fill(0)

        var connection: Socket? = null
        try {
            serverExecutor.execute {
                runCatching {
                    server.accept().apply { receiveBufferSize = 1_024 }
                }.fold(accepted::complete, accepted::completeExceptionally)
            }
            val started = android.os.SystemClock.elapsedRealtime()
            val failure = runCatching {
                PcPdfSender(
                    connectTimeoutMs = 500,
                    readTimeoutMs = 250,
                ).send(
                    pairing = pairing,
                    pdfFile = pdfFile,
                    filename = "android-bounded-write-test.pdf",
                )
            }.exceptionOrNull()
            val elapsed = android.os.SystemClock.elapsedRealtime() - started
            connection = accepted.get(2, TimeUnit.SECONDS)

            assertTrue(failure is SocketTimeoutException)
            assertTrue("PDF send exceeded its Android timeout bound: $elapsed ms", elapsed < 2_000)
        } finally {
            runCatching { connection?.close() }
            runCatching { server.close() }
            serverExecutor.shutdownNow()
            assertTrue(serverExecutor.awaitTermination(2, TimeUnit.SECONDS))
            pairing.clearSensitiveData()
            assertTrue(pdfFile.delete() || !pdfFile.exists())
        }
    }
}
