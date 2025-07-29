/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import android.graphics.Paint
import android.graphics.RectF
import io.github.rwpp.desktop.GameEngine
import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.game.base.Rect
import io.github.rwpp.game.world.World

class WorldImpl : World {
    override val cx: Float
        get() = GameEngine.B().cw
    override val cy: Float
        get() = GameEngine.B().cx

    override fun getAllUnits(): List<Unit> {
        TODO("Not yet implemented")
    }

    override fun drawText(text: String, x: Float, y: Float, paint: GamePaint) {
        GameEngine.B().bO.a(text, x, y, paint as Paint)
    }

    override fun drawRect(rect: Rect, paint: GamePaint) {
        GameEngine.B().bO.a(rect as RectF, paint as Paint)
    }

    override fun drawCircle(x: Float, y: Float, radius: Float, paint: GamePaint) {
        GameEngine.B().bO.a(x, y, radius, paint as Paint)
    }

    override fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float, paint: GamePaint) {
        GameEngine.B().bO.a(startX, startY, endX, endY, paint as Paint)
    }
}