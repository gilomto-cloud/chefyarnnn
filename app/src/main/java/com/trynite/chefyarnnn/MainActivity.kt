package com.trynite.chefyarnnn

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.trynite.chefyarnnn.data.repository.GitHubRelease
import com.trynite.chefyarnnn.data.repository.FirebaseAiRepository
import com.trynite.chefyarnnn.data.repository.GitHubReleaseRepository
import com.trynite.chefyarnnn.data.repository.PalengkeRepository
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.navigation.AppScaffold
import com.trynite.chefyarnnn.ui.theme.ChefyarnnnTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val releaseRepository = GitHubReleaseRepository()
    private var availableRelease by mutableStateOf<GitHubRelease?>(null)
    private var isDownloadingUpdate by mutableStateOf(value = false)
    private var updateError by mutableStateOf<String?>(null)
    private var pendingInstallAfterSettings = false

    override fun onResume() {
        super.onResume()
        if (pendingInstallAfterSettings) {
            pendingInstallAfterSettings = false
            if (packageManager.canRequestPackageInstalls()) {
                availableRelease?.let(::downloadAndInstall)
            } else {
                updateError = "Allow ChefYarnnn to install apps in Settings, then try again."
            }
            return
        }
        checkForUpdate()
    }

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
                    palengkeRepository = palengkeRepository,
                )
            }
            availableRelease?.let { release ->
                UpdateDialog(
                    release = release,
                    isDownloading = isDownloadingUpdate,
                    errorMessage = updateError,
                    onDismiss = { if (!isDownloadingUpdate) availableRelease = null },
                ) {
                    downloadAndInstall(release)
                }
            }
        }
    }

    private fun checkForUpdate() {
        if (BuildConfig.DEBUG) return

        val preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val lastCheck = preferences.getLong(LAST_CHECK_KEY, 0L)
        if ((now - lastCheck) < CHECK_INTERVAL_MILLIS) return
        preferences.edit { putLong(LAST_CHECK_KEY, now) }

        lifecycleScope.launch {
            try {
                availableRelease = withContext(Dispatchers.IO) {
                    releaseRepository.findNewerRelease(BuildConfig.VERSION_NAME)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                preferences.edit { remove(LAST_CHECK_KEY) }
                Log.w(TAG, "GitHub release check failed.", exception)
            }
        }
    }

    private fun downloadAndInstall(release: GitHubRelease) {
        updateError = null
        if (!packageManager.canRequestPackageInstalls()) {
            pendingInstallAfterSettings = true
            try {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        "package:$packageName".toUri()
                    )
                )
            } catch (exception: Exception) {
                pendingInstallAfterSettings = false
                updateError = "Could not open the app installation permission settings."
                Log.e(TAG, "Unable to open app installation settings.", exception)
            }
            return
        }

        lifecycleScope.launch {
            isDownloadingUpdate = true
            try {
                val apkFile = releaseRepository.downloadApk(release, cacheDir)
                val apkUri = FileProvider.getUriForFile(
                    this@MainActivity,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    apkFile
                )
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, APK_MIME_TYPE)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                availableRelease = null
                startActivity(installIntent)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                updateError = "Could not download or open the update. Please try again."
                Log.e(TAG, "GitHub release installation failed.", exception)
            } finally {
                isDownloadingUpdate = false
            }
        }
    }

    private companion object {
        const val TAG = "MainActivity"
        const val PREFERENCES_NAME = "github_release_updates"
        const val LAST_CHECK_KEY = "last_check_time"
        const val CHECK_INTERVAL_MILLIS = 6 * 60 * 60 * 1000L
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}

@Composable
private fun UpdateDialog(
    release: GitHubRelease,
    isDownloading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update available") },
        text = {
            Column {
                Text("ChefYarnnn ${release.versionName} is ready to download.")
                if (isDownloading) {
                    Text("Downloading update…", modifier = Modifier.padding(top = 8.dp))
                }
                errorMessage?.let {
                    Text(it, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onUpdate, enabled = !isDownloading) {
                Text(if (isDownloading) "Downloading…" else "Download update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDownloading) {
                Text("Later")
            }
        }
    )
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
