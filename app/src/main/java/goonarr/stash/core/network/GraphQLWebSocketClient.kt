package goonarr.stash.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import timber.log.Timber

/**
 * Connection states for the WebSocket client.
 */
sealed class WebSocketState {
    data object Disconnected : WebSocketState()
    data object Connecting : WebSocketState()
    data object Connected : WebSocketState()
    data class Reconnecting(val attempt: Int) : WebSocketState()
    data class Error(val message: String) : WebSocketState()
}

/**
 * A reusable GraphQL WebSocket client for subscriptions.
 * Handles connection lifecycle, protocol handshakes, keep-alive pings, and automatic reconnection.
 */
@Singleton
class GraphQLWebSocketClient @Inject constructor(
    private val client: HttpClient,
    private val json: Json
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _state = MutableStateFlow<WebSocketState>(WebSocketState.Disconnected)
    val state: StateFlow<WebSocketState> = _state.asStateFlow()

    // Connection state
    private var session: DefaultClientWebSocketSession? = null
    private var connectionJob: Job? = null
    private var pingJob: Job? = null

    // Subscription handlers
    private val handlers = ConcurrentHashMap<String, (String) -> Unit>()

    // Connection parameters for reconnection
    private var connectionUrl: String? = null
    private var connectionApiKey: String? = null

    // Retry logic
    private var retryCount = 0
    private val maxRetryDelay = 30.0

    // Outgoing message queue for send operations
    private val sendChannel = Channel<String>(Channel.BUFFERED)

    /**
     * Connects to the Stash GraphQL WebSocket endpoint.
     */
    fun connect(url: String, apiKey: String) {
        connectionUrl = url
        connectionApiKey = apiKey
        retryCount = 0
        performConnect()
    }

    private fun performConnect() {
        val url = connectionUrl ?: return
        val apiKey = connectionApiKey ?: ""

        // Cancel existing connection
        disconnectInternal()

        val currentState = if (retryCount > 0) {
            WebSocketState.Reconnecting(retryCount)
        } else {
            WebSocketState.Connecting
        }
        _state.value = currentState

        // Convert HTTP URL to WebSocket URL
        val wsUrl = url.toWebSocketUrl()
        Timber.d("📡 Connecting to $wsUrl")

        connectionJob = scope.launch {
            try {
                // Parse the URL
                val urlParts = parseUrl(wsUrl)

                client.webSocket(
                    method = HttpMethod.Get,
                    host = urlParts.host,
                    port = urlParts.port,
                    path = urlParts.path,
                    request = {
                        headers.append("Sec-WebSocket-Protocol", "graphql-ws")
                        if (apiKey.isNotEmpty()) {
                            headers.append("ApiKey", apiKey)
                        }
                    }
                ) {
                    session = this
                    Timber.d("🔌 WebSocket session established")

                    // Send connection init
                    sendMessage(
                        buildJsonObject {
                            put("type", "connection_init")
                            putJsonObject("payload") {}
                        }.toString()
                    )

                    // Start message receiver
                    launch { processSendQueue() }

                    // Receive messages
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> handleProtocolMessage(frame.readText())
                            is Frame.Ping -> Timber.v("🏓 Ping received")
                            is Frame.Pong -> Timber.v("🏓 Pong received")
                            is Frame.Close -> {
                                Timber.d("🔴 WebSocket closed by server")
                                break
                            }
                            else -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ WebSocket connection error")
                _state.value = WebSocketState.Error(e.message ?: "Unknown error")
            }

            // Connection ended, schedule reconnect if still configured
            if (connectionUrl != null) {
                scheduleReconnect()
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.processSendQueue() {
        for (message in sendChannel) {
            try {
                send(Frame.Text(message))
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to send message")
            }
        }
    }

    /**
     * Disconnects from the WebSocket server.
     */
    fun disconnect() {
        connectionUrl = null
        connectionApiKey = null
        handlers.clear()
        disconnectInternal()
        _state.value = WebSocketState.Disconnected
    }

    private fun disconnectInternal() {
        pingJob?.cancel()
        pingJob = null

        connectionJob?.cancel()
        connectionJob = null

        scope.launch {
            try {
                session?.close()
            } catch (_: Exception) {}
            session = null
        }
    }

    private fun scheduleReconnect() {
        retryCount++
        val delay = min(2.0.pow(retryCount), maxRetryDelay).toLong() * 1000
        Timber.d("🔄 Reconnecting in ${delay / 1000}s (attempt #$retryCount)...")

        scope.launch {
            delay(delay)
            if (connectionUrl != null) {
                performConnect()
            }
        }
    }

    /**
     * Subscribes to a GraphQL subscription.
     */
    fun subscribe(id: String, query: String, handler: (String) -> Unit) {
        handlers[id] = handler

        // If already connected, send subscription immediately
        if (_state.value == WebSocketState.Connected) {
            sendSubscription(id, query)
        }
    }

    /**
     * Unsubscribes from a GraphQL subscription.
     */
    fun unsubscribe(id: String) {
        handlers.remove(id)

        if (_state.value == WebSocketState.Connected) {
            val message = buildJsonObject {
                put("id", id)
                put("type", "stop")
            }.toString()
            sendMessage(message)
        }
    }

    private fun sendSubscription(id: String, query: String) {
        val message = buildJsonObject {
            put("id", id)
            put("type", "start")
            putJsonObject("payload") {
                put("query", query)
                putJsonObject("variables") {}
            }
        }.toString()
        sendMessage(message)
        Timber.d("📤 Subscribed: $id")
    }

    private fun sendMessage(message: String) {
        scope.launch {
            sendChannel.send(message)
        }
    }

    private fun handleProtocolMessage(text: String) {
        try {
            val jsonObj = json.decodeFromString<JsonObject>(text)
            val type = jsonObj["type"]?.toString()?.trim('"') ?: return

            when (type) {
                "connection_ack" -> {
                    Timber.d("✅ Protocol handshake complete")
                    retryCount = 0
                    _state.value = WebSocketState.Connected
                    startPing()
                    resubscribeAll()
                }

                "ka" -> {
                    // Keep-alive, ignore
                }

                "data" -> {
                    val id = jsonObj["id"]?.toString()?.trim('"')
                    if (id != null) {
                        handlers[id]?.invoke(text)
                    }
                }

                "error" -> {
                    Timber.e("❌ Protocol error: $text")
                }

                else -> {
                    Timber.v("Unknown message type: $type")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse protocol message: $text")
        }
    }

    private fun startPing() {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive) {
                delay(30_000)
                try {
                    session?.send(Frame.Ping(ByteArray(0)))
                } catch (e: Exception) {
                    Timber.e(e, "❌ Ping failed")
                }
            }
        }
    }

    private fun resubscribeAll() {
        // Re-subscribe all handlers after reconnection
        // Note: We just log here since subscriptions should be re-triggered by services
        Timber.d("📥 Connection restored, ${handlers.size} handlers registered")
    }

    // Helper to convert HTTP URL to WebSocket URL
    private fun String.toWebSocketUrl(): String {
        return this.replace("http://", "ws://").replace("https://", "wss://")
    }

    // Helper to parse URL into components
    private data class UrlParts(val host: String, val port: Int, val path: String)

    private fun parseUrl(url: String): UrlParts {
        val withoutProtocol = url.removePrefix("ws://").removePrefix("wss://")
        val isSecure = url.startsWith("wss://")

        val pathIndex = withoutProtocol.indexOf('/')
        val hostPort = if (pathIndex >= 0) withoutProtocol.substring(0, pathIndex) else withoutProtocol
        val path = if (pathIndex >= 0) withoutProtocol.substring(pathIndex) else "/"

        val portIndex = hostPort.indexOf(':')
        val host = if (portIndex >= 0) hostPort.substring(0, portIndex) else hostPort
        val port = if (portIndex >= 0) {
            hostPort.substring(portIndex + 1).toIntOrNull() ?: if (isSecure) 443 else 80
        } else {
            if (isSecure) 443 else 80
        }

        return UrlParts(host, port, path)
    }
}
