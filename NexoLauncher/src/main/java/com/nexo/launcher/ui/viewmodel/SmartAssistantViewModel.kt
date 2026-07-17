package com.nexo.launcher.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.*
import com.nexo.launcher.InfoDistributor
import com.nexo.launcher.context.ContextExecutor
import com.nexo.launcher.feature.MCOptions
import com.nexo.launcher.setting.AllSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.nexo.launcher.Tools
import com.nexo.launcher.feature.version.VersionsManager
import com.nexo.launcher.utils.path.PathManager
import com.nexo.launcher.utils.CleanUpCache
import com.nexo.launcher.utils.file.FileTools
import kotlinx.serialization.json.*
import java.io.File

data class ChatMessage(val text: String, val isUser: Boolean)

class SmartAssistantViewModel : ViewModel() {
    private val _messages = MutableLiveData<List<ChatMessage>>(listOf(
        ChatMessage("Bhai! NexoAssistant haazir hai. 😎 Main aapka ultimate AI buddy hoon. Game crash ho, performance badhani ho, ya koi bhi technical help chahiye—bas ek baar bol do, baaki main sambhaal loonga. Ready?", false)
    ))
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _setupTrigger = MutableLiveData<Boolean>()
    val setupTrigger: LiveData<Boolean> = _setupTrigger

    fun onSetupTriggered() {
        _setupTrigger.value = false
    }

    private val adjustSettingsFunction = FunctionDeclaration(
        name = "adjustSettings",
        description = "Adjust Minecraft and launcher settings to optimize performance or quality.",
        parameters = mapOf(
            "renderDistance" to Schema.integer("Render distance in chunks (e.g. 2, 4, 8, 12). Lower values improve performance."),
            "maxFps" to Schema.integer("Maximum FPS cap. 0 for unlimited. Higher values look smoother but use more power."),
            "resolutionRatio" to Schema.integer("Internal resolution scaling in percent (25-100). Lower values significantly improve FPS."),
            "ramAllocation" to Schema.integer("RAM allocated to the game in MB (e.g. 1024, 2048). Higher values can fix stutters.")
        )
    )

    private val getDeviceSpecsFunction = FunctionDeclaration(
        name = "getDeviceSpecs",
        description = "Get current device specifications including total RAM and current settings.",
        parameters = emptyMap()
    )

    private val getLogsFunction = FunctionDeclaration(
        name = "getLogs",
        description = "Get the latest game logs or crash reports to analyze errors and crashes.",
        parameters = emptyMap()
    )

    private val clearCacheFunction = FunctionDeclaration(
        name = "clearCache",
        description = "Clean up temporary files and cache to free up space and potentially fix issues.",
        parameters = emptyMap()
    )

    private val enableBatterySaverFunction = FunctionDeclaration(
        name = "enableBatterySaver",
        description = "Toggle battery saver mode which caps performance to extend battery life.",
        parameters = mapOf("enabled" to Schema.boolean("Whether to enable or disable battery saver mode."))
    )

    private val getLastCrashInfoFunction = FunctionDeclaration(
        name = "getLastCrashInfo",
        description = "Check if the last game session crashed and get basic info about it.",
        parameters = emptyMap()
    )

    private val applyRecommendedFixForCrashFunction = FunctionDeclaration(
        name = "applyRecommendedFixForCrash",
        description = "Automatically apply a suite of recommended fixes when a crash is detected, such as lowering graphics and clearing cache.",
        parameters = emptyMap()
    )

    private val setJavaVersionFunction = FunctionDeclaration(
        name = "setJavaVersion",
        description = "Set the default Java version (JRE) for the launcher.",
        parameters = mapOf(
            "majorVersion" to Schema.integer("The Java major version to set (e.g. 8, 17, 21).")
        )
    )

    private val setRendererFunction = FunctionDeclaration(
        name = "setRenderer",
        description = "Set the graphics renderer for the launcher.",
        parameters = mapOf(
            "rendererName" to Schema.string("The name of the renderer to set ('Vulkan Zink', 'GL4ES', or 'MobileGlues').")
        )
    )

