/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
val RWOutlinedTextColors
    @Composable get() =
        TextFieldDefaults.outlinedTextFieldColors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            containerColor = Color.Transparent,
            selectionColors = TextSelectionColors(Color.Black, Color.DarkGray.copy(.4f)),
            focusedLabelColor = Color.Black,
            disabledLabelColor = Color.Black,
            cursorColor = Color.Black,
            focusedBorderColor = Color(44, 95, 45),
            unfocusedBorderColor = Color(151, 188, 98)
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

val RWCheckBoxColors
    @Composable get() = CheckboxDefaults.colors(checkedColor = Color(151, 188, 98))