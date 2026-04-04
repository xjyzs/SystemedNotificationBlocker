package com.xjyzs.systemednotificationblocker

import android.annotation.SuppressLint
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.xjyzs.systemednotificationblocker.ui.theme.SystemedNotificationBlockerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SystemedNotificationBlockerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainUI(
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@SuppressLint("WorldReadableFiles")
@Composable
fun MainUI(modifier: Modifier) {
    var blacklistModeMM by remember { mutableStateOf(false) }
    var blacklistModeQQ by remember { mutableStateOf(false) }
    var groupsMM by remember { mutableStateOf("") }
    var groupsQQ by remember { mutableStateOf("") }
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    var showDialog by remember { mutableStateOf(false) }
    val pref = context.getSharedPreferences("main", Context.MODE_WORLD_READABLE)
    LaunchedEffect(Unit) {
        try {
            blacklistModeMM = pref.getBoolean("blacklistModeMM", false)
            groupsMM = pref.getString("groupsMM", "") ?: ""
            blacklistModeQQ = pref.getBoolean("blacklistModeQQ", false)
            groupsQQ = pref.getString("groupsQQ", "") ?: ""
        } catch (e: Exception) {
            Toast.makeText(
                context, context.getString(R.string.grant_root_first, e.message), Toast.LENGTH_SHORT
            ).show()
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.confirm_reboot)) },
            confirmButton = {
                TextButton({
                    clickVibrate(vibrator)
                    try {
                        Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot"))
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.grant_root_first, e.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton({
                    clickVibrate(vibrator)
                    showDialog = false
                }) {
                    Text("取消")
                }
            })
    }
    Column(
        modifier
            .wrapContentSize(Alignment.Center)
            .verticalScroll(rememberScrollState())
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isModuleActive()) {
                Icon(Icons.Default.CheckCircle, null, tint = Color.Green)

            } else {
                Icon(Icons.Default.AddCircle, null, Modifier.rotate(45f), tint = Color.Red)
            }
            Text(
                if (isModuleActive()) {
                    stringResource(R.string.module_activated)
                } else {
                    stringResource(R.string.module_not_activated)
                }, fontSize = 24.sp, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(36.dp))
        Text("微信", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                {
                    blacklistModeMM = !blacklistModeMM
                    pref.edit {
                        putBoolean("blacklistModeMM", blacklistModeMM)
                    }

                }, colors = ButtonDefaults.buttonColors(
                    Color.Transparent, LocalContentColor.current
                ), shape = RectangleShape, contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    stringResource(R.string.blacklist_mode),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.weight(1f))
                Switch(checked = blacklistModeMM, onCheckedChange = {
                    blacklistModeMM = it
                    clickVibrate(vibrator)
                    pref.edit {
                        putBoolean("blacklistModeMM", blacklistModeMM)
                    }
                })
            }
        }
        Spacer(Modifier.height(30.dp))
        Text(
            "${
                stringResource(
                    if (blacklistModeMM) {
                        R.string.blacklist
                    } else {
                        R.string.whitelist
                    }
                )
            }${stringResource(R.string.group)}"
        )
        TextField(groupsMM, {
            groupsMM = it
            pref.edit {
                putString("groupsMM", groupsMM)
            }
        }, Modifier.fillMaxWidth(), maxLines = 10)
        Spacer(Modifier.height(36.dp))


        Text("QQ", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                {
                    blacklistModeQQ = !blacklistModeQQ
                    pref.edit {
                        putBoolean("blacklistModeQQ", blacklistModeQQ)
                    }

                }, colors = ButtonDefaults.buttonColors(
                    Color.Transparent, LocalContentColor.current
                ), shape = RectangleShape, contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    stringResource(R.string.blacklist_mode),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.weight(1f))
                Switch(checked = blacklistModeQQ, onCheckedChange = {
                    blacklistModeQQ = it
                    clickVibrate(vibrator)
                    pref.edit {
                        putBoolean("blacklistModeQQ", blacklistModeQQ)
                    }
                })
            }
        }
        Spacer(Modifier.height(30.dp))
        Text(
            "${
                stringResource(
                    if (blacklistModeQQ) {
                        R.string.blacklist
                    } else {
                        R.string.whitelist
                    }
                )
            }${stringResource(R.string.group)}"
        )
        TextField(groupsQQ, {
            groupsQQ = it
            pref.edit {
                putString("groupsQQ", groupsQQ)
            }
        }, Modifier.fillMaxWidth(), maxLines = 10)
        Spacer(Modifier.height(60.dp))
        Button({
            showDialog = true
        }, Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.reboot_OS))
        }
    }
}


fun clickVibrate(vibrator: Vibrator) {
    val attributes = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH)
    vibrator.vibrate(
        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK), attributes
    )
}

@Keep
fun isModuleActive(): Boolean = false