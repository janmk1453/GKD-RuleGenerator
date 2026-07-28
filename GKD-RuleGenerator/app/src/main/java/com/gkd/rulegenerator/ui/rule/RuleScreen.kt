// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.ui.rule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField

@Composable
fun RuleScreen(viewModel: RuleViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // 显示当前选中的节点信息
        if (uiState.selectedNode != null) {
            item {
                SmallTitle(text = "当前选中节点")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val node = uiState.selectedNode!!
                        Text(text = "应用: ${uiState.appName} (${uiState.appId})")
                        Text(text = "Activity: ${uiState.activityId}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "节点类名: ${node.attr.name ?: "无"}")
                        Text(text = "节点文本: ${node.attr.text ?: "无"}")
                        Text(text = "节点描述: ${node.attr.desc ?: "无"}")
                        Text(text = "节点ID: ${node.attr.id ?: "无"}")
                        Text(text = "节点VID: ${node.attr.vid ?: "无"}")
                        Text(text = "可点击: ${node.attr.clickable}")
                    }
                }
            }
        }

        item {
            SmallTitle(text = "选择模板")
        }

        items(uiState.templates) { template ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = template.name)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = template.description)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.selectTemplate(template) }
                    ) {
                        Text("使用此模板")
                    }
                }
            }
        }

        if (uiState.selectedTemplate != null) {
            item {
                SmallTitle(text = "选择器配置")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "模板: ${uiState.selectedTemplate!!.name}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "生成的选择器:")
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = uiState.generatedSelector,
                            onValueChange = { viewModel.updateSelector(it) },
                            label = "GKD 选择器",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.generateRule() }
                        ) {
                            Text("生成规则")
                        }
                    }
                }
            }
        }

        if (uiState.generatedRule != null) {
            item {
                SmallTitle(text = "生成的规则")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "规则名称: ${uiState.generatedRule!!.name}")
                        Text(text = "规则描述: ${uiState.generatedRule!!.description}")
                        Text(text = "动作: ${uiState.generatedRule!!.action}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "订阅规则 JSON:")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = uiState.generatedSubscriptionJson)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.copyRule(context) }
                        ) {
                            Text("复制规则")
                        }
                    }
                }
            }
        }
    }
}
