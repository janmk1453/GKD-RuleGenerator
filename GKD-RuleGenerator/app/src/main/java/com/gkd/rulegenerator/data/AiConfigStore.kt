// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AiConfig(
    val apiUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val systemPromptEnabled: Boolean = true
)

class AiConfigStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_config", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<AiConfig> = _config.asStateFlow()

    private fun loadConfig(): AiConfig = AiConfig(
        apiUrl = prefs.getString(KEY_API_URL, "") ?: "",
        apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
        model = prefs.getString(KEY_MODEL, "") ?: "",
        systemPromptEnabled = prefs.getBoolean(KEY_SYSTEM_PROMPT, true)
    )

    fun updateApiUrl(url: String) {
        prefs.edit().putString(KEY_API_URL, url).apply()
        _config.update { it.copy(apiUrl = url) }
    }

    fun updateApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key).apply()
        _config.update { it.copy(apiKey = key) }
    }

    fun updateModel(model: String) {
        prefs.edit().putString(KEY_MODEL, model).apply()
        _config.update { it.copy(model = model) }
    }

    fun updateSystemPromptEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYSTEM_PROMPT, enabled).apply()
        _config.update { it.copy(systemPromptEnabled = enabled) }
    }

    companion object {
        private const val KEY_API_URL = "api_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model"
        private const val KEY_SYSTEM_PROMPT = "system_prompt_enabled"

        @Volatile
        private var INSTANCE: AiConfigStore? = null

        fun getInstance(context: Context): AiConfigStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AiConfigStore(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
