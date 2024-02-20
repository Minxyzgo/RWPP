/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package io.github.rwpp.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import io.github.rwpp.android.R


@Composable
actual fun deliciousFonts(): FontFamily {
    return FontFamily(
        Font(R.font.deliciousbold, FontWeight.Bold),
        Font(R.font.deliciousbolditalic, FontWeight.Bold, FontStyle.Italic),
        Font(R.font.deliciousitalic, FontWeight.Normal, FontStyle.Italic)
    )
}