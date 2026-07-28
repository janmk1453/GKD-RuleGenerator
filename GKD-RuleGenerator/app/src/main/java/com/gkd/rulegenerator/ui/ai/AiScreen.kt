// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.ui.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AiScreen(viewModel: AiViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPromptDialog by remember { mutableStateOf(false) }
    var showNodeInfoDialog by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("text", text))
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 功能按钮区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Button(
                    onClick = { showPromptDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("查看系统提示词")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showNodeInfoDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("查看节点信息")
                }
            }
        }

        // 节点信息区域
        if (uiState.selectedNode != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "当前选中节点",
                        style = MiuixTheme.textStyles.body1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val node = uiState.selectedNode!!
                    Text(text = "应用: ${uiState.appName} (${uiState.appId})")
                    Text(text = "文本: ${node.attr.text ?: "无"} | VID: ${node.attr.vid ?: "无"}")
                    Text(text = "可点击: ${node.attr.clickable} | 类名: ${node.attr.name?.substringAfterLast(".") ?: "无"}")
                }
            }
        }

        if (uiState.selectedNode == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "请先在快照页面选择目标节点，然后告诉我你想要实现什么功能",
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (uiState.error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = uiState.error!!,
                    modifier = Modifier.padding(12.dp),
                    color = MiuixTheme.colorScheme.error
                )
            }
        }

        // 消息列表（支持文本选择）
        SelectionContainer {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatMessageCard(
                        message = message,
                        onCopy = { copyToClipboard(message.content) }
                    )
                }
            }
        }

        // 输入区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                TextField(
                    value = uiState.inputText,
                    onValueChange = { viewModel.updateInput(it) },
                    label = "描述你想要的规则（如：点击跳过按钮）",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.sendMessage() }
                ) {
                    Text("发送")
                }
            }
        }
    }

    // 系统提示词对话框
    WindowDialog(
        show = showPromptDialog,
        onDismissRequest = { showPromptDialog = false },
        title = "系统提示词",
    ) {
        val dismiss = LocalDismissState.current
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                item {
                    Text(
                        text = uiState.systemPrompt,
                        style = MiuixTheme.textStyles.body2
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { dismiss?.invoke() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("关闭")
            }
        }
    }

    // 节点信息对话框
    WindowDialog(
        show = showNodeInfoDialog,
        onDismissRequest = { showNodeInfoDialog = false },
        title = "节点详细信息",
    ) {
        val dismiss = LocalDismissState.current
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (uiState.selectedNode != null) {
                val node = uiState.selectedNode!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    item {
                        Text(text = "应用信息:")
                        Text(text = "  名称: ${uiState.appName}")
                        Text(text = "  包名: ${uiState.appId}")
                        Text(text = "  Activity: ${uiState.activityId}")
                        Text(text = "  屏幕: ${uiState.screenWidth}x${uiState.screenHeight}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "节点属性:")
                        Text(text = "  ID: ${node.attr.id ?: "无"}")
                        Text(text = "  VID: ${node.attr.vid ?: "无"}")
                        Text(text = "  类名: ${node.attr.name ?: "无"}")
                        Text(text = "  文本: ${node.attr.text ?: "无"}")
                        Text(text = "  描述: ${node.attr.desc ?: "无"}")
                        Text(text = "  可点击: ${node.attr.clickable}")
                        Text(text = "  可编辑: ${node.attr.editable}")
                        Text(text = "  可见: ${node.attr.visibleToUser}")
                        Text(text = "  边界: (${node.attr.left}, ${node.attr.top}) - (${node.attr.right}, ${node.attr.bottom})")
                        Text(text = "  尺寸: ${node.attr.width}x${node.attr.height}")
                        Text(text = "  深度: ${node.attr.depth}")
                        Text(text = "  子节点数: ${node.attr.childCount}")
                    }
                }
            } else {
                Text(text = "未选中节点")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { dismiss?.invoke() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("关闭")
            }
        }
    }
}

@Composable
private fun ChatMessageCard(
    message: ChatMessage,
    onCopy: () -> Unit
) {
    var showThinking by remember { mutableStateOf(false) }
    val hasThinking = message.thinking.isNotBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (message.isUser) "你" else "AI",
                    style = MiuixTheme.textStyles.body1,
                    modifier = Modifier.weight(1f)
                )
                if (message.isStreaming) {
                    Text(
                        text = "思考中...",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(
                    onClick = onCopy
                ) {
                    Text("复制")
                }
            }

            // 思维链（可展开）
            if (hasThinking) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (showThinking) "收起思考过程 ▲" else "展开思考过程 ▼",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.clickable { showThinking = !showThinking }
                )
                AnimatedVisibility(
                    visible = showThinking,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        BasicText(
                            text = message.thinking,
                            style = TextStyle(fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurface),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // 正文内容
            if (message.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                BasicText(
                    text = message.content,
                    style = TextStyle(color = MiuixTheme.colorScheme.onSurface)
                )
            }
        }
    }
}
