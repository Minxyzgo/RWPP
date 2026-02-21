/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl

import androidx.compose.ui.graphics.Color
import io.github.rwpp.game.base.BaseFactory
import io.github.rwpp.game.base.GamePaint

fun BaseFactory.createPaint(color: Color, style: GamePaint.Style): GamePaint {
    return createPaint(
        colorFloatToInt(color.alpha),
        colorFloatToInt(color.red),
        colorFloatToInt(color.green),
        colorFloatToInt(color.blue),
        style
    )
}

@Suppress("NOTHING_TO_INLINE")
private inline fun colorFloatToInt(floatValue: Float): Int {
    val clamped = floatValue.coerceIn(0.0f, 1.0f)
    return (clamped * 255.0f + 0.5f).toInt()
}