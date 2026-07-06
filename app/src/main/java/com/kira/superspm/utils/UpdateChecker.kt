package com.kira.superspm.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateChecker {
    private const val GITHUB_RELEASE_URL = "https://api.github.com/repos/tig-kira/superspm/releases/latest"
    private const val GITHUB_RELEASE_PAGE_URL = "https://github.com/tig-kira/superspm/releases"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    data class UpdateResult(
        val hasUpdate: Boolean,
        val latestVersion: String,
        val currentVersion: String,
        val releaseUrl: String,
        val apkDownloadUrl: String,
        val releaseNotes: String
    )

    suspend fun checkForUpdates(currentVersion: String): UpdateResult {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(GITHUB_RELEASE_URL)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext UpdateResult(
                            hasUpdate = false,
                            latestVersion = currentVersion,
                            currentVersion = currentVersion,
                            releaseUrl = GITHUB_RELEASE_PAGE_URL,
                            apkDownloadUrl = "",
                            releaseNotes = ""
                        )
                    }

                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val latestVersion = json.optString("tag_name", currentVersion)
                        .removePrefix("v")
                    val releaseNotes = json.optString("body", "")

                    val assets = json.optJSONArray("assets")
                    var apkUrl = ""
                    if (assets != null && assets.length() > 0) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk")) {
                                apkUrl = asset.optString("browser_download_url", "")
                                break
                            }
                        }
                    }
                    if (apkUrl.isEmpty()) {
                        apkUrl = GITHUB_RELEASE_PAGE_URL
                    }

                    val hasUpdate = compareVersions(latestVersion, currentVersion) > 0

                    UpdateResult(
                        hasUpdate = hasUpdate,
                        latestVersion = latestVersion,
                        currentVersion = currentVersion,
                        releaseUrl = GITHUB_RELEASE_PAGE_URL,
                        apkDownloadUrl = apkUrl,
                        releaseNotes = releaseNotes
                    )
                }
            } catch (e: Exception) {
                UpdateResult(
                    hasUpdate = false,
                    latestVersion = currentVersion,
                    currentVersion = currentVersion,
                    releaseUrl = GITHUB_RELEASE_PAGE_URL,
                    apkDownloadUrl = "",
                    releaseNotes = ""
                )
            }
        }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLength) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 > p2) return 1
            if (p1 < p2) return -1
        }
        return 0
    }
}