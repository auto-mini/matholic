package com.local.matholickiosk.kiosk.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import java.io.File

object QrPdfExporter {
    private val sharedFileCleanupHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun export(
        context: Context,
        displayName: String,
        qrBitmap: Bitmap,
    ): File {
        val directory = File(context.cacheDir, EXPORT_DIRECTORY).apply { mkdirs() }
        cleanupExpired(directory)
        val output = File(directory, "matholic-qr-card-${System.currentTimeMillis()}.pdf")
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
            .setMinMargins(PrintAttributes.Margins(500, 500, 500, 500))
            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            .build()
        try {
            ParcelFileDescriptor.open(
                output,
                ParcelFileDescriptor.MODE_CREATE or
                    ParcelFileDescriptor.MODE_TRUNCATE or
                    ParcelFileDescriptor.MODE_READ_WRITE,
            ).use { destination ->
                check(
                    QrPrintPdfWriter.write(
                        context = context,
                        attributes = attributes,
                        displayName = displayName,
                        qrBitmap = qrBitmap,
                        destination = destination.fileDescriptor,
                        cancellationSignal = CancellationSignal(),
                    ),
                ) {
                    "PDF export was cancelled"
                }
            }
            return output
        } catch (failure: Throwable) {
            output.delete()
            throw failure
        } finally {
            releaseSensitiveBitmap(qrBitmap)
        }
    }

    internal fun <T> consumeSensitiveBitmap(
        bitmap: Bitmap,
        operation: (Bitmap) -> T,
    ): T =
        try {
            operation(bitmap)
        } finally {
            releaseSensitiveBitmap(bitmap)
        }

    fun cleanupExpired(context: Context) {
        cleanupExpired(File(context.cacheDir, EXPORT_DIRECTORY))
    }

    internal fun scheduleSharedFileCleanup(
        context: Context,
        file: File,
        delayMillis: Long = SHARED_FILE_CLEANUP_DELAY_MS,
    ) {
        require(delayMillis >= 0L) { "Cleanup delay must not be negative" }
        val exportDirectory = File(context.cacheDir, EXPORT_DIRECTORY).canonicalFile
        val export = file.canonicalFile
        require(export.parentFile == exportDirectory) {
            "Shared PDF must be inside the QR export directory"
        }
        sharedFileCleanupHandler.postDelayed(
            { export.delete() },
            delayMillis,
        )
    }

    private fun cleanupExpired(directory: File) {
        val cutoff = System.currentTimeMillis() - EXPORT_RETENTION_MS
        directory.listFiles()
            ?.filter { it.isFile && it.lastModified() < cutoff }
            ?.forEach(File::delete)
    }

    private fun releaseSensitiveBitmap(bitmap: Bitmap) {
        if (!bitmap.isRecycled) {
            if (bitmap.isMutable) bitmap.eraseColor(Color.WHITE)
            bitmap.recycle()
        }
    }

    private const val EXPORT_DIRECTORY = "qr_exports"
    private const val SHARED_FILE_CLEANUP_DELAY_MS = 30_000L
    private const val EXPORT_RETENTION_MS = 60L * 60L * 1_000L
}
