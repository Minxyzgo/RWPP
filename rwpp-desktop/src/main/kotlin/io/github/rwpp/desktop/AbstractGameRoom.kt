/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop

import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope
import com.corrodinggames.rts.gameFramework.e
import com.corrodinggames.rts.gameFramework.j.c
import io.github.rwpp.appKoin
import io.github.rwpp.desktop.impl.PlayerImpl
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.DisconnectEvent
import io.github.rwpp.event.events.MapChangedEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.data.RoomOption
import io.github.rwpp.game.map.*
import io.github.rwpp.game.team.TeamMode
import io.github.rwpp.game.units.UnitType
import io.github.rwpp.mapDir
import io.github.rwpp.net.packets.GamePacket
import io.github.rwpp.utils.Reflect
import java.io.File
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


abstract class AbstractGameRoom  : GameRoom {
    override var maxPlayerCount: Int
        get() = PlayerInternal.c
        set(value) {
            if (maxPlayerCount != value) PlayerInternal.b(value, true)
        }
    override val isHost: Boolean
        get() = GameEngine.B().bX.C || singlePlayer
    override val isHostServer: Boolean
        get() = GameEngine.B().bX.H
    override val localPlayer: Player
        get() {
            return (GameEngine.B().bX.z ?: GameEngine.B().bs) as Player
        }
    override var sharedControl: Boolean
        get() = GameEngine.B().bX.ay.l
        set(value) {
            GameEngine.B().bX.ay.l = value
        }
    override val randomSeed: Int
        get() = GameEngine.B().bX.ay.q
    override val mapType: MapType
        get() = io.github.rwpp.game.map.MapType.entries[GameEngine.B().bX.ay.a.ordinal]
    override var selectedMap: GameMap
        get() = io.github.rwpp.appKoin.get<Game>().getAllMaps().firstOrNull {
            (if (isHost) GameEngine.B().bX.az else GameEngine.B().bX.ay.b)?.endsWith((it.mapName + it.getMapSuffix()).replace("\\", "/")) ?: false
        } ?: NetworkMap(formatMapName(GameEngine.B().bX.ay.b) as String)
        set(value) {
            if (isHostServer) {
                GameEngine.B().bX.a(
                    GameEngine.B().bX.e().apply {
                        b = (value.mapName + value.getMapSuffix())
                    }
                )
            } else {
                val realPath = (
                        when (value.mapType) {
                            MapType.SkirmishMap -> "maps/skirmish/"
                            MapType.CustomMap -> "mods/maps/"
                            MapType.SavedGame -> "saves/"
                            else -> ""
                        }) + (value.mapName + value.getMapSuffix()).replace("\\", "/")
                GameEngine.B().bX.az = realPath
                GameEngine.B().bX.ay.a = com.corrodinggames.rts.gameFramework.j.ai.entries[value.mapType.ordinal]
                GameEngine.B().bX.ay.b = (value.mapName + value.getMapSuffix())
                MapChangedEvent(value.displayName()).broadcastIn()
                updateUI()
            }
        }
    override var displayMapName: String
        get() = formatMapName(GameEngine.B().bX.ay.b) as String
        set(value) {
            GameEngine.B().bX.ay.b = value
        }
    override var startingCredits: Int
        get() = GameEngine.B().bX.ay.c
        set(value) {
            GameEngine.B().bX.ay.c = value
        }
    override var startingUnits: Int
        get() = GameEngine.B().bX.ay.g
        set(value) {
            GameEngine.B().bX.ay.g = value
        }
    override var fogMode: FogMode
        get() = io.github.rwpp.game.map.FogMode.entries[GameEngine.B().bX.ay.d]
        set(value) {
            GameEngine.B().bX.ay.d = value.ordinal
        }
    override var revealedMap: Boolean
        get() = GameEngine.B().bX.ay.e
        set(value) {
            GameEngine.B().bX.ay.e = value
        }
    override var aiDifficulty: Int
        get() = GameEngine.B().bX.ay.f
        set(value) {
            GameEngine.B().bX.ay.f = value
        }
    override var incomeMultiplier: Float
        get() = GameEngine.B().bX.ay.h
        set(value) {
            GameEngine.B().bX.ay.h = value
        }
    override var noNukes: Boolean
        get() = GameEngine.B().bX.ay.i
        set(value) {
            GameEngine.B().bX.ay.i = value
        }
    override var allowSpectators: Boolean
        get() = GameEngine.B().bX.ay.o
        set(value) {
            GameEngine.B().bX.ay.o = value
        }
    override var lockedRoom: Boolean
        get() = GameEngine.B().bX.ay.p
        set(value) {
            GameEngine.B().bX.ay.p = value
            if (isHost && value) sendSystemMessage("Room has been locked. Now self can't join the room")
        }
    override var teamLock: Boolean
        get() = GameEngine.B().bX.ay.m
        set(value) {
            GameEngine.B().bX.ay.m = value
        }
    override val mods: Array<String>
        get() = roomMods
    override var isRWPPRoom: Boolean = false
    override var option: RoomOption = RoomOption()
    override val isConnecting: Boolean
        get() = GameEngine.B().bX.B
    override val isStartGame: Boolean
        get() = isGaming
    override var teamMode: TeamMode? = null
    override val isSinglePlayerGame: Boolean
        get() = singlePlayer

