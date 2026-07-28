// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.ui.ai

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkd.core.snapshot.model.Node
import com.gkd.rulegenerator.data.AiApiClient
import com.gkd.rulegenerator.data.AiConfigStore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "AiViewModel"

@Immutable
data class ChatMessage(
    val content: String = "",
    val thinking: String = "",
    val isUser: Boolean,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class AiUiState(
    val messages: ImmutableList<ChatMessage> = persistentListOf(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val appId: String = "",
    val appName: String = "",
    val activityId: String = "",
    val screenWidth: Int = 0,
    val screenHeight: Int = 0,
    val selectedNode: Node? = null,
    val parentNode: Node? = null,
    val systemPrompt: String = GkdPromptBuilder.buildSystemPrompt()
)

class AiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    private val apiClient = AiApiClient()
    private var configStore: AiConfigStore? = null

    fun init(configStore: AiConfigStore) {
        this.configStore = configStore
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun setSnapshotInfo(appId: String, appName: String, activityId: String, screenWidth: Int, screenHeight: Int) {
        _uiState.update {
            it.copy(
                appId = appId,
                appName = appName,
                activityId = activityId,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )
        }
    }

    fun setSelectedNode(node: Node?, parentNode: Node? = null) {
        _uiState.update { it.copy(selectedNode = node, parentNode = parentNode) }
    }

    fun sendMessage() {
        val input = _uiState.value.inputText.trim()
        if (input.isEmpty()) return

        val userMessage = ChatMessage(content = input, isUser = true)
        _uiState.update {
            it.copy(
                messages = (it.messages + userMessage).toPersistentList(),
                inputText = "",
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val state = _uiState.value
                val config = configStore?.config?.value

                if (config == null || config.apiKey.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "请先在设置中配置 API 密钥"
                        )
                    }
                    return@launch
                }

                val nodeInfoPrompt = if (state.selectedNode != null) {
                    GkdPromptBuilder.buildNodeInfoPrompt(
                        appId = state.appId,
                        appName = state.appName,
                        activityId = state.activityId,
                        screenWidth = state.screenWidth,
                        screenHeight = state.screenHeight,
                        nodeId = state.selectedNode!!.attr.id,
                        nodeVid = state.selectedNode!!.attr.vid,
                        nodeName = state.selectedNode!!.attr.name,
                        nodeText = state.selectedNode!!.attr.text,
                        nodeDesc = state.selectedNode!!.attr.desc,
                        nodeClickable = state.selectedNode!!.attr.clickable,
                        nodeEditable = state.selectedNode!!.attr.editable,
                        nodeVisibleToUser = state.selectedNode!!.attr.visibleToUser,
                        nodeLeft = state.selectedNode!!.attr.left,
                        nodeTop = state.selectedNode!!.attr.top,
                        nodeRight = state.selectedNode!!.attr.right,
                        nodeBottom = state.selectedNode!!.attr.bottom,
                        nodeWidth = state.selectedNode!!.attr.width,
                        nodeHeight = state.selectedNode!!.attr.height,
                        nodeDepth = state.selectedNode!!.attr.depth,
                        nodeChildCount = state.selectedNode!!.attr.childCount,
                        parentNodeName = state.parentNode?.attr?.name,
                        parentNodeId = state.parentNode?.attr?.id,
                        parentNodeText = state.parentNode?.attr?.text
                    )
                } else {
                    "（未选中节点，请先在快照页面选择目标节点）"
                }

                val userRequestPrompt = GkdPromptBuilder.buildUserRequestPrompt(input)
                val fullUserMessage = "$nodeInfoPrompt\n\n$userRequestPrompt"

                // 添加一个流式消息占位
                val streamingMsg = ChatMessage(content = "", thinking = "", isUser = false, isStreaming = true)
                _uiState.update {
                    it.copy(messages = (it.messages + streamingMsg).toPersistentList())
                }

                Log.d(TAG, "Starting stream: ${config.apiUrl}, model: ${config.model}")

                apiClient.chatCompletionStream(
                    apiUrl = config.apiUrl,
                    apiKey = config.apiKey,
                    model = config.model,
                    systemPrompt = state.systemPrompt,
                    userMessage = fullUserMessage
                ).collect { chunk ->
                    _uiState.update { s ->
                        val msgs = s.messages.toMutableList()
                        val last = msgs.lastOrNull()
                        if (last != null && !last.isUser) {
                            msgs[msgs.lastIndex] = last.copy(
                                content = chunk.content,
                                thinking = chunk.thinking,
                                isStreaming = !chunk.isDone
                            )
                        }
                        s.copy(messages = msgs.toPersistentList(), isLoading = chunk.isDone)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage error", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "请求失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(messages = persistentListOf()) }
    }

    override fun onCleared() {
        super.onCleared()
        apiClient.close()
    }
}
