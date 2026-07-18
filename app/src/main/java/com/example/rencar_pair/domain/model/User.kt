package com.example.rencar_pair.domain.model

data class User(
    val id: String,
    val fullName: String,
    val token: String,
    val phone: String? = null,
    val role: UserRole = UserRole.Pending
)

enum class UserRole {
    Pending,
    Customer,
    Admin;

    companion object {
        /**
         * Maps the API's UPPER_CASE role strings (e.g. "PENDING", "CUSTOMER", "ADMIN")
         * to the corresponding [UserRole] enum value.
         * Returns [Pending] as a safe fallback for any unknown value.
         */
        fun fromApiString(value: String?): UserRole {
            return when (value?.uppercase()) {
                "CUSTOMER" -> Customer
                "ADMIN" -> Admin
                "PENDING" -> Pending
                else -> Pending
            }
        }
    }
}
