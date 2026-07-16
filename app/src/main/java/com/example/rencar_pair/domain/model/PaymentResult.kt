package com.example.rencar_pair.domain.model

enum class PaymentStatus {
    Success, Failed, Unknown;

    companion object {
        fun fromApiString(value: String?): PaymentStatus = when (value?.uppercase()) {
            "SUCCESS" -> Success
            "PAID" -> Success
            "FAILED" -> Failed
            "UNPAID" -> Failed
            else -> Unknown
        }
    }
}

data class PaymentResult(
    val status: PaymentStatus,
    val transactionId: String?,
    val errorMessage: String?
)
