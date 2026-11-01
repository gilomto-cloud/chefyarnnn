package com.trynite.chefyarnnn

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
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
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.trynite.chefyarnnn.data.repository.GitHubRelease
import com.trynite.chefyarnnn.data.repository.FirebaseAiRepository
import com.trynite.chefyarnnn.data.repository.GitHubReleaseRepository
import com.trynite.chefyarnnn.data.repository.PalengkeRepository
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.navigation.AppScaffold
import com.trynite.chefyarnnn.ui.screen.GoogleSignInScreen
import com.trynite.chefyarnnn.ui.theme.ChefyarnnnTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager by lazy { CredentialManager.create(this) }
    private var firebaseUser by mutableStateOf(firebaseAuth.currentUser)
    private var isSigningIn by mutableStateOf(false)
    private var signInError by mutableStateOf<String?>(null)
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        firebaseUser = auth.currentUser
    }

    private val releaseRepository = GitHubReleaseRepository()
    private var availableRelease by mutableStateOf<GitHubRelease?>(null)
    private var isDownloadingUpdate by mutableStateOf(value = false)
    private var updateError by mutableStateOf<String?>(null)
    private var pendingInstallAfterSettings = false

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        firebaseAuth.removeAuthStateListener(authStateListener)
        super.onStop()
    }

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
                if (firebaseUser?.isAnonymous == false) {
                    AppScaffold(
                        recipeRepository = recipeRepository,
                        geminiRepository = geminiRepository,
                        palengkeRepository = palengkeRepository,
                        accountName = firebaseUser?.displayName.orEmpty(),
                        accountEmail = firebaseUser?.email.orEmpty(),
                        onSignOut = ::signOut
                    )
                } else {
                    GoogleSignInScreen(
                        isSigningIn = isSigningIn,
                        errorMessage = signInError,
                        onSignIn = ::signInWithGoogle
                    )
                }
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

    private fun signInWithGoogle() {
        if (isSigningIn) return

        lifecycleScope.launch {
            isSigningIn = true
            signInError = null
            try {
                val webClientId = getString(R.string.default_web_client_id)
                check(webClientId.isNotBlank()) {
                    "Google sign-in isn't configured. Add a Web OAuth client in Firebase " +
                        "and download the updated google-services.json."
                }
                val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(this@MainActivity, request)
                val credential = result.credential
                check(
                    credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    "Google did not return a valid sign-in credential."
                }

                val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleToken.idToken, null)
                withContext(Dispatchers.IO) {
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser?.isAnonymous == true) {
                        try {
                            Tasks.await(currentUser.linkWithCredential(firebaseCredential))
                        } catch (_: FirebaseAuthUserCollisionException) {
                            Tasks.await(firebaseAuth.signInWithCredential(firebaseCredential))
                        }
                    } else {
                        Tasks.await(firebaseAuth.signInWithCredential(firebaseCredential))
                    }
                }
                firebaseUser = firebaseAuth.currentUser
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: GetCredentialCancellationException) {
                val detail = exception.message.orEmpty()
                signInError = if (detail.contains("reauth", ignoreCase = true)) {
                    "Google couldn't reauthenticate this account. In Firebase Console, enable " +
                        "Google sign-in and add this app's SHA-1/SHA-256 signing fingerprints, " +
                        "then download the updated google-services.json."
                } else {
                    "Google sign-in was cancelled. Tap Continue with Google to try again."
                }
                Log.w(TAG, "Google Credential Manager sign-in was cancelled: $detail", exception)
            } catch (exception: GetCredentialException) {
                signInError = exception.message?.let { "Google sign-in failed: $it" }
                    ?: "Google sign-in failed. Check Firebase's Google provider and app configuration."
                Log.w(TAG, "Google Credential Manager sign-in failed.", exception)
            } catch (exception: Exception) {
                signInError = exception.localizedMessage
                    ?: "Google sign-in failed. Check your connection and try again."
                Log.e(TAG, "Firebase Google sign-in failed.", exception)
            } finally {
                isSigningIn = false
            }
        }
    }

    private fun signOut() {
        firebaseAuth.signOut()
        firebaseUser = null
        lifecycleScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.w(TAG, "Unable to clear Credential Manager state after sign-out.", exception)
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
            palengkeRepository = palengkeRepository,
            accountName = "",
            accountEmail = "",
            onSignOut = {}
        )
    }
}
