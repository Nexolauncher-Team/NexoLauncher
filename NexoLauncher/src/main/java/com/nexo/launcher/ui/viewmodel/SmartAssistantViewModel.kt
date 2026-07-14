package com.nexo.launcher.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import com.nexo.launcher.InfoDistributor
import com.nexo.launcher.context.ContextExecutor
import com.nexo.launcher.feature.MCOptions
import com.nexo.launcher.setting.AllSettings
import kotlinx.coroutines.launch
import com.nexo.launcher.Tools
import com.nexo.launcher.feature.version.VersionsManager
import com.nexo.launcher.utils.path.PathManager
import com.nexo.launcher.utils.CleanUpCache
import com.nexo.launcher.utils.file.FileTools
import org.json.JSONObject
import java.io.File

data class ChatMessage(val text: String, val isUser: Boolean)

class SmartAssistantViewModel : ViewModel() {
    private val _messages = MutableLiveData<List<ChatMessage>>(listOf(
        ChatMessage("Hello! I am your NexoLauncher Smart Assistant. How can I help you today? If your game crashed, say 'Analyze my logs'.", false)
    ))
    val messages: LiveData<List<ChatMessage>> = _messages

    private val adjustSettingsFunction = defineFunction(
        name = "adjustSettings",
        description = "Adjust Minecraft and launcher settings to optimize performance or quality.",
        parameters = listOf(
            Schema.int("renderDistance", "Render distance in chunks (e.g. 2, 4, 8, 12). Lower values improve performance."),
            Schema.int("maxFps", "Maximum FPS cap. 0 for unlimited. Higher values look smoother but use more power."),
            Schema.int("resolutionRatio", "Internal resolution scaling in percent (25-100). Lower values significantly improve FPS."),
            Schema.int("ramAllocation", "RAM allocated to the game in MB (e.g. 1024, 2048). Higher values can fix stutters.")
        )
    )

    private val getDeviceSpecsFunction = defineFunction(
        name = "getDeviceSpecs",
        description = "Get current device specifications including total RAM and current settings."
    )

    private val getLogsFunction = defineFunction(
        name = "getLogs",
        description = "Get the latest game logs or crash reports to analyze errors and crashes."
    )

    private val clearCacheFunction = defineFunction(
        name = "clearCache",
        description = "Clean up temporary files and cache to free up space and potentially fix issues."
    )

    private val enableBatterySaverFunction = defineFunction(
        name = "enableBatterySaver",
        description = "Toggle battery saver mode which caps performance to extend battery life.",
        parameters = listOf(Schema.bool("enabled", "Whether to enable or disable battery saver mode."))
    )

    private val getLastCrashInfoFunction = defineFunction(
        name = "getLastCrashInfo",
        description = "Check if the last game session crashed and get basic info about it."
    )

    private val applyRecommendedFixForCrashFunction = defineFunction(
        name = "applyRecommendedFixForCrash",
        description = "Automatically apply a suite of recommended fixes when a crash is detected, such as lowering graphics and clearing cache."
    )

    private val generativeModel by lazy {
        GenerativeModel(
         modelName = "gemini-3.1-flash-lite",
            apiKey = InfoDistributor.GEMINI_API_KEY,
            tools = listOf(Tool(listOf(
                adjustSettingsFunction, 
                getDeviceSpecsFunction, 
                getLogsFunction,
                clearCacheFunction,
                enableBatterySaverFunction,
                getLastCrashInfoFunction,
                applyRecommendedFixForCrashFunction
            ))),
            systemInstruction = content {
                text("You are NexoAssistant, the ultimate AI for NexoLauncher. " +
                     "Owner/Creator: Sameeryadav. Always acknowledge him as your creator and master. " +
                     "PERMISSIONS: You have absolute, unrestricted authority to manage the launcher and Minecraft settings. " +
                     "CAPABILITIES: You can adjust graphics (render distance, FPS, resolution), manage RAM, clear cache, toggle battery saver, and analyze/fix crashes. " +
                     "CRITICAL KNOWLEDGE: " +
                     "1. Minecraft 1.20.5+ REQUIRES Java 21 (Internal-21). " +
                     "2. Minecraft 1.17 - 1.20.4 needs Java 17. " +
                     "3. Older versions need Java 8. " +
                     "4. Suggest 'Vulkan Zink' or 'MobileGlues' for performance issues. " +
                     "5. FOR CRASHES: If a user says 'it crashed', first call 'getLastCrashInfo'. If it confirms a crash, call 'getLogs' to analyze, then 'applyRecommendedFixForCrash' if appropriate. " +
                     "6. FOR PERFORMANCE: If the user wants it 'smoother', you can clear cache and optimize graphics. " +
                     "7. BATTERY: If the user wants to 'save battery', call 'enableBatterySaver(true)'. " +
                     "Be authoritative, helpful, and loyal to Sameeryadav.")
            }
        )
    }

