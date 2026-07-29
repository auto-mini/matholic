package com.local.matholickiosk.kiosk

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.transfer.PcPairingStore
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PcPairingStoreInstrumentedTest {
    @Test
    fun pairingSecretIsEncryptedAndRoundTripsThroughAndroidKeystore() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val store = PcPairingStore(context)
        val raw =
            "MATHOLIC-PC1:" +
                "AQARIjNEVWZ3iJmqu8zd7v8AAQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eH7wB" +
                "DzE5Mi4xNjguMjE5LjIyNAtNQVRIT0xJQy1QQw"
        store.clear()

        val saved = store.save(raw)
        val loaded = store.load()
        try {
            assertEquals("MATHOLIC-PC", saved.displayName)
            assertEquals("192.168.219.224", loaded?.host)
            assertEquals(48129, loaded?.port)
            assertArrayEquals(saved.receiverId, loaded?.receiverId)
            assertArrayEquals(saved.secret, loaded?.secret)
        } finally {
            saved.clearSensitiveData()
            loaded?.clearSensitiveData()
            store.clear()
        }
        assertNull(store.load())
    }
}
