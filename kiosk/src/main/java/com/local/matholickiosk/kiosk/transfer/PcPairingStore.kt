package com.local.matholickiosk.kiosk.transfer

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class PcPairingStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun save(rawPairing: String): PcReceiverPairing {
        val pairing = PcReceiverPairing.decode(rawPairing)
        return try {
            save(pairing)
            pairing
        } catch (error: Exception) {
            pairing.clearSensitiveData()
            throw error
        }
    }

    fun save(pairing: PcReceiverPairing) {
        val plaintext = pairing.encodeBytes()
        try {
            saveEncrypted(plaintext)
        } finally {
            plaintext.fill(0)
        }
    }

    private fun saveEncrypted(plaintext: ByteArray) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(plaintext)
        try {
            check(
                preferences.edit()
                    .putString(CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                    .putString(IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                    .commit(),
            ) { "PC pairing state could not be persisted" }
        } finally {
            ciphertext.fill(0)
        }
    }

    fun load(): PcReceiverPairing? {
        val ciphertextEncoded = preferences.getString(CIPHERTEXT, null) ?: return null
        val ivEncoded = preferences.getString(IV, null) ?: return null
        val ciphertext = Base64.decode(ciphertextEncoded, Base64.NO_WRAP)
        val iv = Base64.decode(ivEncoded, Base64.NO_WRAP)
        val plaintext = try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(GCM_TAG_BITS, iv),
            )
            cipher.doFinal(ciphertext)
        } finally {
            ciphertext.fill(0)
            iv.fill(0)
        }
        return try {
            PcReceiverPairing.decode(plaintext)
        } finally {
            plaintext.fill(0)
        }
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER,
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val PREFERENCES = "pc_receiver_pairing"
        const val CIPHERTEXT = "ciphertext"
        const val IV = "iv"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "matholic_pc_receiver_pairing_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
    }
}
