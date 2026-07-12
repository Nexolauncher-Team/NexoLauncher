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

    private val generativeModel by lazy {
        GenerativeModel(
         modelName = "gemini-3.1-flash-lite",
            apiKey = InfoDistributor.GEMINI_API_KEY,
            systemInstruction = content {
                text("You are a helpful assistant for a Minecraft launcher app called NexoLauncher. " +
                     "You can answer Minecraft gameplay questions, troubleshoot common launcher issues, and give general tips. " +
                     "Keep responses concise.")
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
                val response = chat.sendMessage(userText)
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
}
