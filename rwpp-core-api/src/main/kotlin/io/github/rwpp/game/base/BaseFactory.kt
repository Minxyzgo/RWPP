/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.base

import io.github.rwpp.utils.parseColorToArgb


interface BaseFactory {
    fun createPaint(color: String, style: GamePaint.Style): GamePaint {
        val argb = parseColorToArgb(color)
        return createPaint(argb, style)
    }

    fun createPaint(
        a: Int, r: Int, g: Int, b: Int,
        style: GamePaint.Style
    ): GamePaint {
        val argb = a shl 24 or (r shl 16) or (g shl 8) or b
        return createPaint(argb, style)
    }

    fun createPaint(argb: Int, style: GamePaint.Style): GamePaint

    fun createRect(left: Float, top: Float, right: Float, bottom: Float): Rect
}