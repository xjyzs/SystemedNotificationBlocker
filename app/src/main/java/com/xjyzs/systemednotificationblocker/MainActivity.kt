package com.xjyzs.systemednotificationblocker

import android.content.Context
import android.os.Bundle
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xjyzs.systemednotificationblocker.ui.theme.SystemedNotificationBlockerTheme
import java.io.OutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SystemedNotificationBlockerTheme {
                Scaffold (modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainUI(Modifier.padding(innerPadding).fillMaxSize())
                }
            }
        }
    }
}


const val dataPath = "/data/system/SystemedNotificationBlocker/"

@Composable
fun MainUI(modifier: Modifier) {
    var blacklistMode by remember { mutableStateOf(false) }
    var groups by remember { mutableStateOf("") }
    val context = LocalContext.current
    val vibrator= context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    var showDialog by remember { mutableStateOf(false) }
    var process: Process
    var outputStream by remember { mutableStateOf<OutputStream?>(null) }
    LaunchedEffect(Unit) {
        try {
            if (readAsRoot("${dataPath}blacklistMode") == "true\n") {
                blacklistMode = true
            }
            val txt = readAsRoot("${dataPath}groups")
            groups = if (txt.isNotEmpty()) {
                txt.substring(0, txt.length - 1)
            } else {
                txt
            }
            process = ProcessBuilder("su").apply {
                redirectErrorStream(true)
            }.start()
            outputStream = process.outputStream
            outputStream!!.write(("mkdir -p ${dataPath}\n").toByteArray())
            outputStream!!.write(("chown system:system ${dataPath}\n").toByteArray())
            outputStream!!.flush()
        } catch (e: Exception) {
            Toast.makeText(context, "请先授予root权限：" + e.message, Toast.LENGTH_SHORT).show()
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("确认重启吗") },
            confirmButton = {
                TextButton({
                    clickVibrate(vibrator)
                    outputStream!!.write(("reboot\n").toByteArray())
                    outputStream!!.flush()
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton({ clickVibrate(vibrator)
                    showDialog = false }) {
                    Text("取消")
                }
            })
    }
    Column(modifier.wrapContentSize(Alignment.Center).verticalScroll(rememberScrollState())){
        Row(verticalAlignment = Alignment.CenterVertically){
            if (isModuleActive()) {
                Icon(Icons.Default.CheckCircle, null, tint = Color.Green)

            }else{
                Icon(Icons.Default.AddCircle, null, Modifier.rotate(45f), tint = Color.Red)
            }
            Text(
                if (isModuleActive()) {
                    "模块已激活"
                } else {
                    "模块未激活"
                }, fontSize = 24.sp, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(36.dp))
        Row(verticalAlignment = Alignment.CenterVertically){
            Button(
                {
                    blacklistMode = !blacklistMode
                    outputStream!!.write(("echo $blacklistMode > ${dataPath}blacklistMode\n").toByteArray())
                    outputStream!!.flush()
                },
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent,
                    LocalContentColor.current
                ),
                shape = RectangleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("黑名单模式", fontSize = 24.sp, fontWeight = FontWeight.Normal)
                Spacer(Modifier.weight(1f))
                Switch(checked = blacklistMode, onCheckedChange = {
                    blacklistMode = it
                    clickVibrate(vibrator)
                    outputStream!!.write(("echo $blacklistMode > ${dataPath}blacklistMode\n").toByteArray())
                    outputStream!!.write(("chown system:system ${dataPath}blacklistMode\n").toByteArray())
                    outputStream!!.flush()
                })
            }
        }
        Spacer(Modifier.height(30.dp))
        Text("${if (blacklistMode){"黑"}else{"白"}}名单群聊名称(每行一个)")
        TextField(groups, {
            groups=it
            outputStream!!.write(("echo '''$groups''' > ${dataPath}groups\n").toByteArray())
            outputStream!!.write(("chown system:system ${dataPath}groups\n").toByteArray())
            outputStream!!.flush()
        }, Modifier.fillMaxWidth(), maxLines = 10)
        Spacer(Modifier.height(60.dp))
        Button({
            showDialog=true
        }, Modifier.fillMaxWidth()) {
            Text("重启系统")
        }
    }
}


fun clickVibrate(vibrator: Vibrator){
    val attributes = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH)
    vibrator.vibrate(
        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK),
        attributes
    )
}

@Keep
fun isModuleActive(): Boolean = false

fun readAsRoot(path: String): String {
    val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $path"))
    return process.inputStream.bufferedReader().use { it.readText() }
}