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
import io.github.rwpp.game.units.comp.EntityRangeUnitComp
import io.github.rwpp.graphics.GL
import io.github.rwpp.graphics.GL20
import io.github.rwpp.graphics.GLConstants
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

@InjectClass(i::class)
object GameInject {
    val room by lazy {
        appKoin.get<Game>().gameRoom
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
        with(EntityRangeUnitComp) {
            GL20.glEnable(GLConstants.GL_DEPTH_TEST)
            GL20.glDepthMask(true)
            GL20.glClear(GLConstants.GL_DEPTH_BUFFER_BIT)

            GL20.glDepthRange(0.0, 1.0)
            GL20.glDepthFunc(GLConstants.GL_LESS)
            beforeDrawRange()
            entityRangeShader?.bind()
            GL.gameCanvas.scale(world.gameScale, world.gameScale)
            layerGroups.values.forEach { group ->
                group.forEach { GL.gameCanvas.drawRange(it, getTeamPaint(it.player.team), it.maxAttackRange) }
            }
            entityRangeShader?.unbind()
            GL20.glDepthFunc(GLConstants.GL_LEQUAL) // 还原默认
            GL20.glDisable(GLConstants.GL_DEPTH_TEST)
        }
    }
}