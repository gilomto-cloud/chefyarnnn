package com.trynite.chefyarnnn.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trynite.chefyarnnn.data.repository.ChefAiRepository
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.model.Recipe
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
        val trimmed = tagText.trim()
        if (trimmed.isNotBlank() && !_uiState.value.ingredientTags.contains(trimmed)) {
            _uiState.update { current ->
                current.copy(
                    ingredientTags = current.ingredientTags + trimmed,
                    tagInputText = ""
                )
            }
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { current ->
            current.copy(ingredientTags = current.ingredientTags.filter { it != tag })
        }
    }

    fun togglePreset(preset: String) {
        // Strip parenthetical English for tag if desired or use as tag
        val tag = preset.split("(").first().trim()
        _uiState.update { current ->
            if (current.ingredientTags.contains(tag)) {
                current.copy(ingredientTags = current.ingredientTags.filter { it != tag })
            } else {
                current.copy(ingredientTags = current.ingredientTags + tag)
            }
        }
    }

    fun setCapturedImage(bitmap: Bitmap?) {
        _uiState.update { it.copy(capturedBitmap = bitmap) }
    }

    fun clearImage() {
        _uiState.update { it.copy(capturedBitmap = null) }
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
                    imageBitmap = bitmap
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
