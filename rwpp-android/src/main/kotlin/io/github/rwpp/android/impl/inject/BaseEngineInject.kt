/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import com.corrodinggames.rts.game.i
import io.github.rwpp.android._gameSpeed
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.android.mainThreadChannel
import io.github.rwpp.appKoin
import io.github.rwpp.game.Game
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.utils.Reflect

@InjectClass(com.corrodinggames.rts.game.i::class)
object BaseEngineInject {
    val room by lazy { appKoin.get<Game>().gameRoom }
    @Inject("b", InjectMode.InsertBefore)
    fun onUpdate(deltaTime: Float) {
        mainThreadChannel.tryReceive().getOrNull()?.invoke()
        if (room.isHost && _gameSpeed != 1f) {
            GameEngine.t().bU.K = 1f / _gameSpeed
            GameEngine.t().bU.M = 1f / _gameSpeed
            (GameEngine.t() as i).G = _gameSpeed
        } else {
            GameEngine.t().bU.M = 1f
            (GameEngine.t() as i).G = 1f
        }
    }
}