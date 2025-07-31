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
}