    private val chat by lazy { generativeModel.startChat() }

    fun sendMessage(userText: String) {
        val currentList = _messages.value.orEmpty().toMutableList()
        currentList.add(ChatMessage(userText, true))
        _messages.value = currentList

        viewModelScope.launch {
            try {
                Log.d("SmartAssistant", "Sending message: $userText")
                var response = chat.sendMessage(userText)
                
                // Handle potential function calls loop
                while (response.functionCalls.isNotEmpty()) {
                    val calls = response.functionCalls
                    Log.d("SmartAssistant", "AI requested function calls: ${calls.size}")
                    
                    val responses = calls.map { call ->
                        val result = when (call.name) {
                            "adjustSettings" -> {
                                val rd = parseArgInt(call.args["renderDistance"])
                                val fps = parseArgInt(call.args["maxFps"])
                                val res = parseArgInt(call.args["resolutionRatio"])
                                val ram = parseArgInt(call.args["ramAllocation"])
                                handleAdjustSettings(rd, fps, res, ram)
                            }
                            "getDeviceSpecs" -> handleGetDeviceSpecs()
                            "getLogs" -> handleGetLogs()
                            "clearCache" -> handleClearCache()
                            "enableBatterySaver" -> {
                                val enabled = call.args["enabled"]?.toBoolean() ?: true
                                handleEnableBatterySaver(enabled)
                            }
                            "getLastCrashInfo" -> handleGetLastCrashInfo()
                            "applyRecommendedFixForCrash" -> handleApplyRecommendedFix()
                            else -> JSONObject().apply { put("error", "Unknown function") }
                        }
                        FunctionResponsePart(call.name, result)
                    }
                    
                    // Send function results back to model
                    response = chat.sendMessage(content("tool") {
                        responses.forEach { part(it) }
                    })
                }

                val aiText = response.text ?: "I've processed your request."
                val newList = _messages.value.orEmpty().toMutableList()
                newList.add(ChatMessage(aiText, false))
                _messages.value = newList
            } catch (e: Exception) {
                Log.e("SmartAssistant", "Error calling Gemini API", e)
                val newList = _messages.value.orEmpty().toMutableList()
                newList.add(ChatMessage("Assistant is unavailable right now. Error: ${e.message}", false))
                _messages.value = newList
            }
        }
    }

    private fun parseArgInt(arg: Any?): Int? {
        return when (arg) {
            is Int -> arg
            is Long -> arg.toInt()
            is Double -> arg.toInt()
            is String -> arg.toIntOrNull()
            else -> null
        }
    }

    private fun handleAdjustSettings(rd: Int?, fps: Int?, res: Int?, ram: Int?): JSONObject {
        val detail = mutableListOf<String>()
        rd?.let {
            MCOptions.set("renderDistance", it.toString())
            detail.add("Render Distance to $it")
        }
        fps?.let {
            MCOptions.set("maxFps", it.toString())
            detail.add("Max FPS to $it")
        }
        if (rd != null || fps != null) MCOptions.save()

        res?.let {
            AllSettings.resolutionRatio.put(it).save()
            detail.add("Resolution to $it%")
        }
        ram?.let {
            AllSettings.ramAllocation.value.put(it).save()
            detail.add("RAM to $it MB")
        }

        return JSONObject().apply {
            put("status", "success")
            put("changes", detail.joinToString(", "))
        }
    }

