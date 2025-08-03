/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.graphics.Paint
import io.github.rwpp.game.base.BaseFactory
import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.game.base.Rect
import org.koin.core.annotation.Single

@Single
class BaseFactoryImpl : BaseFactory {
    override fun createPaint(
        argb: Int,
        style: GamePaint.Style
    ): GamePaint {
        val paint = Paint()
        paint.color = argb
        paint.style = Paint.Style.entries[style.ordinal]
        return GamePaintImpl(paint)
    }

    override fun createRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ): Rect {
        return RectImpl(android.graphics.Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt()))
    }
}