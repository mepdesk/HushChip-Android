package uk.co.signstr.app.nip46

import android.util.Log
import kotlinx.serialization.json.*
import okhttp3.*
import java.util.concurrent.TimeUnit

class NostrRelay(
    private val url: String,
    private val listener: RelayListener
) {
    interface RelayListener {
        fun onEvent(relay: String, subscriptionId: String, eventJson: JsonObject)
        fun onOk(relay: String, eventId: String, accepted: Boolean, message: String)
        fun onConnected(relay: String)
        fun onDisconnected(relay: String)
        fun onError(relay: String, error: String)
    }

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }
    private var reconnectAttempts = 0
    private var shouldReconnect = true

    companion object {
        private const val TAG = "NostrRelay"
        private const val MAX_RECONNECT_DELAY = 60_000L
    }

    fun connect() {
        shouldReconnect = true
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Connected to $url")
                reconnectAttempts = 0
                listener.onConnected(url)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                listener.onDisconnected(url)
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure on $url: ${t.message}")
                listener.onError(url, t.message ?: "Unknown error")
                listener.onDisconnected(url)
                scheduleReconnect()
            }
        })
    }

    private fun handleMessage(text: String) {
        try {
            val arr = json.parseToJsonElement(text).jsonArray
            when (arr[0].jsonPrimitive.content) {
                "EVENT" -> {
                    val subId = arr[1].jsonPrimitive.content
                    val event = arr[2].jsonObject
                    listener.onEvent(url, subId, event)
                }
                "OK" -> {
                    val eventId = arr[1].jsonPrimitive.content
                    val accepted = arr[2].jsonPrimitive.boolean
                    val msg = if (arr.size > 3) arr[3].jsonPrimitive.content else ""
                    listener.onOk(url, eventId, accepted, msg)
                }
                "EOSE" -> { /* End of stored events - no action needed */ }
                "NOTICE" -> {
                    val msg = arr[1].jsonPrimitive.content
                    Log.d(TAG, "NOTICE from $url: $msg")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing relay message: ${e.message}")
        }
    }

    fun subscribe(subscriptionId: String, filters: JsonObject) {
        val msg = buildJsonArray {
            add(JsonPrimitive("REQ"))
            add(JsonPrimitive(subscriptionId))
            add(filters)
        }.toString()
        webSocket?.send(msg)
        Log.d(TAG, "REQ sent to $url: $msg")
    }

    fun publish(eventJson: String) {
        val msg = "[\"EVENT\",$eventJson]"
        webSocket?.send(msg)
        Log.d(TAG, "EVENT sent to $url")
    }

    fun close() {
        shouldReconnect = false
        webSocket?.close(1000, "Closing")
        webSocket = null
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect) return
        reconnectAttempts++
        val delay = minOf(
            (1000L * (1L shl minOf(reconnectAttempts, 6))),
            MAX_RECONNECT_DELAY
        )
        Log.d(TAG, "Reconnecting to $url in ${delay}ms (attempt $reconnectAttempts)")
        Thread {
            Thread.sleep(delay)
            if (shouldReconnect) connect()
        }.start()
    }

    val relayUrl get() = url
}
