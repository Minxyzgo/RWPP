/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RWOutlinedTextColors
    @Composable get() =
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = MaterialTheme.colorScheme.secondary,
            selectionColors = TextSelectionColors(MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.primaryContainer.copy(.4f)),
            focusedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedLabelColor = MaterialTheme.colorScheme.secondary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurface
        )

val RWSliderColors
    @Composable get() =
        SliderDefaults.colors(
            thumbColor =  MaterialTheme.colorScheme.primary,
            activeTrackColor =  MaterialTheme.colorScheme.primary,
            activeTickColor = MaterialTheme.colorScheme.onSecondary,
            inactiveTrackColor = MaterialTheme.colorScheme.onSurface,
            inactiveTickColor = MaterialTheme.colorScheme.onSurface
        )

val RWSelectionColors
    @Composable get() = TextSelectionColors(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary
    )

val RWTextFieldColors
    @Composable get() = TextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )

val RWCheckBoxColors
    @Composable get() = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondaryContainer)

val defaultRWPPColorScheme = darkColorScheme(
        surface = Color(27, 18, 18),
        surfaceContainer = Color.DarkGray,
        onSurface = Color.White,
        primaryContainer = Color(151, 188, 98),
        secondary = Color(160, 191, 124),
        secondaryContainer = Color(151, 188, 98),
        onSecondaryContainer = Color(151, 188, 98),
        primary = Color(151, 188, 98),
        onSecondary = Color(44, 95, 45),
        background = Color(53, 57, 53),
        inversePrimary = Color.Green,
        onTertiaryContainer = Color.White,
        surfaceTint = Color.White,
    )


val themes = mapOf(
    "RWPP" to defaultRWPPColorScheme,

    "Material Purple" to darkColorScheme(),

    "Material Default" to darkColorScheme(
        primary = Color(0xFFBB86FC),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF6200EE),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFF03DAC6),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF005047),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFF03DAC6),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFF003E3E),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF543F6A),
    ),

    // copy from https://github.com/MFlisar/ComposeThemer/tree/main
    "Amber Blue" to darkColorScheme(
        primary = Color(0xFFFFB300),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFFC87200),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFF82B1FF),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF3770CF),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFF448AFF),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFF0B429C),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF6A510A),
    ),

    "Aqua Blue" to darkColorScheme(
        primary = Color(0xFF5DB3D5),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF297EA0),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFA1E9DF),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF005049),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFFA0E5E5),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFF004F50),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF2F515F),
    ),

    "Bahama And Trinidad" to darkColorScheme(
        primary = Color(0xFF4585B5),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF095D9E),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFE57C4A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDD520F),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFF9CD5F9),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFF3A7292),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF253F52),
    ),

    "Gold Sunset" to darkColorScheme(
        primary = Color(0xFFEDA85E),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFFB86914),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFD28F60),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFFB5642C),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFFDDAB88),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFFBF7D4E),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF684D2F),
    ),

    "Flutter Dash" to darkColorScheme(
        primary = Color(0xFFB4E6FF),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF1E8FDB),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFF99CCF9),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF202B6D),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFFBAA99D),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFF514239),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF52666A),
    ),

    "Hippie Blue" to  darkColorScheme(
        primary = Color(0xFF669DB3),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF078282),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFFC6E75),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFF92001A),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFFF75F67),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFF580810),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF324851),
    ),

    "Pink Sakura" to darkColorScheme(
        primary = Color(0xFFEEC4D8),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFFCE5B78),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFF5D6C6),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFFEBA689),
        onSecondaryContainer = Color(0xFF000000),
        tertiary = Color(0xFFF7E0D4),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFFEEBDA8),
        onTertiaryContainer = Color(0xFF000000),
        inversePrimary = Color(0xFF695860),
    ),

    "Blumine" to darkColorScheme(
        primary = Color(0xFF82BACE),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF04666F),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFFFD682),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF9E7910),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFF243E4D),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFF426173),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF3E545C),
    ),

    "Green Money" to darkColorScheme(
        primary = Color(0xFF7AB893),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF224430),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFD5D6A8),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF515402),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFFBBBE74),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFF404204),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF3A5344),
    ),

    "Rosewood" to darkColorScheme(
        primary = Color(0xFF9C5A69),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF5F111E),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFEDCE9B),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF805E23),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFFF5DFB9),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFF8E6E3C),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF482E34),
    ),

    "Verdun Lime" to darkColorScheme(
        primary = Color(0xFFBCD063),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF3F4C00),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFFFE17B),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF3B2F00),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFF78D3EC),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFF224E43),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF555D31),
    ),

    "Grey Law" to darkColorScheme(
        primary = Color(0xFF90A4AE),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF37474F),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFF815AA3),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFF421F62),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFF373D5C),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFF1D2449),
        onTertiaryContainer = Color(0xFFFFFFFF),
        error = Color(0xFFCF6679),
        onError = Color(0xFF000000),
        errorContainer = Color(0xFFB1384E),
        onErrorContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF434B4F),
    ),

    "Red Tornado" to darkColorScheme(
        primary = Color(0xFFEF9A9A),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFFB71C1C),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFF8BBD0),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFFAD1457),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFFFCE4EC),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFFC2185B),
        onTertiaryContainer = Color(0xFFFFFFFF),
        inversePrimary = Color(0xFF694747),
    )
)