    override var gameMapTransformer: ((XMLMap) -> Unit)? = null

    override var gameSpeed: Float
        get() = _gameSpeed
        set(value) { _gameSpeed = value}

    @Suppress("unchecked_cast")
    override fun getPlayers(): List<Player> {
        return (asField.get(GameEngine.B().bX.ay) as Array<Player?>).mapNotNull { it }.toList()
    }

    override suspend fun roomDetails(): String {
        return GameEngine.B().bX.at()
    }

    override fun sendChatMessage(message: String) {
        GameEngine.B().bX.m(message)
    }

    override fun sendSystemMessage(message: String) {
        GameEngine.B().bX.j(message)
    }

    override fun sendQuickGameCommand(command: String) {
        GameEngine.B().bX.k(command)
    }

    override fun pauseOrResumeGame(pause: Boolean) {
        if (isSinglePlayerGame) {
            _gameSpeed = if (pause) 0f else 1f
        } else {
            GameEngine.B().bX.aj = pause
            GameEngine.B().bX.ak = pause
        }
    }

    override fun sendMessageToPlayer(player: Player?, title: String?, message: String, color: Int) {
        if (player != null && player != localPlayer) {
            player.client?.sendPacketToClient(
                GamePacket.getChatPacket(
                    title,
                    message,
                    color
                )
            )
        } else {
            // New Message:
            io.github.rwpp.utils.Reflect.call(
                GameEngine.B().bX, "b", listOf(
                    com.corrodinggames.rts.gameFramework.j.c::class,
                    Int::class,
                    String::class,
                    String::class,
                ), listOf(null, color, title, message)
            )
        }
    }

    override fun sendSurrender(player: Player) {
        if (isHost) {
            val player = player as PlayerImpl
            Reflect.reifiedSet<com.corrodinggames.rts.game.n>(
                player as com.corrodinggames.rts.game.n, "at", true
            )
            val B = GameEngine.B()
            val b2: e = B.cf.b()
            b2.i = player
            b2.r = true // system action
            b2.u = 100 // type
            B.bX.a(b2)
        }
    }

    override fun spawnUnit(player: Player, unitType: UnitType, x: Float, y: Float, size: Int) {
        if (isHost) {
            val B = GameEngine.B()
            val b2: e = B.cf.b()
            b2.r = true // system action
            b2.i = player as com.corrodinggames.rts.game.n
            b2.u = 5 // type
            b2.a(x, y, unitType as com.corrodinggames.rts.game.units.`as`, size)
            B.bX.a(b2)
        }
    }

    override fun syncAllPlayer() {
        if (isHost) {
            appKoin.get<Game>().post {
                GameEngine.B().bX.a(false, false, true)
            }
        }
    }

    override fun addAI(count: Int) {
        repeat(count) {
            val B = GameEngine.B()
            if (isHost) {
                val var2 = com.corrodinggames.rts.game.n.G()
                if (var2 == -1) {
                    return@repeat
                }

                val var3 = com.corrodinggames.rts.game.a.a(var2)
                var3.v = "AI"
                var3.r = var2 % 2
                var3.x = B.bX.ay.f
                B.bX.aq()
                B.bX.d.a(var3)
                B.bX.e(null as c?)
            } else if (isHostServer) {
                sendQuickGameCommand("-addai")
            }
        }

        if (isHost) {
            updateUI()
        }
    }


