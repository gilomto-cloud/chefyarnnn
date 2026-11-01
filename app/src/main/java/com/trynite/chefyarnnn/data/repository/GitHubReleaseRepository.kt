package com.trynite.chefyarnnn.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit

data class GitHubRelease(
    val tagName: String,
    val versionName: String,
    val apkDownloadUrl: String,
)

class GitHubReleaseRepository {
    private val httpClient = OkHttpClient.Builder()
        .callTimeout(3, TimeUnit.MINUTES)
        .build()

    suspend fun findNewerRelease(currentVersionName: String): GitHubRelease? =
        withContext(Dispatchers.IO) {
            val currentVersion = SemanticVersion.parse(currentVersionName)
                ?: throw IOException("The installed app version is not a valid semantic version.")
            val request = Request.Builder()
                .url("$API_URL/repos/$REPOSITORY/releases/latest")
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "ChefYarnnn-Android")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("GitHub release check failed with HTTP ${response.code}.")
                }

                val releaseJson = JSONObject(
                    response.body.string(),
                )
                val tagName = releaseJson.getString("tag_name")
                val releaseVersion = SemanticVersion.parse(tagName.removePrefix("v"))
                    ?: return@withContext null
                if (releaseVersion <= currentVersion) return@withContext null

                val apkUrl = releaseJson.getJSONArray("assets")
                    .let { assets ->
                        (0 until assets.length()).asSequence()
                            .map { assets.getJSONObject(it) }
                            .firstOrNull {
                                it.optString("name").endsWith(".apk", ignoreCase = true)
                            }
                            ?.optString("browser_download_url")
                    }
                    ?.takeIf(::isGitHubDownloadUrl)
                    ?: throw IOException("The latest GitHub release does not contain a downloadable APK.")

                GitHubRelease(
                    tagName = tagName,
                    versionName = releaseVersion.toString(),
                    apkDownloadUrl = apkUrl,
                )
            }
        }

    suspend fun downloadApk(release: GitHubRelease, cacheDirectory: File): File =
        withContext(Dispatchers.IO) {
            if (!isGitHubDownloadUrl(release.apkDownloadUrl)) {
                throw IOException("The release APK URL is not a trusted GitHub URL.")
            }
            val request = Request.Builder()
                .url(release.apkDownloadUrl)
                .header("User-Agent", "ChefYarnnn-Android")
                .build()
            val apkFile = File(cacheDirectory, UPDATE_FILE_NAME)

            try {
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("APK download failed with HTTP ${response.code}.")
                    }
                    val body = response.body
                    FileOutputStream(apkFile).use { output ->
                        body.byteStream().use { input -> input.copyTo(output) }
                    }
                }
                if (apkFile.length() == 0L) {
                    throw IOException("The downloaded APK is empty.")
                }
                apkFile
            } catch (exception: Exception) {
                apkFile.delete()
                throw exception
            }
        }

    private fun isGitHubDownloadUrl(value: String): Boolean {
        val uri = runCatching { URI(value) }.getOrNull() ?: return false
        return (uri.scheme == "https") && (uri.host == "github.com")
    }

    private data class SemanticVersion(
        val major: Int,
        val minor: Int,
        val patch: Int
    ) : Comparable<SemanticVersion> {
        override fun compareTo(other: SemanticVersion): Int =
            compareValuesBy(this, other, SemanticVersion::major, SemanticVersion::minor, SemanticVersion::patch)

        override fun toString(): String = "$major.$minor.$patch"

        companion object {
            fun parse(value: String): SemanticVersion? {
                val parts = value.split('.')
                if (parts.size != 3) return null
                val numbers = parts.map { it.toIntOrNull() ?: return null }
                return SemanticVersion(numbers[0], numbers[1], numbers[2])
            }
        }
    }

    private companion object {
        const val API_URL = "https://api.github.com"
        const val REPOSITORY = "gilomto-cloud/chefyarnnn"
        const val UPDATE_FILE_NAME = "chefyarnnn-update.apk"
    }
}
