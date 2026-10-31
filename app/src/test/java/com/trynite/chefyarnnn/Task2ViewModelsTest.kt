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
import kotlinx.coroutines.test.resetMain
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        recipeRepository = RecipeRepository()
        palengkeRepository = PalengkeRepository()
        geminiRepository = object : ChefAiRepository {
            override suspend fun generateRecipeFromPantry(
                pantryItems: List<String>,
                imageBitmap: Bitmap?
            ): Recipe = recipeRepository.getRecipeById("recipe_adobo")!!

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
    fun testPantryViewModelTagManagement() = runTest {
        val viewModel = PantryViewModel(geminiRepository, recipeRepository)

        viewModel.onTagInputChanged("Pork belly")
        viewModel.addTag()

        assertTrue(viewModel.uiState.value.ingredientTags.contains("Pork belly"))
        assertEquals("", viewModel.uiState.value.tagInputText)

        viewModel.togglePreset("Bawang (Garlic)")
        assertTrue(viewModel.uiState.value.ingredientTags.contains("Bawang"))

        viewModel.removeTag("Pork belly")
        assertFalse(viewModel.uiState.value.ingredientTags.contains("Pork belly"))
    }

    @Test
    fun testPantryViewModelSavesRecipeReturnedByAiRepository() = runTest {
        val viewModel = PantryViewModel(geminiRepository, recipeRepository)
        viewModel.togglePreset("Baboy (Pork)")

        viewModel.generateDishes()

        // Wait for generated recipe state update
        val state = withTimeout(3000) {
            viewModel.uiState.first { it.generatedRecipe != null }
        }

        assertNotNull(state.generatedRecipe)
        assertTrue(state.isRecipeSaved)
        assertFalse(state.isLoading)
    }
}
