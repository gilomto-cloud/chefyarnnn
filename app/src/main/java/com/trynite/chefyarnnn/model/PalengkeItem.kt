package com.trynite.chefyarnnn.model

import kotlinx.serialization.Serializable

@Serializable
data class PalengkeItem(
    val id: String,
    val name: String,
    val amount: String,
    val unit: String,
    val category: MarketCategory,
    val price: String = "",
    val isChecked: Boolean = false,
    val recipeOriginTitle: String? = null
)
