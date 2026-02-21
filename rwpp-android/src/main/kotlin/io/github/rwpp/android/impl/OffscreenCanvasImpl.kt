/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import GameCanvas
import android.graphics.Canvas
import io.github.rwpp.android.getPaint
import io.github.rwpp.android.getRect
import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.game.base.Rect

class OffscreenGameCanvasImpl(var canvas: Canvas) : GameCanvas {
    override fun drawText(
        text: String,
        x: Float,
        y: Float,
        paint: GamePaint
    ) {
        canvas.drawText(text, x, y, getPaint(paint))
    }

    override fun drawRect(
        rect: Rect,
        paint: GamePaint
    ) {
        canvas.drawRect(getRect(rect), getPaint(paint))
    }

    override fun drawCircle(
        x: Float,
        y: Float,
        radius: Float,
        paint: GamePaint
    ) {
        canvas.drawCircle(x, y, radius, getPaint(paint))
    }

    override fun drawLine(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        paint: GamePaint
    ) {
        canvas.drawLine(startX, startY, endX, endY, getPaint(paint))
    }

    override fun scale(scaleX: Float, scaleY: Float) {
        canvas.scale(scaleX, scaleY)
    }
}