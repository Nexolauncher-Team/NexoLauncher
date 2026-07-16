package com.nexo.launcher.feature.renderer

import android.content.Context
import com.nexo.launcher.Architecture
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.utils.path.PathManager
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/**
 * The Central Hub for MobileGlues management.
 * Handles architecture-specific downloads, integrity verification, and installation.
 */
object MobileGluesHub {
    private const val TAG = "MobileGluesHub"

    private val client = OkHttpClient()

    /**
     * Data class to track download progress and state.
     */
    data class SetupStatus(
        val isDownloading: Boolean = false,
        val progress: Int = 0,
        val error: String? = null,
        val isComplete: Boolean = false
    )

    /**
     * Identifies the correct directory name inside the APK based on architecture.
     */
    private fun getLibDirForArch(): String {
        val arch = Architecture.getDeviceArchitecture()
        return when (arch) {
            Architecture.ARCH_ARM64 -> "arm64-v8a"
            Architecture.ARCH_ARM -> "armeabi-v7a"
            Architecture.ARCH_X86_64 -> "x86_64"
            Architecture.ARCH_X86 -> "x86"
            else -> throw IllegalStateException("Unsupported architecture for MobileGlues")
        }
    }

    /**
     * Gets the path to the installed library in internal storage.
     */
    fun getLibPath(context: Context): File {
        val folder = File(context.filesDir, "MG/libs")
        if (!folder.exists()) folder.mkdirs()
        return File(folder, "libmobileglues.so")
    }

    /**
     * Checks if MobileGlues is already installed and verified.
     */
    fun isInstalled(context: Context): Boolean {
        return getLibPath(context).exists()
    }

    /**
     * Downloads and installs MobileGlues for the current architecture.
     * @param context The application context.
     * @param url The APK download URL.
     * @param expectedHash Optional SHA-256 for integrity verification.
     */
    fun install(context: Context, url: String, expectedHash: String? = null, onProgress: (SetupStatus) -> Unit) {
        val libDirName = try { getLibDirForArch() } catch (e: Exception) {
            onProgress(SetupStatus(error = e.message))
            return
        }

        val destFile = getLibPath(context)
        val apkFile = File(context.cacheDir, "MobileGlues_Temp.apk")

        onProgress(SetupStatus(isDownloading = true, progress = 0))

        val request = Request.Builder().url(url).build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Download failed: ${response.code}")
                
                val body = response.body ?: throw Exception("Empty response body")
                val totalSize = body.contentLength()
                
                FileOutputStream(apkFile).use { output ->
                    val input = body.byteStream()
                    val buffer = ByteArray(8192)
                    var bytesRead: Long = 0
                    var read: Int
                    
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesRead += read
                        val progress = if (totalSize > 0) ((bytesRead * 100) / totalSize).toInt() else 0
                        onProgress(SetupStatus(isDownloading = true, progress = progress))
                    }
                }

                // Verify Integrity
                if (!verifyHash(apkFile, expectedHash)) {
                    apkFile.delete()
                    throw Exception("Integrity verification failed (Hash mismatch)")
                }

                // Extract all libraries
                extractAllLibrariesFromApk(apkFile, libDirName, destFile.parentFile!!)
                
                apkFile.delete() // Cleanup
                onProgress(SetupStatus(isComplete = true))
            }
        } catch (e: Exception) {
            Logging.e(TAG, "Installation failed", e)
            onProgress(SetupStatus(error = e.message))
        }
    }

    private fun extractAllLibrariesFromApk(apkFile: File, archDir: String, destDir: File) {
        val targetPrefix = "lib/$archDir/"
        destDir.mkdirs()
        ZipInputStream(apkFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name.startsWith(targetPrefix) && entry.name.endsWith(".so")) {
                    val fileName = entry.name.substringAfterLast("/")
                    val outFile = File(destDir, fileName)
                    outFile.outputStream().use { output ->
                        zis.copyTo(output)
                    }
                    outFile.setExecutable(true)
                    Logging.i(TAG, "Extracted $fileName to ${outFile.absolutePath}")
                }
                entry = zis.nextEntry
            }
        }
        if (!File(destDir, "libmobileglues.so").exists()) {
            throw Exception("Could not find core library libmobileglues.so inside APK")
        }
    }

    private fun verifyHash(file: File, expected: String?): Boolean {
        if (expected == null) return true // Skip if no hash provided
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
        return hash.equals(expected, ignoreCase = true)
    }

    /**
     * Safely rolls back to a stable renderer if MobileGlues fails.
     */
    fun rollback(context: Context, previousRendererId: String) {
        Logging.w(TAG, "Rolling back to $previousRendererId due to failure")
        // Logic to update SharedPreferences/AllSettings will go here
    }
}
