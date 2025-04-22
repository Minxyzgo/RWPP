/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.comp

import io.github.rwpp.appKoin
import io.github.rwpp.config.Settings
import io.github.rwpp.game.Game
import io.github.rwpp.game.base.BaseFactory
import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.game.base.Rect
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.game.units.MovementType
import io.github.rwpp.game.world.World
import io.github.rwpp.widget.argb
import org.koin.core.annotation.Factory
import kotlin.math.ceil

@Factory
open class UnitComp {
    open fun onDraw(unit: GameUnit, delta: Float) {
        if (!unit.isDead && unit.maxAttackRange > 70) {
            if (settings.showUnitTargetLine
                && (game.gameRoom.isSinglePlayerGame || game.gameRoom.localPlayer.team == unit.player.team)
                && unit.target != null
                && unit.target?.isDead != true) {
                world.drawLine(unit.x - world.cx, unit.y - world.cy, unit.target!!.x - world.cx, unit.target!!.y - world.cy, getTeamPaint(unit))
            }

            val condition = when (unit.type.movementType) {
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
            if (condition) {
                drawRange(unit, unit.maxAttackRange)
            }
        }
    }

    open fun onDrawBar(unit: GameUnit, rect: Rect, paint: GamePaint) {
        var realPaint = paint

        //以颜色判断是否在绘制血条
        if ((paint.argb == argb1 || paint.argb == argb2) && settings.improvedHealthBar) {
            val percentage = ceil(unit.health) / unit.maxHealth
            realPaint = when (percentage) {
                in 0.6..1.0 -> paint
                in 0.3..0.6 -> paintYellow
                else -> paintBlack
            }
        }

        world.drawRect(rect, realPaint)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getTeamPaint(target: GameUnit): GamePaint {
        return if (game.gameRoom.localPlayer.team != target.player.team) paintRedStroke else paintGreenStroke
    }

    private fun drawRange(unit: GameUnit, range: Float) {
        world.drawCircle(unit.x - world.cx, unit.y - world.cy, range, getTeamPaint(unit))
    }

    companion object {
        val paintBlack by lazy { factory.createPaint(255, 0, 0, 0, GamePaint.Style.FILL) }
        val paintYellow by lazy { factory.createPaint(200, 237, 145, 33, GamePaint.Style.FILL) }
        val paintGreenStroke by lazy { factory.createPaint(200, 144, 238, 144, GamePaint.Style.STROKE) }
        val paintRedStroke by lazy { factory.createPaint(200, 255, 0, 0, GamePaint.Style.STROKE) }

        // 单位血条 敌我的颜色
        private val argb1 = argb(200, 183, 44, 44)
        private val argb2 = argb(200, 0, 150, 0)

        private val game: Game by lazy { appKoin.get<Game>() }
        private val world: World by lazy { game.world }
        private val settings by lazy { appKoin.get<Settings>() }
        private val factory by lazy { appKoin.get<BaseFactory>() }
    }
}