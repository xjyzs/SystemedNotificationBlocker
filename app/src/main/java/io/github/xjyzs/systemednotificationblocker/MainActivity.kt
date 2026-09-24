package io.github.xjyzs.systemednotificationblocker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import io.github.xjyzs.systemednotificationblocker.ui.theme.SystemedNotificationBlockerTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SystemedNotificationBlockerTheme {
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    topBar = {
                        LargeFlexibleTopAppBar(
                            title = { Text(stringResource(R.string.app_name)) },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) { innerPadding ->
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
    var blacklistModeMM by remember { mutableStateOf(true) }
    var blacklistModeQQ by remember { mutableStateOf(true) }
    val groupsMM = remember { mutableStateListOf<String>() }
    val groupsQQ = remember { mutableStateListOf<String>() }
    var removePrefix by remember { mutableStateOf(false) }
    var muteGroupNote by remember { mutableStateOf(true) }
    var muteGroupTodo by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    var showDialog by remember { mutableStateOf(false) }
    val pref = remember {
        runCatching {
            context.getSharedPreferences("main", Context.MODE_WORLD_READABLE)
        }.getOrElse {
            context.getSharedPreferences("main", Context.MODE_PRIVATE)
        }
    }
    LaunchedEffect(Unit) {
        try {
            blacklistModeMM = pref.getBoolean("blacklistModeMM", true)
            val groupsMMStr = pref.getString("groupsMM", "") ?: ""
            groupsMM.clear()
            groupsMM.addAll(groupsMMStr.split("\n"))
            blacklistModeQQ = pref.getBoolean("blacklistModeQQ", true)
            val groupsQQStr = pref.getString("groupsQQ", "") ?: ""
            groupsQQ.clear()
            groupsQQ.addAll(groupsQQStr.split("\n"))
            removePrefix = pref.getBoolean("removePrefix", false)
            muteGroupNote = pref.getBoolean("muteGroupNote", true)
            muteGroupTodo = pref.getBoolean("muteGroupTodo", true)
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
            .padding(horizontal = 10.dp)
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
                }, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))
        SwitchRow("移除\"@所有人\"前缀", removePrefix) {
            clickVibrate(vibrator)
            removePrefix = !removePrefix
            pref.edit {
                putBoolean("removePrefix", removePrefix)
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 20.dp))
        Text("微信", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        SwitchRow("静默重复接龙", muteGroupNote) {
            clickVibrate(vibrator)
            muteGroupNote = !muteGroupNote
            pref.edit {
                putBoolean("muteGroupNote", muteGroupNote)
            }
        }
        SwitchRow(stringResource(R.string.blacklist_mode), blacklistModeMM) {
            blacklistModeMM = !blacklistModeMM
            clickVibrate(vibrator)
            pref.edit {
                putBoolean("blacklistModeMM", blacklistModeMM)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "${
                stringResource(if (blacklistModeMM) R.string.blacklist else R.string.whitelist)
            }${stringResource(R.string.group)}", color = MaterialTheme.colorScheme.primary
        )
        GroupsEditor(groupsMM, "groupsMM", pref, vibrator)


        HorizontalDivider(Modifier.padding(vertical = 20.dp))


        Text("QQ", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        SwitchRow("屏蔽群待办", muteGroupTodo) {
            muteGroupTodo = !muteGroupTodo
            clickVibrate(vibrator)
            pref.edit {
                putBoolean("muteGroupTodo", muteGroupTodo)
            }
        }

        SwitchRow(stringResource(R.string.blacklist_mode), blacklistModeQQ) {
            blacklistModeQQ = !blacklistModeQQ
            clickVibrate(vibrator)
            pref.edit {
                putBoolean("blacklistModeQQ", blacklistModeQQ)
            }
        }
        Text(
            "${
                stringResource(if (blacklistModeQQ) R.string.blacklist else R.string.whitelist)
            }${stringResource(R.string.group)}", color = MaterialTheme.colorScheme.primary
        )
        GroupsEditor(groupsQQ, "groupsQQ", pref, vibrator)
        HorizontalDivider(Modifier.padding(vertical = 20.dp))
        Button({
            showDialog = true
        }, Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.reboot_OS))
        }
    }
}


fun clickVibrate(vibrator: Vibrator) {
    if (Build.VERSION.SDK_INT < 33) return
    val attributes = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH)
    vibrator.vibrate(
        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK), attributes
    )
}

@Keep
fun isModuleActive(): Boolean = false