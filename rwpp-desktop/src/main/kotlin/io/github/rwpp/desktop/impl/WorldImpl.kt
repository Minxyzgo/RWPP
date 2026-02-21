/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import android.graphics.Paint
import android.graphics.RectF
import com.corrodinggames.rts.game.i
import io.github.rwpp.desktop.GameEngine
import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.game.base.Rect
import io.github.rwpp.game.units.GameObject
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.game.world.World
import io.github.rwpp.utils.Reflect

class WorldImpl : World {

    override val cameraX: Float
        get() = GameEngine.B().cw
    override val cameraY: Float
        get() = GameEngine.B().cx
    override val flame: Int
        get() = GameEngine.B().bx
    override val zoom: Float
        get() = GameEngine.B().cV
    override val gameScale: Float
        get() = GameEngine.B().cX
    override val selectedUnits: List<GameUnit>
        get() = GameEngine.B().bS.bZ as List<GameUnit>

    override fun selectUnit(unit: GameUnit) {
        GameEngine.B().bS.k(unit as com.corrodinggames.rts.game.units.am)
    }

    override fun unselectUnit(unit: GameUnit) {
        GameEngine.B().bS.l(unit as com.corrodinggames.rts.game.units.am)
    }

    override fun clearSelectedUnits() {
        GameEngine.B().bS.y()
    }

    private val allOnScreenUnits: com.corrodinggames.rts.gameFramework.utility.s by lazy {
        Reflect.reifiedGet<i, com.corrodinggames.rts.gameFramework.utility.s>(GameEngine.B() as i?, "W")!!
    }

    override fun projectionCamera() {
        GameEngine.B().R()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAllObjectOnScreen(): List<GameObject> {
        return allOnScreenUnits as List<GameObject>
    }

    @Suppress("unchecked_cast")
    override fun getAllObject(): List<GameObject> {
        return com.corrodinggames.rts.gameFramework.w.er as List<GameObject>
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