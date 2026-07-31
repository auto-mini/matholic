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
    private val onGuidance: (QrFrameGuidance) -> Unit = {},
    private val onQuality: (QrFrameQuality) -> Unit = {},
    private val onRawQr: (String) -> Boolean = { false },
) : ImageAnalysis.Analyzer, Closeable {
    private val processing = AtomicBoolean(false)
    private val deliveryGate = QrDecisionDeliveryGate()
    private val qualityStabilizer = QrFrameQualityStabilizer()
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build(),
    )
    @Volatile
    private var frontFacing = false
    private var lastGuidance: QrFrameGuidance? = null
    private var lastGuidanceAtNanos = 0L
    private var lastQuality: QrFrameQuality? = null
    private var lastQualityAtNanos = 0L

    fun setEnabled(value: Boolean) {
        if (!value) {
            lastGuidance = null
            lastQuality = null
            qualityStabilizer.reset()
        }
        deliveryGate.setEnabled(value)
    }

    fun isEnabled(): Boolean = deliveryGate.isEnabled()

    fun setFrontFacing(value: Boolean) {
        frontFacing = value
        lastGuidance = null
    }

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
            val frameQuality = sampleFrameQuality(imageProxy)
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnCompleteListener { task ->
                    try {
                        if (task.isSuccessful) {
                            val barcodes = task.result
                            var matholicQrDetected = false
                            var matholicGuidance: QrFrameGuidance? = null
                            if (barcodes.size == 1) {
                                val barcode = barcodes[0]
                                val rawValue = barcode.rawValue
                                if (rawValue != null && onRawQr(rawValue)) {
                                    return@addOnCompleteListener
                                }
                                if (barcode.rawValue?.startsWith("MQR1:") == true) {
                                    matholicQrDetected = true
                                    matholicGuidance = deliverGuidance(
                                        barcode,
                                        imageProxy.width,
                                        imageProxy.height,
                                        imageProxy.imageInfo.rotationDegrees,
                                    )
                                }
                            }
                            qualityStabilizer.accept(
                                frameQuality.takeIf { barcodes.isEmpty() },
                            )?.let(::deliverQuality)
                            if (
                                matholicQrDetected &&
                                matholicGuidance != QrFrameGuidance.CENTERED
                            ) {
                                return@addOnCompleteListener
                            }
                            val decision = codec.decideFrame(barcodes.map { it.rawValue })
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

    private fun deliverGuidance(
        barcode: Barcode,
        sourceWidth: Int,
        sourceHeight: Int,
        rotationDegrees: Int,
    ): QrFrameGuidance? {
        val boundingBox = barcode.boundingBox ?: return null
        val rotated = rotationDegrees == 90 || rotationDegrees == 270
        val imageWidth = if (rotated) sourceHeight else sourceWidth
        val imageHeight = if (rotated) sourceWidth else sourceHeight
        val guidance = QrPositionGuide.classify(
            bounds = QrFrameBounds(
                left = boundingBox.left,
                top = boundingBox.top,
                right = boundingBox.right,
                bottom = boundingBox.bottom,
            ),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            mirrorHorizontally = frontFacing,
        )
        val now = System.nanoTime()
        if (
            guidance != lastGuidance ||
            now - lastGuidanceAtNanos >= GUIDANCE_REPEAT_NANOS
        ) {
            lastGuidance = guidance
            lastGuidanceAtNanos = now
            onGuidance(guidance)
        }
        return guidance
    }

    private fun closeFrame(imageProxy: ImageProxy) {
        try {
            imageProxy.close()
        } catch (_: Exception) {
            // CameraX owns the frame; a close failure must not terminate the analyzer thread.
        }
    }

    private fun sampleFrameQuality(imageProxy: ImageProxy): QrFrameQuality? {
        val plane = imageProxy.planes.firstOrNull() ?: return null
        val buffer = plane.buffer.duplicate()
        val width = imageProxy.width
        val height = imageProxy.height
        if (width <= 0 || height <= 0 || plane.rowStride <= 0) return null
        val samples = IntArray(QUALITY_GRID * QUALITY_GRID)
        var count = 0
        for (row in 0 until QUALITY_GRID) {
            val y = ((row + 0.5f) * height / QUALITY_GRID).toInt().coerceIn(0, height - 1)
            for (column in 0 until QUALITY_GRID) {
                val x = ((column + 0.5f) * width / QUALITY_GRID).toInt().coerceIn(0, width - 1)
                val index = y * plane.rowStride + x * plane.pixelStride
                if (index in 0 until buffer.limit()) {
                    samples[count++] = buffer.get(index).toInt() and 0xff
                }
            }
        }
        return QrFrameQualityClassifier.classify(samples.copyOf(count))
    }

    private fun deliverQuality(quality: QrFrameQuality) {
        val now = System.nanoTime()
        if (quality != lastQuality || now - lastQualityAtNanos >= QUALITY_REPEAT_NANOS) {
            lastQuality = quality
            lastQualityAtNanos = now
            onQuality(quality)
        }
    }

    private companion object {
        const val GUIDANCE_REPEAT_NANOS = 400_000_000L
        const val QUALITY_REPEAT_NANOS = 1_200_000_000L
        const val QUALITY_GRID = 12
    }
}
