package com.local.matholickiosk.webpoc

import android.content.Context
import android.content.Intent

internal const val EXTRA_REMOTE_SUPPORT_ENABLED = "enabled"
internal const val EXTRA_REMOTE_SUPPORT_DURATION_SECONDS = "duration_seconds"
internal const val DEFAULT_REMOTE_SUPPORT_DURATION_SECONDS = 30 * 60

internal fun applyRemoteSupportIntent(
    context: Context,
    intent: Intent,
): Boolean = runCatching {
    val store = RemoteSupportStore(context)
    if (intent.getBooleanExtra(EXTRA_REMOTE_SUPPORT_ENABLED, false)) {
        val seconds = intent
            .getIntExtra(
                EXTRA_REMOTE_SUPPORT_DURATION_SECONDS,
                DEFAULT_REMOTE_SUPPORT_DURATION_SECONDS,
            )
            .toLong()
        store.enable(seconds * 1_000L)
    } else {
        store.disable()
    }
}.isSuccess
