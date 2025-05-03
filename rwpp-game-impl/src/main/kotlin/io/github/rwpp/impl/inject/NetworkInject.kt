/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl.inject

import com.corrodinggames.rts.gameFramework.j.`as`
import com.corrodinggames.rts.gameFramework.j.k
import io.github.rwpp.*
import io.github.rwpp.core.UI
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.ChatMessageEvent
import io.github.rwpp.event.events.SystemMessageEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.units.GameCommandActions
import io.github.rwpp.impl.*
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult
import io.github.rwpp.logger
import io.github.rwpp.net.Client
import io.github.rwpp.net.InternalPacketType
import io.github.rwpp.net.Net
import io.github.rwpp.packageName
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException

@InjectClass(com.corrodinggames.rts.gameFramework.j.ad::class)
object NetworkInject {
    @Inject("a", injectMode = InjectMode.InsertBefore)
    fun onBanUnits(netPacket: com.corrodinggames.rts.gameFramework.e): Any{
        val actionString = netPacket.k.a()
        if(actionString.startsWith("u_")) {
            if(actionString.removePrefix("u_").removePrefix("c_") in bannedUnitList) {
                return InterruptResult.Unit
            }
        }
        if(netPacket.j == null) return Unit
        val realAction = GameCommandActions.from(netPacket.j.d().ordinal)
        val u = netPacket.j.a()
        return if(u is com.corrodinggames.rts.game.units.`as`) {
            if(realAction == GameCommandActions.BUILD && u.v() in bannedUnitList) {
                InterruptResult.Unit
            } else Unit
        } else Unit
    }

    @Inject("c", injectMode = InjectMode.InsertBefore)
    fun onReceivePacket(auVar: com.corrodinggames.rts.gameFramework.j.au): Any {
        return when(val type = auVar.b) {
            InternalPacketType.PREREGISTER_INFO.type -> {
                with(GameEngine.B().bX) {
                    if (this.C) return@with
                    val kVar16 = k(auVar)
                    val cVar14 = auVar.a
                    val str = kVar16.l()
                    if (str.startsWith(packageName)) {
                        io.github.rwpp.impl.inject.NetworkInject.gameRoom.isRWPPRoom = true
//                        gameRoom.option = Toml.decodeFromString(RoomOption.serializer(), str.removePrefix(packageName))
//                        val v = gameRoom.option.protocolVersion
//                        if (v != protocolVersion) {
//                            gameRoom.disconnect()
//                            UI.showWarning(
//                                "Different protocol version. yours: $protocolVersion server's: $v",
//                                true
//                            )
//                            return@with
//                        }
                    }
                    val f11 = kVar16.f()
                    val f12 = kVar16.f()
                    kVar16.f()
                    kVar16.l()
                    this.S = kVar16.l()
                    cVar14.E = f12
                    if (f11 >= 1) {
                        this.T = kVar16.f()
                    }
                    if (f11 >= 2) {
                        this.U = kVar16.f()
                        this.V = kVar16.f()
                    }

                    h(cVar14)
                }

                InterruptResult.Unit
            }

            else -> {
                net.listeners[type]?.forEach { listener ->
                    val result = listener.invoke(
                        auVar.a as? Client,
                        net.packetDecoders[type]!!.invoke(
                            DataInputStream(
                                ByteArrayInputStream(auVar.c)
                            )
                        )
                    )
                    if (result) return InterruptResult.Unit
                }

                Unit
            }
        }
    }

    @Inject("g", InjectMode.Override)
    fun onPlayerJoin(c: com.corrodinggames.rts.gameFramework.j.c) {
        val asVar = `as`()
        try {
            val B = GameEngine.B()
            asVar.c(packageName /* + Toml.encodeToString(RoomOption.serializer(), gameRoom.option) */)
            asVar.a(2)
            asVar.a(B.bX.e)
            asVar.a(B.c(true))
            asVar.c(B.l())
            asVar.c(B.bX.ab())
            asVar.a(c.M)
            asVar.a(B.bX.W)
            asVar.a(0)
            B.bX.a(c, asVar.b(InternalPacketType.PREREGISTER_INFO.type))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Inject("a", injectMode = InjectMode.InsertBefore)
    fun onReceiveChat(
        cVar: com.corrodinggames.rts.gameFramework.j.c?,
        nVar: com.corrodinggames.rts.game.n?,
        str: String?,
        str2: String?,
        cVar2: com.corrodinggames.rts.gameFramework.j.c?
    ): Any {
        val room = appKoin.get<Game>().gameRoom
        val player = room.getPlayers()
            .firstOrNull { nVar != null && (it as PlayerImpl?)?.self == nVar }

        if ((str2 ?: "").startsWith(commands.prefix)
            && player != null
            && player != room.localPlayer
            && room.isHost
        ) {
            commands.handleCommandMessage(str2 ?: "", player) { room.sendMessageToPlayer(player, "RWPP", it) }
            return InterruptResult.Unit
        } else {
            return Unit
        }
    }

    @Inject("b", injectMode = InjectMode.InsertBefore)
    fun onShowChat(
        c: com.corrodinggames.rts.gameFramework.j.c?,
        i: Int,
        str: String?,
        str2: String?): Any {
        val room = appKoin.get<Game>().gameRoom
        val player = room.getPlayers()
            .firstOrNull {
                if (gameRoom.isHost)
                    c != null && it.client == c
                else it.name == str
            }

        if ((str2 ?: "").startsWith(commands.prefix) && room.isHost)
            return InterruptResult.Unit

        if (player == null) {
            SystemMessageEvent(str2 ?: "").broadcastIn(onFinished = {
                UI.onReceiveChatMessage(str ?: "",str2 ?: "", i)
            })
        } else {
            logger.info("Received chat message from ${player.name}")
            ChatMessageEvent(
                str ?: "",str2 ?: "", player, i
            ).broadcastIn(onFinished = {
                UI.onReceiveChatMessage(it.sender, it.message, i)
            })
        }
        return Unit
    }

    private val net by lazy { appKoin.get<Net>() }
    private val gameRoom by lazy { appKoin.get<Game>().gameRoom }
}