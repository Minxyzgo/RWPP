/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import io.github.rwpp.shared.generated.resources.*
import io.github.rwpp.shared.generated.resources.Delicious_Bold
import io.github.rwpp.shared.generated.resources.Delicious_BoldItalic
import io.github.rwpp.shared.generated.resources.Delicious_Italic
import io.github.rwpp.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun deliciousFonts(): FontFamily {
    return FontFamily(
        Font(Res.font.Delicious_Bold, FontWeight.Bold),
        Font(Res.font.Delicious_BoldItalic, FontWeight.Bold, FontStyle.Italic),
        Font(Res.font.Delicious_Italic, FontWeight.Normal, FontStyle.Italic)
    )
}

@Composable
fun JostFonts(): FontFamily {
    return FontFamily(
        Font(Res.font.Jost_Regular, FontWeight.Normal),
        Font(Res.font.Jost_Thin, FontWeight.Thin),
        Font(Res.font.Jost_Light, FontWeight.Light),
        Font(Res.font.Jost_LightItalic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.Jost_ExtraLight, FontWeight.ExtraLight),
        Font(Res.font.Jost_ExtraLightItalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(Res.font.Jost_Medium, FontWeight.Medium),
        Font(Res.font.Jost_MediumItalic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.Jost_ThinItalic, FontWeight.Thin, FontStyle.Italic),
        Font(Res.font.Jost_BoldItalic, FontWeight.Bold, FontStyle.Italic),
        Font(Res.font.Jost_Italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.Jost_ExtraBold, FontWeight.ExtraBold, FontStyle.Normal),
        Font(Res.font.Jost_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.Jost_SemiBoldItalic, FontWeight.SemiBold, FontStyle.Italic),
        Font(Res.font.Jost_ExtraBoldItalic, FontWeight.ExtraBold, FontStyle.Italic)
    )
}

@Composable
fun ValoraxFont(): FontFamily =
    FontFamily(
        Font(Res.font.Valorax_lg25V, FontWeight.Normal)
    )
