package com.example.rencar_pair.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class User(
    val id: String,
    val fullName: String,
    val token: String,
    val role: UserRole = UserRole.Pending
)

enum class UserRole {
    Pending,
    Customer,
    Admin
}
