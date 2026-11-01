package com.trynite.chefyarnnn

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import com.trynite.chefyarnnn.data.repository.FirebaseAiRepository
import com.trynite.chefyarnnn.data.repository.PalengkeRepository
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.navigation.AppScaffold
import com.trynite.chefyarnnn.ui.screen.AppUpdateDialog
import com.trynite.chefyarnnn.ui.theme.ChefyarnnnTheme
import com.trynite.chefyarnnn.ui.viewmodel.AppUpdateState
import com.trynite.chefyarnnn.ui.viewmodel.AppUpdateViewModel

class MainActivity : ComponentActivity() {
    private val appUpdateViewModel: AppUpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val recipeRepository = RecipeRepository()
        val geminiRepository = FirebaseAiRepository()
        val palengkeRepository = PalengkeRepository()

        setContent {
            val updateState by appUpdateViewModel.state.collectAsState()
            ChefyarnnnTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppScaffold(
                        recipeRepository = recipeRepository,
                        geminiRepository = geminiRepository,
                        palengkeRepository = palengkeRepository
                    )
                    AppUpdateDialog(
                        state = updateState,
                        onInstall = {
                            (updateState as? AppUpdateState.Ready)?.let { installUpdate(it.update.apkFile) }
                        },
                        onRetry = appUpdateViewModel::checkForUpdate,
                        onDismiss = appUpdateViewModel::dismiss
                    )
                }
            }
        }
    }

    private fun installUpdate(apkFile: java.io.File) {
        if (!packageManager.canRequestPackageInstalls()) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    "package:$packageName".toUri()
                )
            )
            return
        }

        try {
            val apkUri = FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                apkFile
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(installIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Walang installer na makapagbubukas ng update.", Toast.LENGTH_LONG).show()
        } catch (_: SecurityException) {
            Toast.makeText(this, "Hindi pinayagan ng Android ang pag-install ng update.", Toast.LENGTH_LONG).show()
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
