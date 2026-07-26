package com.local.matholickiosk.kiosk.print

import android.content.ClipData
import android.content.Intent
import android.net.Uri

object QrPdfShareIntentFactory {
    fun create(uri: Uri, displayName: String): Intent =
        Intent(Intent.ACTION_SEND)
            .setType(PDF_MIME_TYPE)
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_SUBJECT, "매쓰홀릭 QR 카드 · $displayName")
            .apply { clipData = ClipData.newRawUri(CLIP_LABEL, uri) }
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    private const val PDF_MIME_TYPE = "application/pdf"
    private const val CLIP_LABEL = "매쓰홀릭 QR 카드 PDF"
}
