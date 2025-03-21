/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject

import io.github.rwpp.appKoin
import io.github.rwpp.desktop.defeatedPlayerSet
import io.github.rwpp.desktop.gameOver
import io.github.rwpp.desktop.impl.GameEngine
import io.github.rwpp.desktop.impl.PlayerInternal
import io.github.rwpp.desktop.isGaming
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.GameOverEvent
import io.github.rwpp.event.events.PlayerDefeatedEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.Player
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.logger

@InjectClass(PlayerInternal::class)
object PlayerInject {
    @Inject("a", InjectMode.InsertBefore)
    fun PlayerInternal.onUpdate(deltaTime: Float): Any {
        if (!isGaming || defeatedPlayerSet.contains(this)) return Unit

        this as Player

        //当玩家失败时，将其加入到已失败玩家集合中，并广播玩家失败事件
        if (isDefeated) {
            defeatedPlayerSet.add(this)
            PlayerDefeatedEvent(this).broadcastIn()

            var lastTeam: Int? = null

            if (gameOver) return Unit

            for (i in 0..<game.gameRoom.maxPlayerCount) {
                val p = PlayerInternal.k(i)

                if (p != null && !p.b() && !p.G && !p.F && !p.E) {
                    if (lastTeam != null && lastTeam != p.r) return Unit
                    lastTeam = p.r
                }
            }

            //当不存在其它队伍时，游戏结束
            if (lastTeam != null) {
                logger.info("Game Over! Team $lastTeam wins!")
                GameOverEvent(GameEngine.B().by, lastTeam).broadcastIn()
                gameOver = true
            }
        }

        return Unit
    }

    private val game by lazy { appKoin.get<Game>() }
}