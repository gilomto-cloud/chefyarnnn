package com.trynite.chefyarnnn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.model.Recipe
import com.trynite.chefyarnnn.model.RecipeCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val searchQuery: String = "",
    val selectedCategory: RecipeCategory? = null,
    val recipes: List<Recipe> = emptyList()
)

class HomeViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<RecipeCategory?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        recipeRepository.recipes,
        _searchQuery,
        _selectedCategory
    ) { recipes, query, category ->
        val filtered = recipeRepository.searchRecipes(query, category)
        HomeUiState(
            searchQuery = query,
            selectedCategory = category,
            recipes = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState(recipes = recipeRepository.getRecipes())
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: RecipeCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    class Factory(private val recipeRepository: RecipeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(recipeRepository) as T
        }
    }
}
