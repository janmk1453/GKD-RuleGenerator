// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.core.rule.template

import com.gkd.core.rule.model.ActionType
import com.gkd.core.rule.model.GkdRule
import com.gkd.core.snapshot.model.Node

interface RuleTemplate {
    val name: String
    val description: String
    val action: ActionType
    fun generateRule(config: Map<String, Any>): GkdRule
    fun generateSelector(node: Node): String
}

// 基于节点属性生成选择器
fun generateSelectorFromNode(node: Node): String {
    val selectors = mutableListOf<String>()
    
    // 优先使用vid
    node.attr.vid?.takeIf { it.isNotBlank() }?.let {
        selectors.add("[vid=\"$it\"]")
    }
    
    // 其次使用id
    if (selectors.isEmpty()) {
        node.attr.id?.takeIf { it.isNotBlank() }?.let {
            selectors.add("[id=\"$it\"]")
        }
    }
    
    // 使用text
    if (selectors.isEmpty()) {
        node.attr.text?.takeIf { it.isNotBlank() }?.let {
            selectors.add("[text=\"$it\"]")
        }
    }
    
    // 使用desc
    if (selectors.isEmpty()) {
        node.attr.desc?.takeIf { it.isNotBlank() }?.let {
            selectors.add("[desc=\"$it\"]")
        }
    }
    
    // 如果都没有，使用name和clickable组合
    if (selectors.isEmpty()) {
        node.attr.name?.takeIf { it.isNotBlank() }?.let {
            val nameShort = it.substringAfterLast(".")
            selectors.add("[name=\"$nameShort\"]")
        }
        if (node.attr.clickable) {
            selectors.add("[clickable=true]")
        }
    }
    
    return selectors.joinToString("")
}

// 点击节点模板
class ClickNodeTemplate : RuleTemplate {
    override val name = "点击节点"
    override val description = "点击指定的UI节点"
    override val action = ActionType.CLICK

    override fun generateRule(config: Map<String, Any>): GkdRule {
        val appId = config["appId"] as? String ?: ""
        val selector = config["selector"] as? String ?: ""
        val activityId = config["activityId"] as? String
        val ruleName = config["ruleName"] as? String ?: "点击节点"

        return GkdRule(
            id = "click_node_${System.currentTimeMillis()}",
            name = ruleName,
            description = "点击目标节点",
            appId = appId,
            activityId = activityId,
            selector = selector,
            action = ActionType.CLICK,
            matchOnce = true
        )
    }

    override fun generateSelector(node: Node): String {
        return generateSelectorFromNode(node)
    }
}

// 长按节点模板
class LongClickNodeTemplate : RuleTemplate {
    override val name = "长按节点"
    override val description = "长按指定的UI节点"
    override val action = ActionType.LONG_CLICK

    override fun generateRule(config: Map<String, Any>): GkdRule {
        val appId = config["appId"] as? String ?: ""
        val selector = config["selector"] as? String ?: ""
        val activityId = config["activityId"] as? String
        val ruleName = config["ruleName"] as? String ?: "长按节点"

        return GkdRule(
            id = "long_click_node_${System.currentTimeMillis()}",
            name = ruleName,
            description = "长按目标节点",
            appId = appId,
            activityId = activityId,
            selector = selector,
            action = ActionType.LONG_CLICK,
            matchOnce = true
        )
    }

    override fun generateSelector(node: Node): String {
        return generateSelectorFromNode(node)
    }
}

// 开屏广告跳过模板
class SplashAdTemplate : RuleTemplate {
    override val name = "开屏广告跳过"
    override val description = "跳过应用启动时的开屏广告"
    override val action = ActionType.CLICK

    override fun generateRule(config: Map<String, Any>): GkdRule {
        val appId = config["appId"] as? String ?: ""
        val skipTexts = config["skipTexts"] as? List<String> ?: listOf("跳过", "Skip")
        val activityId = config["activityId"] as? String

        val selector = buildString {
            append("[")
            skipTexts.forEachIndexed { index, text ->
                if (index > 0) append(" || ")
                append("text*=\"$text\"")
            }
            append("][text.length<10][visibleToUser=true]")
        }

        return GkdRule(
            id = "splash_ad_${System.currentTimeMillis()}",
            name = "开屏广告跳过",
            description = "自动跳过开屏广告",
            appId = appId,
            activityId = activityId,
            selector = selector,
            action = ActionType.CLICK,
            matchOnce = true
        )
    }

    override fun generateSelector(node: Node): String {
        val texts = mutableListOf<String>()
        node.attr.text?.takeIf { it.isNotBlank() }?.let { texts.add(it) }
        if (texts.isEmpty()) texts.addAll(listOf("跳过", "Skip"))
        
        return buildString {
            append("[")
            texts.forEachIndexed { index, text ->
                if (index > 0) append(" || ")
                append("text*=\"$text\"")
            }
            append("][text.length<10][visibleToUser=true]")
        }
    }
}

