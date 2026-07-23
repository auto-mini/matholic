package com.local.matholickiosk.kiosk.bridge

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Binder

class CredentialBridgeProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        enforceTrustedCaller()
        require(projection == null && selection == null && selectionArgs == null && sortOrder == null) {
            "Credential bridge does not accept query modifiers"
        }
        val segments = uri.pathSegments
        require(uri.authority == CredentialBridgeContract.AUTHORITY)
        require(segments.size == 2 && segments[0] == "v1") { "Invalid credential bridge URI" }
        val payload = OneTimeCredentialBroker.consume(segments[1]) ?: return null
        return payload.use {
            MatrixCursor(
                arrayOf(
                    CredentialBridgeContract.COLUMN_EXPECTED_NAME,
                    CredentialBridgeContract.COLUMN_USERNAME,
                    CredentialBridgeContract.COLUMN_PASSWORD,
                ),
                1,
            ).apply {
                addRow(
                    arrayOf(
                        payload.expectedDisplayName,
                        String(payload.username),
                        String(payload.password),
                    ),
                )
            }
        }
    }

    override fun getType(uri: Uri): String =
        "vnd.android.cursor.item/vnd.matholic-kiosk.credentials"

    override fun insert(uri: Uri, values: ContentValues?): Uri? =
        throw UnsupportedOperationException("Read-once provider")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("Read-once provider")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = throw UnsupportedOperationException("Read-once provider")

    private fun enforceTrustedCaller() {
        val callingUid = Binder.getCallingUid()
        if (callingUid == android.os.Process.myUid()) return
        val packages = requireNotNull(context).packageManager.getPackagesForUid(callingUid).orEmpty()
        if (CredentialBridgeContract.TRUSTED_CONSUMER_PACKAGE !in packages) {
            throw SecurityException("Untrusted credential bridge caller")
        }
    }
}
