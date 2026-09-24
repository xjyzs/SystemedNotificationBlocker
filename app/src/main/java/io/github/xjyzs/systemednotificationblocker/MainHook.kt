package io.github.xjyzs.systemednotificationblocker

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
    var shouldMuteGroupNote = mutableMapOf<String, Int>() // 数值 > 0 时不屏蔽，收到非接龙消息时数值会减少
    var lastGroupNoteTime = 0L

    @SuppressLint("PrivateApi")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == "android" || lpparam.packageName == "system") {
            val pref = XSharedPreferences(
                "com.xjyzs.systemednotificationblocker", "main"
            )
            pref.reload()
            val blacklistModeMM = pref.getBoolean("blacklistModeMM", true)
            val groupsMMSet = (pref.getString("groupsMM", "") ?: "").split("\n").map { it.trim() }
                .filter { it.isNotEmpty() }.toSet()
            val blacklistModeQQ = pref.getBoolean("blacklistModeQQ", true)
            val groupsQQSet = (pref.getString("groupsQQ", "") ?: "").split("\n").map { it.trim() }
                .filter { it.isNotEmpty() }.toSet()
            val removePrefix = pref.getBoolean("removePrefix", false)
            val muteGroupNote = pref.getBoolean("muteGroupNote", true)
            val muteGroupTodo = pref.getBoolean("muteGroupTodo", true)
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
                                    param.args[5] = System.currentTimeMillis().toInt() // 防止通知被覆盖、撤回
                                    if (muteGroupNote) {
                                        if (System.currentTimeMillis() - lastGroupNoteTime > 20) { // 同一条消息可能被 Hook 多次，只处理第一次的
                                            if (": #接龙" in text && "1. " in text) {
                                                if (shouldMuteGroupNote.getOrDefault(
                                                        title, 0
                                                    ) > 0
                                                ) {
                                                    // 静音
                                                    XposedHelpers.setObjectField(
                                                        notification,
                                                        "mChannelId",
                                                        "reminder_channel_id"
                                                    )
                                                }
                                                shouldMuteGroupNote[title] = 2 // 接龙消息之间可穿插 1 条非接龙消息
                                            } else if (title in shouldMuteGroupNote && shouldMuteGroupNote[title] != 0) shouldMuteGroupNote[title] =
                                                shouldMuteGroupNote[title]!! - 1
                                        }
                                        lastGroupNoteTime = System.currentTimeMillis()
                                    }
                                    if ("@所有人 " in text) {
                                        var shouldBlock = !blacklistModeMM
                                        for (group in groupsMMSet) {
                                            if (blacklistModeMM) { // 黑名单
                                                if (group in title) {
                                                    shouldBlock = true
                                                    break
                                                }
                                            } else { // 白名单
                                                if (group in title) {
                                                    shouldBlock = false
                                                    break
                                                }
                                            }
                                        }
                                        if (shouldBlock) {
                                            singleLog("微信", title, text)
                                            param.result = null
                                        } else if (removePrefix && ": @所有人 " in text && text.substringAfter(
                                                ": @所有人 "
                                            ).isNotEmpty()
                                        ) {
                                            text = text.replaceFirst("@所有人 ", "")
                                            extras.putCharSequence(Notification.EXTRA_TEXT, text)
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
                                if (muteGroupTodo) {
                                    if ("还有待办需要处理" in title && "马上处理" in text || "有人设置了群待办" in title) {
                                        singleLog("QQ", title, text)
                                        param.result = null
                                        return
                                    }
                                }
                                if ("[有全体消息]" in text && text[0] == '[') {
                                    var shouldBlock = !blacklistModeQQ
                                    for (group in groupsQQSet) {
                                        if (blacklistModeQQ) { // 黑名单
                                            if (group in title) {
                                                shouldBlock = true
                                                break
                                            }
                                        } else { // 白名单
                                            if (group in title) {
                                                shouldBlock = false
                                                break
                                            }
                                        }
                                    }
                                    if (shouldBlock) {
                                        singleLog("QQ", title, text)
                                        param.result = null
                                    } else if (removePrefix) {
                                        logToFile(text)
                                        var newText = text.replaceFirst("[有全体消息]", "")
                                        val modified = newText.length < text.length
                                        if (": @全体成员 " in newText && newText.substringAfter(": @全体成员 ")
                                                .isNotEmpty()
                                        ) {
                                            newText = newText.replaceFirst("@全体成员 ", "")
                                            logToFile(newText)
                                        }
                                        if (modified) extras.putCharSequence(
                                            Notification.EXTRA_TEXT, newText
                                        )
                                    }
                                }
                            }
                        }
                    })
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
                })
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

fun singleLog(type: String, title: String, text: String) {
    logToFile(
        "${
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
            ).format(
                Date()
            )
        } 成功拦截 $type 消息："
    )
    logToFile("标题: $title")
    logToFile("内容: ${text}\n")
}