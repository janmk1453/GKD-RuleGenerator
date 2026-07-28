// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.core.snapshot.model

import kotlinx.serialization.Serializable

@Serializable
data class Snapshot(
    val id: Long,
    val appId: String,
    val activityId: String,
    val screenHeight: Int,
    val screenWidth: Int,
    val isLandscape: Boolean,
    val appInfo: AppInfo,
    val gkdAppInfo: GkdAppInfo,
    val device: Device,
    val nodes: List<Node>
)

@Serializable
data class AppInfo(
    val id: String,
    val name: String,
    val versionCode: Int,
    val versionName: String,
    val isSystem: Boolean
)

@Serializable
data class GkdAppInfo(
    val id: String,
    val name: String,
    val versionCode: Int,
    val versionName: String
)

@Serializable
data class Device(
    val device: String,
    val model: String,
    val manufacturer: String,
    val brand: String,
    val sdkInt: Int,
    val release: String
)

@Serializable
data class Node(
    val id: Int,
    val pid: Int,
    val attr: NodeAttr
)

@Serializable
data class NodeAttr(
    val id: String? = null,
    val vid: String? = null,
    val name: String? = null,
    val text: String? = null,
    val desc: String? = null,
    val clickable: Boolean = false,
    val focusable: Boolean = false,
    val checkable: Boolean = false,
    val checked: Boolean = false,
    val editable: Boolean = false,
    val longClickable: Boolean = false,
    val visibleToUser: Boolean = true,
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val childCount: Int = 0,
    val index: Int = 0,
    val depth: Int = 0
)
