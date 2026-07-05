package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.domain.model.UserRole
import com.example.rencar_pair.domain.repository.AuthRepository
import kotlinx.coroutines.delay

class FakeAuthRepository : AuthRepository {
    private var currentUser: User? = null

    override suspend fun login(phone: String): NetworkResult<String> {
        delay(1000)
        return if (phone.isNotBlank()) {
            NetworkResult.Success("fake_transaction_id")
        } else {
            NetworkResult.Error("Phone cannot be empty")
        }
    }

    override suspend fun verifyOtp(phone: String, code: String): NetworkResult<User> {
        delay(1000)
        return if (code == "123456") {
            currentUser = User(
                id = "fake_id",
                fullName = "Fake User",
                token = "fake_token",
                role = UserRole.Customer
            )
            NetworkResult.Success(currentUser!!)
        } else {
            NetworkResult.Error("Invalid OTP. Use 123456.")
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): NetworkResult<User> {
        delay(1000)
        currentUser = User(
            id = "fake_id",
            fullName = fullName,
            token = "fake_token",
            role = UserRole.Customer
        )
        return NetworkResult.Success(currentUser!!)
    }

    override suspend fun logout(): NetworkResult<String> {
        delay(500)
        currentUser = null
        return NetworkResult.Success("Logged out successfully")
    }

    override suspend fun getCurrentUser(): NetworkResult<User> {
        delay(500)
        return currentUser?.let { NetworkResult.Success(it) }
            ?: NetworkResult.Error("Not logged in")
    }

    override suspend fun refreshSession(): NetworkResult<User> {
        delay(500)
        return currentUser?.let { NetworkResult.Success(it) }
            ?: NetworkResult.Error("Not logged in")
    }

    override suspend fun getSavedToken(): String? {
        return currentUser?.token
    }

    override suspend fun clearSession() {
        currentUser = null
    }
}
