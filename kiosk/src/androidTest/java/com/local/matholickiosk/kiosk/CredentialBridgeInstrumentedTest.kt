package com.local.matholickiosk.kiosk

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.bridge.CredentialBridgeContract
import com.local.matholickiosk.kiosk.bridge.OneTimeCredentialBroker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CredentialBridgeInstrumentedTest {
    @Test
    fun providerReturnsPayloadOnceAndNeverPlacesItInUri() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val username = "bridge-user".toCharArray()
        val password = "bridge-password".toCharArray()
        val handle = OneTimeCredentialBroker.publish(
            "가상학생-가",
            username,
            password,
        )
        username.fill('\u0000')
        password.fill('\u0000')

        assertEquals(false, handle.uri.toString().contains("bridge-user"))
        context.contentResolver.query(handle.uri, null, null, null, null).use { cursor ->
            requireNotNull(cursor)
            cursor.moveToFirst()
            assertEquals(
                "가상학생-가",
                cursor.getString(cursor.getColumnIndexOrThrow(CredentialBridgeContract.COLUMN_EXPECTED_NAME)),
            )
            assertEquals(
                "bridge-user",
                cursor.getString(cursor.getColumnIndexOrThrow(CredentialBridgeContract.COLUMN_USERNAME)),
            )
            assertEquals(
                "bridge-password",
                cursor.getString(cursor.getColumnIndexOrThrow(CredentialBridgeContract.COLUMN_PASSWORD)),
            )
        }
        assertNull(context.contentResolver.query(handle.uri, null, null, null, null))
    }

    @Test
    fun expiredAndRevokedHandlesCannotBeConsumed() {
        val expired = OneTimeCredentialBroker.publish(
            "가상학생-가",
            "user".toCharArray(),
            "password".toCharArray(),
            nowElapsedMs = 1_000,
        )
        assertNull(OneTimeCredentialBroker.consume(expired.id, nowElapsedMs = 31_001))

        val revoked = OneTimeCredentialBroker.publish(
            "가상학생-나",
            "user".toCharArray(),
            "password".toCharArray(),
            nowElapsedMs = 2_000,
        )
        OneTimeCredentialBroker.revoke(revoked.id)
        assertNull(OneTimeCredentialBroker.consume(revoked.id, nowElapsedMs = 2_001))
    }
}
