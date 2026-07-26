package com.local.matholickiosk.kiosk.qr

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class QrImageAnalyzer(
    private val codec: QrTokenCodec = QrTokenCodec(),
    private val onDecision: (QrFrameDecision) -> Unit,
) : ImageAnalysis.Analyzer, Closeable {
    private val processing = AtomicBoolean(false)
    private val deliveryGate = QrDecisionDeliveryGate()
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build(),
    )

    fun setEnabled(value: Boolean) {
        deliveryGate.setEnabled(value)
    }

    fun isEnabled(): Boolean = deliveryGate.isEnabled()

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val frameGeneration = deliveryGate.currentFrameGeneration()
        if (frameGeneration == null || !processing.compareAndSet(false, true)) {
            closeFrame(imageProxy)
            return
        }
        val completed = AtomicBoolean(false)
        fun completeFrame() {
            if (completed.compareAndSet(false, true)) {
                processing.set(false)
                closeFrame(imageProxy)
            }
        }

        try {
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                completeFrame()
                return
            }
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnCompleteListener { task ->
                    try {
                        if (task.isSuccessful) {
                            val decision = codec.decideFrame(task.result.map { it.rawValue })
                            if (decision !is QrFrameDecision.Ignore) {
                                deliveryGate.deliverIfCurrent(
                                    frameGeneration,
                                    decision,
                                    onDecision,
                                )
                            }
                        }
                    } catch (_: Exception) {
                        // Ignore a failed frame and leave the analyzer ready for the next one.
                    } finally {
                        completeFrame()
                    }
                }
        } catch (_: Exception) {
            completeFrame()
        }
    }

    override fun close() {
        deliveryGate.setEnabled(false)
        scanner.close()
    }

    private fun closeFrame(imageProxy: ImageProxy) {
        try {
            imageProxy.close()
        } catch (_: Exception) {
            // CameraX owns the frame; a close failure must not terminate the analyzer thread.
        }
    }
}
