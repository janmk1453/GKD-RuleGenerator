// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.ui.snapshot

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkd.core.snapshot.model.Node
import com.gkd.core.snapshot.parser.SnapshotParser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

private const val TAG = "SnapshotVM"

@Immutable
data class SnapshotUiState(
    val snapshotJson: String = "",
    val nodes: ImmutableList<Node> = persistentListOf(),
    val selectedNode: Node? = null,
    val screenshot: Bitmap? = null,
    val appId: String = "",
    val appName: String = "",
    val activityId: String = "",
    val screenWidth: Int = 0,
    val screenHeight: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFromZip: Boolean = false
)

class SnapshotViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SnapshotUiState())
    val uiState: StateFlow<SnapshotUiState> = _uiState.asStateFlow()

    fun updateSnapshotJson(json: String) {
        _uiState.update { it.copy(snapshotJson = json, isFromZip = false) }
        parseSnapshot(json)
    }

    fun importZipFile(inputStream: InputStream?) {
        if (inputStream == null) {
            Log.e(TAG, "importZipFile: inputStream is null")
            return
        }

        Log.d(TAG, "importZipFile: starting import")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = withContext(Dispatchers.IO) {
                    parseZipFile(inputStream)
                }
                if (result != null) {
                    Log.d(TAG, "importZipFile: success, appId=${result.appId}, nodes=${result.nodes.size}, hasScreenshot=${result.screenshot != null}")
                    Log.d(TAG, "importZipFile: screenshot size=${result.screenshot?.width}x${result.screenshot?.height}")
                    _uiState.update {
                        it.copy(
                            snapshotJson = "",
                            nodes = result.nodes.toPersistentList(),
                            screenshot = result.screenshot,
                            appId = result.appId,
                            appName = result.appName,
                            activityId = result.activityId,
                            screenWidth = result.screenWidth,
                            screenHeight = result.screenHeight,
                            isLoading = false,
                            isFromZip = true
                        )
                    }
                } else {
                    Log.e(TAG, "importZipFile: parseZipFile returned null")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "无法解析快照文件"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "importZipFile: exception", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "未知错误"
                    )
                }
            }
        }
    }

    private data class ZipParseResult(
        val json: String,
        val nodes: List<Node>,
        val screenshot: Bitmap?,
        val appId: String,
        val appName: String,
        val activityId: String,
        val screenWidth: Int,
        val screenHeight: Int
    )

    private fun parseZipFile(inputStream: InputStream): ZipParseResult? {
        Log.d(TAG, "parseZipFile: starting")
        val zipInputStream = ZipInputStream(inputStream.buffered())
        var jsonContent: String? = null
        var screenshotBytes: ByteArray? = null
        var screenshotFileName: String? = null

        try {
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                Log.d(TAG, "parseZipFile: found entry: ${entry.name}, size=${entry.size}")
                when {
                    entry.name.endsWith(".json") -> {
                        jsonContent = zipInputStream.bufferedReader().readText()
                        Log.d(TAG, "parseZipFile: read json, length=${jsonContent.length}")
                    }
                    entry.name.endsWith(".png") || entry.name.endsWith(".jpg") || entry.name.endsWith(".jpeg") -> {
                        screenshotBytes = zipInputStream.readBytes()
                        screenshotFileName = entry.name
                        Log.d(TAG, "parseZipFile: read image ${entry.name}, bytes=${screenshotBytes.size}")
                    }
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseZipFile: error reading zip", e)
            return null
        } finally {
            zipInputStream.close()
        }

        if (jsonContent == null) {
            Log.e(TAG, "parseZipFile: no json file found in zip")
            return null
        }

        val snapshot = SnapshotParser.parseOrNull(jsonContent)
        if (snapshot == null) {
            Log.e(TAG, "parseZipFile: failed to parse json")
            return null
        }

        Log.d(TAG, "parseZipFile: parsed snapshot, appId=${snapshot.appId}, nodes=${snapshot.nodes.size}")

        // 在IO线程中解码Bitmap并压缩
        val screenshot = screenshotBytes?.let { bytes ->
            try {
                Log.d(TAG, "parseZipFile: decoding bitmap from ${screenshotFileName}, bytes=${bytes.size}")
                
                // 先获取图片尺寸
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                Log.d(TAG, "parseZipFile: original image size: ${options.outWidth}x${options.outHeight}")
                
                // 计算采样率，限制最大尺寸为1080p
                val maxDimension = 1080
                val sampleSize = calculateInSampleSize(options, maxDimension, maxDimension)
                Log.d(TAG, "parseZipFile: sample size: $sampleSize")
                
                // 解码压缩后的Bitmap
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
                Log.d(TAG, "parseZipFile: decoded bitmap: ${bitmap?.width}x${bitmap?.height}, config=${bitmap?.config}")
                bitmap
            } catch (e: Exception) {
                Log.e(TAG, "parseZipFile: failed to decode bitmap", e)
                null
            }
        }

        return ZipParseResult(
            json = jsonContent,
            nodes = snapshot.nodes,
            screenshot = screenshot,
            appId = snapshot.appId,
            appName = snapshot.appInfo.name,
            activityId = snapshot.activityId,
            screenWidth = snapshot.screenWidth,
            screenHeight = snapshot.screenHeight
        )
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun parseSnapshot(json: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val snapshot = SnapshotParser.parseOrNull(json)
                if (snapshot != null) {
                    _uiState.update {
                        it.copy(
                            nodes = snapshot.nodes.toPersistentList(),
                            appId = snapshot.appId,
                            appName = snapshot.appInfo.name,
                            activityId = snapshot.activityId,
                            screenWidth = snapshot.screenWidth,
                            screenHeight = snapshot.screenHeight,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "无法解析快照"
                        )
                    }
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

    fun handleTap(offset: Offset) {
        val state = _uiState.value
        Log.d(TAG, "handleTap: offset=$offset, screenWidth=${state.screenWidth}, screenHeight=${state.screenHeight}")
        
        val nodes = state.nodes
        val tappedNode = nodes.lastOrNull { node ->
            offset.x >= node.attr.left &&
                    offset.x <= node.attr.right &&
                    offset.y >= node.attr.top &&
                    offset.y <= node.attr.bottom
        }
        Log.d(TAG, "handleTap: tappedNode=${tappedNode?.attr?.name}")
        _uiState.update { it.copy(selectedNode = tappedNode) }
    }
}
