package com.example.rencar_pair.data.socket

import com.example.rencar_pair.data.remote.TokenHolder
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import org.json.JSONObject
import java.net.URI

class RenCarSocketClient(
    private val tokenHolder: TokenHolder
) {
    private val socketCache = java.util.concurrent.ConcurrentHashMap<String, SocketWrapper>()

    private inner class SocketWrapper(val namespace: String) {
        val socket: Socket
        var subscriberCount = 0

        init {
            val token = tokenHolder.token ?: ""
            val options = IO.Options.builder()
                .setAuth(mapOf("token" to token))
                .setTransports(arrayOf(WebSocket.NAME))
                // Remove setForceNew(true) to avoid exhausting sockets
                .build()
            
            socket = IO.socket(URI.create(namespace), options)
        }

        @Synchronized
        fun subscribe() {
            if (subscriberCount == 0) {
                socket.connect()
            }
            subscriberCount++
        }

        @Synchronized
        fun unsubscribe() {
            subscriberCount--
            if (subscriberCount <= 0) {
                socket.disconnect()
                socketCache.remove(namespace)
            }
        }
    }

    private fun getWrapper(namespace: String): SocketWrapper {
        return socketCache.getOrPut(namespace) {
            SocketWrapper(namespace)
        }
    }

    fun observeEvent(namespace: String, eventName: String): Flow<JSONObject> {
        if (tokenHolder.token.isNullOrBlank()) return emptyFlow()

        return callbackFlow {
            val wrapper = getWrapper(namespace)
            wrapper.subscribe()
            val socket = wrapper.socket
            
            val eventListener = Emitter.Listener { args ->
                val raw = args.firstOrNull()
                val json = when (raw) {
                    is JSONObject -> raw
                    is String -> runCatching { JSONObject(raw) }.getOrNull()
                    else -> null
                }
                if (json != null) {
                    trySend(json)
                }
            }
            
            val connectErrorListener = Emitter.Listener { args ->
                close(IllegalStateException(args.firstOrNull()?.toString() ?: "Socket.IO connect error"))
            }

            socket.on(eventName, eventListener)
            socket.on(Socket.EVENT_CONNECT_ERROR, connectErrorListener)

            awaitClose {
                socket.off(eventName, eventListener)
                socket.off(Socket.EVENT_CONNECT_ERROR, connectErrorListener)
                wrapper.unsubscribe()
            }
        }
    }
}
