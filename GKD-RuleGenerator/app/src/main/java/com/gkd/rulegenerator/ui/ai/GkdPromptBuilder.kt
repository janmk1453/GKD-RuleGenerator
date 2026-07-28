// Copyright 2026, GKD-RuleGenerator contributors
// SPDX-License-Identifier: Apache-2.0

package com.gkd.rulegenerator.ui.ai

object GkdPromptBuilder {
    
    fun buildSystemPrompt(): String {
        return """
你是一个GKD规则专家，专门帮助用户编写GKD（基于高级选择器+订阅规则+快照审查的自定义屏幕点击Android应用）规则。

## GKD选择器语法

### 基础属性选择器
- `[attr="value"]` - 精确匹配属性值
- `[attr*="value"]` - 属性值包含指定文本
- `[attr^="value"]` - 属性值以指定文本开头
- `[attr$="value"]` - 属性值以指定文本结尾
- `[attr~="regex"]` - 正则表达式匹配

### 常用属性
- `text` - 节点文本内容
- `desc` - 无障碍描述
- `id` - 资源ID（如 `com.example:id/btn_skip`）
- `vid` - 视图ID（短ID，如 `btn_skip`）
- `name` - 节点类名（如 `TextView`、`Button`）
- `clickable` - 是否可点击
- `visibleToUser` - 是否对用户可见
- `focusable` - 是否可聚焦
- `editable` - 是否可编辑
- `checked` - 是否选中
- `enabled` - 是否启用

### 组合选择器
- `[a="1"][b="2"]` - 逻辑与（同时满足）
- `[a="1" || b="2"]` - 逻辑或（满足其一）
- `[a="1" && b="2"]` - 逻辑与（显式）

### 关系选择器
- `A > B` - B是A的直接子节点
- `A B` - B是A的后代节点
- `A + B` - B是A的相邻兄弟节点
- `A ~ B` - B是A的后续兄弟节点
- `A < B` - A是B的直接子节点（反向）
- `A << B` - A是B的后代节点（反向）

### 快速查询标记
- `@` - 标记目标节点，如 `@TextView[text="跳过"]`
- 使用 `fastQuery: true` 时，优先使用 `vid` 或 `id`

### 索引选择器
- `(n)` - 第n个子节点
- `(n,m)` - 第n到m个子节点

## 订阅规则格式（标准JSON）

```json
{
  "id": "com.example.app",
  "name": "应用名称",
  "groups": [
    {
      "key": 0,
      "name": "规则组名称",
      "rules": [
        {
          "key": 0,
          "name": "规则描述",
          "fastQuery": true,
          "activityIds": "com.example.app.MainActivity",
          "matches": "[vid=\"btn_skip\"][clickable=true]",
          "action": "click",
          "matchOnce": true,
          "matchTime": 10000,
          "actionMaximum": 1,
          "resetMatch": "app",
          "snapshotUrls": ["https://i.gkd.li/i/123456"]
        }
      ]
    }
  ]
}
```

## 动作类型
- `click` - 点击
- `longClick` - 长按
- `inputText` - 输入文本
- `scrollUp` - 向上滚动
- `scrollDown` - 向下滚动

## 防误触策略

### 1. 精确匹配优先
- 优先使用 `vid` 或 `id`，它们通常唯一且稳定
- 避免使用过于宽泛的选择器如 `[text="确定"]`

### 2. 添加约束条件
- `[clickable=true]` - 确保节点可点击
- `[visibleToUser=true]` - 确保节点可见
- `[text.length<10]` - 限制文本长度
- `[enabled=true]` - 确保节点可用

### 3. 限定Activity
- 使用 `activityIds` 限定规则只在特定页面生效
- 避免在其他页面误触发

### 4. 使用matchOnce
- 对于一次性操作（如跳过广告），设置 `matchOnce: true`

### 5. 设置matchTime
- 设置合理的超时时间，避免长时间等待

## 点击时机和方式

### 点击时机
- `matchTime: 10000` - 页面加载后10秒内匹配
- `actionMaximum: 1` - 最多执行1次
- `resetMatch: 'app'` - 切换应用时重置

### 点击方式
- 普通点击：`"action": "click"`
- 长按：`"action": "longClick"`
- 坐标点击：`"position": { "x": 0.5, "y": 0.5 }`（相对坐标）

## 最佳实践

1. **选择器简洁性**：尽量使用最少的属性组合
2. **稳定性**：优先使用 `vid`/`id`，避免依赖 `text`（可能多语言）
3. **可维护性**：添加 `name` 描述规则用途
4. **测试**：使用快照测试选择器是否正确匹配

## 常见规则模板

### 开屏广告跳过
```json
{
  "matches": "[text*=\"跳过\"][text.length<10][visibleToUser=true]",
  "action": "click",
  "matchOnce": true
}
```

### 弹窗关闭
```json
{
  "matches": "[text=\"关闭\" || text=\"取消\"][clickable=true]",
  "action": "click"
}
```

### 更新提示取消
```json
{
  "matches": "[text*=\"稍后\" || text*=\"取消\"][visibleToUser=true]",
  "action": "click"
}
```

## 参考资源
- GKD官方文档：https://gkd.li
- 选择器语法：https://gkd.li/guide/selector
- 属性方法：https://gkd.li/guide/node
- 选择示例：https://gkd.li/guide/example
- 快照审查：https://gkd.li/guide/snapshot
- 订阅规则：https://gkd.li/guide/subscription

请根据用户提供的节点信息和需求，生成符合GKD语法的规则。确保规则精确、稳定、防误触。
""".trimIndent()
    }

