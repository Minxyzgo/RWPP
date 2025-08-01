/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("unused", "UnusedReceiverParameter", "UNUSED_PARAMETER")

package io.github.rwpp.desktop.impl.inject

import com.corrodinggames.rts.gameFramework.j.c
import com.corrodinggames.rts.gameFramework.l
import com.corrodinggames.rts.java.Main
import io.github.rwpp.appKoin
import io.github.rwpp.config.Settings
import io.github.rwpp.desktop.GameEngine
import io.github.rwpp.desktop.impl.RwOutputStream
import io.github.rwpp.desktop.main
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.RefreshUIEvent
import io.github.rwpp.event.events.StartGameEvent
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.utils.Reflect
import io.github.rwpp.welcomeMessage

@InjectClass(Main::class)
object MainInject {
    @Inject("c", InjectMode.Override)
    fun onRefreshUI() {
        RefreshUIEvent().broadcastIn()
    }

    @Inject("c", InjectMode.Override)
    fun onPlayerJoin(
        client: c, m1: String?, m2: String?
    ) {
        if(appKoin.get<Settings>().showWelcomeMessage != true) return
        val rwOutputStream = RwOutputStream()
        rwOutputStream.c(welcomeMessage)
        rwOutputStream.c(3)
        rwOutputStream.b("RWPP")
        rwOutputStream.a(null as c?)
        rwOutputStream.a(-1)
        GameEngine.B().bX.a(client, rwOutputStream.b(141))
    }

    @Inject("b", InjectMode.Override)
    fun Main.onStartGame() {
        val f = Reflect.get<com.corrodinggames.rts.gameFramework.utility.aj>(this, "f")!!
        f.a(Runnable
        {
            val B: l = GameEngine.B()
            com.corrodinggames.rts.appFramework.n::class.java.getDeclaredMethod("r").invoke(null)
            if(B.bL == null || !B.bL.W) {
                B.bX.af()
                return@Runnable
            }
            B.bX.bd = true
            B.bH = false
            B.aq = false
            val p = Reflect.get<com.corrodinggames.rts.java.d.a>(this, "p")!!
            val i = Reflect.get<com.corrodinggames.rts.java.b.a>(this, "i")!!
            i.c(false)
            com.corrodinggames.librocket.a.a().f()
            if(p.c != null) {
                p.c.root.resumeNonMenu()
                return@Runnable
            }
            GameEngine.T()
        })

        StartGameEvent().broadcastIn()
    }
}
