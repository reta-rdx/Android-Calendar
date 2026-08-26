# 日历（Calendar）

一个使用 Kotlin 与 Jetpack Compose 开发的 Android 本地日历应用。项目支持月、周、日三种视图，可在设备本地管理日程，并提供农历展示、日程提醒以及 iCalendar（`.ics`）导入导出。

## 功能特性

- 月视图、周视图和日视图自由切换
- 快速回到今天，浏览上一周期或下一周期
- 新建、查看、编辑和删除日程
- 支持标题、描述、地点、起止时间及全天日程
- 月/周视图展示农历信息与节气
- 支持通知提醒和全屏闹钟提醒
- 闹钟支持稍后提醒（贪睡）
- 从 `.ics` 文件导入日程，或将全部日程导出为 `.ics`
- 使用 Room 在本地持久化数据，无需登录或联网
- 适配 Material 3、深色模式及 Android 动态配色

## 技术栈

| 类别 | 技术 |
| --- | --- |
| 语言 | Kotlin 2.0.21 |
| UI | Jetpack Compose、Material 3 |
| 架构 | ViewModel + Repository |
| 数据存储 | Room 2.6.1 |
| 异步处理 | Kotlin Coroutines、Flow |
| 后台与提醒 | AlarmManager、BroadcastReceiver、WorkManager |
| 数据交换 | iCalendar（ICS） |
| 构建 | Gradle 8.13、Android Gradle Plugin 8.13.0 |

## 环境要求

- Android Studio（建议使用支持 AGP 8.13 的较新版本）
- JDK 17 或更高版本
- Android SDK 36
- Android 7.0（API 24）或更高版本的设备/模拟器

项目使用 Gradle Wrapper，通常不需要单独安装 Gradle。农历依赖已经以 `app/libs/lunar-1.7.7.jar` 的形式包含在仓库中。

## 快速开始

1. 克隆项目并进入目录：

   ```bash
   git clone <repository-url>
   cd calendar
   ```

2. 使用 Android Studio 打开项目，等待 Gradle 同步完成。

3. 准备 API 24 或更高版本的模拟器或 Android 设备，然后运行 `app` 配置。

也可以在命令行构建 Debug APK：

```powershell
.\gradlew.bat assembleDebug
```

macOS 或 Linux：

```bash
./gradlew assembleDebug
```

构建产物位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 使用说明

- 使用底部导航栏切换月、周、日视图。
- 点击左右箭头切换日期范围，点击“今天”返回当前日期。
- 点击右下角的 `+`，或点击月视图中的日期，新建日程。
- 点击已有日程可查看详情，并继续编辑或删除。
- 在右上角更多菜单中导入或导出 `.ics` 日程文件。
- 创建日程时可以选择提前 5 分钟至 2 小时提醒，并选择闹钟或通知类型。

## 权限说明

应用会根据 Android 版本请求以下权限：

| 权限 | 用途 |
| --- | --- |
| 通知权限 | 显示日程通知（Android 13+） |
| 精确闹钟权限 | 尽可能准时触发日程提醒（Android 12+） |
| 读取外部存储 | 在旧版 Android 上读取导入文件（最高 Android 12L） |
| 振动与音频设置 | 播放闹钟并提供振动反馈 |

如果没有授予精确闹钟权限，应用会降级使用非精确提醒，触发时间可能存在偏差。

## 项目结构

```text
app/src/main/java/com/example/calendar/
├── data/                 # Room 实体、DAO、数据库与 Repository
├── receiver/             # 日程提醒广播接收器
├── ui/
│   ├── components/       # 月/周/日视图与时间选择器
│   ├── screens/          # 日历、日程编辑/详情、闹钟等页面
│   └── theme/            # Compose 主题、颜色与字体
├── util/                 # 农历、提醒、权限及 ICS 导入导出工具
├── viewmodel/            # 日历状态与业务逻辑
├── AlarmActivity.kt      # 全屏闹钟入口
└── MainActivity.kt       # 应用入口
```

## 测试与检查

运行本地单元测试：

```powershell
.\gradlew.bat test
```

在已连接的设备或模拟器上运行仪器测试：

```powershell
.\gradlew.bat connectedAndroidTest
```

运行静态检查：

```powershell
.\gradlew.bat lint
```

## 数据与隐私

日程数据保存在设备本地的 Room 数据库 `calendar_database` 中。应用本身不要求账号，也不会主动将日程上传到远程服务。导出的 `.ics` 文件通常保存在系统下载目录；若系统存储接口不可用，则保存到应用的外部文件目录。

## 参与开发

欢迎通过 Issue 或 Pull Request 提交问题与改进。提交代码前，建议至少执行一次 `test`、`lint` 和 `assembleDebug`，并在目标 Android 版本上验证通知及精确闹钟权限流程。
