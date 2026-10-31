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
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

interface ChefAiRepository {
    suspend fun generateRecipeFromPantry(
        pantryItems: List<String>,
        imageBitmap: Bitmap? = null
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
        ).generativeModel(modelName = "gemini-3.5-flash")
    }

    override suspend fun generateRecipeFromPantry(
        pantryItems: List<String>,
        imageBitmap: Bitmap?
    ): Recipe = withContext(ioDispatcher) {
        val prompt = buildString {
            append("You are 'Nanay/Tito Chef', an expert Filipino cook. ")
            append("Given these pantry items: ${pantryItems.joinToString(", ")}. ")
            if (imageBitmap != null) append("Use the attached image as context. ")
            append("Create a realistic Filipino recipe using mostly the listed ingredients. ")
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
        val response = model.generateContent(request)
        val jsonText = cleanJsonResponse(response.text.orEmpty())
        require(jsonText.isNotBlank()) { "Firebase AI returned an empty recipe response." }
        parseRecipeJson(jsonText)
    }

    override suspend fun chatWithNanayTito(
        userMessage: String,
        chatHistory: List<ChatMessage>
    ): String = withContext(ioDispatcher) {
        val persona = """
            Ikaw si Nanay/Tito Chef, ang warm, maalaga, at maasahan na Filipino home chef.
            Magbigay ng maiiwasan, praktikal, at masaya na Taglish na tugon.
        """.trimIndent()

        val contextBuilder = StringBuilder("$persona\n\nRecent chat history:\n")
        chatHistory.takeLast(6).forEach { msg ->
            val senderLabel = if (msg.sender == ChatSender.USER) "User" else "Nanay/Tito Chef"
            contextBuilder.append("$senderLabel: ${msg.content}\n")
        }
        contextBuilder.append("User: $userMessage\nNanay/Tito Chef:")
        val response = model.generateContent(contextBuilder.toString())
        response.text?.trim()?.takeIf { it.isNotBlank() }
            ?: error("Firebase AI returned an empty chat response.")
    }

    private fun cleanJsonResponse(raw: String): String {
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun parseRecipeJson(jsonStr: String): Recipe {
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
                category = category,
                prepTimeMinutes = prepTime,
                cookTimeMinutes = cookTime,
                ingredients = ingredients,
                instructions = instructions,
                imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=800",
                localTermExplanations = terms.ifEmpty { mapOf("gisa" to "Sauté garlic and onions until fragrant", "tantsa" to "Adjust seasoning to taste") },
                isAiGenerated = true
            )
        } catch (e: Exception) {
            throw IllegalStateException("Firebase AI returned an invalid recipe response.", e)
        }
    }
}
