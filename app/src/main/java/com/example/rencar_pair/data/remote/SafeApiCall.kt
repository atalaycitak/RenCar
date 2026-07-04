package com.example.rencar_pair.data.remote

import com.example.rencar_pair.domain.NetworkResult
import kotlinx.coroutines.CancellationException
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
            NetworkResult.Error(
                message = response.errorBody()?.string() ?: "Unknown error",
                code = response.code()
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        NetworkResult.Error(e.message ?: "Network error")
    }
}

suspend fun <T> safeApiCall(
    call: suspend () -> Response<T>
): NetworkResult<T> {
    return safeApiCall(call) { it }
}
