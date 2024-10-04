/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.proxy

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.corrodinggames.rts.appFramework.ClosingActivity
import com.corrodinggames.rts.appFramework.MultiplayerBattleroomActivity
import com.corrodinggames.rts.gameFramework.j.ae
import com.corrodinggames.rts.gameFramework.k
import com.github.minxyzgo.rwij.InjectMode
import com.github.minxyzgo.rwij.InterruptResult
import com.github.minxyzgo.rwij.setFunction
import io.github.rwpp.*
import io.github.rwpp.R
import io.github.rwpp.android.*
import io.github.rwpp.android.impl.ClientImpl
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.android.impl.asGamePacket
import io.github.rwpp.android.impl.sendKickToClient
import io.github.rwpp.config.Settings
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.CallReloadModEvent
import io.github.rwpp.event.events.KickedEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.data.RoomOption
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.game.units.GameCommandActions
import io.github.rwpp.net.Net
import io.github.rwpp.net.PacketType
import io.github.rwpp.net.packets.ModPacket
import io.github.rwpp.utils.io.GameInputStream
import net.peanuuutz.tomlkt.Toml
import java.io.File
import java.util.*

object NetProxy {
    init {
        ae::class.setFunction {
            addProxy("X") {
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
                        ae.a(notificationManager)
                        ae.a(builder, "multiplayerStatusId")
                        if (Build.VERSION.SDK_INT >= 16) {
                            builder.build()
                        }
                        notificationManager.notify(1, builder.notification)
                    }
                }

            }
            addProxy("d", String::class, String::class) { self: ae, arg1: String, arg2: String ->
                if (!GameEngine.aR) {
                    val t: k = GameEngine.t()
                    if (!self.G && !t.bY.g()) {
                        var isActivityVisible: Boolean =
                            MultiplayerBattleroomActivity.isActivityVisible()
                        val abVar = t.an
                        if (abVar != null && !abVar.isPaused) {
                            isActivityVisible = true
                        }
                        if (isActivityVisible) {
                            if (self.bD) {
                                ae.f(2)
                                return@addProxy Unit
                            }
                            return@addProxy Unit
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
                            builder.setContentText(arg1 + ": " + arg2)
                            builder.setSmallIcon(R.drawable.icon)
                            builder.setContentIntent(activity)
                            builder.setOngoing(false)
                            builder.setAutoCancel(true)
                            ae.a(notificationManager)
                            ae.a(builder, "multiplayerChatId")
                            notificationManager.notify(2, builder.getNotification())
                            self.bD = true
                        }
                    }
                }
            }

            addProxy("x", mode = InjectMode.InsertBefore) { _: Any? ->
                isReturnToBattleRoom = true
                Unit
            }