// 弹窗关闭模板
class PopupCloseTemplate : RuleTemplate {
    override val name = "弹窗关闭"
    override val description = "关闭弹窗广告"
    override val action = ActionType.CLICK

    override fun generateRule(config: Map<String, Any>): GkdRule {
        val appId = config["appId"] as? String ?: ""
        val closeTexts = config["closeTexts"] as? List<String> ?: listOf("关闭", "取消")
        val activityId = config["activityId"] as? String

        val selector = buildString {
            append("[")
            closeTexts.forEachIndexed { index, text ->
                if (index > 0) append(" || ")
                append("text*=\"$text\"")
            }
            append("][visibleToUser=true]")
        }

        return GkdRule(
            id = "popup_close_${System.currentTimeMillis()}",
            name = "弹窗关闭",
            description = "自动关闭弹窗广告",
            appId = appId,
            activityId = activityId,
            selector = selector,
            action = ActionType.CLICK
        )
    }

    override fun generateSelector(node: Node): String {
        val texts = mutableListOf<String>()
        node.attr.text?.takeIf { it.isNotBlank() }?.let { texts.add(it) }
        if (texts.isEmpty()) texts.addAll(listOf("关闭", "取消"))
        
        return buildString {
            append("[")
            texts.forEachIndexed { index, text ->
                if (index > 0) append(" || ")
                append("text*=\"$text\"")
            }
            append("][visibleToUser=true]")
        }
    }
}

// 更新提示模板
class UpdatePromptTemplate : RuleTemplate {
    override val name = "更新提示"
    override val description = "关闭应用更新提示"
    override val action = ActionType.CLICK

    override fun generateRule(config: Map<String, Any>): GkdRule {
        val appId = config["appId"] as? String ?: ""
        val laterTexts = config["laterTexts"] as? List<String> ?: listOf("稍后", "取消", "以后再说")
        val activityId = config["activityId"] as? String

        val selector = buildString {
            append("[")
            laterTexts.forEachIndexed { index, text ->
                if (index > 0) append(" || ")
                append("text*=\"$text\"")
            }
            append("][visibleToUser=true]")
        }

        return GkdRule(
            id = "update_prompt_${System.currentTimeMillis()}",
            name = "更新提示关闭",
            description = "关闭应用更新提示弹窗",
            appId = appId,
            activityId = activityId,
            selector = selector,
            action = ActionType.CLICK
        )
    }

    override fun generateSelector(node: Node): String {
        val texts = mutableListOf<String>()
        node.attr.text?.takeIf { it.isNotBlank() }?.let { texts.add(it) }
        if (texts.isEmpty()) texts.addAll(listOf("稍后", "取消", "以后再说"))
        
        return buildString {
            append("[")
            texts.forEachIndexed { index, text ->
                if (index > 0) append(" || ")
                append("text*=\"$text\"")
            }
            append("][visibleToUser=true]")
        }
    }
}

// 青少年模式模板
class TeenModeTemplate : RuleTemplate {
    override val name = "青少年模式"
    override val description = "关闭青少年模式弹窗"
    override val action = ActionType.CLICK

    override fun generateRule(config: Map<String, Any>): GkdRule {
        val appId = config["appId"] as? String ?: ""
        val confirmTexts = config["confirmTexts"] as? List<String> ?: listOf("我知道了", "知道了")
        val activityId = config["activityId"] as? String

        val selector = buildString {
            append("[")
            confirmTexts.forEachIndexed { index, text ->
                if (index > 0) append(" || ")
                append("text*=\"$text\"")
            }
            append("][visibleToUser=true]")
        }

        return GkdRule(
            id = "teen_mode_${System.currentTimeMillis()}",
            name = "青少年模式关闭",
            description = "关闭青少年模式弹窗",
            appId = appId,
            activityId = activityId,
            selector = selector,
            action = ActionType.CLICK
        )
    }

    override fun generateSelector(node: Node): String {
        val texts = mutableListOf<String>()
        node.attr.text?.takeIf { it.isNotBlank() }?.let { texts.add(it) }
        if (texts.isEmpty()) texts.addAll(listOf("我知道了", "知道了"))
        
        return buildString {
            append("[")
            texts.forEachIndexed { index, text ->
                if (index > 0) append(" || ")
                append("text*=\"$text\"")
            }
            append("][visibleToUser=true]")
        }
    }
}

object RuleTemplateManager {
    private val templates = listOf(
        ClickNodeTemplate(),
        LongClickNodeTemplate(),
        SplashAdTemplate(),
        PopupCloseTemplate(),
        UpdatePromptTemplate(),
        TeenModeTemplate()
    )

    fun getTemplates(): List<RuleTemplate> = templates

    fun getTemplateByName(name: String): RuleTemplate? {
        return templates.find { it.name == name }
    }
}