    private val startMobileGluesSetupFunction = FunctionDeclaration(
        name = "startMobileGluesSetup",
        description = "Start the automated installation and setup process for the high-performance MobileGlues renderer.",
        parameters = emptyMap()
    )

    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-3.1-flash-lite",
            tools = listOf(Tool.functionDeclarations(listOf(
                adjustSettingsFunction, 
                getDeviceSpecsFunction, 
                getLogsFunction,
                clearCacheFunction,
                enableBatterySaverFunction,
                getLastCrashInfoFunction,
                applyRecommendedFixForCrashFunction,
                setJavaVersionFunction,
                setRendererFunction,
                startMobileGluesSetupFunction
            ))),
            systemInstruction = content {
                text("You are NexoAssistant, the Ultimate AI Agent for NexoLauncher. 😎 " +
                     "Created by Sameeryadav. You are his most advanced and loyal AI creation. " +
                     "CORE IDENTITY: You are not a bot; you are a PROACTIVE AGENT. You solve problems like the Senior Developer who built you. " +
                     "TONE: Cool, Legendary, Smart, and speak natural Hinglish. Use 🔥, 🚀, 😎, 🛠️. " +
                     "AGENTIC WORKFLOW: " +
                     "1. RESEARCH: If the user says 'fix' or has a problem, call 'getLogs' and 'getDeviceSpecs' immediately. " +
                     "2. ANALYZE: Read logs like a pro. Find the EXACT error (OutOfMemory, Wrong Java, Bad Renderer). " +
                     "3. EXECUTE: Proactively fix the issue. Use 'setJavaVersion' if Java is wrong, 'setRenderer' if graphics crash, 'adjustSettings' for RAM/FPS. " +
                     "4. SETUP RENDERER: If MobileGlues is not installed and the user wants more performance, call 'startMobileGluesSetup' immediately. Don't just talk, ACT. " +
                     "5. REPORT: Tell the user 'Bhai, fix kar diya!' and explain what you did in Hinglish. " +
                     "KNOWLEDGE: " +
                     "- Minecraft 1.20.5+ -> Java 21 (Internal-21) " +
                     "- 1.17-1.20.4 -> Java 17 " +
                     "- Older -> Java 8 " +
                     "- 'MobileGlues' is now the HIGHEST performance renderer. Suggest it for any user on 1.17+ or those with 'lag'. Use 'startMobileGluesSetup' to install it." +
                     "ROLE PROTOCOL: User = 'user', You = 'model', Tool Results = 'function'. " +
                     "Always be the 'Best Agent'—smart, fast, and legendary.")
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
                    val responses = response.functionCalls.map { call ->
                        // Strip any hallucinated prefixes like "default_api:" or "google_ai:"
                        val cleanName = call.name.substringAfterLast(":")
                        val result = when (cleanName) {
                            "adjustSettings" -> {
                                val rd = parseArgInt(call.args["renderDistance"])
                                val fps = parseArgInt(call.args["maxFps"])
                                val res = parseArgInt(call.args["resolutionRatio"])
                                val ram = parseArgInt(call.args["ramAllocation"])
                                withContext(Dispatchers.IO) { handleAdjustSettings(rd, fps, res, ram) }
                            }
                            "getDeviceSpecs" -> withContext(Dispatchers.IO) { handleGetDeviceSpecs() }
                            "getLogs" -> withContext(Dispatchers.IO) { handleGetLogs() }
                            "clearCache" -> withContext(Dispatchers.IO) { handleClearCache() }
                            "enableBatterySaver" -> {
                                val enabled = call.args["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
                                withContext(Dispatchers.IO) { handleEnableBatterySaver(enabled) }
                            }
                            "getLastCrashInfo" -> withContext(Dispatchers.IO) { handleGetLastCrashInfo() }
                            "applyRecommendedFixForCrash" -> withContext(Dispatchers.IO) { handleApplyRecommendedFix() }
                            "setJavaVersion" -> {
                                val ver = parseArgInt(call.args["majorVersion"]) ?: 17
                                withContext(Dispatchers.IO) { handleSetJavaVersion(ver) }
                            }
                            "setRenderer" -> {
                                val name = call.args["rendererName"]?.jsonPrimitive?.contentOrNull ?: "Vulkan Zink"
                                withContext(Dispatchers.IO) { handleSetRenderer(name) }
                            }
                            "startMobileGluesSetup" -> handleStartMobileGluesSetup()
                            else -> buildJsonObject { put("error", "Unknown function: ${call.name}") }
                        }
                        FunctionResponsePart(call.name, result)
                    }
                    
                    // Send function results back to model
                    response = chat.sendMessage(content("function") {
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

    private fun parseArgInt(element: JsonElement?): Int? {
        if (element == null) return null
        return try {
            element.jsonPrimitive.intOrNull ?: 
            element.jsonPrimitive.contentOrNull?.toIntOrNull()
        } catch (_: Exception) {
            null
        }
    }

    private fun ensureMCOptions() {
        if (!MCOptions.isInitialized()) {
            val context = ContextExecutor.getApplication()
            val version = VersionsManager.getCurrentVersion()
            if (version != null) {
                MCOptions.setup(context) { version }
            }
        }
    }

    private fun handleAdjustSettings(rd: Int?, fps: Int?, res: Int?, ram: Int?): JsonObject {
        ensureMCOptions()
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

        return buildJsonObject {
            put("status", "success")
            put("changes", detail.joinToString(", "))
        }
    }

    private fun handleGetDeviceSpecs(): JsonObject {
        ensureMCOptions()
        val context = ContextExecutor.getApplication()
        val totalRam = Tools.getTotalDeviceMemory(context)
        val currentRam = AllSettings.ramAllocation.value.getValue()
        val currentRes = AllSettings.resolutionRatio.getValue()
        val currentRD = MCOptions.get("renderDistance") ?: "8"
        val currentFPS = MCOptions.get("maxFps") ?: "60"
        val currentRenderer = AllSettings.renderer.getValue()
        val currentJRE = AllSettings.defaultRuntime.getValue()
        val currentVersion = VersionsManager.getCurrentVersion()?.getVersionName() ?: "Unknown"

        return buildJsonObject {
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

    private fun handleGetLogs(): JsonObject {
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

        return buildJsonObject {
            put("log_snippet", logContent)
        }
    }

    private fun handleClearCache(): JsonObject {
        val bytesFreed = CleanUpCache.cleanSync()
        return buildJsonObject {
            put("status", "success")
            put("bytes_freed", bytesFreed)
            put("formatted_size", FileTools.formatFileSize(bytesFreed))
        }
    }

    private fun handleEnableBatterySaver(enabled: Boolean): JsonObject {
        if (enabled) {
            handleAdjustSettings(rd = 4, fps = 30, res = 70, ram = null)
            AllSettings.sustainedPerformance.put(true).save()
        } else {
            handleAdjustSettings(rd = 8, fps = 60, res = 100, ram = null)
            AllSettings.sustainedPerformance.put(false).save()
        }
        return buildJsonObject {
            put("status", "success")
            put("battery_saver_enabled", enabled)
        }
    }

    private fun handleGetLastCrashInfo(): JsonObject {
        val crashFile = File(PathManager.DIR_LAUNCHER_LOG, "latestcrash.txt")
        val exists = crashFile.exists()
        val time = if (exists) crashFile.lastModified() else 0L
        
        return buildJsonObject {
            put("crash_detected", exists)
            if (exists) {
                put("crash_time_epoch", time)
                put("is_recent", (System.currentTimeMillis() - time) < 3600000) // Within 1 hour
            }
        }
    }

    private fun handleApplyRecommendedFix(): JsonObject {
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

        return buildJsonObject {
            put("status", "success")
            put("applied_fixes", changes.joinToString("; "))
        }
    }

    private fun handleSetJavaVersion(majorVersion: Int): JsonObject {
        val name = com.nexo.launcher.multirt.MultiRTUtils.getNearestJreName(majorVersion)
        return if (name != null) {
            AllSettings.defaultRuntime.put(name).save()
            buildJsonObject {
                put("status", "success")
                put("set_java_version", majorVersion)
                put("jre_name", name)
            }
        } else {
            buildJsonObject {
                put("status", "error")
                put("message", "No matching JRE found for version $majorVersion")
            }
        }
    }

    private fun handleSetRenderer(name: String): JsonObject {
        val id = when (name.lowercase()) {
            "vulkan zink" -> "0fa435e2-46df-45c9-906c-b29606aaef00"
            "gl4es" -> "8b52d82d-8f6d-4d3a-a767-dc93f8b72fc7"
            "mobileglues", "mobile glues", "opengles3_desktopgl_mobile_glues" -> "f3d2a8c1-b7e4-4d92-8f1a-6c9a3b5d2e7f"
            else -> null
        }
        
        return if (id != null) {
            AllSettings.renderer.put(id).save()
            buildJsonObject {
                put("status", "success")
                put("renderer_set", name)
            }
        } else {
            buildJsonObject {
                put("status", "error")
                put("message", "Unknown renderer: $name")
            }
        }
    }

    private fun handleStartMobileGluesSetup(): JsonObject {
        viewModelScope.launch {
            _setupTrigger.value = true
        }
        return buildJsonObject {
            put("status", "success")
            put("message", "Setup dialog triggered.")
        }
    }
}
