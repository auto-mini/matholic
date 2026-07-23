package com.local.matholickiosk.kiosk.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

enum class CredentialField(val aadName: String) {
    USERNAME("matholic-username"),
    PASSWORD("matholic-password"),
}

data class EncryptedValue(
    val ciphertext: ByteArray,
    val iv: ByteArray,
    val version: Int = CURRENT_VERSION,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

interface CredentialCipher {
    fun encrypt(studentId: String, field: CredentialField, plaintext: CharArray): EncryptedValue
    fun decrypt(studentId: String, field: CredentialField, encrypted: EncryptedValue): CharArray
}

class AndroidKeystoreCredentialCipher(
    private val keyAlias: String = KEY_ALIAS,
) : CredentialCipher {
    override fun encrypt(
        studentId: String,
        field: CredentialField,
        plaintext: CharArray,
    ): EncryptedValue {
        val bytes = encodeUtf8(plaintext)
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            cipher.updateAAD(aad(studentId, field))
            EncryptedValue(cipher.doFinal(bytes), cipher.iv.copyOf())
        } finally {
            bytes.fill(0)
        }
    }

    override fun decrypt(
        studentId: String,
        field: CredentialField,
        encrypted: EncryptedValue,
    ): CharArray {
        require(encrypted.version == EncryptedValue.CURRENT_VERSION) {
            "Unsupported credential encryption version"
        }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(GCM_TAG_BITS, encrypted.iv),
        )
        cipher.updateAAD(aad(studentId, field))
        val plaintext = cipher.doFinal(encrypted.ciphertext)
        return try {
            StandardCharsets.UTF_8.decode(ByteBuffer.wrap(plaintext))
                .let { chars -> CharArray(chars.remaining()).also(chars::get) }
        } finally {
            plaintext.fill(0)
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(keyAlias, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private fun aad(studentId: String, field: CredentialField): ByteArray =
        "$studentId\u0000${field.aadName}".toByteArray(StandardCharsets.UTF_8)

    private fun encodeUtf8(chars: CharArray): ByteArray {
        val byteBuffer = StandardCharsets.UTF_8.encode(java.nio.CharBuffer.wrap(chars))
        return ByteArray(byteBuffer.remaining()).also(byteBuffer::get)
    }

    companion object {
        const val KEY_ALIAS = "matholic-kiosk-credential-v1"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE_BITS = 256
        private const val GCM_TAG_BITS = 128
    }
}
