package com.local.matholickiosk.webpoc

import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal data class ConnectTarget(val host: String, val port: Int)

internal object ConnectTargetPolicy {
    private val exactHosts = setOf(
        "www.googletagmanager.com",
        "wcs.naver.net",
    )

    fun parseRequestLine(requestLine: String): ConnectTarget? {
        val parts = requestLine.trim().split(Regex("\\s+"))
        if (parts.size != 3 || parts[0] != "CONNECT" || !parts[2].startsWith("HTTP/1.")) return null
        val separator = parts[1].lastIndexOf(':')
        if (separator <= 0 || separator == parts[1].lastIndex) return null
        val host = parts[1].substring(0, separator)
            .lowercase(Locale.ROOT)
            .removeSuffix(".")
        val port = parts[1].substring(separator + 1).toIntOrNull() ?: return null
        if (!host.matches(Regex("[a-z0-9.-]+")) || host.startsWith('.') || ".." in host) return null
        return ConnectTarget(host, port)
    }

    fun isAllowed(target: ConnectTarget): Boolean = target.port == 443 &&
        (target.host.endsWith(".matholic.com") || target.host in exactHosts)
}

/**
 * A loopback-only CONNECT tunnel. It never terminates TLS, records no traffic, and only resolves
 * the small HTTPS host allowlist required by the Matholic pages.
 */
internal class LoopbackConnectProxy private constructor(
    private val serverSocket: ServerSocket,
) : Closeable {
    private val closed = AtomicBoolean(false)
    private val executor = Executors.newCachedThreadPool { task ->
        Thread(task, "matholic-loopback-proxy").apply { isDaemon = true }
    }
    private val activeSockets = Collections.synchronizedSet(mutableSetOf<Socket>())

    val port: Int = serverSocket.localPort

    init {
        executor.execute(::acceptLoop)
    }

    private fun acceptLoop() {
        while (!closed.get()) {
            val client = try {
                serverSocket.accept()
            } catch (_: IOException) {
                return
            }
            activeSockets += client
            executor.execute { handle(client) }
        }
    }

    private fun handle(client: Socket) {
        var upstream: Socket? = null
        try {
            client.soTimeout = IO_TIMEOUT_MS
            val input = BufferedInputStream(client.getInputStream())
            val requestLine = readRequestLineAndHeaders(input)
            val target = requestLine?.let(ConnectTargetPolicy::parseRequestLine)
            if (target == null || !ConnectTargetPolicy.isAllowed(target)) {
                writeResponse(client, "HTTP/1.1 403 Forbidden\r\nConnection: close\r\n\r\n")
                return
            }

            upstream = Socket().apply {
                connect(InetSocketAddress(target.host, target.port), CONNECT_TIMEOUT_MS)
                soTimeout = 0
            }
            activeSockets += upstream
            writeResponse(client, "HTTP/1.1 200 Connection Established\r\n\r\n")
            client.soTimeout = 0

            val upstreamSocket = upstream
            executor.execute {
                try {
                    upstreamSocket.getInputStream().copyTo(client.getOutputStream(), COPY_BUFFER_SIZE)
                } catch (_: IOException) {
                    // Either peer closed the tunnel.
                } finally {
                    closeSocket(client)
                    closeSocket(upstreamSocket)
                }
            }
            try {
                input.copyTo(upstreamSocket.getOutputStream(), COPY_BUFFER_SIZE)
            } catch (_: IOException) {
                // Either peer closed the tunnel.
            }
        } catch (_: IOException) {
            runCatching {
                writeResponse(client, "HTTP/1.1 502 Bad Gateway\r\nConnection: close\r\n\r\n")
            }
        } finally {
            closeSocket(client)
            upstream?.let(::closeSocket)
        }
    }

    private fun readRequestLineAndHeaders(input: BufferedInputStream): String? {
        val bytes = ByteArrayOutputStream()
        var sequence = 0
        while (bytes.size() < MAX_HEADER_BYTES) {
            val next = input.read()
            if (next < 0) return null
            bytes.write(next)
            sequence = when {
                sequence == 0 && next == '\r'.code -> 1
                sequence == 1 && next == '\n'.code -> 2
                sequence == 2 && next == '\r'.code -> 3
                sequence == 3 && next == '\n'.code -> 4
                next == '\r'.code -> 1
                else -> 0
            }
            if (sequence == 4) {
                return bytes.toString(StandardCharsets.ISO_8859_1.name()).lineSequence().firstOrNull()
            }
        }
        return null
    }

    private fun writeResponse(socket: Socket, response: String) {
        socket.getOutputStream().apply {
            write(response.toByteArray(StandardCharsets.ISO_8859_1))
            flush()
        }
    }

    private fun closeSocket(socket: Socket) {
        activeSockets.remove(socket)
        runCatching { socket.close() }
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        runCatching { serverSocket.close() }
        synchronized(activeSockets) { activeSockets.toList() }.forEach(::closeSocket)
        executor.shutdownNow()
    }

    companion object {
        fun start(): LoopbackConnectProxy {
            val server = ServerSocket().apply {
                reuseAddress = true
                bind(InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), ACCEPT_BACKLOG)
            }
            return LoopbackConnectProxy(server)
        }

        private const val ACCEPT_BACKLOG = 16
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val IO_TIMEOUT_MS = 10_000
        private const val MAX_HEADER_BYTES = 8 * 1024
        private const val COPY_BUFFER_SIZE = 16 * 1024
    }
}
