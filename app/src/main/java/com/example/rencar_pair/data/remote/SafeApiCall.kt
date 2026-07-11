package com.example.rencar_pair.data.remote

import com.example.rencar_pair.domain.NetworkResult
import kotlinx.coroutines.CancellationException
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

suspend fun <T, R> safeApiCall(
    call: suspend () -> Response<T>,
    transform: (T) -> R
): NetworkResult<R> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            response.body()?.let { NetworkResult.Success(transform(it)) }
                ?: NetworkResult.Error("Empty response body")
        } else {
            val errorMessage = parseErrorBody(response.errorBody()?.string(), response.code())
            NetworkResult.Error(message = errorMessage, code = response.code())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val msg = when (e) {
            is java.net.UnknownHostException, is java.net.ConnectException -> "Bağlantı hatası: ${e.message}"
            is java.net.SocketTimeoutException -> "Sunucuya bağlanılamadı (Timeout)"
            is java.io.IOException -> "Ağ hatası (${e.javaClass.simpleName}): ${e.message}"
            else -> "Beklenmeyen bir hata oluştu: ${e.localizedMessage}"
        }
        android.util.Log.e("SafeApiCall", "API Error: $msg", e)
        NetworkResult.Error(msg)
    }
}

suspend fun <T> safeApiCall(
    call: suspend () -> Response<T>
): NetworkResult<T> {
    return safeApiCall(call) { it }
}

/**
 * Tries to extract a human-readable message from the API error body JSON.
 * The backend returns `{"message": "..."}` or `{"message": [...]}` (validation errors).
 * Falls back to a generic HTTP error message if the body cannot be parsed.
 */
private fun parseErrorBody(rawBody: String?, httpCode: Int): String {
    if (rawBody.isNullOrBlank()) return "HTTP $httpCode error"
    return try {
        val json = JSONObject(rawBody)
        when {
            json.has("message") -> {
                // message can be a String or a JSON array (validation errors from NestJS)
                val msg = json.get("message")
                if (msg is String) msg
                else json.getJSONArray("message").join(", ").replace("\"", "")
            }
            json.has("error") -> json.getString("error")
            else -> rawBody
        }
    } catch (e: JSONException) {
        rawBody
    }
}