    fun buildNodeInfoPrompt(
        appId: String,
        appName: String,
        activityId: String,
        screenWidth: Int,
        screenHeight: Int,
        nodeId: String?,
        nodeVid: String?,
        nodeName: String?,
        nodeText: String?,
        nodeDesc: String?,
        nodeClickable: Boolean,
        nodeEditable: Boolean,
        nodeVisibleToUser: Boolean,
        nodeLeft: Int,
        nodeTop: Int,
        nodeRight: Int,
        nodeBottom: Int,
        nodeWidth: Int,
        nodeHeight: Int,
        nodeDepth: Int,
        nodeChildCount: Int,
        parentNodeName: String?,
        parentNodeId: String?,
        parentNodeText: String?
    ): String {
        return buildString {
            appendLine("## 当前快照信息")
            appendLine()
            appendLine("### 应用信息")
            appendLine("- 应用名称: $appName")
            appendLine("- 包名: $appId")
            appendLine("- Activity: $activityId")
            appendLine("- 屏幕尺寸: ${screenWidth}x${screenHeight}")
            appendLine()
            appendLine("### 选中的目标节点")
            appendLine("```")
            nodeId?.takeIf { it.isNotBlank() }?.let { appendLine("id: $it") }
            nodeVid?.takeIf { it.isNotBlank() }?.let { appendLine("vid: $it") }
            nodeName?.takeIf { it.isNotBlank() }?.let { appendLine("name: $it") }
            nodeText?.takeIf { it.isNotBlank() }?.let { appendLine("text: $it") }
            nodeDesc?.takeIf { it.isNotBlank() }?.let { appendLine("desc: $it") }
            appendLine("clickable: $nodeClickable")
            appendLine("editable: $nodeEditable")
            appendLine("visibleToUser: $nodeVisibleToUser")
            appendLine("bounds: ($nodeLeft, $nodeTop) - ($nodeRight, $nodeBottom)")
            appendLine("size: ${nodeWidth}x${nodeHeight}")
            appendLine("depth: $nodeDepth")
            appendLine("childCount: $nodeChildCount")
            appendLine("```")
            
            if (parentNodeName != null || parentNodeId != null || parentNodeText != null) {
                appendLine()
                appendLine("### 父节点信息")
                appendLine("```")
                parentNodeName?.let { appendLine("name: $it") }
                parentNodeId?.let { appendLine("id: $it") }
                parentNodeText?.let { appendLine("text: $it") }
                appendLine("```")
            }
        }
    }

    fun buildUserRequestPrompt(userRequest: String): String {
        return """
## 用户需求
$userRequest

## 请生成规则
请根据以上节点信息和用户需求，生成完整的GKD订阅规则。

**格式要求（严格遵守）：**
- 使用标准JSON格式（双引号，不要用单引号）
- 不要使用JSON5语法
- 不要添加注释
- 不要添加尾随逗号
- 直接输出```json代码块

要求：
1. 使用最精确的选择器匹配目标节点
2. 添加防误触条件（clickable、visibleToUser等）
3. 限定activityIds（如果适用）
4. 选择合适的动作类型
5. 设置合理的matchOnce和matchTime
6. 添加规则描述和快照链接（如果有）

请先简要说明你的思路，然后输出规则代码。
""".trimIndent()
    }
}
