package com.local.matholickiosk.kiosk

import androidx.camera.core.ImageProxy
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.qr.QrImageAnalyzer
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrImageAnalyzerInstrumentedTest {
    @Test
    fun synchronousFrameReadFailureClosesFrameAndAllowsNextFrame() {
        val closeCount = AtomicInteger()
        val analyzer = QrImageAnalyzer(onDecision = {})
        analyzer.setEnabled(true)

        try {
            analyzer.analyze(failingImageProxy(closeCount))
            analyzer.analyze(failingImageProxy(closeCount))

            assertEquals(2, closeCount.get())
        } finally {
            analyzer.close()
        }
    }

    private fun failingImageProxy(closeCount: AtomicInteger): ImageProxy =
        Proxy.newProxyInstance(
            ImageProxy::class.java.classLoader,
            arrayOf(ImageProxy::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "getImage" -> throw IllegalStateException("synthetic image read failure")
                "close" -> {
                    closeCount.incrementAndGet()
                    null
                }
                "toString" -> "FailingImageProxy"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                else -> error("Unexpected ImageProxy call: ${method.name}")
            }
        } as ImageProxy
}
