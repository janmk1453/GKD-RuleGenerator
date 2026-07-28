// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.core.rule.model

import kotlinx.serialization.Serializable

@Serializable
data class GkdRule(
    val id: String,
    val name: String,
    val description: String,
    val appId: String,
    val activityId: String? = null,
    val selector: String,
    val excludeSelector: String? = null,
    val action: ActionType,
    val position: Position? = null,
    val delay: Long = 0,
    val matchOnce: Boolean = true,
    val priority: Int = 0
)

@Serializable
enum class ActionType {
    CLICK,
    LONG_CLICK,
    INPUT_TEXT,
    SCROLL_UP,
    SCROLL_DOWN
}

@Serializable
data class Position(
    val x: Float? = null,
    val y: Float? = null,
    val xRatio: Float? = null,
    val yRatio: Float? = null
)
