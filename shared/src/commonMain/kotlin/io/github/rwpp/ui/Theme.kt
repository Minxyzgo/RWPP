/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RWOutlinedTextColors
    @Composable get() =
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            cursorColor = Color.Black,
            selectionColors = TextSelectionColors(Color.Black, Color.DarkGray.copy(.4f)),
            focusedBorderColor = Color(151, 188, 98),
            unfocusedBorderColor = Color.DarkGray,
            focusedLabelColor = Color.Black,
            disabledLabelColor = Color.Black,
        )

val RWSliderColors
    @Composable get() =
        SliderDefaults.colors(
            thumbColor = Color(151, 188, 98),
            activeTrackColor = Color(151, 188, 98),
            activeTickColor = Color(44, 95, 45),
            inactiveTrackColor = Color.White,
            inactiveTickColor = Color.Black
        )

val RWButtonColors
    @Composable get() = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = Color.Black.copy(.7f),
        disabledContainerColor = Color.Black.copy(.7f),
        disabledContentColor = Color.Black.copy(.7f))

val RWSelectionColors
    @Composable get() = TextSelectionColors(
        Color(151, 188, 98),
        Color(151, 188, 98)
    )

val RWCheckBoxColors
    @Composable get() = CheckboxDefaults.colors(checkedColor = Color(151, 188, 98))