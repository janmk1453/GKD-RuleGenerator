// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            SmallTitle(text = "AI 配置")
        }

        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "API 地址",
                        style = MiuixTheme.textStyles.body1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = uiState.apiUrl,
                        onValueChange = { viewModel.updateApiUrl(it) },
                        label = "OpenAI 兼容 API 地址",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "API 密钥",
                        style = MiuixTheme.textStyles.body1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = uiState.apiKey,
                        onValueChange = { viewModel.updateApiKey(it) },
                        label = "sk-...",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "模型名称",
                        style = MiuixTheme.textStyles.body1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = uiState.model,
                        onValueChange = { viewModel.updateModel(it) },
                        label = "gpt-4o / deepseek-chat / ...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            SmallTitle(text = "显示设置")
        }

        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
            ) {
                SwitchPreference(
                    title = "深色模式",
                    checked = uiState.isDarkMode,
                    onCheckedChange = { viewModel.updateDarkMode(it) }
                )
                SwitchPreference(
                    title = "显示节点边界",
                    checked = uiState.showNodeBounds,
                    onCheckedChange = { viewModel.updateShowNodeBounds(it) }
                )
            }
        }

        item {
            SmallTitle(text = "关于")
        }

        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(text = "版本: 1.0.0")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "GKD 规则生成器 - 可视化生成 GKD 订阅规则")
                }
            }
        }
    }
}
