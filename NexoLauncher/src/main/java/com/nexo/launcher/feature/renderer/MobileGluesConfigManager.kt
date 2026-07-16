package com.nexo.launcher.feature.renderer

import android.content.Context
import com.nexo.launcher.feature.log.Logging
import org.json.JSONObject
import java.io.File

/**
 * Manages MobileGlues configuration in internal storage.
 * Provides Import/Export functionality for advanced users.
 */
object MobileGluesConfigManager {
    private const val CONFIG_FILE_NAME = "config.json"

    fun getConfigFolder(context: Context): File {
        val folder = File(context.filesDir, "MG")
        if (!folder.exists()) folder.mkdirs()
        return folder
    }

    fun getConfigFile(context: Context): File {
        return File(getConfigFolder(context), CONFIG_FILE_NAME)
    }

    fun loadConfig(context: Context): JSONObject {
        val file = getConfigFile(context)
        if (!file.exists()) {
            return createDefaultConfig(context)
        }
        return try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            Logging.e("MGConfig", "Failed to load config", e)
            createDefaultConfig(context)
        }
    }

    private fun createDefaultConfig(context: Context): JSONObject {
        val config = JSONObject().apply {
            put("enableANGLE", 0)
            put("enableNoError", 0)
            put("fsr1Setting", 0)
            put("enableExtGL43", 1)
            put("enableExtComputeShader", 1)
            put("ignoreShaderError", 1)
            put("shaderCacheSize", 256)
            put("useSustainPerformance", 0)
            put("dsaExtension", 0)
        }
        saveConfig(context, config)
        return config
    }

    fun saveConfig(context: Context, config: JSONObject) {
        try {
            getConfigFile(context).writeText(config.toString(2))
        } catch (e: Exception) {
            Logging.e("MGConfig", "Failed to save config", e)
        }
    }

    /**
     * Proactively updates configuration based on detected hardware.
     */
    fun optimizeForHardware(context: Context, vendor: String) {
        val config = loadConfig(context)
        if (vendor.contains("Adreno", true)) {
            // Adreno often needs ANGLE for better Vulkan/ES compatibility in translation
            config.put("enableANGLE", 1)
        } else if (vendor.contains("Mali", true)) {
            // Mali drivers benefit from compute shader extensions
            config.put("enableExtComputeShader", 1)
        }
        saveConfig(context, config)
    }

    /**
     * Exports the internal config to the specified file (usually in Downloads).
     */
    fun exportConfig(context: Context, destFile: File): Boolean {
        return try {
            val source = getConfigFile(context)
            if (source.exists()) {
                source.copyTo(destFile, overwrite = true)
                true
            } else false
        } catch (e: Exception) {
            Logging.e("MGConfig", "Export failed", e)
            false
        }
    }

    /**
     * Imports a config file into internal storage.
     */
    fun importConfig(context: Context, sourceFile: File): Boolean {
        return try {
            if (sourceFile.exists()) {
                val json = JSONObject(sourceFile.readText()) // Validation
                saveConfig(context, json)
                true
            } else false
        } catch (e: Exception) {
            Logging.e("MGConfig", "Import failed", e)
            false
        }
    }
}
