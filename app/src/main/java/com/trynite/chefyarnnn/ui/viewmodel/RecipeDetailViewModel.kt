package com.trynite.chefyarnnn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trynite.chefyarnnn.data.repository.PalengkeRepository
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.model.Ingredient
import com.trynite.chefyarnnn.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val checkedIngredientNames: Set<String> = emptySet(),
    val expandedLocalTerms: Set<String> = emptySet(),
    val snackbarMessage: String? = null
)

class RecipeDetailViewModel(
    private val recipeRepository: RecipeRepository,
    private val palengkeRepository: PalengkeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    fun loadRecipe(recipeId: String) {
        val recipe = recipeRepository.getRecipeById(recipeId)
        _uiState.update {
            it.copy(
                recipe = recipe,
                checkedIngredientNames = emptySet(),
                expandedLocalTerms = emptySet(),
                snackbarMessage = null
            )
        }
    }

    fun toggleIngredientCheck(ingredientName: String) {
        _uiState.update { current ->
            val updated = current.checkedIngredientNames.toMutableSet()
            if (updated.contains(ingredientName)) {
                updated.remove(ingredientName)
            } else {
                updated.add(ingredientName)
            }
            current.copy(checkedIngredientNames = updated)
        }
    }

    fun addMissingIngredientsToPalengke() {
        val recipe = _uiState.value.recipe ?: return
        val uncheckedIngredients = recipe.ingredients.filter { ing ->
            !_uiState.value.checkedIngredientNames.contains(ing.name)
        }
        val ingredientsToAdd = if (uncheckedIngredients.isEmpty()) recipe.ingredients else uncheckedIngredients

        palengkeRepository.addIngredientsFromRecipe(
            recipe.copy(ingredients = ingredientsToAdd)
        )
        val count = ingredientsToAdd.size
        _uiState.update {
            it.copy(snackbarMessage = "$count sangkap ang naidagdag sa Smart Palengke Checklist!")
        }
    }

    fun toggleLocalTerm(term: String) {
        _uiState.update { current ->
            val set = current.expandedLocalTerms.toMutableSet()
            if (set.contains(term)) {
                set.remove(term)
            } else {
                set.add(term)
            }
            current.copy(expandedLocalTerms = set)
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    class Factory(
        private val recipeRepository: RecipeRepository,
        private val palengkeRepository: PalengkeRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecipeDetailViewModel(recipeRepository, palengkeRepository) as T
        }
    }
}
