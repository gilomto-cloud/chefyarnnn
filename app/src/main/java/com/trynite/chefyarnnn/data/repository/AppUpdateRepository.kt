package com.trynite.chefyarnnn.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale

data class DownloadedAppUpdate(
    val versionName: String,
    val apkFile: File,
)

class AppUpdateRepository(
    private val filesDirectory: File,
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    suspend fun downloadLatestUpdate(
        installedVersion: String,
        onProgress: (versionName: String, progress: Int?) -> Unit
    ): DownloadedAppUpdate? = withContext(Dispatchers.IO) {
        val releaseRequest = Request.Builder()
            .url(LATEST_RELEASE_URL)
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .build()

        val releaseJson = httpClient.newCall(releaseRequest).execute().use { response ->
            if (response.code == 404) return@withContext null
            if (!response.isSuccessful) {
                throw IllegalStateException("GitHub release check failed (HTTP ${response.code}).")
            }
            response.body.string()
        }

        val release = JSONObject(releaseJson)
        val tagName = release.optString("tag_name")
        val versionName = RELEASE_TAG_REGEX.matchEntire(tagName)?.groupValues?.get(1)
            ?: throw IllegalStateException("The latest release tag must use the vMAJOR.MINOR.PATCH format.")
        if (!isNewerAppVersion(versionName, installedVersion)) return@withContext null

        val assets = release.optJSONArray("assets")
            ?: throw IllegalStateException("The latest GitHub release has no APK assets.")
        var apkAsset: JSONObject? = null
        for (index in 0 until assets.length()) {
            val asset = assets.optJSONObject(index) ?: continue
            if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                apkAsset = asset
                break
            }
        }
        val asset = apkAsset
            ?: throw IllegalStateException("The latest GitHub release does not contain an APK.")
        val downloadUrl = asset.optString("browser_download_url")
        if (downloadUrl.isBlank() || !downloadUrl.startsWith("https://github.com/")) {
            throw IllegalStateException("The APK download URL is invalid.")
        }
        val expectedDigest = asset.optString("digest")
            .takeIf { it.startsWith("sha256:", ignoreCase = true) }
            ?.substringAfter(':')
            ?.lowercase(Locale.ROOT)

        val updateDirectory = File(filesDirectory, "updates")
        if (!updateDirectory.exists() && !updateDirectory.mkdirs()) {
            throw IllegalStateException("Unable to create the app update directory.")
        }
        val apkFile = File(updateDirectory, "update-$versionName.apk")
        val temporaryFile = File(updateDirectory, "update-$versionName.apk.part")
        try {
            if (
                apkFile.isFile &&
                apkFile.length() > 0L &&
                (expectedDigest == null || apkFile.sha256() == expectedDigest)
            ) {
                return@withContext DownloadedAppUpdate(versionName, apkFile)
            }
            apkFile.delete()

            val apkRequest = Request.Builder()
                .url(downloadUrl)
                .header("Accept", "application/octet-stream")
                .build()
            httpClient.newCall(apkRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("APK download failed (HTTP ${response.code}).")
                }
                val finalUrl = response.request.url
                if (finalUrl.scheme != "https" ||
                    (finalUrl.host != "github.com" && !finalUrl.host.endsWith(".githubusercontent.com"))
                ) {
                    throw IllegalStateException("The APK download redirected to an untrusted host.")
                }

                val body = response.body
                val contentLength = body.contentLength()
                if (contentLength > MAX_APK_SIZE_BYTES) {
                    throw IllegalStateException("The APK exceeds the allowed download size.")
                }
                val digest = MessageDigest.getInstance("SHA-256")
                var downloadedBytes = 0L
                var lastProgress: Int? = null
                var reportedUnknownProgress = false
                onProgress(versionName, 0)
                body.byteStream().use { input ->
                    FileOutputStream(temporaryFile).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val bytesRead = input.read(buffer)
                            if (bytesRead < 0) break
                            downloadedBytes += bytesRead
                            if (downloadedBytes > MAX_APK_SIZE_BYTES) {
                                throw IllegalStateException("The APK exceeds the allowed download size.")
                            }
                            digest.update(buffer, 0, bytesRead)
                            output.write(buffer, 0, bytesRead)
                            val progress = if (contentLength > 0L) {
                                ((downloadedBytes * 100) / contentLength).toInt().coerceIn(0, 100)
                            } else {
                                null
                            }
                            if (progress != null && progress != lastProgress) {
                                onProgress(versionName, progress)
                                lastProgress = progress
                            } else if (progress == null && !reportedUnknownProgress) {
                                onProgress(versionName, null)
                                reportedUnknownProgress = true
                            }
                        }
                    }
                }

                if (downloadedBytes == 0L) {
                    throw IllegalStateException("GitHub returned an empty APK download.")
                }
                if (contentLength > 0L && downloadedBytes != contentLength) {
                    throw IllegalStateException("The APK download was incomplete.")
                }
                val actualDigest = digest.digest().joinToString("") {
                    "%02x".format(Locale.ROOT, it.toInt() and 0xff)
                }
                if (expectedDigest != null && actualDigest != expectedDigest) {
                    throw IllegalStateException("The downloaded APK failed its SHA-256 integrity check.")
                }
            }

            if (!temporaryFile.renameTo(apkFile)) {
                throw IllegalStateException("Unable to prepare the downloaded APK for installation.")
            }
            DownloadedAppUpdate(versionName, apkFile)
        } finally {
            temporaryFile.delete()
        }
    }

    private fun File.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead < 0) break
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") {
            "%02x".format(Locale.ROOT, it.toInt() and 0xff)
        }
    }

    companion object {
        private const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/gilomto-cloud/chefyarnnn/releases/latest"
        private const val MAX_APK_SIZE_BYTES = 250L * 1024L * 1024L
        private val RELEASE_TAG_REGEX = Regex("""^v(\d+\.\d+\.\d+)$""")
    }
}

internal fun isNewerAppVersion(latestVersion: String, installedVersion: String): Boolean {
    val latest = parseAppVersion(latestVersion) ?: return false
    val installed = parseAppVersion(installedVersion) ?: return false
    return when {
        latest.major != installed.major -> latest.major > installed.major
        latest.minor != installed.minor -> latest.minor > installed.minor
        else -> latest.patch > installed.patch
    }
}

private data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
)

private fun parseAppVersion(version: String): AppVersion? {
    val match = APP_VERSION_REGEX.matchEntire(version) ?: return null
    return try {
        AppVersion(
            major = match.groupValues[1].toInt(),
            minor = match.groupValues[2].toInt(),
            patch = match.groupValues[3].ifEmpty { "0" }.toInt()
        )
    } catch (_: NumberFormatException) {
        null
    }
}

private val APP_VERSION_REGEX = Regex("""^v?(\d+)\.(\d+)(?:\.(\d+))?$""")
