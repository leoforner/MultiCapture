package com.example.multicapture.chat

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*

class ChatWebSocketClient {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val _messages = MutableStateFlow<String>("")
    val messages = _messages.asStateFlow()

    fun connect(url: String) {
        if (url.isBlank()) return
        val request = try {
            Request.Builder().url(url).build()
        } catch (e: Exception) {
            _messages.value = "URL Inválida"
            return
        }
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                _messages.value = text
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _messages.value = "Erro na conexão chat: ${t.message}"
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _messages.value = ""
    }
}
