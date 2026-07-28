# GKD-RuleGenerator 项目结构说明

## 项目概述

GKD 规则生成器是一款基于 MIUIX 库开发的安卓应用，用于可视化生成 GKD 规则。

## 项目结构

```
GKD-RuleGenerator/
├── app/                              # 主应用模块
│   ├── src/main/
│   │   ├── java/com/gkd/rulegenerator/
│   │   │   ├── di/                   # 依赖注入
│   │   │   │   └── AppModule.kt      # Hilt 模块
│   │   │   ├── ui/                   # UI 层
│   │   │   │   ├── ai/               # AI 助手页面
│   │   │   │   │   ├── AiScreen.kt
│   │   │   │   │   └── AiViewModel.kt
│   │   │   │   ├── navigation/       # 导航配置
│   │   │   │   │   └── AppNavigation.kt
│   │   │   │   ├── rule/             # 规则生成页面
│   │   │   │   │   ├── RuleScreen.kt
│   │   │   │   │   └── RuleViewModel.kt
│   │   │   │   ├── settings/         # 设置页面
│   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   └── SettingsViewModel.kt
│   │   │   │   ├── snapshot/         # 快照页面
│   │   │   │   │   ├── SnapshotScreen.kt
│   │   │   │   │   └── SnapshotViewModel.kt
│   │   │   │   ├── theme/            # 主题配置
│   │   │   │   │   └── Theme.kt
│   │   │   │   └── MainScreen.kt     # 主页面
│   │   │   ├── util/                 # 工具类
│   │   │   │   └── ToastUtils.kt
│   │   │   ├── GKDRuleGeneratorApplication.kt
│   │   │   └── MainActivity.kt
│   │   └── res/                      # 资源文件
│   │       ├── values/
│   │       │   ├── strings.xml
│   │       │   └── themes.xml
│   │       └── values-zh-rCN/
│   │           └── strings.xml
│   └── build.gradle.kts
├── core/                             # 核心模块
│   ├── snapshot/                     # 快照解析
│   │   ├── src/main/java/com/gkd/core/snapshot/
│   │   │   ├── model/
│   │   │   │   └── Snapshot.kt
│   │   │   └── parser/
│   │   │       └── SnapshotParser.kt
│   │   └── src/test/java/com/gkd/core/snapshot/
│   │       └── SnapshotParserTest.kt
│   ├── selector/                     # 选择器引擎
│   │   └── src/main/java/com/gkd/core/selector/
│   │       ├── engine/
│   │       │   └── SelectorEngine.kt
│   │       └── model/
│   │           └── Selector.kt
│   └── rule/                         # 规则生成
│       └── src/main/java/com/gkd/core/rule/
│           ├── model/
│           │   └── GkdRule.kt
│           └── template/
│               └── RuleTemplate.kt
├── miuix/                            # MIUIX 库（本地依赖）
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── build.bat
├── .gitignore
└── README.md
```

## 核心功能

### 1. 快照管理模块
- 导入 GKD 快照（JSON 格式）
- 可视化节点树
- 节点选择和详情展示

### 2. 规则生成模块
- 模板规则系统（开屏广告、弹窗关闭、更新提示、青少年模式）
- 参数配置界面
- 规则代码生成和复制

### 3. AI 助手模块
- 对话界面
- AI 规则生成（待实现）

### 4. 设置模块
- AI 服务配置
- 显示设置
- 关于页面

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose + MIUIX
- **架构**：MVVM + Clean Architecture
- **依赖注入**：Hilt
- **异步**：Kotlin Coroutines + Flow
- **序列化**：Kotlin Serialization

## 构建和运行

### 前置条件
- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 35

### 构建步骤
1. 克隆项目
2. 在 Android Studio 中打开项目
3. 同步 Gradle 依赖
4. 运行应用

或者使用命令行：
```bash
# Windows
build.bat

# Linux/Mac
./gradlew assembleDebug
```

## 开发指南

### 添加新功能
1. 在 `core` 模块中添加业务逻辑
2. 在 `app/ui` 模块中添加 UI 组件
3. 使用 Hilt 进行依赖注入
4. 遵循 MVVM 架构模式

### UI 规范
- 所有 UI 组件使用 MIUIX 库
- 使用 squircle modifier 处理圆角
- 遵循 MIUI 设计语言
- 支持宽屏适配（≥600dp）

### 国际化
- 所有用户字符串使用 `stringResource(R.string.xxx)`
- 新增字符串同时添加到 `values/strings.xml` 和 `values-zh-rCN/strings.xml`
- Key 命名：`{页面}_{描述}`，通用按钮 `common_` 前缀

## 许可证

Apache License 2.0
