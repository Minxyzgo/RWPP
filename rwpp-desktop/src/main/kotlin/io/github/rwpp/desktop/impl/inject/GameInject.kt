/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject

import com.corrodinggames.rts.game.i
import io.github.rwpp.appKoin
import io.github.rwpp.desktop.GameEngine
import io.github.rwpp.desktop._gameSpeed
import io.github.rwpp.game.Game
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.game.units.comp.EntityRangeUnitComp
import io.github.rwpp.game.units.comp.EntityRangeUnitComp.Companion.entityRangeShader
import io.github.rwpp.game.world.World
import io.github.rwpp.graphics.ShaderProgram
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.utils.Reflect
import org.lwjgl.opengl.GL11

@InjectClass(i::class)
object GameInject {
    val room by lazy {
        appKoin.get<Game>().gameRoom
    }

    private val game: Game by lazy { appKoin.get<Game>() }

    private val world: World by lazy { appKoin.get<Game>().world }

    private val allyUnitToDrawListCache: MutableList<GameUnit> = mutableListOf()
    private val enemyUnitToDrawListCache: MutableList<GameUnit> = mutableListOf()

    val allOnScreenUnits: com.corrodinggames.rts.gameFramework.utility.s by lazy {
        Reflect.reifiedGet<i, com.corrodinggames.rts.gameFramework.utility.s>(GameEngine.B() as i?, "W")!!
    }

    @Inject("x", InjectMode.Override)
    fun noBackground() {

    }

    @Inject("b", InjectMode.InsertBefore)
    fun updateAndRender(deltaSpeed: Float) {
        if (room.isHost && _gameSpeed != 1f) {
            GameEngine.B().bX.K = 1f / _gameSpeed
            GameEngine.B().bX.a(1f / _gameSpeed, "speed")
            (GameEngine.B() as i).H = _gameSpeed
        } else {
            (GameEngine.B() as i).H = 1f
            GameEngine.B().bX.K = null
        }
    }

    @Inject("b", InjectMode.InsertAfter)
    fun injectRenderObject(draw: com.corrodinggames.rts.gameFramework.m.l, delta: Float) {
        val engine =  GameEngine.B()
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT)

        GL11.glDepthRange(0.0, 0.01)
        GL11.glDepthFunc(GL11.GL_LESS)

        val unitsSnapShot = allOnScreenUnits.a()

        engine.R() //project camera

        allyUnitToDrawListCache.clear()
        enemyUnitToDrawListCache.clear()

        entityRangeShader.bind()
        unitsSnapShot.forEach { unit ->
            if (unit is GameUnit) {
                if (game.gameRoom.localPlayer.team == unit.player.team) {
                    allyUnitToDrawListCache.add(unit)
                } else {
                    enemyUnitToDrawListCache.add(unit)
                }
            }
        }
        allyUnitToDrawListCache.forEach { EntityRangeUnitComp.drawRange(it, it.maxAttackRange) }
        enemyUnitToDrawListCache.forEach { EntityRangeUnitComp.drawRange(it, it.maxAttackRange) }
        ShaderProgram.unbind()

        GL11.glDepthRange(0.0, 1.0)
        GL11.glDepthFunc(GL11.GL_LEQUAL) // 还原默认
        GL11.glDisable(GL11.GL_DEPTH_TEST)
    }
}