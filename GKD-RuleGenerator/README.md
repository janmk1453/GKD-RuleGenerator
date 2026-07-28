# GKD 规则生成器

基于 MIUIX 库开发的安卓应用，用于可视化生成 GKD 规则。

## 功能特性

- **快照管理**：导入和解析 GKD 快照，可视化节点树
- **规则生成**：使用模板快速生成 GKD 规则
- **AI 助手**：通过 AI 辅助生成规则
- **设置配置**：配置 AI 服务、显示选项等

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose + MIUIX
- **架构**：MVVM + Clean Architecture
- **依赖注入**：Hilt
- **异步**：Kotlin Coroutines + Flow
- **序列化**：Kotlin Serialization

## 项目结构

```
GKD-RuleGenerator/
├── app/                              # 主应用模块
│   ├── src/main/
│   │   ├── java/com/gkd/rulegenerator/
│   │   │   ├── di/                   # 依赖注入
│   │   │   ├── ui/                   # UI 层
│   │   │   ├── domain/               # 领域层
│   │   │   ├── data/                 # 数据层
│   │   │   └── util/                 # 工具类
│   │   └── res/                      # 资源文件
│   └── build.gradle.kts
├── core/                             # 核心模块
│   ├── snapshot/                     # 快照解析
│   ├── selector/                     # 选择器引擎
│   └── rule/                         # 规则生成
├── miuix/                            # MIUIX 库（本地依赖）
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

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

```bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease

# 运行测试
./gradlew test
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
