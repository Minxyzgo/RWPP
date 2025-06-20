/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.corrodinggames.rts.appFramework.ClosingActivity
import com.corrodinggames.rts.appFramework.MultiplayerBattleroomActivity
import com.corrodinggames.rts.gameFramework.j.ae
import com.corrodinggames.rts.gameFramework.k
import io.github.rwpp.*
import io.github.rwpp.android.bannedUnitList
import io.github.rwpp.android.impl.ClientImpl
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.android.impl.PlayerImpl
import io.github.rwpp.android.isReturnToBattleRoom
import io.github.rwpp.core.UI
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.ChatMessageEvent
import io.github.rwpp.event.events.SystemMessageEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.data.RoomOption
import io.github.rwpp.game.units.GameCommandActions
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult
import io.github.rwpp.net.InternalPacketType
import io.github.rwpp.net.Net
import io.github.rwpp.utils.Reflect
import net.peanuuutz.tomlkt.Toml
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.*

@InjectClass(ae::class)
object NetInject {

    @SuppressLint("WrongConstant")
    @Inject("X", InjectMode.Override)
    fun notify1() {
        if (!GameEngine.aR) {
            val t: k = GameEngine.t()
            val intent = Intent(
                t.al,
                ClosingActivity::class.java
            )
            // support for android 12
            val activity = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    t.al, 0, Intent(
                        t.al,
                        ClosingActivity::class.java
                    ), PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getActivity(
                    t.al, 0, Intent(
                        t.al,
                        ClosingActivity::class.java
                    ), PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            val notificationManager =
                t.al.getSystemService("notification") as NotificationManager
            if (Build.VERSION.SDK_INT >= 11) {
                val i = Build.VERSION.SDK_INT
                val builder = Notification.Builder(t.al)
                builder.setContentTitle("Rusted Warfare Multiplayer")
                builder.setContentText("A multiplayer game is in progress")
                builder.setSmallIcon(R.drawable.icon)
                builder.setContentIntent(activity)
                builder.setOngoing(true)
                Reflect.call<ae, Any>(null, "a", listOf(notificationManager::class), listOf(notificationManager))
                Reflect.call<ae, Any>(null, "a", listOf(builder::class, String::class), listOf(builder, "multiplayerStatusId"))
                if (Build.VERSION.SDK_INT >= 16) {
                    builder.build()
                }
                notificationManager.notify(1, builder.notification)
            }
        }
    }

    @SuppressLint("WrongConstant")
    @Inject("d", InjectMode.Override)
    fun ae.notify2(arg1: String, arg2: String) {
        if (!GameEngine.aR) {
            val t: k = GameEngine.t()
            if (!this.G && !t.bY.g()) {
                var isActivityVisible: Boolean =
                    MultiplayerBattleroomActivity.isActivityVisible()
                val abVar = t.an
                if (abVar != null && !abVar.isPaused) {
                    isActivityVisible = true
                }
                if (isActivityVisible) {
                    if (Reflect.get<Boolean>(this, "bD") == true) {
                        Reflect.call<ae, Any>(null, "f", listOf(Int::class), listOf(2))
                        return Unit
                    }
                    return Unit
                }
                val notificationManager: NotificationManager =
                    t.al.getSystemService("notification") as NotificationManager
                val intent = Intent(
                    t.al,
                    ClosingActivity::class.java
                )
                // support for android 12
                val activity = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(
                        t.al, 0, Intent(
                            t.al,
                            ClosingActivity::class.java
                        ), PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    PendingIntent.getActivity(
                        t.al, 0, Intent(
                            t.al,
                            ClosingActivity::class.java
                        ), PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
                if (Build.VERSION.SDK_INT >= 11) {
                    val builder: Notification.Builder = Notification.Builder(t.al)
                    builder.setContentTitle("Rusted Warfare Multiplayer")
                    builder.setContentText("$arg1: $arg2")
                    builder.setSmallIcon(io.github.rwpp.R.drawable.icon)
                    builder.setContentIntent(activity)
                    builder.setOngoing(false)
                    builder.setAutoCancel(true)
                    Reflect.call<ae, Any>(null, "a", listOf(notificationManager::class), listOf(notificationManager))
                    Reflect.call<ae, Any>(null, "a", listOf(builder::class, String::class), listOf(builder, "multiplayerChatId"))
                    notificationManager.notify(2, builder.getNotification())
                    Reflect.set(this, "bD", true)
                }
            }
        }
    }

    @Inject("x", InjectMode.InsertBefore)
    fun onReturnToBattleRoom() {
        isReturnToBattleRoom = true
    }

    @Inject("a", InjectMode.InsertBefore)
    fun onProcessPacket(packet: com.corrodinggames.rts.gameFramework.j.bi): Any {
        return when (val type = packet.b) {
            InternalPacketType.PREREGISTER_INFO.type -> {
                val r0 = com.corrodinggames.rts.gameFramework.j.j(packet);     // Catch: java.lang.Throwable -> L603
                val r1 = packet.a
                val r14 = GameEngine.t().bU
                val str = r0.b.readUTF() // Catch: java.lang.Throwable -> L603
                if (str.startsWith(packageName)) {
                    val gameRoom = appKoin.get<Game>().gameRoom
                    gameRoom.isRWPPRoom = true
//                    gameRoom.option = Toml.decodeFromString(RoomOption.serializer(), str.removePrefix(packageName))
//                    val v = gameRoom.option.protocolVersion
//                    if (v != protocolVersion) {
//                        gameRoom.disconnect()
//                        UI.showWarning("Different protocol version. yours: $protocolVersion server's: $v", true)
//                        return InterruptResult(Unit)
//                    }
                }
                val r2 = r0.b.readInt() // Catch: java.lang.Throwable -> L603
                val r3 = r0.b.readInt() // Catch: java.lang.Throwable -> L603
                r0.b.readInt() // Catch: java.lang.Throwable -> L603
                r0.b.readUTF() // Catch: java.lang.Throwable -> L603
                r14.U = r0.b.readUTF() // Catch: java.lang.Throwable -> L603
                r1.F = r3 // Catch: java.lang.Throwable -> L603

                if (r2 >= 1) r14.V = r0.b.readInt();

                if (r2 >= 2) {
                    r14.W = r0.b.readInt();     // Catch: java.lang.Throwable -> L603
                    r14.X = r0.b.readInt();
                }

                r14::class.java.getDeclaredMethod("f", r1::class.java).apply {
                    isAccessible = true
                }.invoke(r14, r1)


                InterruptResult(Unit)
            }

            InternalPacketType.PREREGISTER_INFO_RECEIVE.type -> {
                val j = com.corrodinggames.rts.gameFramework.j.j(packet)
                val c = packet.a

                j.b.readUTF()
                val i = j.b.readInt()
                j.b.readInt()
                if (i >= 1) j.b.readUTF()
                if (i >= 2) j.a()?.let {
                    c.p = it
                }

                if (i >= 4) GameEngine.ab()

                val t = GameEngine.t()
                val gameRoom = appKoin.get<Game>().gameRoom
                val a = com.corrodinggames.rts.gameFramework.j.bg()
                a.b(packageName /*+ Toml.encodeToString(RoomOption.serializer(), gameRoom.option)*/)
                a.c(2)
                a.c(t.bU.e)
                a.c(t.a(true))
                a.b(t.h())
                if (t.bN.networkServerId == null) {
                    t.bN.networkServerId = UUID.randomUUID().toString()
                    t.bN.save()
                }
                a.b(t.bN.networkServerId)
                a.c(c.N)
                a.c(t.bU.Y)
                a.c(0)

                Reflect.call<ae, Any>(
                    t.bU,
                    "a",
                    listOf(c::class, com.corrodinggames.rts.gameFramework.j.bi::class),
                    listOf(c, a.a(InternalPacketType.PREREGISTER_INFO.type))
                )
                InterruptResult.Unit
            }


            else -> {
                net.listeners[type]?.forEach { listener ->
                    val result = listener.invoke(
                        ClientImpl(packet.a),
                        net.packetDecoders[type]!!.invoke(
                            DataInputStream(
                                ByteArrayInputStream(packet.c)
                            )
                        )
                    )
                    if (result) return InterruptResult.Unit
                }
                Unit
            }
        }
    }

    @Inject("a", InjectMode.InsertBefore)
    fun onReceiveGameCommand(b3: com.corrodinggames.rts.gameFramework.e): Any {
        val actionString = b3.k.b

        if(actionString.removePrefix("u_") in bannedUnitList) {
            return InterruptResult.Unit
        }

        if(b3.j == null) return Unit
        val realAction = GameCommandActions.from(b3.j.a.ordinal)
        val u = b3.j.b
        return if(realAction == GameCommandActions.BUILD && u is com.corrodinggames.rts.game.units.el) {
            if(u.i() in bannedUnitList) {
                InterruptResult.Unit
            } else Unit
        } else Unit
    }

    @Inject("a", injectMode = InjectMode.InsertBefore)
    fun onReceiveChat(
        cVar: com.corrodinggames.rts.gameFramework.j.c?,
        pVar: com.corrodinggames.rts.game.p?,
        str: String?,
        str2: String?,
        cVar2: com.corrodinggames.rts.gameFramework.j.c?
    ): Any {
        val room = appKoin.get<Game>().gameRoom

        val player = room.getPlayers()
            .firstOrNull { pVar != null && (it as PlayerImpl?)?.player == pVar }

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

    @Inject("k", injectMode = InjectMode.InsertBefore)
    fun onSendChatMessage(message: String): Any{
        return if (gameRoom.isHost && message.startsWith(commands.prefix)) {
            commands.handleCommandMessage(message, gameRoom.localPlayer) { gameRoom.sendMessageToPlayer(gameRoom.localPlayer, "RWPP", it) }
            InterruptResult.Unit
        } else Unit
    }

    @Inject("a", injectMode = InjectMode.InsertBefore)
    fun onShowChat(
        c: com.corrodinggames.rts.gameFramework.j.c?,
        i: Int,
        str: String?,
        str2: String?): Any {
        val room = appKoin.get<Game>().gameRoom
        val player = room.getPlayers()
            .firstOrNull {
                if (gameRoom.isHost)
                    (c != null && (it.client as ClientImpl?)?.client == c) || (c == null && it.name == room.localPlayer.name)
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

    private val gameRoom by lazy { appKoin.get<Game>().gameRoom }
    private val net by lazy { appKoin.get<Net>() }
}