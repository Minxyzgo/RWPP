/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.graphics.Paint
import android.graphics.RectF
import com.corrodinggames.rts.game.i
import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.game.base.Rect
import io.github.rwpp.game.units.GameObject
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.game.world.World
import io.github.rwpp.utils.Reflect

class WorldImpl : World {
    override val cameraX: Float
        get() = GameEngine.t().ct
    override val cameraY: Float
        get() = GameEngine.t().cu
    override val flame: Int
        get() = GameEngine.t().bu
    override val zoom: Float
        get() = GameEngine.t().cS
    override val gameScale: Float
        get() = GameEngine.t().cU
    override val selectedUnits: List<GameUnit>
        get() = GameEngine.t().bP.bZ as List<GameUnit>

    override fun selectUnit(unit: GameUnit) {
        GameEngine.t().bP.c(unit as com.corrodinggames.rts.game.units.ce)
    }

    override fun unselectUnit(unit: GameUnit) {
        GameEngine.t().bP.d(unit as com.corrodinggames.rts.game.units.ce)
    }

    override fun clearSelectedUnits() {
        GameEngine.t().bP.h()
    }

    private val allOnScreenUnits: com.corrodinggames.rts.gameFramework.utility.v by lazy {
        Reflect.reifiedGet<i, com.corrodinggames.rts.gameFramework.utility.v>(GameEngine.t() as i?, "V")!!
    }

    override fun projectionCamera() {
        GameEngine.t().I()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAllObjectOnScreen(): List<GameObject> {
        return allOnScreenUnits as List<GameObject>
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAllObject(): List<GameObject> {
        return com.corrodinggames.rts.gameFramework.ah.et as List<GameObject>
    }


    override fun drawText(
        text: String,
        x: Float,
        y: Float,
        paint: GamePaint
    ) {
        GameEngine.t().bL.a(text, x, y, paint as Paint)
    }

    override fun drawRect(rect: Rect, paint: GamePaint) {
        GameEngine.t().bL.a(rect as RectF, paint as Paint)
    }

    override fun drawCircle(
        x: Float,
        y: Float,
        radius: Float,
        paint: GamePaint
    ) {
        GameEngine.t().bL.a(x, y, radius, (paint as GamePaintImpl).paint)
    }

    override fun drawLine(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        paint: GamePaint
    ) {
        GameEngine.t().bL.a(startX, startY, endX, endY, (paint as GamePaintImpl).paint)
    }
}