package com.trynite.chefyarnnn.model

import kotlinx.serialization.Serializable

@Serializable
enum class RecipeCategory(val displayName: String) {
    ULAM("Ulam"),
    MERIENDA("Merienda"),
    KAKANIN("Kakanin"),
    PANG_NEGOSYO("Pang-Negosyo");

    companion object {
        fun fromString(value: String): RecipeCategory {
            return when (value.trim().lowercase()) {
                "ulam" -> ULAM
                "merienda" -> MERIENDA
                "kakanin" -> KAKANIN
                "pang-negosyo", "pangnegosyo", "negosyo" -> PANG_NEGOSYO
                else -> ULAM
            }
        }
    }
}

@Serializable
data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val category: RecipeCategory,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val imageUrl: String = "",
    val localTermExplanations: Map<String, String> = emptyMap(),
    val isAiGenerated: Boolean = false
)
