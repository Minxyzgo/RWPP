/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import io.github.rwpp.android.defeatedPlayerSet
import io.github.rwpp.android.gameOver
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.android.impl.PlayerInternal
import io.github.rwpp.android.isGaming
import io.github.rwpp.appKoin
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.GameOverEvent
import io.github.rwpp.event.events.PlayerDefeatedEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.Player
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.logger

@InjectClass(com.corrodinggames.rts.game.p::class)
object PlayerInject {
    @Inject("a", InjectMode.InsertBefore)
    fun com.corrodinggames.rts.game.p.onUpdate(delta: Float) {
        if ((I || J) && isGaming && !defeatedPlayerSet.contains(this)) {
            defeatedPlayerSet.add(this)
            PlayerDefeatedEvent(this as Player).broadcastIn()

            var lastTeam: Int? = null

            if (gameOver) return

            for (i in 0..<game.gameRoom.maxPlayerCount) {
                val p = PlayerInternal.j[i]

                if (p != null && !p.a() && !p.J && !p.I && !p.H) {
                    if (lastTeam != null && lastTeam != p.s) return
                    lastTeam = p.s
                }
            }

            if (lastTeam != null) {
                logger.info("Game Over! Team $lastTeam wins!")
                GameOverEvent(GameEngine.t().bv, lastTeam).broadcastIn()
                gameOver = true
            }
        }
    }

    private val game: Game by appKoin.inject()
}