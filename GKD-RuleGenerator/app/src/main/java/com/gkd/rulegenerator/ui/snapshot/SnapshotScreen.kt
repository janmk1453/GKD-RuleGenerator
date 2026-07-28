// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.ui.snapshot

import android.util.Log
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField

private const val TAG = "SnapshotScreen"

@Composable
fun SnapshotScreen(viewModel: SnapshotViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            Log.d(TAG, "zipPicker: selected uri=$it")
            val inputStream = context.contentResolver.openInputStream(it)
            viewModel.importZipFile(inputStream)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
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
                    Text(text = "导入快照")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { zipPickerLauncher.launch("application/zip") }
                    ) {
                        Text("选择快照压缩包")
                    }
                    if (!uiState.isFromZip) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "或粘贴快照 JSON:")
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = uiState.snapshotJson,
                            onValueChange = { viewModel.updateSnapshotJson(it) },
                            label = "快照 JSON",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (uiState.isLoading) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "加载中...",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (uiState.error != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "错误: ${uiState.error}",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Red
                    )
                }
            }
        }

        if (uiState.appId.isNotEmpty()) {
            item {
                SmallTitle(text = "应用信息")
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
                        Text(text = "应用: ${uiState.appName}")
                        Text(text = "包名: ${uiState.appId}")
                        Text(text = "Activity: ${uiState.activityId}")
                        Text(text = "屏幕: ${uiState.screenWidth} x ${uiState.screenHeight}")
                    }
                }
            }
        }

        if (uiState.screenshot != null) {
            item {
                SmallTitle(text = "截图预览")
            }

            item {
                val screenshot = uiState.screenshot!!
                Log.d(TAG, "Rendering screenshot: ${screenshot.width}x${screenshot.height}, config=${screenshot.config}, isRecycled=${screenshot.isRecycled}")
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    val aspectRatio = screenshot.width.toFloat() / screenshot.height.toFloat()
                    Log.d(TAG, "Aspect ratio: $aspectRatio")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    Log.d(TAG, "Tap detected at: $offset")
                                    viewModel.handleTap(offset)
                                }
                            }
                    ) {
                        // 使用Android原生ImageView显示截图
                        AndroidView(
                            factory = { ctx ->
                                Log.d(TAG, "AndroidView factory called, creating ImageView")
                                ImageView(ctx).apply {
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    setImageBitmap(screenshot)
                                    Log.d(TAG, "ImageView.setImageBitmap called, bitmap=${screenshot.width}x${screenshot.height}")
                                }
                            },
                            update = { imageView ->
                                Log.d(TAG, "AndroidView update called, bitmap=${screenshot.width}x${screenshot.height}, isRecycled=${screenshot.isRecycled}")
                                if (!screenshot.isRecycled) {
                                    imageView.setImageBitmap(screenshot)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // 绘制选中节点的高亮效果
                        if (uiState.selectedNode != null) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val scaleX = size.width / uiState.screenWidth
                                val scaleY = size.height / uiState.screenHeight
                                val node = uiState.selectedNode!!

                                // 绘制选中节点的半透明红色边框
                                drawRect(
                                    color = Color.Red.copy(alpha = 0.5f),
                                    topLeft = Offset(
                                        x = node.attr.left * scaleX,
                                        y = node.attr.top * scaleY
                                    ),
                                    size = Size(
                                        width = (node.attr.right - node.attr.left) * scaleX,
                                        height = (node.attr.bottom - node.attr.top) * scaleY
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } else if (uiState.appId.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "无截图数据",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            }
        }

        item {
            SmallTitle(text = "节点详情")
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
                    val node = uiState.selectedNode
                    if (node != null) {
                        Text(text = "ID: ${node.attr.id ?: "无"}")
                        Text(text = "VID: ${node.attr.vid ?: "无"}")
                        Text(text = "类名: ${node.attr.name ?: "无"}")
                        Text(text = "文本: ${node.attr.text ?: "无"}")
                        Text(text = "描述: ${node.attr.desc ?: "无"}")
                        Text(text = "可点击: ${node.attr.clickable}")
                        Text(text = "可编辑: ${node.attr.editable}")
                        Text(text = "可见: ${node.attr.visibleToUser}")
                        Text(text = "边界: (${node.attr.left}, ${node.attr.top}) - (${node.attr.right}, ${node.attr.bottom})")
                        Text(text = "尺寸: ${node.attr.width} x ${node.attr.height}")
                        Text(text = "深度: ${node.attr.depth}")
                        Text(text = "索引: ${node.attr.index}")
                        Text(text = "子节点数: ${node.attr.childCount}")
                    } else {
                        Text(text = "点击截图选择节点")
                    }
                }
            }
        }
    }
}
