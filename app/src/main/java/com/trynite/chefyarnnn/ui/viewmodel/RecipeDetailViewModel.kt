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

data class StepTimerState(
    val durationSeconds: Int,
    val remainingSeconds: Int = durationSeconds,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
)

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val checkedIngredientNames: Set<String> = emptySet(),
    val expandedLocalTerms: Set<String> = emptySet(),
    val stepTimers: Map<Int, StepTimerState> = emptyMap(),
    val snackbarMessage: String? = null
)

class RecipeDetailViewModel(
    private val recipeRepository: RecipeRepository,
    private val palengkeRepository: PalengkeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()
    private val timerJobs = mutableMapOf<Int, Job>()

    fun loadRecipe(recipeId: String) {
        timerJobs.values.forEach(Job::cancel)
        timerJobs.clear()
        val recipe = recipeRepository.getRecipeById(recipeId)
        _uiState.update {
            it.copy(
                recipe = recipe,
                checkedIngredientNames = emptySet(),
                expandedLocalTerms = emptySet(),
                stepTimers = emptyMap(),
                snackbarMessage = null
            )
        }
    }

    fun setStepTimer(stepIndex: Int, durationSeconds: Int) {
        if (stepIndex < 0 || durationSeconds <= 0) return
        timerJobs.remove(stepIndex)?.cancel()
        _uiState.update { current ->
            current.copy(
                stepTimers = current.stepTimers + (
                    stepIndex to StepTimerState(durationSeconds = durationSeconds)
                    )
            )
        }
    }

    fun startStepTimer(stepIndex: Int) {
        val timer = _uiState.value.stepTimers[stepIndex] ?: return
        if (timer.isRunning || timer.remainingSeconds <= 0) return
        timerJobs.remove(stepIndex)?.cancel()
        _uiState.update { current ->
            current.copy(
                stepTimers = current.stepTimers + (
                    stepIndex to timer.copy(isRunning = true, isFinished = false)
                    )
            )
        }
        timerJobs[stepIndex] = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val currentTimer = _uiState.value.stepTimers[stepIndex] ?: break
                if (!currentTimer.isRunning) break
                val remaining = (currentTimer.remainingSeconds - 1).coerceAtLeast(0)
                val finished = remaining == 0
                _uiState.update { current ->
                    current.copy(
                        stepTimers = current.stepTimers + (
                            stepIndex to currentTimer.copy(
                                remainingSeconds = remaining,
                                isRunning = !finished,
                                isFinished = finished
                            )
                            ),
                        snackbarMessage = if (finished) "Tapos na ang timer para sa hakbang ${stepIndex + 1}!" else current.snackbarMessage
                    )
                }
                if (finished) break
            }
            timerJobs.remove(stepIndex)
        }
    }

    fun pauseStepTimer(stepIndex: Int) {
        timerJobs.remove(stepIndex)?.cancel()
        _uiState.update { current ->
            val timer = current.stepTimers[stepIndex] ?: return@update current
            current.copy(
                stepTimers = current.stepTimers + (stepIndex to timer.copy(isRunning = false))
            )
        }
    }

    fun resetStepTimer(stepIndex: Int) {
        timerJobs.remove(stepIndex)?.cancel()
        _uiState.update { current ->
            val timer = current.stepTimers[stepIndex] ?: return@update current
            current.copy(
                stepTimers = current.stepTimers + (
                    stepIndex to timer.copy(
                        remainingSeconds = timer.durationSeconds,
                        isRunning = false,
                        isFinished = false
                    )
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
