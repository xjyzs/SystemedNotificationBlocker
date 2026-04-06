# 系统级微信/QQ @所有人消息屏蔽 Xposed 模块
还在被不该出现的@**所有人**消息打扰吗？来试试这款屏蔽器吧!
## 实现原理
本项目为 Xposed 模块，通过 hook 系统框架`com.android.server.notification.NotificationManagerService`的`notificationManagerClass`实现
## 使用方法
**前往[Releases](https://github.com/xjyzs/SystemedNotificationBlocker/releases)下载符合你设备的 apk**
> 对于一般设备，Android 8.0+ 下载`app-arm64Minsdk26-release.apk`  
> Android 10+ 下载`app-arm64Minsdk29-release.apk`  
> Android 15+ 下载`app-arm64Minsdk35-release.apk`  
> 模拟器可下载`app-x86_64-release.apk`  
> 如果不清楚，下载`app-universal-release.apk`
> 
在 Lsposed 中启用本模块，勾选系统框架  
打开本模块，配置自定义屏蔽逻辑  
重启手机

  
> 欢迎提交Issue和Pull Request改进项目!
