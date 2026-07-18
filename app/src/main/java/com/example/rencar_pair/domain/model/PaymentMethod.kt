package com.example.rencar_pair.domain.model

/**
 * Saved card summary. Full card number and CVC are never sent to the cards API.
 */
data class SavedCard(
    val cardToken: String,
    val cardAlias: String,
    val binNumber: String,
    val cardAssociation: String,
    val expMonth: Int? = null,
    val expYear: Int? = null,
    val isDefault: Boolean = false
) {
    val last4: String
        get() = binNumber.takeLast(4)
}
