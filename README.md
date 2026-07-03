# SuperSPM

一个基于 Jetpack Compose 和 MiuiX 的 Android 速度测量应用，支持实时速度显示、历史记录管理和速度曲线分析。

## 功能特性

- 📊 **实时速度显示** - 指针式码表实时显示当前速度
- 🗺️ **位置信息** - 显示当前经纬度、地址和定位精度
- 📝 **历史记录** - 记录并保存每次测速数据
- 📈 **速度曲线** - 可视化展示速度变化趋势
- ⚙️ **设置** - 支持速度单位切换（km/h、m/s、mph）和位置刷新时间设置

## 技术栈

- **语言**: Kotlin
- **框架**: Jetpack Compose
- **UI库**: MiuiX
- **导航**: Jetpack Navigation
- **数据库**: Room
- **状态管理**: ViewModel + Koin
- **定位**: Android Location

## 项目结构

```
app/src/main/java/com/kira/superspm/
├── data/              # 数据层
│   ├── dao/           # Room DAO
│   ├── database/      # 数据库配置
│   ├── entity/        # 数据库实体
│   └── repository/    # 数据仓库
├── service/           # 服务层
│   └── LocationService.kt  # 定位服务
├── ui/                # UI层
│   ├── theme/         # 主题配置
│   ├── AboutScreen.kt       # 关于页面
│   ├── AppNavHost.kt        # 导航配置
│   ├── DetailScreen.kt      # 历史详情页面
│   ├── HistoryScreen.kt     # 历史记录页面
│   ├── MainActivity.kt      # 主活动
│   ├── OpenSourceScreen.kt  # 开源项目页面
│   ├── SettingsScreen.kt    # 设置页面
│   └── SpeedometerScreen.kt # 码表页面
├── utils/             # 工具类
├── viewmodel/         # ViewModel
└── App.kt             # 应用入口
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
adb install app/build/outputs/apk/debug/app-debug.apk

# 安装 release 版本（需要签名）
adb install app/build/outputs/apk/release/app-release.apk
```

## 使用说明

1. **授权定位权限** - 首次启动需要授予定位权限
2. **开始测速** - 点击"开始"或"记录并开始"按钮
3. **查看历史** - 切换到历史记录页面查看过往记录
4. **分析详情** - 点击历史记录查看速度曲线和统计数据

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
