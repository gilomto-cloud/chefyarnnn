package com.trynite.chefyarnnn

import android.graphics.Bitmap
import com.trynite.chefyarnnn.data.repository.ChefAiRepository
import com.trynite.chefyarnnn.data.repository.PalengkeRepository
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.model.RecipeCategory
import com.trynite.chefyarnnn.model.ChatMessage
import com.trynite.chefyarnnn.model.Recipe
import com.trynite.chefyarnnn.ui.viewmodel.HomeViewModel
import com.trynite.chefyarnnn.ui.viewmodel.PantryViewModel
import com.trynite.chefyarnnn.ui.viewmodel.RecipeDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Task2ViewModelsTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var palengkeRepository: PalengkeRepository
    private lateinit var geminiRepository: ChefAiRepository
    private var requestedRecipeCategory: RecipeCategory? = null

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        recipeRepository = RecipeRepository()
        palengkeRepository = PalengkeRepository()
        geminiRepository = object : ChefAiRepository {
            override suspend fun generateRecipeFromPantry(
                pantryItems: List<String>,
                imageBitmap: Bitmap?,
                preferredCategory: RecipeCategory?
            ): Recipe {
                requestedRecipeCategory = preferredCategory
                return recipeRepository.getRecipeById("recipe_adobo")!!
                    .copy(id = "ai_test_recipe", isAiGenerated = true)
            }

            override suspend fun chatWithNanayTito(
                userMessage: String,
                chatHistory: List<ChatMessage>
            ): String = "Sige anak!"
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testHomeViewModelSearchAndFilter() = runTest {
        val viewModel = HomeViewModel(recipeRepository)

        assertEquals("", viewModel.uiState.value.searchQuery)

        viewModel.onSearchQueryChange("Adobo")
        assertEquals("Adobo", viewModel.uiState.value.searchQuery)
        val filtered = viewModel.uiState.value.recipes
        assertTrue(filtered.any { it.title.contains("Adobo") })

        viewModel.onCategorySelect(RecipeCategory.MERIENDA)
        assertEquals(RecipeCategory.MERIENDA, viewModel.uiState.value.selectedCategory)

        viewModel.clearSearch()
        assertEquals("", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun testHomeViewModelFavoritesQuickMealsAndSort() = runTest {
        val viewModel = HomeViewModel(recipeRepository)

        // Toggle favorite for Adobo
        viewModel.toggleFavorite("recipe_adobo")
        assertEquals(1, viewModel.uiState.value.favoriteCount)

        // Filter by favorites only
        viewModel.toggleFavoritesFilter()
        assertTrue(viewModel.uiState.value.onlyFavorites)
        assertEquals(1, viewModel.uiState.value.recipes.size)
        assertEquals("recipe_adobo", viewModel.uiState.value.recipes.first().id)

        // Reset filters
        viewModel.resetFilters()
        assertFalse(viewModel.uiState.value.onlyFavorites)
        assertEquals(recipeRepository.getRecipes().size, viewModel.uiState.value.recipes.size)

        // Filter quick meals (<= 30 mins)
        viewModel.toggleQuickMealsFilter()
        assertTrue(viewModel.uiState.value.onlyQuickMeals)
        assertTrue(viewModel.uiState.value.recipes.all { (it.prepTimeMinutes + it.cookTimeMinutes) <= 30 })

        // Sort by cook time asc
        viewModel.onSortOrderChange(com.trynite.chefyarnnn.data.repository.SortOrder.TIME_ASC)
        val times = viewModel.uiState.value.recipes.map { it.prepTimeMinutes + it.cookTimeMinutes }
        assertEquals(times, times.sorted())
    }

    @Test
    fun testRecipeDetailViewModelChecklistAndPalengke() = runTest {
        val viewModel = RecipeDetailViewModel(recipeRepository, palengkeRepository)
        val recipeId = "recipe_adobo"

        viewModel.loadRecipe(recipeId)
        val state = viewModel.uiState.value
        assertNotNull(state.recipe)
        assertEquals("recipe_adobo", state.recipe?.id)

        val firstIng = state.recipe!!.ingredients.first().name
        viewModel.toggleIngredientCheck(firstIng)
        assertTrue(viewModel.uiState.value.checkedIngredientNames.contains(firstIng))

        viewModel.addMissingIngredientsToPalengke()
        assertNotNull(viewModel.uiState.value.snackbarMessage)
        val palengkeItems = palengkeRepository.items.value
        assertTrue(palengkeItems.isNotEmpty())
    }

    @Test
    fun testLoadingSameRecipePreservesRunningTimerAndCheckedIngredients() = runTest {
        val viewModel = RecipeDetailViewModel(recipeRepository, palengkeRepository)
        val recipeId = "recipe_adobo"
        viewModel.loadRecipe(recipeId)
        val ingredientName = viewModel.uiState.value.recipe!!.ingredients.first().name
        viewModel.toggleIngredientCheck(ingredientName)
        viewModel.setRecipeTimer(durationSeconds = 10)
        viewModel.startRecipeTimer()
        advanceTimeBy(1_000)
        runCurrent()

        viewModel.loadRecipe(recipeId)

        assertTrue(viewModel.uiState.value.checkedIngredientNames.contains(ingredientName))
        assertEquals(9, viewModel.uiState.value.recipeTimer?.remainingSeconds)
        assertTrue(viewModel.uiState.value.recipeTimer?.isRunning == true)

        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(8, viewModel.uiState.value.recipeTimer?.remainingSeconds)
    }

    @Test
    fun testRecipeTimerCanBePausedResetAndFinishes() = runTest {
        val viewModel = RecipeDetailViewModel(recipeRepository, palengkeRepository)
        viewModel.loadRecipe("recipe_adobo")
        viewModel.setRecipeTimer(durationSeconds = 2)

        viewModel.startRecipeTimer()
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(1, viewModel.uiState.value.recipeTimer?.remainingSeconds)

        viewModel.pauseRecipeTimer()
        advanceTimeBy(2_000)
        runCurrent()
        assertEquals(1, viewModel.uiState.value.recipeTimer?.remainingSeconds)

        viewModel.resetRecipeTimer()
        assertEquals(2, viewModel.uiState.value.recipeTimer?.remainingSeconds)

        viewModel.startRecipeTimer()
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(1, viewModel.uiState.value.recipeTimer?.remainingSeconds)
        viewModel.startRecipeTimer()
        advanceTimeBy(1_000)
        runCurrent()
        assertTrue(viewModel.uiState.value.recipeTimer?.isFinished == true)
        assertEquals("Tapos na ang timer para sa resipe!", viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun testPantryViewModelTagManagement() = runTest {
        val viewModel = PantryViewModel(geminiRepository, recipeRepository)

        viewModel.onTagInputChanged("Pork belly, toyo, Suka")
        viewModel.addTag()

        assertTrue(viewModel.uiState.value.ingredientTags.contains("Pork belly"))
        assertTrue(viewModel.uiState.value.ingredientTags.contains("toyo"))
        assertTrue(viewModel.uiState.value.ingredientTags.contains("Suka"))
        assertEquals("", viewModel.uiState.value.tagInputText)

        viewModel.togglePreset("Bawang (Garlic)")
        assertTrue(viewModel.uiState.value.ingredientTags.contains("Bawang"))
        viewModel.onTagInputChanged("bawang")
        viewModel.addTag()
        assertEquals(4, viewModel.uiState.value.ingredientTags.size)

        viewModel.removeTag("Pork belly")
        assertFalse(viewModel.uiState.value.ingredientTags.contains("Pork belly"))
        viewModel.clearIngredients()
        assertTrue(viewModel.uiState.value.ingredientTags.isEmpty())
    }

    @Test
    fun testPantryViewModelSavesGeneratedRecipeOnlyOnRequest() = runTest {
        val viewModel = PantryViewModel(geminiRepository, recipeRepository)
        viewModel.togglePreset("Baboy (Pork)")
        viewModel.setPreferredCategory(RecipeCategory.MERIENDA)

        viewModel.generateDishes()

        // Wait for generated recipe state update
        val state = withTimeout(3000) {
            viewModel.uiState.first { it.generatedRecipe != null }
        }

        assertNotNull(state.generatedRecipe)
        assertFalse(state.isRecipeSaved)
        assertFalse(state.isLoading)
        assertEquals(RecipeCategory.MERIENDA, requestedRecipeCategory)
        assertTrue(viewModel.uiState.value.generatedHistory.isNotEmpty())
        assertEquals(null, recipeRepository.getRecipeById("ai_test_recipe"))

        viewModel.saveGeneratedRecipe(state.generatedRecipe!!)

        assertTrue(viewModel.uiState.value.isRecipeSaved)
        assertNotNull(recipeRepository.getRecipeById("ai_test_recipe"))
    }

    @Test
    fun testPantryViewModelComboPacksAndCategories() = runTest {
        val viewModel = PantryViewModel(geminiRepository, recipeRepository)

        // Combo pack application
        val adoboCombo = viewModel.uiState.value.comboPacks.first { it.name.contains("Adobo") }
        viewModel.applyComboPack(adoboCombo)
        assertTrue(viewModel.uiState.value.ingredientTags.contains("Baboy"))
        assertTrue(viewModel.uiState.value.ingredientTags.contains("Toyo"))

        // Category filter selection
        viewModel.setPresetCategory("Protina")
        assertEquals("Protina", viewModel.uiState.value.activePresetCategory)
        assertTrue(viewModel.uiState.value.presetIngredients.contains("Baka (Beef)"))
    }
}
