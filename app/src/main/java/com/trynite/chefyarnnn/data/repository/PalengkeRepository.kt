package com.trynite.chefyarnnn.data.repository

import android.content.Context
import android.util.Log
import com.trynite.chefyarnnn.model.MarketCategory
import com.trynite.chefyarnnn.model.PalengkeItem
import com.trynite.chefyarnnn.model.Recipe
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class PalengkeRepository(context: Context? = null) {
    private val preferences = context?.getSharedPreferences(
        USER_DATA_PREFERENCES,
        Context.MODE_PRIVATE
    )

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

    private val _items = MutableStateFlow(loadItems())
    val items: StateFlow<List<PalengkeItem>> = _items.asStateFlow()

    fun addItem(
        name: String,
        amount: String,
        unit: String,
        category: MarketCategory,
        price: String = "",
        recipeOriginTitle: String? = null
    ) {
        val newItem = PalengkeItem(
            id = UUID.randomUUID().toString(),
            name = name,
            amount = amount,
            unit = unit,
            category = category,
            price = price,
            isChecked = false,
            recipeOriginTitle = recipeOriginTitle
        )
        _items.value = _items.value + newItem
        saveItems()
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
            saveItems()
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
        saveItems()
    }

    fun updateItemPrice(itemId: String, price: String) {
        _items.value = _items.value.map { item ->
            if (item.id == itemId) item.copy(price = price) else item
        }
        saveItems()
    }

    fun removeItem(itemId: String) {
        _items.value = _items.value.filter { it.id != itemId }
        saveItems()
    }

    fun clearCheckedItems() {
        _items.value = _items.value.filterNot { it.isChecked }
        saveItems()
    }

    fun clearAllItems() {
        _items.value = emptyList()
        saveItems()
    }

    private fun loadItems(): List<PalengkeItem> {
        val storedValue = preferences?.getString(ITEMS_KEY, null) ?: return initialItems
        return try {
            Json.decodeFromString<List<PalengkeItem>>(storedValue)
        } catch (exception: SerializationException) {
            Log.e(TAG, "Could not restore the saved shopping checklist.", exception)
            initialItems
        }
    }

    private fun saveItems() {
        preferences?.edit()?.putString(ITEMS_KEY, Json.encodeToString(_items.value))?.apply()
    }

    private companion object {
        const val TAG = "PalengkeRepository"
        const val USER_DATA_PREFERENCES = "chefyarnnn_user_data"
        const val ITEMS_KEY = "palengke_items"
    }
}
