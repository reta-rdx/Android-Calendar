# 智能日历应用

一个功能丰富的Android日历应用，支持农历显示、事件管理、智能提醒等功能。

## 功能特性

### 📅 日历功能

- **公历农历双显示** - 同时显示公历和农历日期
- **月视图** - 清晰的月份视图，支持滑动切换
- **节假日显示** - 自动显示中国传统节日和法定节假日
- **今日高亮** - 当前日期特殊标记

### 📝 事件管理

- **创建事件** - 支持添加标题、描述、时间等详细信息
- **编辑删除** - 灵活的事件编辑和删除功能
- **事件详情** - 完整的事件信息查看界面
- **数据持久化** - 使用Room数据库本地存储

### ⏰ 智能提醒

- **精确闹钟** - 支持Android 12+的精确闹钟功能
- **多种提醒方式** - 声音、震动、通知多重提醒
- **贪睡功能** - 支持延后提醒
- **锁屏显示** - 闹钟可在锁屏状态下显示

### ⚙️ 系统设置

- **权限管理** - 智能权限检查和申请
- **通知设置** - 支持Android 13+的通知权限
- **文件权限** - 支持日历文件导入导出
- **系统集成** - 深度集成Android系统功能

## 技术栈

### 开发框架

- **Kotlin** - 100% Kotlin开发
- **Jetpack Compose** - 现代化UI框架
- **Material Design 3** - 遵循最新设计规范

### 架构组件

- **Room Database** - 本地数据存储
- **ViewModel** - MVVM架构模式
- **Navigation Compose** - 导航管理
- **WorkManager** - 后台任务调度
- **Coroutines** - 异步编程

### 第三方库

- **Lunar Calendar** - 农历计算库 (lunar-1.7.7.jar)
- **Gson** - JSON数据解析
- **Kotest** - 单元测试框架

## 系统要求

- **最低版本**: Android 7.0 (API 24)
- **目标版本**: Android 14 (API 36)
- **编译版本**: Android 14 (API 36)
- **Kotlin版本**: 1.9.22
- **Compose版本**: 1.5.8

## 权限说明

### 核心权限

- `POST_NOTIFICATIONS` - 发送通知 (Android 13+)
- `SCHEDULE_EXACT_ALARM` - 精确闹钟 (Android 12+)
- `USE_EXACT_ALARM` - 使用精确闹钟

### 功能权限

- `READ_EXTERNAL_STORAGE` - 读取外部存储 (仅用于导入日历文件)
- `VIBRATE` - 震动提醒
- `MODIFY_AUDIO_SETTINGS` - 音频设置

## 安装说明

### 开发环境

1. 安装 Android Studio Arctic Fox 或更高版本
2. 确保 JDK 11 或更高版本
3. 克隆项目到本地
4. 同步Gradle依赖

### 构建应用

```bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease
```

### 运行测试

```bash
# 单元测试
./gradlew test

# UI测试
./gradlew connectedAndroidTest
```

## 项目结构

```
app/
├── src/main/java/com/example/calendar/
│   ├── ui/
│   │   ├── screens/          # 界面屏幕
│   │   └── components/       # UI组件
│   ├── data/                 # 数据模型
│   ├── util/                 # 工具类
│   ├── receiver/             # 广播接收器
│   ├── MainActivity.kt       # 主活动
│   └── AlarmActivity.kt      # 闹钟活动
├── src/main/res/             # 资源文件
├── libs/                     # 第三方库
└── schemas/                  # 数据库架构
```

## 主要功能模块

### 1. 日历显示 (MonthView)

- 月份视图组件
- 农历日期计算
- 事件标记显示

### 2. 事件管理 (EventViewScreen)

- 事件创建和编辑
- 事件详情查看
- 事件删除确认

### 3. 闹钟提醒 (AlarmScreen)

- 全屏闹钟界面
- 多媒体播放
- 震动反馈

### 4. 系统设置 (SettingsScreen)

- 权限状态检查
- 系统设置跳转
- 功能开关管理

### 5. 工具类

- `LunarCalendarUtil` - 农历计算
- `PermissionHelper` - 权限管理
- `WheelTimePicker` - 时间选择器

## 版本信息

- **当前版本**: 1.5 (versionCode: 2)
- **包名**: com.example.calendar
- **最后更新**: 2025年12月

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue
- 发送邮件至开发者
- 参与项目讨论

---

*这是一个现代化的Android日历应用，致力于为用户提供最佳的日程管理体验。*
