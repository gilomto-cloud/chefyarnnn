package com.trynite.chefyarnnn.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowWidthSizeClass
import com.trynite.chefyarnnn.data.repository.ChefAiRepository
import com.trynite.chefyarnnn.data.repository.PalengkeRepository
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.ui.screen.AiChefChatScreen
import com.trynite.chefyarnnn.ui.screen.HomeScreen
import com.trynite.chefyarnnn.ui.screen.PalengkeChecklistScreen
import com.trynite.chefyarnnn.ui.screen.PantryScannerScreen
import com.trynite.chefyarnnn.ui.screen.RecipeDetailScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data object PantryScanner : AppRoute

    @Serializable
    data class AiChefChat(val initialPrompt: String = "") : AppRoute

    @Serializable
    data object PalengkeChecklist : AppRoute

    @Serializable
    data class RecipeDetail(val recipeId: String) : AppRoute
}

enum class TopLevelDestination(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector
) {
    HOME(AppRoute.Home, "Explorer", Icons.Rounded.RestaurantMenu),
    PANTRY(AppRoute.PantryScanner, "Anong Lutuin?", Icons.Rounded.AutoAwesome),
    CHAT(AppRoute.AiChefChat(), "Chef Cindy", Icons.AutoMirrored.Rounded.Chat),
    PALENGKE(AppRoute.PalengkeChecklist, "Palengke", Icons.Rounded.ShoppingBag)
}

@Suppress("DEPRECATION")
@Composable
fun AppScaffold(
    recipeRepository: RecipeRepository,
    geminiRepository: ChefAiRepository,
    palengkeRepository: PalengkeRepository
) {
    val backStack = remember { mutableStateListOf<Any>(AppRoute.Home) }
    val currentRoute = backStack.lastOrNull() ?: AppRoute.Home

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    val isTopLevel = when (currentRoute) {
        is AppRoute.Home,
        is AppRoute.PantryScanner,
        is AppRoute.AiChefChat,
        is AppRoute.PalengkeChecklist -> true
        else -> false
    }

    val selectedDestination = TopLevelDestination.entries.firstOrNull { dest ->
        dest.route::class == currentRoute::class
    } ?: TopLevelDestination.HOME

    Scaffold(
        bottomBar = {
            if (!isExpanded && isTopLevel) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val isSelected = selectedDestination == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    backStack.clear()
                                    backStack.add(destination.route)
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isExpanded && isTopLevel) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    TopLevelDestination.entries.forEach { destination ->
                        val isSelected = selectedDestination == destination
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    backStack.clear()
                                    backStack.add(destination.route)
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            is AppRoute.Home -> NavEntry(key) {
                                HomeScreen(
                                    recipeRepository = recipeRepository,
                                    onRecipeClick = { recipeId ->
                                        backStack.add(AppRoute.RecipeDetail(recipeId))
                                    },
                                    onNavigateToPantryScanner = {
                                        backStack.clear()
                                        backStack.add(AppRoute.PantryScanner)
                                    }
                                )
                            }
                            is AppRoute.PantryScanner -> NavEntry(key) {
                                PantryScannerScreen(
                                    geminiRepository = geminiRepository,
                                    recipeRepository = recipeRepository,
                                    onRecipeGenerated = { recipe ->
                                        backStack.add(AppRoute.RecipeDetail(recipe.id))
                                    }
                                )
                            }
                            is AppRoute.AiChefChat -> NavEntry(key) {
                                AiChefChatScreen(
                                    geminiRepository = geminiRepository,
                                    recipeRepository = recipeRepository,
                                    onViewRecipe = { recipeId ->
                                        backStack.add(AppRoute.RecipeDetail(recipeId))
                                    },
                                    initialPrompt = key.initialPrompt
                                )
                            }
                            is AppRoute.PalengkeChecklist -> NavEntry(key) {
                                PalengkeChecklistScreen(
                                    palengkeRepository = palengkeRepository
                                )
                            }
                            is AppRoute.RecipeDetail -> NavEntry(key) {
                                RecipeDetailScreen(
                                    recipeId = key.recipeId,
                                    recipeRepository = recipeRepository,
                                    palengkeRepository = palengkeRepository,
                                    onBackClick = {
                                        if (backStack.size > 1) {
                                            backStack.removeLastOrNull()
                                        }
                                    },
                                    onAskNanayChef = { prompt ->
                                        backStack.add(AppRoute.AiChefChat(initialPrompt = prompt))
                                    }
                                )
                            }
                            else -> NavEntry(key) {
                                HomeScreen(
                                    recipeRepository = recipeRepository,
                                    onRecipeClick = { recipeId ->
                                        backStack.add(AppRoute.RecipeDetail(recipeId))
                                    },
                                    onNavigateToPantryScanner = {
                                        backStack.clear()
                                        backStack.add(AppRoute.PantryScanner)
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
