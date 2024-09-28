/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import com.corrodinggames.rts.appFramework.GameView
import com.corrodinggames.rts.appFramework.GameViewOpenGL
import com.corrodinggames.rts.appFramework.GameViewThreaded
import com.corrodinggames.rts.appFramework.MultiplayerBattleroomActivity
import com.corrodinggames.rts.gameFramework.h.a
import com.corrodinggames.rts.gameFramework.j.ao
import com.github.minxyzgo.rwij.InjectMode
import com.github.minxyzgo.rwij.InterruptResult
import com.github.minxyzgo.rwij.setFunction
import io.github.rwpp.android.*
import io.github.rwpp.android.impl.proxy.NetProxy
import io.github.rwpp.android.impl.proxy.UnitPathProxy
import io.github.rwpp.appKoin
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.*
import io.github.rwpp.game.Game
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.net.Net
import io.github.rwpp.net.packets.ModPacket
import java.util.concurrent.ConcurrentLinkedQueue


fun doProxy() {

    UnitPathProxy
    NetProxy

    @Suppress("unchecked_cast")
    MultiplayerBattleroomActivity::class.setFunction {
        val messagesField = com.corrodinggames.rts.gameFramework.j.a::class.java.getDeclaredField("a").apply {
            isAccessible = true
        }
        val spawnField = com.corrodinggames.rts.gameFramework.j.b::class.java.getDeclaredField("a").apply {
            isAccessible = true
        }
        val senderField = com.corrodinggames.rts.gameFramework.j.b::class.java.getDeclaredField("b").apply {
            isAccessible = true
        }
        val messageField = com.corrodinggames.rts.gameFramework.j.b::class.java.getDeclaredField("c").apply {
            isAccessible = true
        }
        addProxy(MultiplayerBattleroomActivity::updateUI) {
            RefreshUIEvent().broadCastIn()
        }

        addProxy(MultiplayerBattleroomActivity::startGame) {
            if(!isGaming) appKoin.get<Game>().gameRoom.startGame()
        }

        addProxy(MultiplayerBattleroomActivity::refreshChatLog) {
            val messages = messagesField.get(GameEngine.t().bU.aE) as ConcurrentLinkedQueue<com.corrodinggames.rts.gameFramework.j.b>
            messages.clear() // 暂时忽略
        }

        addProxy(MultiplayerBattleroomActivity::addMessageToChatLog) {
            val messages = messagesField.get(GameEngine.t().bU.aE) as ConcurrentLinkedQueue<com.corrodinggames.rts.gameFramework.j.b>
            val last = messages.poll()
            val sender = senderField.get(last) as String?
            val spawn = spawnField.get(last) as Int
            val message = messageField.get(last) as String
            ChatMessageEvent(sender ?: "", message, spawn).broadCastIn()
        }

        addProxy(MultiplayerBattleroomActivity::askPasswordInternal) { ae: ao? ->
            if(ae == null) return@addProxy
            if(questionOption != null) {
                ae.a(questionOption)
                questionOption = null
                return@addProxy
            }
            val message = ae.b?.let { a.b(it) }
            if(message == "Search units by internal name or text title.") return@addProxy
            QuestionDialogEvent(
                if(ae.b != null)
                    "Server Question"
                else ae.e ?: "Password Required",
                message ?: "This server requires a password to join"
            ).broadCastIn()
            GlobalEventChannel.filter(QuestionReplyEvent::class).subscribeOnce {
                if(!it.cancel) {
                    ae.a(it.message!!)
                } else {
                    ae.a()
                }
            }
        }
    }

    GameEngine::class.setFunction {
        addProxy("g", String::class) { _ : Any?, msg: String ->
            if (msg.startsWith("Kicked") || msg.startsWith("Missing")) KickedEvent(msg).broadCastIn()
        }

//        addProxy("c", String::class) { _ : Any?, _: String ->
//
//        }

        addProxy("d", String::class, mode = InjectMode.InsertBefore) { str: String ->
            if(str == "----- returnToBattleroom -----") {
                isReturnToBattleRoom = true
            }

            Unit
        }

        addProxy("c",String::class, mode = InjectMode.Override) { str: String? ->
            Log.i("RustedWarfare", str ?: "")
        }

        addProxy("b", String::class, String::class, mode = InjectMode.InsertBefore) { _: Any?, title: String?, _: Any? ->
            if (title == "Players") {
                Unit
            } else {
                InterruptResult(Unit)
            }
        }
    }

    com.corrodinggames.rts.appFramework.d::class.setFunction {
        addProxy("b", Activity::class) { activity: Activity ->
            val viewGroup = activity.window.decorView.rootView as ViewGroup
            val i = com.corrodinggames.rts.appFramework.d.d;
            var i2 = i;
            val bMethod = com.corrodinggames.rts.appFramework.d::class.java.getDeclaredMethod("b").apply { isAccessible = true }
            if (i == com.corrodinggames.rts.appFramework.l.f) {
                i2 = bMethod.invoke(null) as Int
            }
            val gameViewThreaded =
                if(i2 == com.corrodinggames.rts.appFramework.l.c)
                    GameViewThreaded(activity, null)
                else if(i2 == com.corrodinggames.rts.appFramework.l.e) GameViewOpenGL(activity, null)
                else if(i2 == com.corrodinggames.rts.appFramework.l.a) GameView(activity, null)
                else if(i2 == com.corrodinggames.rts.appFramework.l.b)
                    if(Build.VERSION.SDK_INT >= 26) GameView(activity, null)
                    else RWPPGameViewNonSurface(activity, null)
                else RWPPGameViewNonSurface(activity, null);
            viewGroup.addView(gameViewThreaded, 0, ViewGroup.LayoutParams(-1, -1));
            gameViewThreaded
        }
    }



    com.corrodinggames.rts.game.units.custom.l::class.setFunction {
        addProxy(
            "a",
            com.corrodinggames.rts.game.units.custom.ab::class,
            java.util.HashMap::class
        ) { p1: Any?, p2: HashMap<Any?, com.corrodinggames.rts.game.units.custom.ac> ->
            val allMods = buildList {
                p2.values.forEach { ac ->
                    val name = ac::class.java.getDeclaredField("a")
                        .also { it.isAccessible = true }
                        .get(ac)
                    if (name != null && name != "null") add(name as String)
                }
            }

            roomMods = allMods.toTypedArray()

            val met = com.corrodinggames.rts.game.units.custom.l::class.java.getDeclaredMethod(
                "__proxy__a",
                com.corrodinggames.rts.game.units.custom.ab::class.java, java.util.HashMap::class.java
            ).apply { isAccessible = true }
            try {
                met.invoke(null, p1, p2)
            } catch (e: Exception) {

                run {
                    val game = appKoin.get<Game>()
                    val net = appKoin.get<Net>()
                    val modManager = appKoin.get<ModManager>()
                    if (allMods.all { modManager.getModByName(it) != null }) {
                        modManager.getAllMods().forEach { it.isEnabled = it.name in allMods }
                        CallReloadModEvent().broadCastIn()
                        return@run
                    } else if(!com.corrodinggames.rts.gameFramework.e.a.f(
                            com.corrodinggames.rts.game.units.custom.ag.h()
                    )) {
                        game.gameRoom.disconnect()
                        KickedEvent("No mod directory found. Please set it at first.").broadCastIn()
                        return@run
                    }

                    val modsName = modManager.getAllMods().map { it.name }
                    if (game.gameRoom.option.canTransferMod) {
                        net.sendPacketToServer(ModPacket.RequestPacket(allMods.filter { it !in modsName }
                            .joinToString(";")))
                        CallStartDownloadModEvent().broadCastIn()
                    } else {
                        game.gameRoom.disconnect()
                        KickedEvent(e.cause?.message ?: "").broadCastIn()
                    }
                }
            }
            Unit
        }
    }


}