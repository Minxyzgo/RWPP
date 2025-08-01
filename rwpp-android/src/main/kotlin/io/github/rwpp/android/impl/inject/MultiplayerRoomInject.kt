/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import com.corrodinggames.rts.appFramework.MultiplayerBattleroomActivity
import com.corrodinggames.rts.gameFramework.h.a
import io.github.rwpp.android.isGaming
import io.github.rwpp.android.questionOption
import io.github.rwpp.appKoin
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.RefreshUIEvent
import io.github.rwpp.game.Game
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult
import io.github.rwpp.ui.UI

@InjectClass(MultiplayerBattleroomActivity::class)
object MultiplayerRoomInject {
    @Inject("updateUI", InjectMode.Override)
    fun onUpdateUI() {
        RefreshUIEvent().broadcastIn()
    }

    @Inject("startGame", InjectMode.Override)
    fun onStartGame() {
        if(!isGaming) appKoin.get<Game>().gameRoom.startGame()
    }

    @Inject("askPasswordInternal", InjectMode.InsertBefore)
    fun onAskPassword(ao: com.corrodinggames.rts.gameFramework.j.ao?): Any? {
        if (ao == null) return Unit
        if(questionOption != null) {
            ao.a(questionOption)
            questionOption = null
            return InterruptResult()
        }

        val message = ao.b?.let { a.b(it) }

        if(message == "Search units by internal name or text title.") return Unit

        UI.showQuestion(
            if(ao.b != null)
                "Server Question"
            else ao.e ?: "Password Required",
            message ?: "This server requires a password to join"
        ) {
            if (it == null) {
                val game = appKoin.get<Game>()
                game.cancelJoinServer()
                ao.a()
            } else {
                ao.a(it)
            }
        }

        return InterruptResult()
    }
}