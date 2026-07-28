@echo off
REM GKD-RuleGenerator 构建脚本
REM Copyright 2026, GKD-RuleGenerator contributors

echo 正在构建 GKD-RuleGenerator...

REM 检查 Gradle 是否存在
if not exist "gradlew.bat" (
    echo 错误: gradlew.bat 不存在
    exit /b 1
)

REM 清理项目
echo 正在清理项目...
call gradlew.bat clean

REM 构建调试版本
echo 正在构建调试版本...
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo 构建成功!
    echo APK 文件位于: app\build\outputs\apk\debug\
) else (
    echo 构建失败!
    exit /b 1
)

pause
