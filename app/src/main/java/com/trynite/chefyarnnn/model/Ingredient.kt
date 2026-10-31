package com.trynite.chefyarnnn.model

import kotlinx.serialization.Serializable

@Serializable
enum class MarketCategory(val displayName: String) {
    GULAYAN("Gulayan"),
    KARNEHAN("Karnehan"),
    PAMPALASA("Pampalasa"),
    OTHERS("Others");

    companion object {
        fun fromString(value: String): MarketCategory {
            return when (value.trim().lowercase()) {
                "gulayan", "vegetable", "vegetables", "produce" -> GULAYAN
                "karnehan", "meat", "seafood", "poultry", "karne" -> KARNEHAN
                "pampalasa", "spices", "spice", "seasoning", "condiment" -> PAMPALASA
                else -> OTHERS
            }
        }
    }
}

@Serializable
data class Ingredient(
    val name: String,
    val amount: String,
    val unit: String,
    val marketCategory: MarketCategory
)
