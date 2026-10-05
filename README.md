# 系统级 微信/QQ 消息优化 Xposed 模块

[[仓库] xjyzs/SystemedNotificationBlocker](https://github.com/xjyzs/SystemedNotificationBlocker)

[[Xposed 模块仓库]](https://github.com/Xposed-Modules-Repo/io.github.xjyzs.systemednotificationblocker)

还在被不该出现的@**所有人**消息打扰吗？来试试这款屏蔽器吧!

## 功能

- 屏蔽指定群聊的 **@所有人** 消息
- 移除通知栏 **@所有人** 前缀
- 在通知栏保留所有消息，包括**已撤回**消息，防止旧消息被新消息覆盖
- 静默重复的**接龙**消息
- 屏蔽**群待办**通知

## 亮点
- 只 Hook 系统框架, 不容易被目标应用检测

## 界面概览
<img height="400" alt="界面" src="https://github.com/user-attachments/assets/f7397201-2b14-459e-bfb0-e81868cee54b" />

## 实现原理

本项目为 Xposed 模块，通过 hook 系统框架`com.android.server.notification.NotificationManagerService`的
`notificationManagerClass`实现

## 使用方法

**前往[Releases](https://github.com/xjyzs/SystemedNotificationBlocker/releases)下载符合你设备的 apk**
> 对于一般设备，Android 8.0+ 下载`app-arm64Minsdk26-release.apk`  
> Android 10+ 下载`app-arm64Minsdk29-release.apk`  
> Android 15+ 下载`app-arm64Minsdk35-release.apk`  
> 模拟器可下载`app-x86_64-release.apk`  
> 如果不清楚，下载`app-universal-release.apk`
>
在 LSPosed 中启用本模块，勾选系统框架  
打开本模块，配置自定义屏蔽逻辑  
重启手机


> 欢迎提交Issue和Pull Request改进项目!
