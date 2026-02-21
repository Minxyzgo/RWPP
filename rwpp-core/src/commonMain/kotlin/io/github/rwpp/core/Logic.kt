/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.core

import io.github.rwpp.appKoin
import io.github.rwpp.config.Settings
import io.github.rwpp.event.EventPriority
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.events.DisconnectEvent
import io.github.rwpp.event.events.GameLoadedEvent
import io.github.rwpp.event.events.ModCheckEvent
import io.github.rwpp.event.events.PlayerJoinEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.io.SizeUtils
import io.github.rwpp.logger
import io.github.rwpp.modDir
import io.github.rwpp.net.InternalPacketType
import io.github.rwpp.net.Net
import io.github.rwpp.net.ServerStatus
import io.github.rwpp.net.packets.ModPacket
import io.github.rwpp.net.packets.ServerPacket
import io.github.rwpp.net.registerPacketListener
import io.github.rwpp.ui.UI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.LinkedList

//typealias TargetPositionWithUnits = Triple<Double, Double, List<GameUnit>>

object Logic : Initialization {
    private var playerCount = 0
    private var modQueue: LinkedList<String>? = null
    private var requiredMods: List<String>? = null
    private val scope = CoroutineScope(SupervisorJob())

    override fun init() {
        registerListeners()

        GlobalEventChannel.filter(PlayerJoinEvent::class).subscribeAlways(priority = EventPriority.MONITOR) { e ->
            logger.info("New player: ${e.player.name}")
            synchronized(Logic) {
                val game = appKoin.get<Game>()
                val room = game.gameRoom
                room.teamMode?.onPlayerJoin(room, e.player)
            }
        }

        GlobalEventChannel.filter(ModCheckEvent::class).subscribeAlways(priority = EventPriority.MONITOR) { e ->
            val game = appKoin.get<Game>()
            val room = game.gameRoom
            val net = appKoin.get<Net>()
            val manager = appKoin.get<ModManager>()
            val allMods = manager.getAllMods()
            if (room.isRWPPRoom && room.option.canTransferMod) {
                // TODO check different mod data.
                val missingMods = e.requiredMods.toMutableList().apply { removeAll(allMods.map { it.name }) }

                if (missingMods.isEmpty()) {
                    net.sendPacketToServer(ModPacket.RequestPacket())
                    allMods.forEach { mod ->
                        if (mod.name in e.requiredMods) {
                            mod.isEnabled = true
                        }
                    }
                    manager.modReload()
                    net.sendPacketToServer(ModPacket.ModReloadFinishPacket())
                } else {
                    modQueue = LinkedList(missingMods)
                    requiredMods = e.requiredMods
                    net.sendPacketToServer(ModPacket.RequestPacket().apply {
                        mods = missingMods.joinToString(",")
                    })
                    setDownloadingTitle(0)
                    e.intercept()
                }
            }
        }

        GlobalEventChannel.filter(DisconnectEvent::class).subscribeAlways(priority = EventPriority.MONITOR) {
            modQueue = null
            requiredMods = null
        }

        GlobalEventChannel.filter(GameLoadedEvent::class).subscribeAlways {
            val game = appKoin.get<Game>()
            val settings = appKoin.get<Settings>()

            when (settings.effectLimitForAllEffects) {
                "Zero" -> game.setEffectLimitForAllEffects(0)
                "Unlimited" -> game.setEffectLimitForAllEffects(Int.MAX_VALUE)
                else -> {}
            }
        }
    }

    fun getNextPlayerId(): Int {
        synchronized(Logic) {
            return ++playerCount
        }
    }

