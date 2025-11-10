package com.xjyzs.systemednotificationblocker

import android.annotation.SuppressLint
import android.app.Notification
import android.os.Bundle
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainHook : IXposedHookLoadPackage {
    @SuppressLint("PrivateApi")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == "android" || lpparam.packageName == "system") {
            try {
                val notificationManagerClass = Class.forName(
                    "com.android.server.notification.NotificationManagerService",
                    false,
                    lpparam.classLoader
                )
                XposedBridge.hookAllMethods(
                    notificationManagerClass,
                    "enqueueNotificationInternal",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val pkg = param.args[0] as? String ?: ""
                            if (pkg == "com.tencent.mm") {
                                val notification = param.args[6] as Notification
                                if (notification.channelId == "message_channel_new_id") {

                                    var title: String
                                    var text: String

                                    val extras: Bundle = notification.extras
                                    title = extras.getString(Notification.EXTRA_TITLE) ?: ""
                                    text = extras.getString(Notification.EXTRA_TEXT) ?: ""
                                    param.args[5] = System.currentTimeMillis().toInt()
                                    var blacklistMode = false
                                    var groups = ""
                                    if (File("${dataPath}blacklistMode").exists()) {
                                        if (File("${dataPath}blacklistMode").readText() == "true\n") {
                                            blacklistMode = true
                                        }
                                    }
                                    if (File("${dataPath}groups").exists()) {
                                        groups = File("${dataPath}groups").readText()
                                    }
                                    if ("@所有人 " in text) {
                                        if (blacklistMode && title in groups || !blacklistMode && title !in groups) {
                                            logToFile(
                                                "${
                                                    SimpleDateFormat(
                                                        "yyyy-MM-dd HH:mm:ss",
                                                        Locale.getDefault()
                                                    ).format(
                                                        Date()
                                                    )
                                                } 成功拦截消息："
                                            )
                                            logToFile("标题: $title")
                                            logToFile("内容: ${text}\n")
                                            param.result = null
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            } catch (e: Throwable) {
                logToFile("Hook失败: ${e.message}")
            }
        } else if (lpparam.packageName == "com.xjyzs.systemednotificationblocker") {
            XposedHelpers.findAndHookMethod(
                "com.xjyzs.systemednotificationblocker.MainActivityKt",
                lpparam.classLoader,
                "isModuleActive",
                object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any {
                        return true
                    }
                }
            )
        }
    }
}


fun logToFile(text: String?) {
    try {
        val file = File("/data/system/SystemedNotificationBlocker/log.txt")
        file.appendText(text + "\n")
    } catch (_: Exception) {
    }
}
