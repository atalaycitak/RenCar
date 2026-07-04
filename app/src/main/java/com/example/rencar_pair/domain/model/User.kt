package com.example.rencar_pair.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class User(
    val id: String,
    val fullName: String,
    val token: String
)
