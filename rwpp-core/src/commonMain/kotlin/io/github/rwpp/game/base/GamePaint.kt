/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.base

import io.github.rwpp.ui.parseColorToArgb

data class GamePaint(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int = 255,
    val style: Style = Style.FILL
) {
    enum class Style {
        FILL,
        STROKE,
        FILL_AND_STROKE
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun newColor(
            color: String,
            style: Style = Style.FILL
        ): GamePaint {
            val argb = parseColorToArgb(color)
            val r = (argb shr 16) and 0xFF
            val g = (argb shr 8) and 0xFF
            val b = argb and 0xFF
            return GamePaint(r, g, b, style = style)
        }
    }
}
