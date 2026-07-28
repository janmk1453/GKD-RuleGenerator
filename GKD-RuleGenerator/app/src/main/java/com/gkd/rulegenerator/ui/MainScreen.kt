// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gkd.rulegenerator.data.AiConfigStore
import com.gkd.rulegenerator.ui.ai.AiScreen
import com.gkd.rulegenerator.ui.ai.AiViewModel
import com.gkd.rulegenerator.ui.rule.RuleScreen
import com.gkd.rulegenerator.ui.rule.RuleViewModel
import com.gkd.rulegenerator.ui.settings.SettingsScreen
import com.gkd.rulegenerator.ui.settings.SettingsViewModel
import com.gkd.rulegenerator.ui.snapshot.SnapshotScreen
import com.gkd.rulegenerator.ui.snapshot.SnapshotViewModel
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Tune

data class TabItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val topAppBarScrollBehavior = MiuixScrollBehavior()

    val configStore = remember { AiConfigStore.getInstance(context) }

    val snapshotViewModel = remember { SnapshotViewModel() }
    val ruleViewModel = remember { RuleViewModel() }
    val aiViewModel = remember { AiViewModel().also { it.init(configStore) } }
    val settingsViewModel = remember { SettingsViewModel().also { it.init(configStore) } }

    val snapshotUiState by snapshotViewModel.uiState.collectAsStateWithLifecycle()

    // 当快照信息更新时，同步到RuleViewModel和AiViewModel
    LaunchedEffect(snapshotUiState.appId, snapshotUiState.appName, snapshotUiState.activityId, snapshotUiState.screenWidth, snapshotUiState.screenHeight) {
        if (snapshotUiState.appId.isNotEmpty()) {
            ruleViewModel.setSnapshotInfo(
                snapshotUiState.appId,
                snapshotUiState.appName,
                snapshotUiState.activityId
            )
            aiViewModel.setSnapshotInfo(
                snapshotUiState.appId,
                snapshotUiState.appName,
                snapshotUiState.activityId,
                snapshotUiState.screenWidth,
                snapshotUiState.screenHeight
            )
        }
    }

    // 当选中节点更新时，同步到RuleViewModel和AiViewModel
    LaunchedEffect(snapshotUiState.selectedNode) {
        ruleViewModel.setSelectedNode(snapshotUiState.selectedNode)
        aiViewModel.setSelectedNode(snapshotUiState.selectedNode)
    }

    val tabs = listOf(
        TabItem("快照", MiuixIcons.Tune),
        TabItem("规则", MiuixIcons.Tune),
        TabItem("AI", MiuixIcons.Tune),
        TabItem("设置", MiuixIcons.Settings)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = "GKD 规则生成器",
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        label = tab.label,
                        icon = tab.icon,
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 300),
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    0 -> SnapshotScreen(viewModel = snapshotViewModel)
                    1 -> RuleScreen(viewModel = ruleViewModel)
                    2 -> AiScreen(viewModel = aiViewModel)
                    3 -> SettingsScreen(viewModel = settingsViewModel)
                }
            }
        }
    }
}
