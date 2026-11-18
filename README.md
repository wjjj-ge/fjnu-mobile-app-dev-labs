# 《移动软件开发》课程实验仓库

福建师范大学 · 计算机与网络空间安全学院 · 软件工程专业

本仓库按实验编号分目录存放，每个实验是一个**独立的 Gradle 工程**（各自的 SDK 版本、依赖互不干扰），根目录只放索引与通用忽略规则。

<table>
  <tr>
    <td align="center" width="50%">
      <img src="lab1-android-basics/docs/screenshots/01-main.png" width="300"><br>
      <b>实验 1 · Android 开发基础</b><br>
      <sub>运行环境自检卡 + 生命周期观测台（9 个回调实时上屏）</sub>
    </td>
    <td align="center" width="50%">
      <img src="lab2-android-layout/docs/screenshots/01-main.png" width="300"><br>
      <b>实验 2 · Android 界面布局</b><br>
      <sub>线性 / 表格 / 约束 ×2 / Compose 五题合一的目录式 App</sub>
    </td>
  </tr>
</table>

运行环境：Android Studio + AGP 9.4.1 / Gradle 9.6 / JDK 21，实测机型为 `Pixel 7 · API 36`（Android 16）。

## 实验索引

| 编号 | 目录 | 主题 | 关键产出 |
| --- | --- | --- | --- |
| 实验 1 | [`lab1-android-basics/`](lab1-android-basics/) | Android 开发基础：运行环境、Activity 生命周期 | 运行环境自检卡 + 生命周期观测台（9 个回调全落地）、5 张实测截图、`logcat` 原始证据 |
| 实验 2 | [`lab2-android-layout/`](lab2-android-layout/) | Android 界面布局：线性 / 表格 / 约束 ×2 / Compose | 五题合一的目录式 App、8 张实机截图、五套布局技法逐题说明 |

后续实验按 `lab2-xxx/`、`lab3-xxx/` 续加，并在上表补一行。

## 各实验说明

- **实验 1**：[`lab1-android-basics/README.md`](lab1-android-basics/README.md)
  - 把向导生成的 Hello World 改造成「运行环境自检 + 生命周期观测台」
  - 8 项设备/运行时信息（厂商、型号、API 级别、ABI、分辨率、dpi、最小宽度、方向、包名、PID）
  - 9 个 `Activity` 生命周期回调，每次输出「墙钟时间 + T+相对毫秒 + 回调名」，屏幕上直接可读
  - 四类场景实测：首次启动 / 页面跳转与返回 / 旋转（配置变更）/ 前后台切换

- **实验 2**：[`lab2-android-layout/README.md`](lab2-android-layout/README.md)
  - 线性布局：外层 4 行等高 + 内层 4 格按课件实测像素归一化的 `layout_weight`（21:33:24:22 等三套比例）
  - 表格布局：`stretchColumns="1"` 让快捷键贴右，`layout_span="2"` 跨列画分组细线
  - 约束布局 1：计算器 16 键四行横链（`spread` + 等权重），不嵌套 `LinearLayout`
  - 约束布局 2：太空订票首页，等权重页签、绿底等宽、白框压接缝、贴底 `DEPART`
  - Compose：`mutableStateListOf` + `LazyColumn` 的待办清单，勾选即重组，计数与删除线同步

## 实测要点（Android 16 / API 36）

| 场景 | 真实回调顺序 |
| --- | --- |
| 首次启动 | `onCreate → onStart → onResume` |
| 旋转屏幕 | `onPause → onStop → onSaveInstanceState → onDestroy → onCreate(重建) → onStart → onRestoreInstanceState → onResume` |
| 切到后台 | `onPause → onStop → onSaveInstanceState`（**`onStop` 先于 `onSaveInstanceState`**，与多数旧教程相反） |
| 从后台返回 | `onRestart → onStart → onResume`（实例没被回收，不重建） |

## 如何把某个实验跑起来

```powershell
cd lab1-android-basics
.\gradlew.bat installDebug          # 编译 + 安装到已连接的模拟器/真机
```

或直接双击 `lab1-android-basics\run-debug.bat`（编译 → 安装 → 重启 App → 打印新 PID）。

**判据**：App 首屏卡片里的「进程 PID」每次运行都不一样，才说明装的是新包。

## 环境

| 项 | 版本 |
| --- | --- |
| Android Studio | 2026.1.4 |
| AGP / Gradle | 9.4.1 / 9.6.0 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 24 |
| JDK | 21 |
| 模拟器 | AVD Pixel7API36（Android 16 / API 36 / x86_64 / 1080×2400 @420dpi） |

## 目录约定

```
fjnu-mobile-app-dev-labs/
├─ README.md                 课程索引（本文件）
├─ .gitignore                整门课通用忽略规则
└─ lab1-android-basics/      实验1（独立 Gradle 工程）
   ├─ README.md              该实验的说明与实测结论
   ├─ app/                   源码、布局、资源
   ├─ docs/screenshots/      证据截图
   ├─ logcat-Lab1Lifecycle.txt
   └─ run-debug.bat          一键编译安装
```

每个实验完成后打一个 tag（`lab1`、`lab2` …），方便按版本点开查看当时的完成状态。
