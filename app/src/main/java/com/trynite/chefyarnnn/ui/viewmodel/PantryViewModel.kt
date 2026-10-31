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

data class PantryUiState(
    val tagInputText: String = "",
    val ingredientTags: List<String> = emptyList(),
    val presetIngredients: List<String> = listOf(
        "Bawang (Garlic)",
        "Sibuyas (Onion)",
        "Kamatis (Tomato)",
        "Toyo (Soy Sauce)",
        "Suka (Vinegar)",
        "Baboy (Pork)",
        "Manok (Chicken)",
        "Itlog (Egg)",
        "Mantika (Cooking Oil)"
    ),
    val preferredCategory: RecipeCategory? = null,
    val capturedBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val generatedRecipe: Recipe? = null,
    val isRecipeSaved: Boolean = false,
    val errorMessage: String? = null
)

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
                recipeRepository.addCustomRecipe(recipe)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generatedRecipe = recipe,
                        isRecipeSaved = true
                    )
                }
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
