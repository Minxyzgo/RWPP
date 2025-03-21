/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject

import android.graphics.Paint
import android.graphics.`Paint$Style`
import android.graphics.RectF
import com.corrodinggames.rts.game.units.am
import com.corrodinggames.rts.gameFramework.l
import io.github.rwpp.appKoin
import io.github.rwpp.config.Settings
import io.github.rwpp.desktop.impl.GameEngine
import io.github.rwpp.desktop.impl.Graphics
import io.github.rwpp.game.Game
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.game.units.MovementType
import io.github.rwpp.inject.*
import kotlin.math.ceil


@InjectClass(am::class)
object UnitInject {
    val colorA = Graphics.b(200, 183, 44, 44)
    val colorB = Graphics.b(200, 0, 150, 0)
    val paintRed = com.corrodinggames.rts.gameFramework.utility.y.a(Graphics.b(255, 0, 0, 0), `Paint$Style`.a)
    val paintYellow = com.corrodinggames.rts.gameFramework.utility.y.a(Graphics.b(237, 145, 33, 0), `Paint$Style`.a)

    val paintRedStroke = com.corrodinggames.rts.gameFramework.utility.y.a(Graphics.b(237, 145, 33, 0), `Paint$Style`.b)

    @Inject("p", InjectMode.InsertBefore)
    fun am.onDraw(delta: Float) {
        this as GameUnit
        if (!isDead && /*this.cN == null && this.cG &&*/ this is com.corrodinggames.rts.game.units.y && this.m() > 70) {
            val condition = when (this.type.movementType) {
                MovementType.BUILDING, MovementType.NONE -> {
                    settings.showBuildingAttackRange
                }

                MovementType.LAND, MovementType.OVER_CLIFF, MovementType.OVER_CLIFF_WATER, MovementType.HOVER-> {
                    settings.showAttackRangeUnit == "Land" || settings.showAttackRangeUnit == "All"
                }

                MovementType.AIR -> {
                    settings.showAttackRangeUnit == "Air" || settings.showAttackRangeUnit == "All"
                }

                MovementType.WATER -> {
                    settings.showAttackRangeUnit == "All"
                }
            }
            if (condition) drawAttackRange(
                this,
                this.m(),
                true,
                false
            )
        }
    }


    @RedirectMethod(
        "a",
        "(FZ)V",
        "com.corrodinggames.rts.gameFramework.m.y",
        "a"
    )
    @Suppress("TYPE_MISMATCH")
    fun am.onDrawStoke(
        rectF: RectF, param: Paint
    ) {
        //HP: cu/cv
        //val health = cj * 2 * x()
        //println(param.e().toString() + "h: $health")
        val B = GameEngine.B()
        var param1 = param
        if (param.e() == colorA || param.e() == colorB) {
            val percentage = ceil(cu) / cv
            param1 = when (percentage) {
                in 0.6..1.0 -> param
                in 0.3..0.6 -> paintYellow
                else -> paintRed
            }

            //setZoom
//            if (B.cV >= 1) {
//                val f3 = this.cj
//                val f4: Float = this.eo - B.cw
//                val f5: Float = (this.ep - B.cx) - this.eq
//                val f6: Float = f3 + 4.0f
//                GameEngine.B().bO.a(
//                    "1",
//                    f4 - f3,
//                    f5 + f6,
//                    com.corrodinggames.rts.gameFramework.utility.y.a(Graphics.b(255, 0, 0, 0), `Paint$Style`.a)
//                )
//            }
        }

        GameEngine.B().bO.a(rectF, param1)
    }

    private fun drawAttackRange(amVar: am, f2: Float, z: Boolean, z2: Boolean) {
        val B: l = GameEngine.B()
        if (com.corrodinggames.rts.gameFramework.utility.y.a(amVar) || z) {
            val f3 = amVar.eo - B.cw
            val f4 = amVar.ep - B.cx
            var paint = am.dg
            if (z2) {
                paint = am.dh
            }
            B.bO.a(f3, f4, f2, if (game.gameRoom.localPlayer.team != (amVar as GameUnit).player.team) paintRedStroke else paint)
        }
    }


    private val game = appKoin.get<Game>()
    private val settings = appKoin.get<Settings>()
}