package com.trynite.chefyarnnn.data.repository

import android.graphics.Bitmap
import com.trynite.chefyarnnn.model.ChatMessage
import com.trynite.chefyarnnn.model.ChatSender
import com.trynite.chefyarnnn.model.Ingredient
import com.trynite.chefyarnnn.model.MarketCategory
import com.trynite.chefyarnnn.model.Recipe
import com.trynite.chefyarnnn.model.RecipeCategory
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

interface ChefAiRepository {
    suspend fun generateRecipeFromPantry(
        pantryItems: List<String>,
        imageBitmap: Bitmap? = null,
        preferredCategory: RecipeCategory? = null
    ): Recipe

    suspend fun chatWithNanayTito(
        userMessage: String,
        chatHistory: List<ChatMessage> = emptyList()
    ): String
}

class FirebaseAiRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ChefAiRepository {

    private val model by lazy {
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(modelName = FLASH_MODEL)
    }

    private val chatModel by lazy {
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = FLASH_MODEL,
            generationConfig = generationConfig {
                maxOutputTokens = CHAT_MAX_OUTPUT_TOKENS
            },
            systemInstruction = content {
                text(
                    "You are Chef Cindy, a practical Filipino cooking assistant. " +
                        "Reply warmly and concisely in Taglish."
                )
            }
        )
    }

    private val lightweightChatModel by lazy {
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = FLASH_LITE_MODEL,
            generationConfig = generationConfig {
                maxOutputTokens = LIGHTWEIGHT_MAX_OUTPUT_TOKENS
            },
            systemInstruction = content {
                text(
                    "You handle short menu-category classification and simple search-style " +
                        "queries for Filipino cooking. Be concise. Do not claim live prices, " +
                        "inventory, or search results you cannot verify."
                )
            }
        )
    }

    private fun requireGoogleAuthenticated() {
        check(FirebaseAuth.getInstance().currentUser?.isAnonymous == false) {
            "Sign in with Google before using Firebase AI."
        }
    }

    override suspend fun generateRecipeFromPantry(
        pantryItems: List<String>,
        imageBitmap: Bitmap?,
        preferredCategory: RecipeCategory?
    ): Recipe = withContext(ioDispatcher) {
        val prompt = buildString {
            append("You are Chef Cindy, a warm and expert Filipino home cook. ")
            append("Given these pantry items: ${pantryItems.joinToString(", ")}. ")
            if (imageBitmap != null) append("Use the attached image as context. ")
            append("Create a realistic Filipino recipe using mostly the listed ingredients. ")
            preferredCategory?.let {
                append("The user specifically wants ${it.displayName}; choose a dish and categorize it as ${it.name}. ")
            }
            append("Return only valid JSON, with no markdown or comments, using this structure: ")
            append("{\"title\":\"\",\"description\":\"\",\"category\":\"ULAM\",")
            append("\"prepTimeMinutes\":15,\"cookTimeMinutes\":25,\"ingredients\":[")
            append("{\"name\":\"\",\"amount\":\"1\",\"unit\":\"pc\",\"marketCategory\":\"GULAYAN\"}],")
            append("\"instructions\":[\"\"],\"localTermExplanations\":{\"gisa\":\"\"}}. ")
            append("category must be ULAM, MERIENDA, KAKANIN, or PANG_NEGOSYO. ")
            append("marketCategory must be GULAYAN, KARNEHAN, PAMPALASA, or OTHERS.")
        }

        val request = content {
            imageBitmap?.let { image(it) }
            text(prompt)
        }
        requireGoogleAuthenticated()
        val response = model.generateContent(request)
        val jsonText = cleanJsonResponse(response.text.orEmpty())
        require(jsonText.isNotBlank()) { "Firebase AI returned an empty recipe response." }
        parseRecipeJson(jsonText, preferredCategory)
    }

    override suspend fun chatWithNanayTito(
        userMessage: String,
        chatHistory: List<ChatMessage>
    ): String = withContext(ioDispatcher) {
        val history = chatHistory.toMutableList().apply {
            if (lastOrNull()?.let {
                    it.sender == ChatSender.USER && it.content == userMessage
                } == true
            ) {
                removeAt(lastIndex)
            }
        }
        val prompt = buildString {
            history.takeLast(CHAT_HISTORY_LIMIT).forEach { message ->
                val sender = if (message.sender == ChatSender.USER) "User" else "Chef Cindy"
                append("$sender: ${message.content}\n")
            }
            append("User: $userMessage\nChef Cindy:")
        }
        requireGoogleAuthenticated()
        val selectedModel = if (isLightweightChatTask(userMessage)) {
            lightweightChatModel
        } else {
            chatModel
        }
        val response = selectedModel.generateContent(prompt)
        response.text?.trim()?.takeIf { it.isNotBlank() }
            ?: error("Firebase AI returned an empty chat response.")
    }

    private fun isLightweightChatTask(userMessage: String): Boolean {
        val normalized = userMessage.trim().lowercase()
        val menuClassificationPhrases = listOf(
            "classify this menu",
            "classify this dish",
            "categorize this dish",
            "what menu category",
            "what category is this",
            "ulam or merienda",
            "ulam, merienda",
            "is this kakanin",
            "is this pang negosyo"
        )
        val searchPrefixes = listOf(
            "search ",
            "search for ",
            "find ",
            "look up ",
            "show me "
        )
        return menuClassificationPhrases.any(normalized::contains) ||
            searchPrefixes.any(normalized::startsWith)
    }

    private fun cleanJsonResponse(raw: String): String {
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun parseRecipeJson(jsonStr: String, preferredCategory: RecipeCategory?): Recipe {
        return try {
            val obj = JSONObject(jsonStr)
            val title = obj.optString("title").trim()
            require(title.isNotBlank()) { "Firebase AI recipe response is missing a title." }
            val description = obj.optString("description").trim()
            require(description.isNotBlank()) { "Firebase AI recipe response is missing a description." }
            val category = RecipeCategory.fromString(obj.optString("category", "ULAM"))
            val prepTime = obj.optInt("prepTimeMinutes", 15)
            val cookTime = obj.optInt("cookTimeMinutes", 25)
            require(prepTime >= 0 && cookTime >= 0) { "Firebase AI returned invalid cooking times." }

            val ingredients = mutableListOf<Ingredient>()
            val ingArray = obj.optJSONArray("ingredients")
            if (ingArray != null) {
                for (i in 0 until ingArray.length()) {
                    val ingObj = ingArray.getJSONObject(i)
                    ingredients.add(
                        Ingredient(
                            name = ingObj.optString("name", "Ingredient"),
                            amount = ingObj.optString("amount", "1"),
                            unit = ingObj.optString("unit", "pc"),
                            marketCategory = MarketCategory.fromString(ingObj.optString("marketCategory", "GULAYAN"))
                        )
                    )
                }
            }
            require(ingredients.isNotEmpty()) { "Firebase AI recipe response has no ingredients." }

            val instructions = mutableListOf<String>()
            val instArray = obj.optJSONArray("instructions")
            if (instArray != null) {
                for (i in 0 until instArray.length()) {
                    instructions.add(instArray.getString(i))
                }
            }
            require(instructions.isNotEmpty()) { "Firebase AI recipe response has no instructions." }

            val terms = mutableMapOf<String, String>()
            val termsObj = obj.optJSONObject("localTermExplanations")
            if (termsObj != null) {
                val keys = termsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    terms[key] = termsObj.getString(key)
                }
            }

            Recipe(
                id = "ai_rec_${UUID.randomUUID().toString().take(8)}",
                title = title,
                description = description,
                category = preferredCategory ?: category,
                prepTimeMinutes = prepTime,
                cookTimeMinutes = cookTime,
                ingredients = ingredients,
                instructions = instructions,
                localTermExplanations = terms.ifEmpty { mapOf("gisa" to "Sauté garlic and onions until fragrant", "tantsa" to "Adjust seasoning to taste") },
                isAiGenerated = true
            )
        } catch (e: Exception) {
            throw IllegalStateException("Firebase AI returned an invalid recipe response.", e)
        }
    }

    private companion object {
        const val FLASH_MODEL = "gemini-3.5-flash"
        const val FLASH_LITE_MODEL = "gemini-3.5-flash-lite"
        const val CHAT_HISTORY_LIMIT = 4
        const val CHAT_MAX_OUTPUT_TOKENS = 256
        const val LIGHTWEIGHT_MAX_OUTPUT_TOKENS = 128
    }
}