            addProxy("a",
                com.corrodinggames.rts.gameFramework.j.bi::class,
                mode = InjectMode.InsertBefore
            ) { _: Any?, packet: com.corrodinggames.rts.gameFramework.j.bi ->
                when(val type = packet.b) {
                    PacketType.PREREGISTER_INFO.type -> {
                        val r0 = com.corrodinggames.rts.gameFramework.j.j(packet);     // Catch: java.lang.Throwable -> L603
                        val r1 = packet.a
                        val r14 = GameEngine.t().bU
                        val str = r0.b.readUTF() // Catch: java.lang.Throwable -> L603
                        if (str.startsWith(packageName)) {
                            val gameRoom = appKoin.get<Game>().gameRoom
                            gameRoom.isRWPPRoom = true
                            gameRoom.option = Toml.decodeFromString(RoomOption.serializer(), str.removePrefix(packageName))
                            val v = gameRoom.option.protocolVersion
                            if (v != protocolVersion) {
                                gameRoom.disconnect()
                                KickedEvent("Different protocol version. yours: $protocolVersion server's: $v").broadCastIn()
                                return@addProxy InterruptResult(Unit)
                            }
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

                    PacketType.PREREGISTER_INFO_RECEIVE.type -> {
                        val j = com.corrodinggames.rts.gameFramework.j.j(packet)
                        val c = packet.a

                        j.b.readUTF()
                        val i = j.b.readInt()
                        j.b.readInt()
                        if(i >= 1) j.b.readUTF()
                        if(i >= 2) j.a()?.let {
                            c.p = it
                        }

                        if(i >= 4) GameEngine.ab()

                        val t = GameEngine.t()
                        val gameRoom = appKoin.get<Game>().gameRoom
                        val a = com.corrodinggames.rts.gameFramework.j.bg()
                        a.b(packageName + Toml.encodeToString(RoomOption.serializer(), gameRoom.option))
                        a.c(2)
                        a.c(t.bU.e)
                        a.c(t.a(true))
                        a.b(t.h())
                        if(t.bN.networkServerId == null) {
                            t.bN.networkServerId = UUID.randomUUID().toString()
                            t.bN.save()
                        }
                        a.b(t.bN.networkServerId)
                        a.c(c.N)
                        a.c(t.bU.Y)
                        a.c(0)

                        t.bU.a(c, a.a(PacketType.PREREGISTER_INFO.type))
                        InterruptResult(Unit)
                    }

                    PacketType.MOD_DOWNLOAD_REQUEST.type -> {
                        val j = com.corrodinggames.rts.gameFramework.j.j(packet)
                        val c = packet.a

                        val gameRoom = appKoin.get<Game>().gameRoom
                        if(gameRoom.isHost) {
                            if(!gameRoom.option.canTransferMod)
                                sendKickToClient(c, "Server didn't support transferring mods.")
                            else {
                                val str = j.b.readUTF()

                                try {
                                    val mods = str.split(";")
                                    mods.map(appKoin.get<ModManager>()::getModByName).forEachIndexed { i, m ->
                                        val bytes = m!!.getBytes()

                                        GameEngine.t().bU.a(c,
                                            ModPacket.ModPackPacket(mods.size, i, "${m.name}.network.rwmod", bytes)
                                                .asGamePacket()
                                        )

                                    }

                                    gameRoom.getPlayers().firstOrNull { it.name == c.A?.w }
                                        ?.data?.ready = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    sendKickToClient(c, "Mod download error. cause: ${e.stackTraceToString().substring(0..100)}...")
                                }
                            }
                        }

                        InterruptResult(Unit)
                    }

                    PacketType.MOD_RELOAD_FINISH.type -> {
                        val gameRoom = appKoin.get<Game>().gameRoom
                        gameRoom.getPlayers().firstOrNull { it.name == packet.a.A?.w }
                            ?.data?.ready = true
                        InterruptResult(Unit)
                    }

                    PacketType.DOWNLOAD_MOD_PACK.type -> {
                        val j = com.corrodinggames.rts.gameFramework.j.j(packet)
                        val c = packet.a
                        val gameInput = GameInputStream(j.b)
                        val size = gameInput.readInt()
                        val index = gameInput.readInt()
                        val name = gameInput.readUTF()
                        val bytes = gameInput.readNextBytes()

                        val modSize = cacheModSize.addAndGet(bytes.size)

                        val gameRoom = appKoin.get<Game>().gameRoom
                        run {
                            if(modSize > maxModSize) {
                                gameRoom.disconnect()
                                KickedEvent("Downloaded mods are too big.").broadCastIn()
                                cacheModSize.set(0)
                                return@run
                            }

                            val fi = File("/storage/emulated/0/rustedWarfare/units/$name")
                            if (fi.exists()) throw RuntimeException("Mod: $name had been installed.")

                            fi.createNewFile()
                            fi.writeBytes(bytes)

                            // TODO 可能顺序存在问题
                            if(index == size - 1) {
                                val modManager = appKoin.get<ModManager>()
                                modManager.modUpdate()
                                modManager.getAllMods().forEach { it.isEnabled = it.name in roomMods }
                                cacheModSize.set(0)
                                CallReloadModEvent().broadCastIn()
                            }
                        }

                        InterruptResult(Unit)
                    }
                    else -> {
                        if(type in 500..1000) {
                            val net = appKoin.get<Net>()
                            val packetType = PacketType.from(type)
                            val listener = net.listeners[packetType]
                            if (listener != null) {
                                listener.invoke(
                                    ClientImpl(packet.a),
                                    net.packetDecoders[packetType]!!.invoke(
                                        com.corrodinggames.rts.gameFramework.j.j(packet).b
                                    )
                                )
                                return@addProxy InterruptResult(Unit)
                            }
                        }
                        Unit
                    }
                }
            }

            addProxy("a", com.corrodinggames.rts.gameFramework.e::class, mode = InjectMode.InsertBefore) { _: Any?, b3: com.corrodinggames.rts.gameFramework.e ->
                val actionString = b3.k.b

                if(actionString.removePrefix("c_").removePrefix("u_") in bannedUnitList) {
                    return@addProxy InterruptResult(Unit)
                }

                if(b3.j == null) return@addProxy Unit
                val realAction = GameCommandActions.from(b3.j.a.ordinal)
                val u = b3.j.b
                if(realAction == GameCommandActions.BUILD && u is com.corrodinggames.rts.game.units.el) {
                    if(u.i() in bannedUnitList) {
                        InterruptResult(Unit)
                    } else Unit
                } else Unit
            }
        }

        val wField = com.corrodinggames.rts.gameFramework.e::class.java.getDeclaredField("w").apply {
            isAccessible = true
        }
        val settings = appKoin.get<Settings>()
        com.corrodinggames.rts.gameFramework.e::class.setFunction {
            addProxy("a", com.corrodinggames.rts.gameFramework.j.bg::class, mode = InjectMode.InsertBefore) { self:  com.corrodinggames.rts.gameFramework.e ->
                if (settings.enhancedReinforceTroops) {
                    val actionString = self.k.b
                    if (actionString != "-1") {
                        val l = wField.get(self) as List<com.corrodinggames.rts.game.units.d.s>
                        val m = com.corrodinggames.rts.gameFramework.utility.p(l.sortedBy { it.cY().size })
                        wField.set(self, m)
                    }
                }
                Unit
            }
        }
    }
}