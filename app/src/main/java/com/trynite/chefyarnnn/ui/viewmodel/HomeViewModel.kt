package com.trynite.chefyarnnn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.data.repository.SortOrder
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
    val onlyFavorites: Boolean = false,
    val onlyQuickMeals: Boolean = false,
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val recipes: List<Recipe> = emptyList(),
    val totalRecipeCount: Int = 0,
    val favoriteCount: Int = 0
)

class HomeViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<RecipeCategory?>(null)
    private val _onlyFavorites = MutableStateFlow(false)
    private val _onlyQuickMeals = MutableStateFlow(false)
    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)

    val uiState: StateFlow<HomeUiState> = combine(
        recipeRepository.recipes,
        _searchQuery,
        _selectedCategory,
        _onlyFavorites,
        _onlyQuickMeals,
        _sortOrder
    ) { array ->
        @Suppress("UNCHECKED_CAST")
        val recipes = array[0] as List<Recipe>
        val query = array[1] as String
        val category = array[2] as RecipeCategory?
        val favorites = array[3] as Boolean
        val quickMeals = array[4] as Boolean
        val sort = array[5] as SortOrder

        val filtered = recipeRepository.searchRecipes(
            query = query,
            category = category,
            onlyFavorites = favorites,
            onlyQuickMeals = quickMeals,
            sortOrder = sort
        )
        HomeUiState(
            searchQuery = query,
            selectedCategory = category,
            onlyFavorites = favorites,
            onlyQuickMeals = quickMeals,
            sortOrder = sort,
            recipes = filtered,
            totalRecipeCount = recipes.size,
            favoriteCount = recipes.count { it.isFavorite }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState(
            recipes = recipeRepository.getRecipes(),
            totalRecipeCount = recipeRepository.getRecipes().size,
            favoriteCount = recipeRepository.getRecipes().count { it.isFavorite }
        )
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: RecipeCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun toggleFavoritesFilter() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun toggleQuickMealsFilter() {
        _onlyQuickMeals.value = !_onlyQuickMeals.value
    }

    fun onSortOrderChange(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    fun toggleFavorite(recipeId: String) {
        recipeRepository.toggleFavorite(recipeId)
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _onlyFavorites.value = false
        _onlyQuickMeals.value = false
        _sortOrder.value = SortOrder.DEFAULT
    }

    class Factory(private val recipeRepository: RecipeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(recipeRepository) as T
        }
    }
}
