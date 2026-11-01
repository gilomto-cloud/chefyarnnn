package com.trynite.chefyarnnn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trynite.chefyarnnn.data.repository.PalengkeRepository
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.model.Ingredient
import com.trynite.chefyarnnn.model.Recipe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeTimerState(
    val durationSeconds: Int,
    val remainingSeconds: Int = durationSeconds,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
)

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val checkedIngredientNames: Set<String> = emptySet(),
    val expandedLocalTerms: Set<String> = emptySet(),
    val recipeTimer: RecipeTimerState? = null,
    val snackbarMessage: String? = null
)

class RecipeDetailViewModel(
    private val recipeRepository: RecipeRepository,
    private val palengkeRepository: PalengkeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()
    private var timerJob: Job? = null

    fun loadRecipe(recipeId: String) {
        if (_uiState.value.recipe?.id == recipeId) return

        timerJob?.cancel()
        timerJob = null
        val recipe = recipeRepository.getRecipeById(recipeId)
        _uiState.update {
            it.copy(
                recipe = recipe,
                checkedIngredientNames = emptySet(),
                expandedLocalTerms = emptySet(),
                recipeTimer = null,
                snackbarMessage = null
            )
        }
    }

    fun setRecipeTimer(durationSeconds: Int) {
        if (durationSeconds <= 0) return
        timerJob?.cancel()
        timerJob = null
        _uiState.update {
            it.copy(recipeTimer = RecipeTimerState(durationSeconds = durationSeconds))
        }
    }

    fun startRecipeTimer() {
        val timer = _uiState.value.recipeTimer ?: return
        if (timer.isRunning || timer.remainingSeconds <= 0) return
        timerJob?.cancel()
        _uiState.update {
            it.copy(recipeTimer = timer.copy(isRunning = true, isFinished = false))
        }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val currentTimer = _uiState.value.recipeTimer ?: break
                if (!currentTimer.isRunning) break
                val remaining = (currentTimer.remainingSeconds - 1).coerceAtLeast(0)
                val finished = remaining == 0
                _uiState.update {
                    it.copy(
                        recipeTimer = currentTimer.copy(
                            remainingSeconds = remaining,
                            isRunning = !finished,
                            isFinished = finished
                        ),
                        snackbarMessage = if (finished) "Tapos na ang timer para sa resipe!" else it.snackbarMessage
                    )
                }
                if (finished) break
            }
            timerJob = null
        }
    }

    fun pauseRecipeTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update {
            val timer = it.recipeTimer ?: return@update it
            it.copy(recipeTimer = timer.copy(isRunning = false))
        }
    }

    fun resetRecipeTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update {
            val timer = it.recipeTimer ?: return@update it
            it.copy(
                recipeTimer = timer.copy(
                    remainingSeconds = timer.durationSeconds,
                    isRunning = false,
                    isFinished = false
                )
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
