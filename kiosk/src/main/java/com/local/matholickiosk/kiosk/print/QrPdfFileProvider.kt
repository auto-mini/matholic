package com.local.matholickiosk.kiosk.print

import androidx.core.content.FileProvider

class QrPdfFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        val created = super.onCreate()
        QrPdfExporter.cleanupExpired(requireNotNull(context))
        return created
    }
}
