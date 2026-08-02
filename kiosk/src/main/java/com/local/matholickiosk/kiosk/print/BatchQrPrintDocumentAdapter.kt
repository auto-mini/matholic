package com.local.matholickiosk.kiosk.print

import android.graphics.Bitmap

data class BatchQrCard(
    val displayName: String,
    val qrBitmap: Bitmap,
)