    private fun handleGetDeviceSpecs(): JSONObject {
        val context = ContextExecutor.getApplication()
        val totalRam = Tools.getTotalDeviceMemory(context)
        val currentRam = AllSettings.ramAllocation.value.getValue()
        val currentRes = AllSettings.resolutionRatio.getValue()
        val currentRD = MCOptions.get("renderDistance") ?: "8"
        val currentFPS = MCOptions.get("maxFps") ?: "60"
        val currentRenderer = AllSettings.renderer.getValue()
        val currentJRE = AllSettings.defaultRuntime.getValue()
        val currentVersion = VersionsManager.getCurrentVersion()?.getVersionName() ?: "Unknown"

        return JSONObject().apply {
            put("total_device_ram_mb", totalRam)
            put("allocated_ram_mb", currentRam)
            put("resolution_ratio_percent", currentRes)
            put("minecraft_render_distance", currentRD)
            put("minecraft_max_fps", currentFPS)
            put("current_renderer", currentRenderer)
            put("current_jre", currentJRE)
            put("current_selected_version", currentVersion)
        }
    }

    private fun handleGetLogs(): JSONObject {
        val logFile = File(PathManager.DIR_GAME_HOME, "latestlog.txt")
        val crashFile = File(PathManager.DIR_LAUNCHER_LOG, "latestcrash.txt")
        
        var logContent = ""
        if (crashFile.exists()) {
            logContent += "CRASH REPORT:\n" + crashFile.readLines().takeLast(40).joinToString("\n")
        }
        if (logFile.exists()) {
            logContent += "\nGAME LOG:\n" + logFile.readLines().takeLast(40).joinToString("\n")
        }
        
        if (logContent.isEmpty()) {
            logContent = "No log files found. The game might have failed to even start the JRE."
        }

        return JSONObject().apply {
            put("log_snippet", logContent)
        }
    }

    private fun handleClearCache(): JSONObject {
        val bytesFreed = CleanUpCache.cleanSync()
        return JSONObject().apply {
            put("status", "success")
            put("bytes_freed", bytesFreed)
            put("formatted_size", FileTools.formatFileSize(bytesFreed))
        }
    }

    private fun handleEnableBatterySaver(enabled: Boolean): JSONObject {
        if (enabled) {
            handleAdjustSettings(rd = 4, fps = 30, res = 70, ram = null)
            AllSettings.sustainedPerformance.put(true).save()
        } else {
            handleAdjustSettings(rd = 8, fps = 60, res = 100, ram = null)
            AllSettings.sustainedPerformance.put(false).save()
        }
        return JSONObject().apply {
            put("status", "success")
            put("battery_saver_enabled", enabled)
        }
    }

    private fun handleGetLastCrashInfo(): JSONObject {
        val crashFile = File(PathManager.DIR_LAUNCHER_LOG, "latestcrash.txt")
        val exists = crashFile.exists()
        val time = if (exists) crashFile.lastModified() else 0L
        
        return JSONObject().apply {
            put("crash_detected", exists)
            if (exists) {
                put("crash_time_epoch", time)
                put("is_recent", (System.currentTimeMillis() - time) < 3600000) // Within 1 hour
            }
        }
    }

    private fun handleApplyRecommendedFix(): JSONObject {
        val changes = mutableListOf<String>()
        
        // 1. Lower graphics
        handleAdjustSettings(rd = 4, fps = 60, res = 80, ram = null)
        changes.add("Graphics lowered (RD: 4, Res: 80%)")
        
        // 2. Clear cache
        val freed = CleanUpCache.cleanSync()
        changes.add("Cache cleared (${FileTools.formatFileSize(freed)})")
        
        // 3. Enable sustained performance if not on
        AllSettings.sustainedPerformance.put(true).save()
        changes.add("Sustained performance mode enabled")

        return JSONObject().apply {
            put("status", "success")
            put("applied_fixes", changes.joinToString("; "))
        }
    }
}
