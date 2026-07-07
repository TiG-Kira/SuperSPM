# SuperSPM

一个基于 Jetpack Compose 和 MiuiX 的 Android 速度测量应用，支持实时速度显示、历史记录管理和速度曲线分析。

## 功能特性

- 📊 **实时速度显示** - 指针式码表实时显示当前速度
- 🗺️ **位置信息** - 显示当前经纬度、地址和定位精度
- 📝 **历史记录** - 记录并保存每次测速数据
- 📈 **速度曲线** - 可视化展示速度变化趋势
- ⚙️ **设置** - 支持速度单位切换（km/h、m/s、mph）和位置刷新时间设置
- 🔌 **插件系统** - 支持 WebUI 和 Native 类型插件扩展

## 技术栈

- **语言**: Kotlin
- **框架**: Jetpack Compose
- **UI库**: MiuiX
- **导航**: Jetpack Navigation
- **数据库**: Room
- **状态管理**: ViewModel + Koin
- **定位**: Android Location
- **插件**: DexClassLoader (Native) / WebView (WebUI)

## 项目结构

```
app/src/main/java/com/kira/superspm/
├── data/              # 数据层
│   ├── dao/           # Room DAO
│   ├── database/      # 数据库配置
│   ├── entity/        # 数据库实体
│   ├── model/         # 数据模型
│   └── repository/    # 数据仓库
├── service/           # 服务层
│   └── LocationService.kt  # 定位服务
├── ui/                # UI层
│   ├── theme/         # 主题配置
│   ├── AboutScreen.kt       # 关于页面
│   ├── AppNavHost.kt        # 导航配置
│   ├── DetailScreen.kt      # 历史详情页面
│   ├── HistoryScreen.kt     # 历史记录页面
│   ├── LabSettingsScreen.kt # 实验室设置页面
│   ├── MainActivity.kt      # 主活动
│   ├── OpenSourceScreen.kt  # 开源项目页面
│   ├── PluginScreen.kt      # 插件管理页面
│   ├── PluginWebPage.kt     # WebUI插件页面
│   ├── PluginNativePage.kt  # Native插件页面
│   ├── SettingsScreen.kt    # 设置页面
│   └── SpeedometerScreen.kt # 码表页面
├── utils/             # 工具类
│   └── PluginManager.kt     # 插件管理器
├── viewmodel/         # ViewModel
└── App.kt             # 应用入口

plugin-sdk/            # 插件SDK模块
└── src/main/kotlin/com/kira/superspm/plugin/
    ├── Plugin.kt      # 插件接口
    ├── BasePlugin.kt  # 插件基类
    └── data/          # 数据模型
```

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- Gradle 8.11.1
- JDK 17
- Android SDK 35

### 构建项目

```bash
# 构建 debug 版本
./gradlew assembleDebug

# 构建 release 版本
./gradlew assembleRelease
```

### 安装到设备

```bash
# 安装 debug 版本
adb install app/build/outputs/apk/debug/SuperSPM_debug_1.2.2.apk

# 安装 release 版本（需要签名）
adb install app/build/outputs/apk/release/SuperSPM_release_1.2.2.apk
```

## 使用说明

1. **授权定位权限** - 首次启动需要授予定位权限
2. **开始测速** - 点击"开始"或"记录并开始"按钮
3. **查看历史** - 切换到历史记录页面查看过往记录
4. **分析详情** - 点击历史记录查看速度曲线和统计数据
5. **管理插件** - 在设置页面进入插件管理，支持导入、启用、禁用和删除插件

## 插件开发

### 插件类型

SuperSPM 支持两种类型的插件：

1. **WebUI 插件** - 使用 HTML/CSS/JS 通过 WebView 运行
2. **Native 插件** - 使用 Kotlin/Java 开发，通过 DexClassLoader 动态加载

### 插件配置文件 (plugin.json)

```json
{
    "name": "插件名称",
    "version": "1.0.0",
    "author": "作者",
    "description": "插件描述",
    "type": "NATIVE",
    "entry": "插件主类名",
    "permissions": ["speed_data", "history_query"],
    "requiresRestart": false
}
```

### 插件权限说明

| 权限名称 | 说明 | 支持的操作 |
|---------|------|-----------|
| `speed_data` | 实时速度数据 | 读取当前速度、位置、里程等实时数据 |
| `history_query` | 历史记录查询 | **只读**查询历史记录，不支持修改和删除 |

