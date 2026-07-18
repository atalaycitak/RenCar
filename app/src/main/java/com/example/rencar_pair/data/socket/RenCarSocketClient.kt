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
    fun observeEvent(namespace: String, eventName: String): Flow<JSONObject> {
        val token = tokenHolder.token ?: return emptyFlow()

        return callbackFlow {
            val options = IO.Options.builder()
                .setAuth(mapOf("token" to token))
                .setTransports(arrayOf(WebSocket.NAME))
                .setForceNew(true)
                .build()
                
            val socket = IO.socket(URI.create(namespace), options)
            
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
            socket.connect()

            awaitClose {
                socket.off(eventName, eventListener)
                socket.off(Socket.EVENT_CONNECT_ERROR, connectErrorListener)
                socket.disconnect()
            }
        }
    }
}
