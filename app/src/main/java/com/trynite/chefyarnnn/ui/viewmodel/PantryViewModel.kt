package com.trynite.chefyarnnn.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trynite.chefyarnnn.data.repository.ChefAiRepository
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.model.Recipe
import com.trynite.chefyarnnn.model.RecipeCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PantryComboPack(
    val name: String,
    val emoji: String,
    val items: List<String>
)

data class PantryUiState(
    val tagInputText: String = "",
    val ingredientTags: List<String> = emptyList(),
    val activePresetCategory: String = "Lahat",
    val presetCategories: Map<String, List<String>> = mapOf(
        "Lahat" to listOf(
            "Bawang (Garlic)",
            "Sibuyas (Onion)",
            "Kamatis (Tomato)",
            "Toyo (Soy Sauce)",
            "Suka (Vinegar)",
            "Baboy (Pork)",
            "Manok (Chicken)",
            "Itlog (Egg)",
            "Mantika (Cooking Oil)",
            "Kangkong",
            "Sitaw",
            "Talong",
            "Kalabasa",
            "Patis",
            "Gata"
        ),
        "Protina" to listOf("Baboy (Pork)", "Manok (Chicken)", "Baka (Beef)", "Isda (Fish)", "Hipon (Shrimp)", "Itlog (Egg)", "Tokwa (Tofu)"),
        "Gulay" to listOf("Kangkong", "Sitaw", "Talong", "Kalabasa", "Sayote", "Repolyo", "Kamatis", "Sibuyas", "Bawang"),
        "Pampalasa" to listOf("Toyo (Soy Sauce)", "Suka (Vinegar)", "Patis", "Paminta", "Bagoong", "Gata", "Sinigang Mix")
    ),
    val comboPacks: List<PantryComboPack> = listOf(
        PantryComboPack("Adobo Pack", "🍲", listOf("Baboy", "Manok", "Bawang", "Toyo", "Suka", "Paminta")),
        PantryComboPack("Sinigang Pack", "🥣", listOf("Baboy", "Kamatis", "Sibuyas", "Kangkong", "Sinigang Mix")),
        PantryComboPack("Gisado Pack", "🍳", listOf("Bawang", "Sibuyas", "Kamatis", "Mantika", "Itlog"))
    ),
    val preferredCategory: RecipeCategory? = null,
    val capturedBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val generatedRecipe: Recipe? = null,
    val isRecipeSaved: Boolean = false,
    val generatedHistory: List<Recipe> = emptyList(),
    val errorMessage: String? = null
) {
    // Legacy support property for presetIngredients
    val presetIngredients: List<String>
        get() = presetCategories[activePresetCategory] ?: presetCategories["Lahat"] ?: emptyList()
}

class PantryViewModel(
    private val geminiRepository: ChefAiRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    fun onTagInputChanged(text: String) {
        _uiState.update { it.copy(tagInputText = text) }
    }

    fun addTag(tagText: String = _uiState.value.tagInputText) {
        val tagsToAdd = tagText
            .split(',', '\n')
            .map(String::trim)
            .filter(String::isNotBlank)
        if (tagsToAdd.isEmpty()) return

        _uiState.update { current ->
            val knownTags = current.ingredientTags.map(String::lowercase).toMutableSet()
            val newTags = tagsToAdd.filter { knownTags.add(it.lowercase()) }
            current.copy(
                ingredientTags = current.ingredientTags + newTags,
                tagInputText = "",
                errorMessage = null
            )
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { current ->
            current.copy(ingredientTags = current.ingredientTags.filter { it != tag })
        }
    }

    fun setPresetCategory(category: String) {
        _uiState.update { it.copy(activePresetCategory = category) }
    }

    fun applyComboPack(combo: PantryComboPack) {
        _uiState.update { current ->
            val knownTags = current.ingredientTags.map(String::lowercase).toMutableSet()
            val newItems = combo.items.filter { knownTags.add(it.lowercase()) }
            current.copy(
                ingredientTags = current.ingredientTags + newItems,
                errorMessage = null
            )
        }
    }

    fun togglePreset(preset: String) {
        val tag = preset.split("(").first().trim()
        _uiState.update { current ->
            if (current.ingredientTags.any { it.equals(tag, ignoreCase = true) }) {
                current.copy(ingredientTags = current.ingredientTags.filterNot { it.equals(tag, ignoreCase = true) })
            } else {
                current.copy(ingredientTags = current.ingredientTags + tag, errorMessage = null)
            }
        }
    }

    fun setCapturedImage(bitmap: Bitmap?) {
        _uiState.update { it.copy(capturedBitmap = bitmap, errorMessage = null) }
    }

    fun clearImage() {
        _uiState.update { it.copy(capturedBitmap = null) }
    }

    fun clearIngredients() {
        _uiState.update { it.copy(ingredientTags = emptyList()) }
    }

    fun showImageError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun setPreferredCategory(category: RecipeCategory?) {
        _uiState.update { it.copy(preferredCategory = category, errorMessage = null) }
    }

    fun generateDishes() {
        val currentState = _uiState.value
        val items = currentState.ingredientTags
        val bitmap = currentState.capturedBitmap

        if (items.isEmpty() && bitmap == null) {
            _uiState.update { it.copy(errorMessage = "Maglagay ng kahit isang sangkap o larawan!") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, generatedRecipe = null, isRecipeSaved = false) }

        viewModelScope.launch {
            try {
                val recipe = geminiRepository.generateRecipeFromPantry(
                    pantryItems = items,
                    imageBitmap = bitmap,
                    preferredCategory = currentState.preferredCategory
                )
                _uiState.update {
                    val updatedHistory = listOf(recipe) + it.generatedHistory.filterNot { r -> r.id == recipe.id }
                    it.copy(
                        isLoading = false,
                        generatedRecipe = recipe,
                        isRecipeSaved = false,
                        generatedHistory = updatedHistory
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Hindi makakonekta sa Firebase AI: ${e.localizedMessage ?: e.javaClass.simpleName}"
                    )
                }
            }
        }
    }

    fun saveGeneratedRecipe(recipe: Recipe) {
        recipeRepository.addCustomRecipe(recipe)
        _uiState.update { it.copy(isRecipeSaved = true) }
    }

    fun toggleFavorite(recipeId: String) {
        recipeRepository.toggleFavorite(recipeId)
        _uiState.update { current ->
            val updatedGen = current.generatedRecipe?.let {
                if (it.id == recipeId) it.copy(isFavorite = !it.isFavorite) else it
            }
            val updatedHist = current.generatedHistory.map {
                if (it.id == recipeId) it.copy(isFavorite = !it.isFavorite) else it
            }
            current.copy(generatedRecipe = updatedGen, generatedHistory = updatedHist)
        }
    }

    fun selectHistoryRecipe(recipe: Recipe) {
        _uiState.update { it.copy(generatedRecipe = recipe, isRecipeSaved = true) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(
        private val geminiRepository: ChefAiRepository,
        private val recipeRepository: RecipeRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PantryViewModel(geminiRepository, recipeRepository) as T
        }
    }
}
