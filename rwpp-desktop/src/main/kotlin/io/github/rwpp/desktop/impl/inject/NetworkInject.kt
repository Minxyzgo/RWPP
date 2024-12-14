/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject

import com.corrodinggames.rts.gameFramework.j.`as`
import com.corrodinggames.rts.gameFramework.j.k
import io.github.rwpp.appKoin
import io.github.rwpp.desktop.bannedUnitList
import io.github.rwpp.desktop.impl.ClientImpl
import io.github.rwpp.desktop.impl.GameEngine
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.KickedEvent
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.data.RoomOption
import io.github.rwpp.game.units.GameCommandActions
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult
import io.github.rwpp.net.Net
import io.github.rwpp.net.PacketType
import io.github.rwpp.packageName
import io.github.rwpp.protocolVersion
import net.peanuuutz.tomlkt.Toml
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
                return InterruptResult(Unit)
            }
        }
        if(netPacket.j == null) return Unit
        val realAction = GameCommandActions.from(netPacket.j.d().ordinal)
        val u = netPacket.j.a()
        return if(u is com.corrodinggames.rts.game.units.`as`) {
            if(realAction == GameCommandActions.BUILD && u.v() in bannedUnitList) {
                InterruptResult(Unit)
            } else Unit
        } else Unit
    }

    @Inject("c", injectMode = InjectMode.InsertBefore)
    fun onReceivePacket(auVar: com.corrodinggames.rts.gameFramework.j.au): Any {
        return when(val type = auVar.b) {
            PacketType.PREREGISTER_INFO.type -> {
                with(GameEngine.B().bX) {
                    if(this.C) return@with
                    val kVar16 = k(auVar)
                    val cVar14 = auVar.a
                    val str = kVar16.l()
                    if(str.startsWith(packageName)) {
                        gameRoom.isRWPPRoom = true
                        gameRoom.option = Toml.decodeFromString(RoomOption.serializer(), str.removePrefix(packageName))
                        val v = gameRoom.option.protocolVersion
                        if (v != protocolVersion) {
                            gameRoom.disconnect()
                            KickedEvent("Different protocol version. yours: $protocolVersion server's: $v").broadCastIn()
                            return@with
                        }
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

                InterruptResult(Unit)
            }
/*
            PacketType.MOD_DOWNLOAD_REQUEST.type -> {
                if(gameRoom.isHost) {
                    val B = GameEngine.B()
                    val c = auVar.a

                    if(!gameRoom.option.canTransferMod)
                        B.bX.a(c, "Server didn't support transferring mods.")
                    else {
                        val k = k(auVar)
                        val str = k.l()

                        try {
                            val mods = str.split(";")
                            mods.map(appKoin.get<ModManager>()::getModByName).forEachIndexed { i, m ->
                                val bytes = m!!.getBytes()
                                B.bX.a(c, ModPacket.ModPackPacket(mods.size, i, "${m.name}.network.rwmod", bytes).asGamePacket())
                            }

                            gameRoom.getPlayers().firstOrNull { it.name == c.z?.v }
                                ?.data?.ready = false
                        } catch (e: Exception) {
                            e.printStackTrace()
                            B.bX.a(c, "Mod download error. cause: ${e.stackTraceToString().substring(0..100)}...")
                        }
                    }
                }

                InterruptResult(Unit)
            }

            PacketType.MOD_RELOAD_FINISH.type -> {
                if(gameRoom.isHost) {
                    gameRoom.getPlayers().firstOrNull { it.name == auVar.a.z?.v }
                        ?.data?.ready = true
                }

                InterruptResult(Unit)
            }

            PacketType.DOWNLOAD_MOD_PACK.type -> {
                val B = GameEngine.B()
                val k = k(auVar)
                val c = auVar.a
                val size = k.f()
                val index = k.f()
                val name = k.l()
                val bytes = k.t()

                val modSize = cacheModSize.addAndGet(bytes.size)

                run {

                    if(modSize > maxModSize) {
                        gameRoom.disconnect()
                        cacheModSize.set(0)
                        KickedEvent("Downloaded mods are too big.").broadCastIn()
                        return@run
                    }

                    val fi = File("mods/units/$name")
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
*/
            else -> {
                if(type in 500..1000) {
                    val packetType = PacketType.from(type)
                    val listener = appKoin.get<Net>().listeners[packetType]
                    if (listener != null) {
                        listener.invoke(
                            ClientImpl(auVar.a),
                            appKoin.get<Net>().packetDecoders[packetType]!!.invoke(
                                DataInputStream(
                                    ByteArrayInputStream(auVar.c)
                                )
                            )
                        )
                        return InterruptResult(Unit)
                    }
                }

                Unit
            }
        }
    }

    @Inject("g")
    fun onPlayerJoin(c: com.corrodinggames.rts.gameFramework.j.c) {
        val asVar = `as`()
        try {
            val B = GameEngine.B()
            asVar.c(packageName + Toml.encodeToString(RoomOption.serializer(), gameRoom.option))
            asVar.a(2)
            asVar.a(B.bX.e)
            asVar.a(B.c(true))
            asVar.c(B.l())
            asVar.c(B.bX.ab())
            asVar.a(c.M)
            asVar.a(B.bX.W)
            asVar.a(0)
            B.bX.a(c, asVar.b(PacketType.PREREGISTER_INFO.type))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private val gameRoom by lazy { appKoin.get<GameRoom>() }
}