package com.trynite.chefyarnnn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.trynite.chefyarnnn.data.repository.FirebaseAiRepository
import com.trynite.chefyarnnn.data.repository.PalengkeRepository
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.navigation.AppScaffold
import com.trynite.chefyarnnn.ui.theme.ChefyarnnnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val recipeRepository = RecipeRepository()
        val geminiRepository = FirebaseAiRepository()
        val palengkeRepository = PalengkeRepository()

        setContent {
            ChefyarnnnTheme {
                AppScaffold(
                    recipeRepository = recipeRepository,
                    geminiRepository = geminiRepository,
                    palengkeRepository = palengkeRepository
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    val recipeRepository = remember { RecipeRepository() }
    val geminiRepository = remember { FirebaseAiRepository() }
    val palengkeRepository = remember { PalengkeRepository() }

    ChefyarnnnTheme {
        AppScaffold(
            recipeRepository = recipeRepository,
            geminiRepository = geminiRepository,
            palengkeRepository = palengkeRepository
        )
    }
}