    override fun applyRoomConfig(
        maxPlayerCount: Int,
        sharedControl: Boolean,
        startingCredits: Int,
        startingUnits: Int,
        fogMode: FogMode,
        aiDifficulty: Int,
        incomeMultiplier: Float,
        noNukes: Boolean,
        allowSpectators: Boolean,
        teamLock: Boolean,
        teamMode: TeamMode?
    ) {
        val B = GameEngine.B()
        if (isHost) {
            this.maxPlayerCount = maxPlayerCount
            this.sharedControl = sharedControl
            this.startingCredits = startingCredits
            this.startingUnits = startingUnits
            this.fogMode = fogMode
            this.aiDifficulty = aiDifficulty
            this.incomeMultiplier = incomeMultiplier
            this.noNukes = noNukes
            this.teamLock = teamLock
            this.allowSpectators = allowSpectators
        }

        if (isHostServer) {
            val e = GameEngine.B().bX.e()
            e.l = sharedControl
            e.c = startingCredits
            e.g = startingUnits
            e.f = aiDifficulty
            e.h = incomeMultiplier
            e.i = noNukes
            GameEngine.B().bX.a(e)
        }

        if (this.teamMode != teamMode) {
            this.teamMode = teamMode
            when (teamMode?.name) {
                "2t" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.a)
                "3t" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.b)
                "FFA" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.c)
                "spectators" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.d)
                null -> {}
                else -> synchronized(io.github.rwpp.core.Logic) { teamMode.onInit(this) }
            }
        }

        if (isHost) {
            updateUI()
        }
    }

    override fun remainingPlayersCount(): Int {
        return com.corrodinggames.rts.game.n.g()
    }

    override fun kickPlayer(player: Player) {
        val B = GameEngine.B()
        val p = (player as PlayerImpl).self
        B.bX.e(p)
        // playerCacheMap.remove(p) maybe kick didn't work out
    }

    override fun disconnect(reason: String) {
        singlePlayer = false
        isRWPPRoom = false
        option = RoomOption()
        bannedUnitList = listOf()
        roomMods = arrayOf()
        teamMode = null
        gameOver = false
        _gameSpeed = 1f

        if (isConnecting) GameEngine.B().bX.b(reason)
        DisconnectEvent(reason).broadcastIn()
    }

    override fun updateUI() {
        GameEngine.B().bX.f()
        GameEngine.B().bX.P()
        GameEngine.B().bX.L()


        val B = GameEngine.B()
        if (B.bX != null) {
            B.bX.O()
            B.bX.d.c()
        }
        if (GameEngine.aU) {
            return
        }
        if (B.bX != null && B.bX.aW) {
            return
        }
//        if (c != null) {
//            c.e.a(c.l)
//        }
    }

    override fun startGame() {
        val B = GameEngine.B()
        gameOver = false
        isGaming = true

        if (isHost || singlePlayer) {
            if (!singlePlayer && gameMapTransformer != null) {
                val xmlMap = XMLMap(selectedMap)
                gameMapTransformer!!.invoke(xmlMap)
                val path = "$mapDir/generated_${selectedMap.mapName + selectedMap.getMapSuffix()}"
                val file = xmlMap.saveToFile(path)
                B.bX.az = path
                io.github.rwpp.event.GlobalEventChannel.filter(io.github.rwpp.event.events.DisconnectEvent::class)
                    .subscribeOnce {
                        file.delete()
                    }
                B.bX.ay.a = com.corrodinggames.rts.gameFramework.j.ai.entries[1]
                updateUI()
            }
            B.bX.ae()
        }
    }

    private fun formatMapName(str: String?): String? {
        var str = str ?: return null
        if (str.contains(File.separator)) {
            val split: Array<String> =
                str.split(Pattern.quote(File.separator).toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            str = split[split.size - 1]
        }
        if (str.contains("/")) {
            val split2 = str.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            str = split2[split2.size - 1]
        }
        var str2: String? = null

        val matcher: Matcher = Pattern.compile("^l\\d*;\\[.*\\](.+)\\.tmx").matcher(str)
        if (matcher.matches()) {
            str2 = matcher.group(1)
            if (str2.isNotEmpty()) {
                str2 = str2.substring(0, 1).uppercase(Locale.getDefault()) + str2.substring(1)
            }
        }

        if (str2 == null) {
            val matcher2: Matcher = Pattern.compile("^l\\d*;(.+)\\.tmx").matcher(str)
            if (matcher2.matches()) {
                str2 = matcher2.group(1)
                if (str2.isNotEmpty()) {
                    str2 = str2.substring(0, 1).uppercase(Locale.getDefault()) + str2.substring(1)
                }
            }
        }
        if (str2 == null) {
            val matcher3: Matcher = Pattern.compile("^ *\\[.*\\](.+)\\.tmx").matcher(str)
            if (matcher3.matches()) {
                str2 = matcher3.group(1)
                if (str2.isNotEmpty()) {
                    str2 = str2.substring(0, 1).uppercase(Locale.getDefault()) + str2.substring(1)
                }
            }
        }
        if (str2 == null) {
            val matcher4: Matcher = Pattern.compile("(.*)\\.tmx").matcher(str)
            if (matcher4.matches()) {
                str2 = matcher4.group(1)
                if (str2.isNotEmpty()) {
                    str2 = str2.substring(0, 1).uppercase(Locale.getDefault()) + str2.substring(1)
                }
            }
        }
        if (str2 == null) {
            str2 = str
        }
        var replace = str2.replace('_', ' ')
        if (replace.endsWith(".rwsave")) {
            replace = replace.replace(".rwsave", VariableScope.nullOrMissingString)
        }
        return replace
    }


    private val asField = PlayerInternal::class.java.getDeclaredField("as")
        .apply { isAccessible = true }
}
