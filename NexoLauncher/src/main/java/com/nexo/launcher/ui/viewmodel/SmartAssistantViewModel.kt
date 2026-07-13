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
import org.json.JSONObject

data class ChatMessage(val text: String, val isUser: Boolean)

class SmartAssistantViewModel : ViewModel() {
    private val _messages = MutableLiveData<List<ChatMessage>>(listOf(
        ChatMessage("Hello! I am your NexoLauncher Smart Assistant. How can I help you today?", false)
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

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = InfoDistributor.GEMINI_API_KEY,
            tools = listOf(Tool(listOf(adjustSettingsFunction, getDeviceSpecsFunction))),
            systemInstruction = content {
                text("You are a helpful assistant for a Minecraft launcher app called NexoLauncher. " +
                     "You can answer Minecraft gameplay questions, troubleshoot common launcher issues, and give general tips. " +
                     "You have access to tools that can actually change the user's settings. " +
                     "For Minecraft 1.17 and higher (like 1.21.4), the 'VirGL' renderer is very slow. " +
                     "ALWAYS suggest using the 'Vulkan Zink' renderer for better performance on modern versions. " +
                     "When a user wants to 'optimize', 'make it smoother', or 'fix lag', use getDeviceSpecs to see current state, " +
                     "then use adjustSettings to apply improvements. Always explain briefly what you changed. Keep responses concise.")
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

        return JSONObject().apply {
            put("total_device_ram_mb", totalRam)
            put("allocated_ram_mb", currentRam)
            put("resolution_ratio_percent", currentRes)
            put("minecraft_render_distance", currentRD)
            put("minecraft_max_fps", currentFPS)
        }
    }
}
