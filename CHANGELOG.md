# Changelog

## [Unreleased] - 2026-10-21

### UI 对齐 KiTerminal-UX
- 移植 miuix-glass 玻璃拟态组件库（`top/yukonga/miuix/kmp/glass/**`）与液态玻璃导航栏
  （`LiquidGlassNavigationBarWithIndicator`），新增 `miuix-blur` / `miuix-shader` /
  kyant0 `backdrop` / `shapes` / `navigationevent-compose` 依赖
- 底部导航栏改为悬浮式：新增「悬浮玻璃 / 液态玻璃 / 经典」三档样式，设置页
  「外观显示 → 底部导航栏样式」可切换；玻璃样式需 Android 13（API 33）及以上，
  低版本自动降级为经典样式
- 页面内容层挂 `layerBackdrop`，为玻璃底栏提供折射素材

### 里程计算重构
- 新增 `MileageTracker`：精度门限、静止抖动、位置跳变、定位断续四道过滤，
  距离改用 `Location.distanceBetween`
- GPS / NETWORK 双 provider 不再同时喂同一个累加器（GPS 新鲜期内的网络定位整点丢弃），
  消除两者定位互跳造成的「幽灵里程」
- 里程与速度统计改为「服务为唯一数据源」：新增 `LocationService.onStatsUpdate` 回传，
  ViewModel 用 `mirrorStats` 镜像，屏幕显示与存库数值从此一致

### 技术更新
- miuix 0.8.5 → 0.9.3（`miuix-android` → `miuix-ui-android`），compose 1.8.3 → 1.11.1
- compileSdk 36 → 37（backdrop / ui-graphics 的 AAR 元数据要求），targetSdk 与 minSdk 不变
- `AndroidManifest.xml` 用 `tools:overrideLibrary` 放行 blur / shader 的 minSdk 33 声明，
  保持 minSdk 26 并配合运行期 API 33 门禁

## [1.2.3] - 2026-07-08

### Bug修复
- 修复传感器测速模式下，切换页面后返回码表页面变为GPS模式的问题（模式状态保存到ViewModel）
- 修复GPS测速里程计算不准确的问题，0km/h时不再增加里程（速度低于1km/h不计算距离）
- 修复历史记录详情页面无数据时显示空卡片的问题（速度曲线和运动轨迹卡片仅在有数据时显示）
- 修复实验室/关于页及子页面卡片选中动画四角溢出的问题（将clickable移到Card内部）

### 功能更新
- 插件系统新增历史记录查询权限（`history_query`），仅支持只读查询，不支持修改和删除
- 更新README.md，添加插件权限说明文档

## [1.2.2] - 2026-07-07

### 新增功能
- 新增插件配置模板页面，包含WEBUI和Native插件的配置模板及示例代码
- 新增Native插件SDK模块，提供BasePlugin基类简化插件开发

### 改进优化
- 所有插件页面TopAppBar实现吸顶效果（PluginScreen、PluginNativePage、PluginWebPage、PluginTemplateScreen）
- 修复Native插件页面无法上下滑动的问题
- 插件页面原配置模板位置改为配置模板卡片入口，点击跳转新页面

### Bug修复
- 修复Native插件加载时的SecurityException（可写dex文件安全限制）
- 修复插件编译脚本中的JSON解析问题
- 修复D8转换过程中的兼容性问题

### 技术更新
- 更新PluginManager，支持新的插件格式和安全加载机制
- 添加Gradle插件编译工具和示例代码到README.md
- 优化插件加载流程，将dex文件复制到优化目录并设置只读权限