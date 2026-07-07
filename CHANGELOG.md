# Changelog

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