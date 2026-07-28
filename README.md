# GKD 规则生成器

基于 MIUIX 的 Android 应用，用于可视化生成 [GKD](https://github.com/gkd-kit/gkd) 订阅规则。支持快照导入、节点选择、模板规则和 AI 辅助生成。

## 功能

- **快照导入**：导入 GKD 快照 zip 文件，在截图上可视化节点
- **节点选择**：点击截图选择目标节点，查看完整属性
- **模板规则**：内置常用模板（开屏广告跳过、弹窗关闭、更新提示、青少年模式）
- **AI 规则生成**：接入 OpenAI 兼容 API，自动构建包含节点信息的提示词，流式输出规则
- **规则导出**：生成标准 JSON 格式的 GKD 订阅规则，一键复制

## 截图

| 快照 | 规则 | AI |
|------|------|-----|
| 导入快照、选择节点 | 模板生成规则 | AI 对话生成规则 |

## 技术栈

| 技术 | 用途 |
|------|------|
| Kotlin | 开发语言 |
| Jetpack Compose | UI 框架 |
| [MIUIX](https://github.com/miuix-kmp/miuix) | UI 组件库 |
| Ktor | HTTP 客户端 |
| kotlinx.serialization | JSON 序列化 |
| kotlinx.collections.immutable | 不可变集合 |

## 构建

### 环境要求

- JDK 17
- Android SDK 35+
- Android Studio Hedgehog 或更高版本

### 命令行构建

```bash
# 克隆仓库
git clone https://github.com/janmk1453/GKD-RuleGenerator.git
cd GKD-RuleGenerator/GKD-RuleGenerator

# 调试版本
./gradlew assembleDebug

# Windows
build.bat
```

APK 输出路径：`app/build/outputs/apk/debug/`

## 使用

1. **导入快照**：在快照页面点击"选择快照压缩包"，选择 GKD 导出的 zip 文件
2. **选择节点**：点击截图上的目标节点，下方显示节点详情
3. **生成规则**：
   - 模板方式：切换到规则页面，选择模板，点击"生成规则"
   - AI 方式：在设置页面配置 API 地址/密钥/模型，切换到 AI 页面描述需求
4. **复制规则**：点击"复制规则"按钮，粘贴到 GKD 订阅中使用

## AI 配置

在设置页面填写：

| 字段 | 说明 | 示例 |
|------|------|------|
| API 地址 | OpenAI 兼容的完整地址或 base URL | `https://api.openai.com/v1` |
| API 密钥 | 服务商提供的密钥 | `sk-...` |
| 模型名称 | 服务商支持的模型 ID | `gpt-4o`、`deepseek-chat` |

支持所有 OpenAI 兼容协议的服务商，地址会自动补全 `/chat/completions` 路径。

## 项目结构

```
GKD-RuleGenerator/
├── app/
│   └── src/main/java/com/gkd/rulegenerator/
│       ├── MainActivity.kt
│       ├── data/              # AI 配置、API 客户端
│       └── ui/
│           ├── MainScreen.kt  # Scaffold + 底部导航
│           ├── snapshot/      # 快照导入与节点选择
│           ├── rule/          # 模板规则生成
│           ├── ai/            # AI 对话与规则生成
│           └── settings/      # API 配置
├── core/
│   ├── snapshot/              # 快照 JSON 解析
│   ├── selector/              # 选择器引擎
│   └── rule/                  # 规则模型与模板
└── gradle/
```

## GKD 规则参考

- [GKD 官方文档](https://gkd.li)
- [选择器语法](https://gkd.li/guide/selector)
- [订阅规则格式](https://gkd.li/guide/subscription)
- [快照审查](https://gkd.li/guide/snapshot)
- [选择器示例](https://gkd.li/guide/example)

## 许可证

[MIT](LICENSE)
