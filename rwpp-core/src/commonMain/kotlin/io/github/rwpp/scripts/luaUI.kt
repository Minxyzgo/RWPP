/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.scripts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import io.github.rwpp.ui.RWTextButton

@Composable
fun LuaWidget.Render() {
    when (this) {
        is LuaWidget.LuaText -> {
            Text(
                text,
                color = color.toComposeColor(),
                fontSize = size.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        is LuaWidget.LuaTextButton -> {
            RWTextButton(text, onClick = onClick)
        }

        is LuaWidget.LuaCheckbox -> TODO()
        is LuaWidget.LuaDropdown -> TODO()
        is LuaWidget.LuaImage -> TODO()
        is LuaWidget.LuaInput -> TODO()
        is LuaWidget.LuaSlider -> TODO()
    }
}