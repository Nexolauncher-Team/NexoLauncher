package com.nexo.launcher.feature.renderer

import android.content.Context
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.setting.AllSettings
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

/**
 * Handles version tracking and update checks for MobileGlues.
 */
object MobileGluesUpdateManager {
    private const val TAG = "MGUpdateManager"
    private const val GITHUB_API_RELEASES = "https://api.github.com/repos/MobileGL-Dev/MobileGlues-release/releases"
    private val client = OkHttpClient()

    /**
     * Information about a MobileGlues release.
     */
    data class ReleaseInfo(
        val version: String,
        val isBeta: Boolean,
        val description: String,
        val downloadUrl: String? = null
    )

    /**
     * Checks for the latest available version based on the user's selected channel.
     */
    fun checkForUpdates(context: Context, channel: String = AllSettings.mobileGluesChannel.getValue(), onResult: (ReleaseInfo?) -> Unit) {
        val request = Request.Builder().url(GITHUB_API_RELEASES).build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use

                    val jsonArray = JSONArray(response.body?.string() ?: "[]")
                    val latest = findLatestForChannel(jsonArray, channel)
                    
                    onResult(latest)
                }
            } catch (e: Exception) {
                Logging.e(TAG, "Update check failed", e)
                onResult(null)
            }
        }.start()
    }

    private fun findLatestForChannel(releases: JSONArray, channel: String): ReleaseInfo? {
        for (i in 0 until releases.length()) {
            val release = releases.getJSONObject(i)
            val tag = release.getString("tag_name")
            val isPrerelease = release.getBoolean("prerelease")
            val body = release.optString("body", "")

            if (channel == "beta" || !isPrerelease) {
                // Find APK asset
                val assets = release.getJSONArray("assets")
                var downloadUrl: String? = null
                for (j in 0 until assets.length()) {
                    val asset = assets.getJSONObject(j)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk")) {
                        downloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }
                return ReleaseInfo(tag, isPrerelease, body, downloadUrl)
            }
        }
        return null
    }

    /**
     * Returns the currently installed version tag stored in settings.
     */
    fun getInstalledVersion(): String {
        // We'll store this in a new setting soon
        return "v0.0.0" 
    }
}