**注意**：历史记录权限仅开放查询功能，插件无法修改或删除任何历史记录数据，确保数据安全。

### WebUI 插件示例

创建一个简单的 WebUI 插件：

**plugin.json**
```json
{
    "name": "速度仪表盘",
    "version": "1.0.0",
    "author": "SuperSPM",
    "description": "自定义速度仪表盘显示",
    "type": "WEB_UI",
    "entry": "index.html",
    "permissions": ["speed_data"],
    "requiresRestart": false
}
```

**index.html**
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>速度仪表盘</title>
    <style>
        body { font-family: sans-serif; text-align: center; padding: 20px; }
        #speed { font-size: 64px; font-weight: bold; color: #FF69B4; }
        #unit { font-size: 24px; color: #666; }
    </style>
</head>
<body>
    <div id="speed">0</div>
    <div id="unit">km/h</div>
    <script>
        window.onSpeedUpdate = function(data) {
            document.getElementById('speed').textContent = Math.round(data.currentSpeed * 3.6);
        };
    </script>
</body>
</html>
```

### Native 插件示例

创建一个简单的 Native 插件：

**plugin.json**
```json
{
    "name": "心率监测",
    "version": "1.0.0",
    "author": "SuperSPM",
    "description": "基于运动强度估算心率",
    "type": "NATIVE",
    "entry": "HeartRateMonitor",
    "permissions": ["speed_data"],
    "requiresRestart": false
}
```

**HeartRateMonitor.kt**
```kotlin
package com.kira.superspm.plugin

import com.kira.superspm.plugin.data.AnalysisResult
import com.kira.superspm.plugin.data.SpeedData

class HeartRateMonitor : BasePlugin(
    name = "心率监测",
    version = "1.0.0",
    author = "SuperSPM",
    description = "基于运动强度估算心率"
) {
    override fun onSpeedUpdate(data: SpeedData): AnalysisResult {
        val speedKmh = data.currentSpeed * 3.6
        val heartRate = (60 + speedKmh * 1.5).toInt()
        
        val metrics = mapOf(
            "heartRate" to "$heartRate BPM",
            "speed" to formatSpeed(data.currentSpeed),
            "duration" to formatDuration(data.recordingDuration)
        )

        return AnalysisResult(
            title = "心率监测",
            description = "当前估算心率: $heartRate BPM",
            icon = "❤️",
            advice = "保持运动，注意心率变化",
            metrics = metrics
        )
    }
}
```

### 插件编译工具

项目提供了 PowerShell 脚本 `build_plugin.ps1` 用于编译 Native 插件：

**使用方法**

```powershell
# 设置环境变量
$env:ANDROID_HOME = "C:\Users\Kira\AppData\Local\Android\Sdk"

# 编译插件
.\build_plugin.ps1 -pluginDir .\heart_rate_plugin_src
```

**编译流程**

1. 读取插件目录中的 `plugin.json` 配置文件
2. 将插件源码复制到 `plugin-sdk` 模块
3. 使用 Gradle 构建 `plugin-sdk` 生成 AAR 文件
4. 从 AAR 中提取 `classes.jar`
5. 使用 D8 工具将 JAR 转换为 `classes.dex`
6. 打包 `classes.dex` 和 `plugin.json` 为 ZIP 文件

**编译输出**

编译成功后，插件 ZIP 文件会生成在插件目录的 `build` 子目录下：

```
heart_rate_plugin_src/
└── build/
    └── 心率监测_1.0.0.zip
```

### 插件导入

1. 将编译好的 ZIP 文件复制到设备
2. 打开 SuperSPM 应用
3. 进入设置 > 插件管理
4. 点击"导入插件"按钮选择 ZIP 文件

## 开源项目

本应用使用了以下开源库：

- [MiuiX UI Library](https://github.com/compose-miuix-ui/miuix)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Jetpack Navigation](https://developer.android.com/jetpack/compose/navigation)
- [Koin](https://insert-koin.io/)
- [Coil](https://coil-kt.github.io/coil/)

## 许可证

本项目遵循 AGPL-3.0 许可证和 MIT 许可证。

## 致谢

部分界面设计灵感来源于 [HyperCeiler](https://github.com/ReChronoRain/HyperCeiler) 项目。