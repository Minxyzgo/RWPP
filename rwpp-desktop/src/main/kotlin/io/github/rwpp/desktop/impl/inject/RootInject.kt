/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("unused", "UnusedReceiverParameter")

package io.github.rwpp.desktop.impl.inject

import com.corrodinggames.librocket.scripts.Root
import com.corrodinggames.librocket.scripts.ScriptContext
import com.corrodinggames.librocket.scripts.ScriptEngine
import io.github.rwpp.inject.Inject
import io.github.rwpp.appKoin
import io.github.rwpp.desktop.*
import io.github.rwpp.desktop.impl.GameEngine
import io.github.rwpp.desktop.isGaming
import io.github.rwpp.desktop.isSandboxGame
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.ChatMessageEvent
import io.github.rwpp.event.events.ReturnMainMenuEvent
import io.github.rwpp.game.Game
import io.github.rwpp.inject.InjectClass
import javax.swing.SwingUtilities

@InjectClass(Root::class)
object RootInject {
    @Inject("showMainMenu")
    fun onShowMainMenu() {
        if(isGaming) {
            if(isSandboxGame) appKoin.get<Game>().gameRoom.disconnect()
            GameEngine.B().bS.u = false
            gameCanvas.isVisible = false
            rwppVisibleSetter(true)
            isGaming = false
            com.corrodinggames.librocket.a.a().b()
            val libRocket = ScriptContext::class.java.getDeclaredField("libRocket").run {
                isAccessible = true
                get(ScriptEngine.getInstance().root)
            } as com.corrodinggames.librocket.b
            libRocket.closeActiveDocument()
            libRocket.clearHistory()

            ReturnMainMenuEvent().broadCastIn()
        }
    }

    @Inject("showBattleroom")
    fun onShowBattleroom() {
        if(isGaming) {
            GameEngine.B().bS.u = false
            gameCanvas.isVisible = false
            rwppVisibleSetter(true)
            isGaming = false
            val libRocket = ScriptContext::class.java.getDeclaredField("libRocket").run {
                isAccessible = true
                get(ScriptEngine.getInstance().root)
            } as com.corrodinggames.librocket.b
            libRocket.closeActiveDocument()
            libRocket.clearHistory()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Inject("receiveChatMessage")
    fun onReceiveChatMessage(
        spawn: Int, s: String?, s1: String?, sender: com.corrodinggames.rts.gameFramework.j.c?
    ) {
        if(playerCacheMap.isEmpty()) appKoin.get<Game>().gameRoom.getPlayers()
        ChatMessageEvent(s ?: "", s1 ?: "", spawn).broadCastIn()
    }

    @Inject("makeSendMessagePopup")
    fun onMakeSendMessagePopup() {
        SwingUtilities.invokeLater {
            showSendMessageDialog()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Inject("makeSendTeamMessagePopupWithDefaultText")
    fun onMakeSendTeamMessagePopupWithDefaultText(str: String) {
        SwingUtilities.invokeLater {
            showSendMessageDialog()
        }
    }
}