// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.gkd.rulegenerator.data.AiConfig
import com.gkd.rulegenerator.data.AiConfigStore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class SettingsUiState(
    val apiUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val isDarkMode: Boolean = false,
    val showNodeBounds: Boolean = true,
    val nodeColor: Int = 0
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var configStore: AiConfigStore? = null

    fun init(configStore: AiConfigStore) {
        this.configStore = configStore
        val config = configStore.config.value
        _uiState.update {
            it.copy(
                apiUrl = config.apiUrl,
                apiKey = config.apiKey,
                model = config.model
            )
        }
    }

    fun updateApiUrl(url: String) {
        _uiState.update { it.copy(apiUrl = url) }
        configStore?.updateApiUrl(url)
    }

    fun updateApiKey(key: String) {
        _uiState.update { it.copy(apiKey = key) }
        configStore?.updateApiKey(key)
    }

    fun updateModel(model: String) {
        _uiState.update { it.copy(model = model) }
        configStore?.updateModel(model)
    }

    fun updateDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun updateShowNodeBounds(show: Boolean) {
        _uiState.update { it.copy(showNodeBounds = show) }
    }

    fun updateNodeColor(color: Int) {
        _uiState.update { it.copy(nodeColor = color) }
    }

    fun openGitHub() {
        // 打开 GitHub 页面
    }
}