    fun registerListeners() {
        val game = appKoin.get<Game>()
        val net = appKoin.get<Net>()

        net.registerPacketListener<ServerPacket.ServerInfoGetPacket>(
            InternalPacketType.PRE_GET_SERVER_INFO_FROM_LIST.type
        ) { client, _ ->
            val room = game.gameRoom

            if (room.isHost) return@registerPacketListener true

            client?.sendPacketToClient(
                ServerPacket.ServerInfoReceivePacket(
                    room.localPlayer.name + "'s game",
                    room.getPlayers().size,
                    room.maxPlayerCount,
                    room.selectedMap.mapName,
                    "",
                    "v1.15 - RWPP Client",
                    room.mods.joinToString(", "),
                    if (room.isStartGame) ServerStatus.InGame else ServerStatus.BattleRoom
                )
            )

            true
        }

        net.registerPacketListener<ModPacket.RequestPacket>(
            ModPacket.MOD_DOWNLOAD_REQUEST
        ) { client, packet ->
            val room = game.gameRoom
            if (!room.isHost) return@registerPacketListener true
            runCatching {
                val player = room.getPlayerByClient(client!!)!!
                player.data.ready = false
                val mods = appKoin.get<ModManager>()
                    .getAllMods()
                    .filter { it.isEnabled && it.name in packet.mods.split(",") }
                mods.forEachIndexed { i, mod ->
                    logger.info("send mod: ${mod.name}")
                    client.sendPacketToClient(
                        ModPacket.ModPackPacket().apply {
                            index = i
                            name = mod.name
                            modBytes = mod.getBytes()
                        }
                    )
                }
            }.onFailure {
                logger.error(it.stackTraceToString())
            }

            true
        }

        net.registerPacketListener<ModPacket.ModPackPacket>(
            ModPacket.DOWNLOAD_MOD_PACK
        ) { client, packet ->
            val room = game.gameRoom
            if (modQueue == null || room.isHost) return@registerPacketListener true
            scope.launch(Dispatchers.IO) {
                runCatching {
                    logger.info("get mod packet: ${packet.name}")
                    modQueue!!.poll()
                    setDownloadingTitle(packet.index + 1)
                    val modFile = File(modDir, packet.name + ".rwmod")
                    if (!modFile.exists()) modFile.createNewFile()
                    modFile.writeBytes(packet.modBytes)

                    if (modQueue!!.isEmpty()) {
                        UI.showNetworkDialog = false
                        val manager = appKoin.get<ModManager>()
                        val mods = manager.getAllMods()
                        mods.forEach { mod ->
                            mod.isEnabled = mod.name in requiredMods!!
                        }
                        manager.modReload()
                        val mods2 = manager.getAllMods()
                        if (requiredMods!!.any { m -> m !in mods2.map { it.name } }) {
                            room.disconnect("Mod download failed.")
                            UI.showWarning("Mod download failed: required mods were not found.", true)
                            return@runCatching
                        }

                        net.sendPacketToServer(ModPacket.ModReloadFinishPacket())
                    } else {
                        UI.showNetworkDialog = true
                    }
                }.onFailure {
                    room.disconnect("Mod download failed.")
                    UI.showWarning("Mod download failed: ${it.stackTraceToString()}", true)
                }
            }

            true
        }

        net.registerPacketListener<ModPacket.ModReloadFinishPacket>(
            ModPacket.MOD_RELOAD_FINISH
        ) { client, packet ->
            val room = game.gameRoom
            runCatching {
                val player = room.getPlayerByClient(client!!)!!
                player.data.ready = true
            }

            true
        }
    }

    //经过测试，分组效果不佳，暂时不使用
    /*
    fun onPathfindingOptimization(targetX: Float, targetY: Float, selectedUnits: List<GameUnit>): List<TargetPositionWithUnits> {
//        val leftX = selectedUnits.minOf { it.x }
//        val rightX = selectedUnits.maxOf { it.x }
//        val topY = selectedUnits.minOf { it.y }
//        val bottomY = selectedUnits.maxOf { it.y }
        // 简单距离迭代，计算每个单位到其他单位的距离，并将距离较短的单位放入同一组
        val groups = mutableListOf<TargetPositionWithUnits>()
        val unassignedUnits = selectedUnits.toMutableList()
        val maxDistance = 10
        while (unassignedUnits.isNotEmpty()) {
            val group = mutableListOf<GameUnit>()
            groups.add(Triple(targetX.toDouble(), targetY.toDouble(), group))
            group.add(unassignedUnits.removeAt(0))
            for (i in unassignedUnits.indices) {
                val unit = unassignedUnits[i]
                val distance = sqrt((unit.x - group.last().x) * (unit.x - group.last().x) + (unit.y - group.last().y) * (unit.y - group.last().y))
                if (distance <= maxDistance) {
                    group.add(unassignedUnits.removeAt(i))
                }
            }
        }

        return groups
    }
    */

    private fun setDownloadingTitle(index: Int) {
        val totalSize = appKoin.get<Game>().gameRoom.option.allModsSize
        val modCount = requiredMods!!.size
        UI.receivingNetworkDialogTitle =
            "Downloading ${modQueue!!.peek()}. total: ${SizeUtils.byteToMB(totalSize.toLong())}. ($index/$modCount)"
        UI.showNetworkDialog = true
    }
}