// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.ui.rule

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkd.core.rule.model.ActionType
import com.gkd.core.rule.model.GkdRule
import com.gkd.core.rule.template.RuleTemplate
import com.gkd.core.rule.template.RuleTemplateManager
import com.gkd.core.rule.template.generateSelectorFromNode
import com.gkd.core.snapshot.model.Node
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class RuleUiState(
    val templates: ImmutableList<RuleTemplate> = persistentListOf(),
    val selectedTemplate: RuleTemplate? = null,
    val appId: String = "",
    val appName: String = "",
    val activityId: String = "",
    val selectedNode: Node? = null,
    val generatedSelector: String = "",
    val generatedRule: GkdRule? = null,
    val generatedSubscriptionJson: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class RuleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RuleUiState())
    val uiState: StateFlow<RuleUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        val templates = RuleTemplateManager.getTemplates()
        _uiState.update { it.copy(templates = templates.toPersistentList()) }
    }

    fun setSnapshotInfo(appId: String, appName: String, activityId: String) {
        _uiState.update { it.copy(appId = appId, appName = appName, activityId = activityId) }
    }

    fun setSelectedNode(node: Node?) {
        _uiState.update { it.copy(selectedNode = node) }
        // 自动生成选择器
        if (node != null) {
            val selector = generateSelectorFromNode(node)
            _uiState.update { it.copy(generatedSelector = selector) }
        }
    }

    fun selectTemplate(template: RuleTemplate) {
        _uiState.update { 
            it.copy(
                selectedTemplate = template, 
                generatedRule = null, 
                generatedSubscriptionJson = ""
            ) 
        }
        // 如果有选中节点，使用模板生成选择器
        val node = _uiState.value.selectedNode
        if (node != null) {
            val selector = template.generateSelector(node)
            _uiState.update { it.copy(generatedSelector = selector) }
        }
    }

    fun updateSelector(selector: String) {
        _uiState.update { it.copy(generatedSelector = selector) }
    }

    fun generateRule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val state = _uiState.value
                val template = state.selectedTemplate ?: return@launch
                
                val config = mapOf(
                    "appId" to state.appId,
                    "activityId" to state.activityId,
                    "selector" to state.generatedSelector,
                    "ruleName" to template.name,
                    "skipTexts" to extractTexts(state.selectedNode),
                    "closeTexts" to extractTexts(state.selectedNode),
                    "updateTexts" to extractTexts(state.selectedNode),
                    "laterTexts" to listOf("稍后", "取消", "以后再说"),
                    "confirmTexts" to listOf("我知道了", "知道了")
                )
                
                val rule = template.generateRule(config)
                val subscriptionJson = generateSubscriptionJson(rule)
                
                _uiState.update {
                    it.copy(
                        generatedRule = rule,
                        generatedSubscriptionJson = subscriptionJson,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun extractTexts(node: Node?): List<String> {
        if (node == null) return emptyList()
        val texts = mutableListOf<String>()
        node.attr.text?.takeIf { it.isNotBlank() }?.let { texts.add(it) }
        node.attr.desc?.takeIf { it.isNotBlank() }?.let { texts.add(it) }
        return texts
    }

    private fun generateSubscriptionJson(rule: GkdRule): String {
        return buildString {
            appendLine("{")
            appendLine("  id: '${rule.appId}',")
            appendLine("  name: '${rule.name}',")
            appendLine("  groups: [")
            appendLine("    {")
            appendLine("      key: 0,")
            appendLine("      name: '${rule.name}',")
            appendLine("      rules: [")
            appendLine("        {")
            appendLine("          key: 0,")
            appendLine("          name: '${rule.description}',")
            
            // 添加activityIds
            rule.activityId?.takeIf { it.isNotBlank() }?.let {
                appendLine("          activityIds: '$it',")
            }
            
            // 添加fastQuery
            if (rule.selector.contains("vid=") || rule.selector.contains("id=")) {
                appendLine("          fastQuery: true,")
            }
            
            // 添加matches
            appendLine("          matches: '${rule.selector}',")
            
            // 添加action
            appendLine("          action: '${getActionString(rule.action)}',")
            
            // 添加matchOnce
            if (rule.matchOnce) {
                appendLine("          matchOnce: true,")
            }
            
            appendLine("        },")
            appendLine("      ],")
            appendLine("    },")
            appendLine("  ],")
            append("}")
        }
    }

    private fun getActionString(action: ActionType): String {
        return when (action) {
            ActionType.CLICK -> "click"
            ActionType.LONG_CLICK -> "longClick"
            ActionType.INPUT_TEXT -> "inputText"
            ActionType.SCROLL_UP -> "scrollUp"
            ActionType.SCROLL_DOWN -> "scrollDown"
        }
    }

    fun copyRule(context: Context) {
        val ruleJson = _uiState.value.generatedSubscriptionJson
        if (ruleJson.isNotBlank()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("GKD Rule", ruleJson)
            clipboard.setPrimaryClip(clip)
        }
    }
}
