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
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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

internal fun interface UpstreamSocketConnector {
    fun connect(target: ConnectTarget, timeoutMs: Int): Socket
}

/** Creates one connected socket and retains no ownership after returning it. */
internal class DirectUpstreamSocketConnector(
    private val socketFactory: () -> Socket = { Socket() },
) : UpstreamSocketConnector {
    override fun connect(target: ConnectTarget, timeoutMs: Int): Socket {
        val socket = socketFactory()
        var connected = false
        try {
            socket.connect(InetSocketAddress(target.host, target.port), timeoutMs)
            connected = true
            return socket
        } finally {
            // Socket.connect() may leave a failed socket open, depending on the runtime.
            if (!connected) runCatching { socket.close() }
        }
    }
}

/**
 * A loopback-only CONNECT tunnel. It never terminates TLS, records no traffic, and only resolves
 * the small HTTPS host allowlist required by the Matholic pages.
 */
internal class LoopbackConnectProxy private constructor(
    private val serverSocket: ServerSocket,
    private val onUnexpectedTermination: () -> Unit,
    private val upstreamSocketConnector: UpstreamSocketConnector,
    private val tunnelIdleTimeoutMs: Int,
) : Closeable {
    private val executor = Executors.newFixedThreadPool(MAX_TUNNELS * 2) { task ->
        Thread(task, "matholic-loopback-proxy").apply { isDaemon = true }
    }
    private val connectionSlots = ResourcePermitPool(MAX_TUNNELS)
    private val activeSockets = CloseableRegistry<Socket> { socket ->
        runCatching { socket.close() }
        Unit
    }

    val port: Int = serverSocket.localPort
    private val acceptThread = Thread(::acceptLoop, "matholic-loopback-proxy-accept").apply {
        isDaemon = true
    }

    init {
        acceptThread.start()
    }

    private fun acceptLoop() {
        while (!activeSockets.isClosed) {
            val client = try {
                serverSocket.accept()
            } catch (_: IOException) {
                if (!activeSockets.isClosed) onUnexpectedTermination()
                return
            }
            if (!connectionSlots.tryAcquire()) {
                runCatching {
                    writeResponse(
                        client,
                        "HTTP/1.1 503 Service Unavailable\r\nConnection: close\r\n\r\n",
                    )
                }
                runCatching { client.close() }
                continue
            }
            var taskOwnsPermit = false
            try {
                if (!activeSockets.register(client)) return
                taskOwnsPermit = ProxyTaskSubmission.submit(
                    executor = executor,
                    task = { handle(client) },
                    onRejected = { closeSocket(client) },
                )
                if (!taskOwnsPermit) return
            } finally {
                if (!taskOwnsPermit) connectionSlots.release()
            }
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

            val connectedUpstream = openUpstream(target)
            upstream = connectedUpstream
            if (!activeSockets.register(connectedUpstream)) return
            writeResponse(client, "HTTP/1.1 200 Connection Established\r\n\r\n")
            client.soTimeout = tunnelIdleTimeoutMs

            if (
                !ProxyTaskSubmission.submit(
                    executor = executor,
                    task = {
                        try {
                            connectedUpstream.getInputStream()
                                .copyTo(client.getOutputStream(), COPY_BUFFER_SIZE)
                        } catch (_: IOException) {
                            // Either peer closed the tunnel.
                        } finally {
                            closeSocket(client)
                            closeSocket(connectedUpstream)
                        }
                    },
                    onRejected = {
                        closeSocket(client)
                        closeSocket(connectedUpstream)
                    },
                )
            ) {
                return
            }
            try {
                input.copyTo(connectedUpstream.getOutputStream(), COPY_BUFFER_SIZE)
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
            connectionSlots.release()
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

    private fun openUpstream(target: ConnectTarget): Socket {
        val socket = upstreamSocketConnector.connect(target, CONNECT_TIMEOUT_MS)
        var configured = false
        try {
            socket.soTimeout = tunnelIdleTimeoutMs
            configured = true
            return socket
        } finally {
            if (!configured) runCatching { socket.close() }
        }
    }

    private fun writeResponse(socket: Socket, response: String) {
        socket.getOutputStream().apply {
            write(response.toByteArray(StandardCharsets.ISO_8859_1))
            flush()
        }
    }

    private fun closeSocket(socket: Socket) {
        activeSockets.close(socket)
    }

    override fun close() {
        if (
            !activeSockets.closeAll {
                runCatching { serverSocket.close() }
            }
        ) {
            return
        }
        executor.shutdownNow()
        runCatching { acceptThread.join(THREAD_SHUTDOWN_TIMEOUT_MS) }
        runCatching { executor.awaitTermination(THREAD_SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS) }
    }

    companion object {
        fun start(onUnexpectedTermination: () -> Unit = {}): LoopbackConnectProxy {
            return create(
                onUnexpectedTermination = onUnexpectedTermination,
                upstreamSocketConnector = DirectUpstreamSocketConnector(),
                tunnelIdleTimeoutMs = TUNNEL_IDLE_TIMEOUT_MS,
            )
        }

        internal fun startForTesting(
            upstreamSocketConnector: UpstreamSocketConnector,
            tunnelIdleTimeoutMs: Int,
            onUnexpectedTermination: () -> Unit = {},
        ): LoopbackConnectProxy {
            require(tunnelIdleTimeoutMs > 0)
            return create(
                onUnexpectedTermination = onUnexpectedTermination,
                upstreamSocketConnector = upstreamSocketConnector,
                tunnelIdleTimeoutMs = tunnelIdleTimeoutMs,
            )
        }

        private fun create(
            onUnexpectedTermination: () -> Unit,
            upstreamSocketConnector: UpstreamSocketConnector,
            tunnelIdleTimeoutMs: Int,
        ): LoopbackConnectProxy {
            val server = ServerSocket().apply {
                reuseAddress = true
                bind(InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), ACCEPT_BACKLOG)
            }
            return LoopbackConnectProxy(
                serverSocket = server,
                onUnexpectedTermination = onUnexpectedTermination,
                upstreamSocketConnector = upstreamSocketConnector,
                tunnelIdleTimeoutMs = tunnelIdleTimeoutMs,
            )
        }

        private const val ACCEPT_BACKLOG = 64
        private const val MAX_TUNNELS = 32
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val IO_TIMEOUT_MS = 10_000
        private const val TUNNEL_IDLE_TIMEOUT_MS = 60_000
        private const val THREAD_SHUTDOWN_TIMEOUT_MS = 1_000L
        private const val MAX_HEADER_BYTES = 8 * 1024
        private const val COPY_BUFFER_SIZE = 16 * 1024
    }
}
