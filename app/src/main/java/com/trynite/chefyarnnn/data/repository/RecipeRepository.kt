package com.trynite.chefyarnnn.data.repository

import com.trynite.chefyarnnn.model.Ingredient
import com.trynite.chefyarnnn.model.MarketCategory
import com.trynite.chefyarnnn.model.Recipe
import com.trynite.chefyarnnn.model.RecipeCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecipeRepository {

    private val defaultTerms = mapOf(
        "gisa" to "Saute garlic, onions, and tomatoes in hot oil until aromatic and translucent.",
        "palambutin" to "Simmer low and slow covered until the meat becomes fork-tender.",
        "sangkutsahin" to "Briefly stir-fry meat in garlic and onions to brown slightly before adding liquids.",
        "tantsa" to "Season according to personal taste preferences (tantsahang Pinoy).",
        "kulo" to "Bring broth or sauce to a active rolling boil before dropping delicate ingredients.",
        "piga" to "Extract fresh coconut milk (gata) or calamansi juice by hand squeezing."
    )

    private val classicRecipes = listOf(
        Recipe(
            id = "recipe_adobo",
            title = "Classic Chicken & Pork Adobo",
            description = "The undisputed staple of Filipino homes: rich savory, garlicky, and vinegary braised chicken and pork.",
            category = RecipeCategory.ULAM,
            prepTimeMinutes = 15,
            cookTimeMinutes = 45,
            ingredients = listOf(
                Ingredient("Pork belly (Liempo)", "500", "g", MarketCategory.KARNEHAN),
                Ingredient("Chicken thighs", "500", "g", MarketCategory.KARNEHAN),
                Ingredient("Soy sauce (Toyo)", "0.5", "cup", MarketCategory.PAMPALASA),
                Ingredient("White vinegar (Suka)", "0.5", "cup", MarketCategory.PAMPALASA),
                Ingredient("Garlic (Bawang)", "1", "head (crushed)", MarketCategory.GULAYAN),
                Ingredient("Bay leaves (Laurel)", "3", "pcs", MarketCategory.PAMPALASA),
                Ingredient("Whole peppercorns (Paminta)", "1", "tsp", MarketCategory.PAMPALASA),
                Ingredient("Cooking oil", "2", "tbsp", MarketCategory.PAMPALASA)
            ),
            instructions = listOf(
                "Sangkutsahin ang karne: Sauté crushed garlic in hot oil until lightly golden, then add pork belly and chicken to brown slightly.",
                "Pour in soy sauce, vinegar, bay leaves, and peppercorns. Do not stir after adding vinegar until it boils for 3-5 minutes.",
                "Cover and lower heat to palambutin the meat for 35 minutes until tender and sauce reduces.",
                "Tantsa the taste: adjust saltiness or acid if desired, then serve sizzling hot with extra garlic rice."
            ),
            imageUrl = "https://images.unsplash.com/photo-1541832676-9b763b0239ab?q=80&w=800",
            localTermExplanations = mapOf(
                "sangkutsahin" to defaultTerms["sangkutsahin"]!!,
                "palambutin" to defaultTerms["palambutin"]!!,
                "tantsa" to defaultTerms["tantsa"]!!
            ),
            isAiGenerated = false
        ),
        Recipe(
            id = "recipe_sinigang",
            title = "Pork Sinigang sa Sampalok",
            description = "Hearty sour tamarind soup packed with tender pork ribs, water spinach (kangkong), radish, and green chili.",
            category = RecipeCategory.ULAM,
            prepTimeMinutes = 20,
            cookTimeMinutes = 50,
            ingredients = listOf(
                Ingredient("Pork ribs (Pati-pati)", "750", "g", MarketCategory.KARNEHAN),
                Ingredient("Tamarind mix (Sampalok mix)", "1", "pack", MarketCategory.PAMPALASA),
                Ingredient("Water spinach (Kangkong)", "1", "bunch", MarketCategory.GULAYAN),
                Ingredient("Radish (Labanos)", "1", "pc sliced", MarketCategory.GULAYAN),
                Ingredient("Taro root (Gabi)", "2", "pcs peeled", MarketCategory.GULAYAN),
                Ingredient("Tomatoes (Kamatis)", "2", "pcs quartered", MarketCategory.GULAYAN),
                Ingredient("Onion (Sibuyas)", "1", "pc sliced", MarketCategory.GULAYAN),
                Ingredient("Long green chili (Siling haba)", "2", "pcs", MarketCategory.GULAYAN),
                Ingredient("Fish sauce (Patis)", "2", "tbsp", MarketCategory.PAMPALASA)
            ),
            instructions = listOf(
                "In a pot, combine pork ribs, onions, tomatoes, and water. Bring to a kulo.",
                "Lower heat to palambutin the pork ribs and gabi for 40 minutes until meat is tender and gabi thickens the broth.",
                "Stir in tamarind soup base and season with patis.",
                "Add labanos and siling haba. Boil for 3 minutes.",
                "Turn off heat and throw in fresh kangkong leaves. Cover pot to let steam cook the greens."
            ),
            imageUrl = "https://images.unsplash.com/photo-1547592166-23ac45744acd?q=80&w=800",
            localTermExplanations = mapOf(
                "kulo" to defaultTerms["kulo"]!!,
                "palambutin" to defaultTerms["palambutin"]!!
            ),
            isAiGenerated = false
        ),
        Recipe(
            id = "recipe_pancit_bihon",
            title = "Fiesta Pancit Bihon Guisado",
            description = "Classic Filipino stir-fried rice noodles loaded with colorful vegetables, chicken, and savory seasonings.",
            category = RecipeCategory.ULAM,
            prepTimeMinutes = 20,
            cookTimeMinutes = 25,
            ingredients = listOf(
                Ingredient("Bihon rice noodles", "400", "g", MarketCategory.OTHERS),
                Ingredient("Chicken breast (shredded)", "250", "g", MarketCategory.KARNEHAN),
                Ingredient("Cabbage (Repolyo)", "0.5", "head sliced", MarketCategory.GULAYAN),
                Ingredient("Carrot", "1", "julienned", MarketCategory.GULAYAN),
                Ingredient("Snow peas (Chicharo)", "100", "g", MarketCategory.GULAYAN),
                Ingredient("Garlic and Onion", "1", "set minced", MarketCategory.GULAYAN),
                Ingredient("Chicken stock", "3", "cups", MarketCategory.PAMPALASA),
                Ingredient("Soy sauce & Oyster sauce", "3", "tbsp", MarketCategory.PAMPALASA),
                Ingredient("Calamansi", "5", "pcs", MarketCategory.GULAYAN)
            ),
            instructions = listOf(
                "Mag-gisa: Heat oil in a large wok and sauté garlic, onion, and shredded chicken until fragrant.",
                "Add carrots, snow peas, and cabbage. Sauté for 2 minutes then remove half for topping.",
                "Pour in chicken broth, soy sauce, and oyster sauce. Bring to a kulo.",
                "Submerge bihon noodles into boiling broth. Stir continuously as noodles absorb broth and soften.",
                "Top with reserved vegetables and serve hot with squeezed calamansi."
            ),
            imageUrl = "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?q=80&w=800",
            localTermExplanations = mapOf(
                "gisa" to defaultTerms["gisa"]!!,
                "kulo" to defaultTerms["kulo"]!!
            ),
            isAiGenerated = false
        ),
        Recipe(
            id = "recipe_kare_kare",
            title = "Rich Peanut Kare-Kare sa Bagoong",
            description = "Traditional Filipino festive stew in rich peanut sauce with tender oxtail, eggplant, and savory bagoong alamang.",
            category = RecipeCategory.ULAM,
            prepTimeMinutes = 25,
            cookTimeMinutes = 90,
            ingredients = listOf(
                Ingredient("Oxtail & Beef tripe", "1", "kg", MarketCategory.KARNEHAN),
                Ingredient("Peanut butter (Ground peanuts)", "1", "cup", MarketCategory.PAMPALASA),
                Ingredient("Annatto water (Achuete)", "0.5", "cup", MarketCategory.PAMPALASA),
                Ingredient("Eggplant (Talong)", "2", "pcs sliced", MarketCategory.GULAYAN),
                Ingredient("Banana blossom (Puso ng saging)", "1", "pc sliced", MarketCategory.GULAYAN),
                Ingredient("String beans (Sitaw)", "1", "bunch cut", MarketCategory.GULAYAN),
                Ingredient("Bok choy (Petchay)", "2", "bunches", MarketCategory.GULAYAN),
                Ingredient("Sauteed shrimp paste (Bagoong)", "0.5", "cup", MarketCategory.PAMPALASA)
            ),
            instructions = listOf(
                "In a large pressure cooker or pot, boil oxtail in water to palambutin for 1.5 hours until meat melts off the bone.",
                "In a deep wok, mag-gisa garlic and onions in achuete oil.",
                "Pour in beef stock and peanut butter, whisking until velvety smooth.",
                "Add cooked oxtail and simmer gently.",
                "Blanch or add vegetables (talong, sitaw, puso ng saging, petchay) until crisp-tender.",
                "Serve warmly accompanied by salty sautéed bagoong alamang."
            ),
            imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=800",
            localTermExplanations = mapOf(
                "palambutin" to defaultTerms["palambutin"]!!,
                "gisa" to defaultTerms["gisa"]!!
            ),
            isAiGenerated = false
        ),
        Recipe(
            id = "recipe_turon",
            title = "Crispy Caramel Turon with Langka",
            description = "Popular Filipino merienda staple: saba bananas rolled in spring roll wrappers, filled with jackfruit, fried to golden caramel crisp.",
            category = RecipeCategory.MERIENDA,
            prepTimeMinutes = 15,
            cookTimeMinutes = 15,
            ingredients = listOf(
                Ingredient("Saba bananas (split lengthwise)", "6", "pcs", MarketCategory.GULAYAN),
                Ingredient("Ripe Jackfruit (Langka strips)", "0.5", "cup", MarketCategory.OTHERS),
                Ingredient("Lumpia wrappers", "12", "pcs", MarketCategory.OTHERS),
                Ingredient("Brown sugar", "1", "cup", MarketCategory.PAMPALASA),
                Ingredient("Cooking oil for frying", "2", "cups", MarketCategory.PAMPALASA)
            ),
            instructions = listOf(
                "Roll split saba bananas generously in brown sugar.",
                "Place banana slice and langka strips on lumpia wrapper, fold sides, and wrap tightly, sealing edge with water.",
                "Heat oil in pan. Sprinkle brown sugar directly into hot oil to create floating caramel glaze.",
                "Fry turon rolls, turning frequently so caramel coats the wrapper evenly until golden and crunchy.",
                "Drain excess oil on rack and enjoy warm!"
            ),
            imageUrl = "https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?q=80&w=800",
            localTermExplanations = mapOf(
                "tantsa" to defaultTerms["tantsa"]!!
            ),
            isAiGenerated = false
        ),
        Recipe(
            id = "recipe_bibingka",
            title = "Special Galapong Bibingka with Salted Egg",
            description = "Traditional baked rice cake topped with salted duck egg, kesong puti, grated coconut, and brushed with melted butter.",
            category = RecipeCategory.KAKANIN,
            prepTimeMinutes = 20,
            cookTimeMinutes = 25,
            ingredients = listOf(
                Ingredient("Rice flour (Galapong)", "2", "cups", MarketCategory.OTHERS),
                Ingredient("Coconut milk (Gata)", "1.5", "cups", MarketCategory.OTHERS),
                Ingredient("Sugar", "0.75", "cup", MarketCategory.PAMPALASA),
                Ingredient("Eggs", "3", "pcs", MarketCategory.KARNEHAN),
                Ingredient("Salted duck egg (Itlog na maalat)", "1", "sliced", MarketCategory.KARNEHAN),
                Ingredient("Fresh grated coconut (Niyog)", "0.5", "cup", MarketCategory.OTHERS),
                Ingredient("Melted butter & Banana leaves", "1", "set", MarketCategory.OTHERS)
            ),
            instructions = listOf(
                "Whisk rice flour, coconut milk, sugar, melted butter, and eggs together until a smooth galapong batter forms.",
                "Line baking pan with wilted banana leaves. Pour batter inside.",
                "Top with sliced salted egg and kesong puti.",
                "Bake at 190°C (375°F) for 20-25 minutes until toothpick comes out clean.",
                "Brush top with butter, sprinkle freshly grated coconut, and serve warm."
            ),
            imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?q=80&w=800",
            localTermExplanations = mapOf(
                "piga" to defaultTerms["piga"]!!
            ),
            isAiGenerated = false
        ),
        Recipe(
            id = "recipe_lumpia_shanghai",
            title = "Pang-Negosyo Lumpia Shanghai",
            description = "Crispy golden pork spring rolls, perfect for party platters, catering, or frozen pack business.",
            category = RecipeCategory.PANG_NEGOSYO,
            prepTimeMinutes = 30,
            cookTimeMinutes = 15,
            ingredients = listOf(
                Ingredient("Ground pork", "1", "kg", MarketCategory.KARNEHAN),
                Ingredient("Carrot (finely minced)", "1", "pc", MarketCategory.GULAYAN),
                Ingredient("Onion & Garlic (minced)", "1", "set", MarketCategory.GULAYAN),
                Ingredient("Eggs", "2", "pcs", MarketCategory.KARNEHAN),
                Ingredient("Soy sauce & Seasoning", "2", "tbsp", MarketCategory.PAMPALASA),
                Ingredient("Lumpia wrappers", "50", "pcs", MarketCategory.OTHERS),
                Ingredient("Sweet chili sauce", "1", "bottle", MarketCategory.PAMPALASA)
            ),
            instructions = listOf(
                "Combine ground pork, minced carrots, garlic, onion, eggs, and seasonings in a bowl. Mix thoroughly.",
                "Scoop 1 tbsp of mixture onto wrapper, roll snugly, and seal edge with water.",
                "Deep fry in medium-hot oil until crispy brown and meat is fully cooked.",
                "Drain oil, slice into bite-sized pieces, and pair with sweet chili dipping sauce."
            ),
            imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=800",
            localTermExplanations = mapOf(
                "tantsa" to defaultTerms["tantsa"]!!
            ),
            isAiGenerated = false
        )
    )

    private val _recipes = MutableStateFlow<List<Recipe>>(classicRecipes)
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    fun getRecipes(): List<Recipe> {
        return _recipes.value
    }

    fun getRecipeById(id: String): Recipe? {
        return _recipes.value.find { it.id == id }
    }

    fun searchRecipes(query: String, category: RecipeCategory? = null): List<Recipe> {
        val trimmed = query.trim().lowercase()
        return _recipes.value.filter { recipe ->
            val matchesCategory = category == null || recipe.category == category
            val matchesQuery = trimmed.isEmpty() ||
                    recipe.title.lowercase().contains(trimmed) ||
                    recipe.description.lowercase().contains(trimmed) ||
                    recipe.ingredients.any { it.name.lowercase().contains(trimmed) }
            matchesCategory && matchesQuery
        }
    }

    fun filterByCategory(category: RecipeCategory): List<Recipe> {
        return _recipes.value.filter { it.category == category }
    }

    fun addCustomRecipe(recipe: Recipe) {
        val updatedList = _recipes.value.toMutableList()
        val index = updatedList.indexOfFirst { it.id == recipe.id }
        if (index >= 0) {
            updatedList[index] = recipe
        } else {
            updatedList.add(0, recipe)
        }
        _recipes.value = updatedList
    }

    fun addRecipes(newRecipes: List<Recipe>) {
        val updatedList = _recipes.value.toMutableList()
        newRecipes.forEach { newRecipe ->
            val index = updatedList.indexOfFirst { it.id == newRecipe.id }
            if (index >= 0) {
                updatedList[index] = newRecipe
            } else {
                updatedList.add(0, newRecipe)
            }
        }
        _recipes.value = updatedList
    }
}
