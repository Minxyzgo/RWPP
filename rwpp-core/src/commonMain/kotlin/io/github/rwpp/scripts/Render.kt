/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.scripts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.rwpp.ui.ComposeWidget
import io.github.rwpp.ui.Widget
import io.github.rwpp.widget.*

@Composable
fun Widget.Render() {
    when (this) {
        is Widget.Text -> {
            Text(
                text,
                color = Color(color.color),
                fontSize = size.sp,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
        }

        is Widget.TextButton -> {
            RWTextButton(text, onClick = onClick)
        }

        is Widget.Image -> {
            AsyncImage(
                model,
                null,
                modifier = Modifier.padding(5.dp)
            )
        }

        is Widget.Checkbox -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                var checked by remember { mutableStateOf(checked()) }
                RWCheckbox(
                    checked,
                    onCheckedChange = {
                        checked = it
                        onCheckedChange(checked)
                    },
                    modifier = Modifier.padding(5.dp),
                    true
                )
                Text(
                    text,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        is Widget.Dropdown -> {
            var selectedIndex by remember { mutableStateOf(options.indexOf(defaultValue())) }
            LargeDropdownMenu(
                modifier = Modifier.defaultMinSize(50.dp).padding(10.dp),
                selectedIndex = selectedIndex,
                items = options.toList(),
                label = label,
                onItemSelected = { i, v -> selectedIndex = i; onChange(i, v) }
            )
        }

        is Widget.TextField -> {
            Modifier.fillMaxWidth()
            var value by remember { mutableStateOf(defaultText()) }
            RWSingleOutlinedTextField(
                label,
                value,
                onValueChange = { value = it; onTextChanged(value) },
                modifier = Modifier.defaultMinSize(100.dp).padding(5.dp)
            )
        }
        is Widget.Slider -> {
            var v by remember { mutableStateOf(value) }
            Slider(
                value = v,
                modifier = Modifier.defaultMinSize(100.dp).padding(5.dp),
                onValueChange = { v = it; onChange(v) },
                colors = RWSliderColors
            )
        }

        is Widget.Column -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                widgets.forEach { it.Render() }
            }
        }

        is Widget.Row -> {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                widgets.forEach { it.Render() }
            }
        }

        is ComposeWidget -> {
            Content()
        }
    }
}