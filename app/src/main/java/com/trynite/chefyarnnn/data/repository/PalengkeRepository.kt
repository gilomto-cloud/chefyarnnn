package com.trynite.chefyarnnn.data.repository

import com.trynite.chefyarnnn.model.MarketCategory
import com.trynite.chefyarnnn.model.PalengkeItem
import com.trynite.chefyarnnn.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class PalengkeRepository {

    private val initialItems = listOf(
        PalengkeItem(
            id = "item_1",
            name = "Garlic (Bawang)",
            amount = "2",
            unit = "heads",
            category = MarketCategory.GULAYAN,
            isChecked = false,
            recipeOriginTitle = "Classic Chicken & Pork Adobo"
        ),
        PalengkeItem(
            id = "item_2",
            name = "Pork belly (Liempo)",
            amount = "500",
            unit = "g",
            category = MarketCategory.KARNEHAN,
            isChecked = false,
            recipeOriginTitle = "Classic Chicken & Pork Adobo"
        ),
        PalengkeItem(
            id = "item_3",
            name = "Soy sauce (Toyo)",
            amount = "1",
            unit = "bottle",
            category = MarketCategory.PAMPALASA,
            isChecked = true,
            recipeOriginTitle = "Staple"
        ),
        PalengkeItem(
            id = "item_4",
            name = "Kangkong",
            amount = "2",
            unit = "bunches",
            category = MarketCategory.GULAYAN,
            isChecked = false,
            recipeOriginTitle = "Pork Sinigang sa Sampalok"
        )
    )

    private val _items = MutableStateFlow<List<PalengkeItem>>(initialItems)
    val items: StateFlow<List<PalengkeItem>> = _items.asStateFlow()

    fun addItem(
        name: String,
        amount: String,
        unit: String,
        category: MarketCategory,
        recipeOriginTitle: String? = null
    ) {
        val newItem = PalengkeItem(
            id = UUID.randomUUID().toString(),
            name = name,
            amount = amount,
            unit = unit,
            category = category,
            isChecked = false,
            recipeOriginTitle = recipeOriginTitle
        )
        _items.value = _items.value + newItem
    }

    fun addIngredientsFromRecipe(recipe: Recipe) {
        val existingNames = _items.value.map { it.name.lowercase().trim() }.toSet()
        val newItems = recipe.ingredients.filterNot { existingNames.contains(it.name.lowercase().trim()) }.map { ing ->
            PalengkeItem(
                id = UUID.randomUUID().toString(),
                name = ing.name,
                amount = ing.amount,
                unit = ing.unit,
                category = ing.marketCategory,
                isChecked = false,
                recipeOriginTitle = recipe.title
            )
        }
        if (newItems.isNotEmpty()) {
            _items.value = _items.value + newItems
        }
    }

    fun toggleItemChecked(itemId: String) {
        _items.value = _items.value.map { item ->
            if (item.id == itemId) {
                item.copy(isChecked = !item.isChecked)
            } else {
                item
            }
        }
    }

    fun removeItem(itemId: String) {
        _items.value = _items.value.filter { it.id != itemId }
    }

    fun clearCheckedItems() {
        _items.value = _items.value.filterNot { it.isChecked }
    }

    fun clearAllItems() {
        _items.value = emptyList()
    }
}
