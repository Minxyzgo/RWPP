/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.units.comp

import GameCanvas
import io.github.rwpp.AppContext
import io.github.rwpp.appKoin
import io.github.rwpp.config.Settings
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.events.DisconnectEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.Player
import io.github.rwpp.game.base.BaseFactory
import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.game.base.Rect
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.game.units.MovementType
import io.github.rwpp.game.world.World
import io.github.rwpp.graphics.ShaderProgram
import io.github.rwpp.impl.createPaint
import io.github.rwpp.logger
import io.github.rwpp.ui.color.getTeamColor
import io.github.rwpp.utils.argb
import org.koin.core.annotation.Factory
import kotlin.math.ceil

@Factory(binds = [UnitComp::class])
open class EntityRangeUnitComp : UnitComp {
    var showAttackRange: Boolean = false

    // TODO 统一 GamePaint和Paint

    // showUnitWayPoint
    override fun onDraw(unit: GameUnit, delta: Float) {
        if (!unit.isDead && unit.maxAttackRange > 70) {
            if (settings.showUnitTargetLine
                && (game.gameRoom.isSinglePlayerGame || game.gameRoom.localPlayer.team == unit.player.team)
                && unit.target != null
                && unit.target?.isDead != true
            ) {
                world.drawLine(
                    unit.x - world.cameraX,
                    unit.y - world.cameraY,
                    unit.target!!.x - world.cameraX,
                    unit.target!!.y - world.cameraY,
                    paintBlack
                )
            }
        }
    }

    override fun onDrawBar(unit: GameUnit, rect: Rect, paint: GamePaint) {
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

    companion object {
        val paintBlack by lazy { factory.createPaint(255, 0, 0, 0, GamePaint.Style.FILL) }
        val paintYellow by lazy { factory.createPaint(200, 237, 145, 33, GamePaint.Style.FILL) }

        private val argb1 = argb(200, 183, 44, 44)
        private val argb2 = argb(200, 0, 150, 0)

        val game: Game by lazy { appKoin.get<Game>() }
        val world: World by lazy { game.world }
        val settings by lazy { appKoin.get<Settings>() }
        val factory by lazy { appKoin.get<BaseFactory>() }
        val app by lazy { appKoin.get<AppContext>() }

        val layerGroups: MutableMap<Int, ArrayList<GameUnit>> = mutableMapOf()

        var entityRangeShader: ShaderProgram? = null

        val teamPaintMap = mutableMapOf<Int, GamePaint>()

        // 目前仅PC可跑
        val vertexSource = """
            #version 120

            uniform float uPriority;

            void main() {
                vec4 pos = gl_ModelViewProjectionMatrix * gl_Vertex;
                
                gl_Position = vec4(pos.xy, uPriority * pos.w, pos.w);
                
                gl_TexCoord[0] = gl_MultiTexCoord0;
                gl_FrontColor = gl_Color;
            }
        """.trimIndent()

        fun GameCanvas.drawRange(unit: GameUnit, paint: GamePaint, range: Float) {
            if (!unit.isDead && unit.maxAttackRange > 70) {
                val condition = when (unit.type.movementType) {
                    MovementType.BUILDING, MovementType.NONE -> {
                        settings.showBuildingAttackRange
                    }

                    MovementType.LAND, MovementType.OVER_CLIFF, MovementType.OVER_CLIFF_WATER, MovementType.HOVER -> {
                        settings.showAttackRangeUnit == "Land" || settings.showAttackRangeUnit == "All"
                    }

                    MovementType.AIR -> {
                        settings.showAttackRangeUnit == "Air" || settings.showAttackRangeUnit == "All"
                    }

                    MovementType.WATER -> {
                        settings.showAttackRangeUnit == "All"
                    }
                }
                val comp = unit.comp.first { it is EntityRangeUnitComp } as EntityRangeUnitComp
                if (condition || comp.showAttackRange) {
                    val priority =
                        if (game.gameRoom.localPlayer.team != unit.player.team) 0.1f else 0.8f
                    entityRangeShader?.setUniform1f("uPriority", priority)
                    drawCircle(
                        unit.x - world.cameraX,
                        unit.y - world.cameraY,
                        range,
                        paint
                    )
                }
            }
        }

        fun beforeDrawRange() {
            val unitsSnapShot = world.getAllObjectOnScreen()
            layerGroups.values.forEach { it.clear() }
            unitsSnapShot.forEach { unit ->
                if (unit is GameUnit) {
                    layerGroups.getOrPut(unit.player.team, { arrayListOf() }).add(unit)
                }
            }
        }

        @Suppress("MemberVisibilityCanBePrivate")
        fun getTeamPaint(team: Int): GamePaint {
            val teamPaint = teamPaintMap.getOrPut(
                team
            ) {
                factory.createPaint(Player.getTeamColor(team % 10).copy(alpha = .2f), GamePaint.Style.FILL)
            }
            return teamPaint
        }

        init {
            logger.info("try loading shaders")
            if (app.isDesktop()) {
                entityRangeShader = ShaderProgram.loadShaderFragFromString(null, vertexSource)
            }
            GlobalEventChannel.filter(DisconnectEvent::class).subscribeAlways {
                layerGroups.clear()
            }
        }
    }
}