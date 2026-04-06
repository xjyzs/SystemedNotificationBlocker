package com.xjyzs.systemednotificationblocker

import android.annotation.SuppressLint
import android.app.Notification
import android.os.Bundle
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
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
            val pref = XSharedPreferences(
                "com.xjyzs.systemednotificationblocker",
                "main"
            )
            pref.reload()
            val blacklistModeMM = pref.getBoolean("blacklistModeMM", true)
            val groupsMM = pref.getString("groupsMM", "") ?: ""
            val blacklistModeQQ = pref.getBoolean("blacklistModeQQ", true)
            val groupsQQ = pref.getString("groupsQQ", "") ?: ""
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
                                    if ("@所有人 " in text) {
                                        if (blacklistModeMM && title in groupsMM || !blacklistModeMM && title !in groupsMM) {
                                            logToFile(
                                                "${
                                                    SimpleDateFormat(
                                                        "yyyy-MM-dd HH:mm:ss",
                                                        Locale.getDefault()
                                                    ).format(
                                                        Date()
                                                    )
                                                } 成功拦截 微信 消息："
                                            )
                                            logToFile("标题: $title")
                                            logToFile("内容: ${text}\n")
                                            param.result = null
                                        }
                                    }
                                }
                            } else if (pkg == "com.tencent.mobileqq") {
                                val notification = param.args[6] as Notification
                                var title: String
                                var text: String
                                val extras: Bundle = notification.extras
                                title = extras.getString(Notification.EXTRA_TITLE) ?: ""
                                text = extras.getString(Notification.EXTRA_TEXT) ?: ""
                                param.args[5] = System.currentTimeMillis().toInt()
                                if ("[有全体消息]" in text && text[0] == '[') {
                                    if (blacklistModeQQ && title in groupsQQ || !blacklistModeQQ && title !in groupsQQ) {
                                        logToFile(
                                            "${
                                                SimpleDateFormat(
                                                    "yyyy-MM-dd HH:mm:ss",
                                                    Locale.getDefault()
                                                ).format(
                                                    Date()
                                                )
                                            } 成功拦截 QQ 消息："
                                        )
                                        logToFile("标题: $title")
                                        logToFile("内容: ${text}\n")
                                        param.result = null
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
        val file = File("/data/system/SystemedNotificationBlockerLogs.txt")
        file.appendText(text + "\n")
    } catch (_: Exception) {
    }
}
