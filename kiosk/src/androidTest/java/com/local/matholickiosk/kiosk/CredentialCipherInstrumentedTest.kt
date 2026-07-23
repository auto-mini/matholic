package com.local.matholickiosk.kiosk

import android.security.keystore.KeyProperties
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.security.AndroidKeystoreCredentialCipher
import com.local.matholickiosk.kiosk.security.CredentialField
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CredentialCipherInstrumentedTest {
    @Test
    fun aesGcmUsesFreshIvAndBindsCiphertextToStudentAndField() {
        val alias = "test-${UUID.randomUUID()}"
        val cipher = AndroidKeystoreCredentialCipher(alias)
        try {
            val first = cipher.encrypt("student-a", CredentialField.USERNAME, "alpha".toCharArray())
            val second = cipher.encrypt("student-a", CredentialField.USERNAME, "alpha".toCharArray())

            assertEqualsVersion(first.version)
            assertFalse(first.iv.contentEquals(second.iv))
            assertFalse(first.ciphertext.contentEquals("alpha".toByteArray()))
            assertArrayEquals(
                "alpha".toCharArray(),
                cipher.decrypt("student-a", CredentialField.USERNAME, first),
            )
            val wrongAad = runCatching {
                cipher.decrypt("student-b", CredentialField.USERNAME, first)
            }
            assertFalse(wrongAad.isSuccess)
            assertNotEquals(0, first.ciphertext.size)
        } finally {
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(alias)
        }
    }

    private fun assertEqualsVersion(version: Int) {
        org.junit.Assert.assertEquals(1, version)
        org.junit.Assert.assertEquals(KeyProperties.KEY_ALGORITHM_AES, "AES")
    }
}
