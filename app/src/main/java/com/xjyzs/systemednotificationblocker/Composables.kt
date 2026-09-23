package com.xjyzs.systemednotificationblocker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit

@Composable
fun SwitchRow(title: String, checked: Boolean, onCheckedChange: () -> Unit) {
    Row(
        Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange() }
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            title, fontSize = 20.sp, fontWeight = FontWeight.Normal
        )
        Spacer(Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = {
            onCheckedChange()
        })
    }
}

@Composable
fun GroupsEditor(
    groups: SnapshotStateList<String>,
    key: String,
    pref: android.content.SharedPreferences,
    vibrator: android.os.Vibrator
) {
    var dialogExpanded by remember { mutableStateOf(false) }
    var indexToDelete by remember { mutableIntStateOf(-1) }
    for (i in groups.indices) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SmallTextField(
                groups[i], {
                    groups[i] = it
                    pref.edit {
                        putString(key, groups.joinToString("\n"))
                    }
                }, Modifier.padding(vertical = 3.dp)
                    .weight(1f)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(40)
                    ), placeholder = {
                    Text(
                        "新的群聊名称 / 关键词", color = MaterialTheme.colorScheme.secondary
                    )
                })
            Spacer(Modifier.width(10.dp))
            IconButton({
                clickVibrate(vibrator)
                indexToDelete = i
                dialogExpanded = true
            }, Modifier.size(24.dp)) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
    Spacer(Modifier.size(3.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.weight(1f))
        IconButton({
            clickVibrate(vibrator)
            groups.add("")
            pref.edit {
                putString(key, groups.joinToString("\n"))
            }
        }, Modifier.size(24.dp)) {
            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
        }
    }

    if (dialogExpanded) AlertDialog(
        { dialogExpanded = false },
        title = { Text("删除群聊名称 / 关键词") },
        text = { Text("确定要删除 \"${groups[indexToDelete]}\" 吗?") },
        confirmButton = {
            TextButton({
                clickVibrate(vibrator)
                groups.removeRange(indexToDelete, indexToDelete + 1)
                indexToDelete = -1
                pref.edit {
                    putString(key, groups.joinToString("\n"))
                }
                dialogExpanded = false
            }) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton({
                clickVibrate(vibrator)
                dialogExpanded = false
            }) {
                Text("取消")
            }
        })
}

@Composable
fun SmallTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    showFrame: Boolean = false,
    showHandle: Boolean = true
) {
    val selectionColors = TextSelectionColors(
        handleColor = if (showHandle) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        }, backgroundColor = MaterialTheme.colorScheme.primary
    )
    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.heightIn(min = 36.dp),
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            maxLines = 7,
            keyboardOptions = keyboardOptions,
            decorationBox = { innerTextField ->
                Box(
                    modifier = if (showFrame) {
                        modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(vertical = 5.dp, horizontal = 10.dp)
                    } else {
                        modifier.padding(vertical = 5.dp, horizontal = 10.dp)
                    }
                ) {
                    if (value.isEmpty() && placeholder != null) {
                        placeholder()
                    }
                    innerTextField()
                }
            })
    }
}