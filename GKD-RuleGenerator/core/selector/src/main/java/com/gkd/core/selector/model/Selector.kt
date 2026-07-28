// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.core.selector.model

data class Selector(
    val expression: String,
    val isQuickQuery: Boolean = false
)

sealed class SelectorNode {
    data class Attribute(
        val name: String,
        val operator: String,
        val value: String
    ) : SelectorNode()

    data class Combinator(
        val type: CombinatorType
    ) : SelectorNode()

    data class Index(
        val start: Int,
        val end: Int? = null
    ) : SelectorNode()
}

enum class CombinatorType {
    DESCENDANT,       // 空格
    CHILD,            // >
    ADJACENT_SIBLING, // +
    GENERAL_SIBLING,  // ~
    PARENT,           // <
    ANCESTOR          // <n
}
