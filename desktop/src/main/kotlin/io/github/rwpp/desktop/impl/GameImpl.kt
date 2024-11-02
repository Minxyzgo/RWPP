/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import android.content.ServerContext
import android.graphics.Point
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import com.corrodinggames.librocket.scripts.Root
import com.corrodinggames.librocket.scripts.ScriptContext
import com.corrodinggames.librocket.scripts.ScriptEngine
import com.corrodinggames.rts.`R$drawable`
import com.corrodinggames.rts.`R$raw`
import com.corrodinggames.rts.gameFramework.ac
import com.corrodinggames.rts.gameFramework.j.`as`
import com.corrodinggames.rts.gameFramework.j.c
import com.corrodinggames.rts.gameFramework.j.k
import com.corrodinggames.rts.gameFramework.l
import com.corrodinggames.rts.gameFramework.n
import com.corrodinggames.rts.gameFramework.utility.ae
import com.corrodinggames.rts.java.Main
import com.corrodinggames.rts.java.audio.lwjgl.OpenALAudio
import com.corrodinggames.rts.java.b
import com.corrodinggames.rts.java.b.a
import com.corrodinggames.rts.java.u
import com.github.minxyzgo.rwij.InjectMode
import com.github.minxyzgo.rwij.InterruptResult
import com.github.minxyzgo.rwij.setFunction
import io.github.rwpp.*
import io.github.rwpp.config.Settings
import io.github.rwpp.core.Logic
import io.github.rwpp.desktop.*
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.*
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.data.RoomOption
import io.github.rwpp.game.map.*
import io.github.rwpp.game.mod.Mod
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.game.team.TeamMode
import io.github.rwpp.game.units.GameCommandActions
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.game.units.MovementType
import io.github.rwpp.net.Net
import io.github.rwpp.net.PacketType
import io.github.rwpp.net.packets.ModPacket
import io.github.rwpp.ui.LoadingContext
import io.github.rwpp.utils.Reflect
import kotlinx.coroutines.channels.Channel
import net.peanuuutz.tomlkt.Toml
import org.koin.core.annotation.Single
import org.koin.core.component.get
import org.lwjgl.opengl.Display
import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO
import javax.swing.SwingUtilities

@Single
class GameImpl : Game {
    private var _missions: List<Mission>? = null
    private var _allMaps: List<GameMap>? = null
    private var _maps = mutableMapOf<MapType, List<GameMap>>()
    private var _units: List<GameUnit>? = null
    private var lastAllUnits: java.util.ArrayList<*>? = null
    private val asField = PlayerInternal::class.java.getDeclaredField("as")
        .apply { isAccessible = true }
    private var playerCacheMap = mutableMapOf<com.corrodinggames.rts.game.n, Player>()
    private var threadConnector: com.corrodinggames.rts.gameFramework.j.an? = null
    private var isSandboxGame: Boolean = false
    private var bannedUnitList: List<String> = listOf()
    private var roomMods: Array<String> = arrayOf()

    override val gameRoom: GameRoom by lazy {
        val B = l.B()
        with(B.bX.ay) {
            object : GameRoom {
                override var maxPlayerCount: Int
                    get() = PlayerInternal.c
                    set(value) { if(maxPlayerCount != value) PlayerInternal.b(value, true) }
                override val isHost: Boolean
                    get() =  B.bX.C || isSandboxGame
                override val isHostServer: Boolean
                    get() = B.bX.H
                override val localPlayer: Player
                    get() {
                        val p = playerCacheMap[B.bX.z]
                        if(p == null) getPlayers()
                        return playerCacheMap[B.bX.z] ?: PlayerImpl(B.bs, this)
                    }
                override var sharedControl: Boolean
                    get() = l
                    set(value) { l = value }
                override val randomSeed: Int
                    get() = q
                override val mapType: MapType
                    get() = MapType.entries[a.ordinal]
                override var selectedMap: GameMap
                    get() = getAllMaps().firstOrNull { (it.mapName + it.getMapSuffix()).replace("\\", "/").endsWith(B.bX.ay.b ?: "") }
                        ?: NetworkMap(mapNameFormatMethod.invoke(null, B.bX.ay.b) as String)
                    set(value) {
                        if(isHostServer) {
                            B.bX.a(
                                B.bX.e().apply {
                                    b = (value.mapName + value.getMapSuffix())
                                }
                            )
                        } else {
                            val realPath = (
                                    when(value.mapType) {
                                        MapType.SkirmishMap -> "maps/skirmish/"
                                        MapType.CustomMap -> "mods/maps/"
                                        MapType.SavedGame -> "saves/"
                                        else -> ""
                                    }) +  (value.mapName + value.getMapSuffix()).replace("\\", "/")
                            B.bX.az = realPath
                            B.bX.ay.a = com.corrodinggames.rts.gameFramework.j.ai.entries[value.mapType.ordinal]
                            b = (value.mapName + value.getMapSuffix())
                            LClass.B().bX.L() // send server info
                        }
                    }
                override var startingCredits: Int
                    get() = c
                    set(value) { c = value }
                override var startingUnits: Int
                    get() = g
                    set(value) { g = value }
                override var fogMode: FogMode
                    get() = FogMode.entries[d]
                    set(value) { d = value.ordinal }
                override val revealedMap: Boolean
                    get() = e
                override var aiDifficulty: Difficulty
                    get() = Difficulty.entries[f + 2]
                    set(value) { f = value.ordinal - 2 }
                override var incomeMultiplier: Float
                    get() = h
                    set(value) { h = value }
                override var noNukes: Boolean
                    get() = i
                    set(value) { i = value }
                override var allowSpectators: Boolean
                    get() = o
                    set(value) { o = value }
                override var lockedRoom: Boolean
                    get() = p
                    set(value) {
                        p = value
                        if(isHost && value) sendSystemMessage("Room has been locked. Now player can't join the room")
                    }
                override var teamLock: Boolean
                    get() = m
                    set(value) { m = value }
                override val mods: Array<String>
                    get() = roomMods
                override var isRWPPRoom: Boolean = false
                override var option: RoomOption = RoomOption()
                override val isConnecting: Boolean
                    get() = LClass.B().bX.B
                override val isStartGame: Boolean
                    get() = isGaming
                override val teamMode: TeamMode?
                    get() = _teamMode

                private var _teamMode: TeamMode? = null

                private val mapNameFormatMethod = com.corrodinggames.rts.appFramework.i::class.java.getDeclaredMethod("e", String::class.java)

                @Suppress("unchecked_cast")
                override fun getPlayers(): List<Player> {
                    return (asField.get(B.bX.ay) as Array<com.corrodinggames.rts.game.n?>).mapNotNull {
                        if(it == null) return@mapNotNull null
                        playerCacheMap.getOrPut(it) { PlayerImpl(it, this).also { p -> PlayerJoinEvent(p).broadCastIn() } }
                    }
                }

                override suspend fun roomDetails(): String {
                    return B.bX.at()
                }

                override fun sendChatMessage(message: String) {
                    B.bX.m(message)
                }

                override fun sendSystemMessage(message: String) {
                    B.bX.j(message)
                }

                override fun sendQuickGameCommand(command: String) {
                    B.bX.k(command)
                }

                override fun addAI(count: Int) {
                    repeat(count) {
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
                    aiDifficulty: Difficulty,
                    incomeMultiplier: Float,
                    noNukes: Boolean,
                    allowSpectators: Boolean,
                    teamLock: Boolean,
                    teamMode: TeamMode?
                ) {
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
                        val e = LClass.B().bX.e()
                        e.l = sharedControl
                        e.c = startingCredits
                        e.g = startingUnits
                        e.f = aiDifficulty.ordinal - 2
                        e.h = incomeMultiplier
                        e.i = noNukes
                        LClass.B().bX.a(e)
                    }

                    if (_teamMode != teamMode) {
                        _teamMode = teamMode
                        when (teamMode?.name) {
                            "2t" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.a)
                            "3t" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.b)
                            "FFA" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.c)
                            "spectators" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.d)
                            null -> {}
                            else -> synchronized(Logic) { teamMode.onInit(this) }
                        }
                    }

                    if (isHost) {
                        LClass.B().bX.f()
                        LClass.B().bX.P()
                        LClass.B().bX.L()
                        updateUI()
                    }
                }

                override fun kickPlayer(player: Player) {
                    val p = (player as PlayerImpl).player
                    B.bX.e(p)
                    // playerCacheMap.remove(p) maybe kick didn't work out
                }

                override fun disconnect() {
                    playerCacheMap.clear()
                    isSandboxGame = false
                    isRWPPRoom = false
                    option = RoomOption()
                    bannedUnitList = listOf()
                    roomMods = arrayOf()
                    _teamMode = null
                    if(isConnecting) B.bX.b("exited")
                    B.bX.ay.a = GameMapType.a
                    B.bX.az = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
                    B.bX.ay.b = "[z;p10]Crossing Large (10p).tmx"
                }

                override fun updateUI() {
                    com.corrodinggames.rts.appFramework.n::class.java.getDeclaredMethod("o").invoke(null)
                }

                override fun startGame() {
                    rwppVisibleSetter(false)
                    gameCanvas.isVisible = true
                    gameCanvas.requestFocus()
                    isGaming = true

                    if(isHost || isSandboxGame) {
                        B.bX.ae()
                    }
                }
            }
        }
    }

    init {
        Root::class.setFunction {
            addProxy(Root::showMainMenu) {
                if(isGaming) {
                    if(isSandboxGame) gameRoom.disconnect()
                    l.B().bS.u = false
                    gameCanvas.isVisible = false
                    rwppVisibleSetter(true)
                    isGaming = false
                    com.corrodinggames.librocket.a.a().b()
//                    game.bQ.slick2dFullScreen = false
//                    GameImpl.game.g()
                    val libRocket = ScriptContext::class.java.getDeclaredField("libRocket").run {
                        isAccessible = true
                        get(ScriptEngine.getInstance().root)
                    } as com.corrodinggames.librocket.b
                    libRocket.closeActiveDocument()
                    libRocket.clearHistory()

                    ReturnMainMenuEvent().broadCastIn()
                }
            }

            addProxy(Root::showBattleroom) {
                if(isGaming) {
                    l.B().bS.u = false
                    gameCanvas.isVisible = false
                    rwppVisibleSetter(true)
                    isGaming = false
//                    game.bQ.slick2dFullScreen = false
//                    GameImpl.game.g()
                    val libRocket = ScriptContext::class.java.getDeclaredField("libRocket").run {
                        isAccessible = true
                        get(ScriptEngine.getInstance().root)
                    } as com.corrodinggames.librocket.b
                    libRocket.closeActiveDocument()
                    libRocket.clearHistory()
                }
            }

            addProxy(Root::receiveChatMessage) { _, spawn, s, s1, _ ->
                if(playerCacheMap.isEmpty()) gameRoom.getPlayers()
                ChatMessageEvent(s ?: "", s1 ?: "", spawn).broadCastIn()
            }

            addProxy(Root::makeSendMessagePopup) {
                SwingUtilities.invokeLater {
                   showSendMessageDialog()
                }
            }

            addProxy(Root::makeSendTeamMessagePopupWithDefaultText) { _, str ->
                SwingUtilities.invokeLater {
                    showSendMessageDialog()
                }
            }
        }

        Main::class.setFunction {
            addProxy("c") {
                RefreshUIEvent().broadCastIn()
            }

            addProxy("c", c::class, String::class, String::class) { _: Any?, c: c, _: Any?, _: Any? ->
                if(get<Settings>().showWelcomeMessage != true) return@addProxy Unit
                val rwOutputStream = RwOutputStream()
                rwOutputStream.c(welcomeMessage)
                rwOutputStream.c(3)
                rwOutputStream.b("RWPP")
                rwOutputStream.a(null as c?)
                rwOutputStream.a(-1)
                LClass.B().bX.a(c, rwOutputStream.b(141))
            }

            addProxy("b", "()") { m: Main ->
                m.f.a(Runnable
                // from class: com.corrodinggames.rts.java.Main.3
                // java.lang.Runnable
                {
                    val B: l = l.B()
                    com.corrodinggames.rts.appFramework.n::class.java.getDeclaredMethod("r").invoke(null)
                    if(B.bL == null || !B.bL.W) {
                        B.bX.af()
                        return@Runnable
                    }
                    B.bX.bd = true
                    B.bH = false
                    B.aq = false
                    m.i.c(false)
                    com.corrodinggames.librocket.a.a().f()
                    m.p.activeDocument
                    if(m.p.c != null) {
                        m.p.c.root.resumeNonMenu()
                        return@Runnable
                    }
                    l.T()
                })

                StartGameEvent().broadCastIn()

            }
        }

        com.corrodinggames.rts.java.b.a::class.setFunction {
            addProxy(com.corrodinggames.rts.java.b.a::p) {
                ServerCallbackImpl().apply {
                    f = main
                }
            }
        }

        val vField = com.corrodinggames.rts.gameFramework.e::class.java.getDeclaredField("v").apply { isAccessible = true }
        val settings = appKoin.get<Settings>()
        com.corrodinggames.rts.gameFramework.e::class.setFunction {
            addProxy("a", com.corrodinggames.rts.gameFramework.j.`as`::class, mode = InjectMode.InsertBefore) { self: com.corrodinggames.rts.gameFramework.e ->
                if (settings.enhancedReinforceTroops) {
                    val actionString = self.k.a()
                    if (actionString != "-1") {
                        val l = vField.get(self) as List<com.corrodinggames.rts.game.units.y>
                        val m = com.corrodinggames.rts.gameFramework.utility.m(l.sortedBy { (it as? com.corrodinggames.rts.game.units.d.l)?.dx()?.size ?: 0 })
                        vField.set(self, m)
                    }
                }

                Unit
            }
        }

        // ban units proxy
        com.corrodinggames.rts.gameFramework.j.ad::class.setFunction {
            addProxy("a", com.corrodinggames.rts.gameFramework.e::class, mode = InjectMode.InsertBefore) { _: Any?, b3: com.corrodinggames.rts.gameFramework.e ->
                val actionString = b3.k.a()
                if(actionString.startsWith("u_")) {
                    if(actionString.removePrefix("u_").removePrefix("c_") in bannedUnitList) {
                        return@addProxy InterruptResult(Unit)
                    }
                }
                if(b3.j == null) return@addProxy Unit
                val realAction = GameCommandActions.from(b3.j.d().ordinal)
                val u = b3.j.a()
                if(u is com.corrodinggames.rts.game.units.`as`) {
                    if(realAction == GameCommandActions.BUILD && u.v() in bannedUnitList) {
                        InterruptResult(Unit)
                    } else Unit
                } else Unit
            }
            addProxy("c", com.corrodinggames.rts.gameFramework.j.au::class, mode = InjectMode.InsertBefore) { _: Any?, auVar: com.corrodinggames.rts.gameFramework.j.au ->
                when(val type = auVar.b) {
                    PacketType.PREREGISTER_INFO.type -> {
                        with(LClass.B().bX) {
                            if(this.C) return@with
                            val kVar16 = k(auVar)
                            val cVar14: c = auVar.a
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

                    PacketType.MOD_DOWNLOAD_REQUEST.type -> {
                        if(gameRoom.isHost) {
                            val B = LClass.B()
                            val c: c = auVar.a

                            if(!gameRoom.option.canTransferMod)
                                B.bX.a(c, "Server didn't support transferring mods.")
                            else {
                                val k = k(auVar)
                                val str = k.l()

                                try {
                                    val mods = str.split(";")
                                    mods.map(get<ModManager>()::getModByName).forEachIndexed { i, m ->
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
                        val B = LClass.B()
                        val k = k(auVar)
                        val c: c = auVar.a
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
                                val modManager = get<ModManager>()
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
                            val packetType = PacketType.from(type)
                            val listener = get<Net>().listeners[packetType]
                            if (listener != null) {
                                listener.invoke(
                                    ClientImpl(auVar.a),
                                    get<Net>().packetDecoders[packetType]!!.invoke(
                                        DataInputStream(
                                            ByteArrayInputStream(auVar.c)
                                        )
                                    )
                                )
                                return@addProxy InterruptResult(Unit)
                            }
                        }

                        Unit
                    }
                }
            }

            addProxy("g", c::class) { _: Any?, c: c ->
                val asVar: `as` = `as`()
                try {
                    val B = LClass.B()
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
        }

        com.corrodinggames.rts.game.units.custom.l::class.setFunction {
            addProxy("a", com.corrodinggames.rts.game.units.custom.ab::class, java.util.HashMap::class) { p1: Any?, p2: HashMap<Any?, com.corrodinggames.rts.game.units.custom.ac> ->
                val allMods = buildList {
                    p2.values.forEach { ac ->
                        val name = ac::class.java.getDeclaredField("a")
                            .also { it.isAccessible = true }
                            .get(ac)
                        if(name != null && name != "null") add(name as String)
                    }
                }

                roomMods = allMods.toTypedArray()

                val met = UnitEngine::class.java.getDeclaredMethod("__proxy__a",
                    com.corrodinggames.rts.game.units.custom.ab::class.java, java.util.HashMap::class.java).apply { isAccessible = true }
                try {
                    met.invoke(null, p1, p2)
                } catch (e: Exception) {

                    run {
                        val modManager = get<ModManager>()
                        if(allMods.all { modManager.getModByName(it) != null }) {
                            modManager.getAllMods().forEach { it.isEnabled = it.name in allMods }
                            CallReloadModEvent().broadCastIn()
                            return@run
                        }

                        val modsName = modManager.getAllMods().map { it.name }
                        if(gameRoom.option.canTransferMod) {
                            get<Net>().sendPacketToServer(ModPacket.RequestPacket(allMods.filter { it !in modsName }.joinToString(";")))
                            CallStartDownloadModEvent().broadCastIn()
                        } else {
                            gameRoom.disconnect()
                            KickedEvent(e.cause?.message ?: "").broadCastIn()
                        }
                    }
                }
                Unit
            }
        }

        com.corrodinggames.rts.gameFramework.e.c::class.setFunction {
            addProxy("f", String::class, mode = InjectMode.InsertBefore) { _: Any?, str: String ->
                if(get<ExternalHandler>().getUsingResource() == null
                    || str.contains("builtin_mods")
                    || (str.contains("maps") && !str.contains("bitmaps"))
                    || str.contains("translations")) return@addProxy Unit
                val result = Reflect.call<com.corrodinggames.rts.gameFramework.e.c, String>(
                    com.corrodinggames.rts.gameFramework.e.a.b,
                    "__proxy__f",
                    listOf(String::class),
                    listOf(str)
                )

                if(result?.contains("assets") != true) return@addProxy InterruptResult(result)

                InterruptResult(resourceOutputDir + result.removePrefix("assets/"))
            }

            addProxy("i", String::class, mode = InjectMode.InsertBefore) { _: Any?, str: String ->
                if(get<ExternalHandler>().getUsingResource() == null
                    || str.contains("builtin_mods")
                    || (str.contains("maps") && !str.contains("bitmaps"))
                    || str.contains("translations")) return@addProxy Unit
                val o = str.let {
                    if(it.startsWith("assets/") || it.startsWith("assets\\"))
                        it.removePrefix("assets/") else it
                }


                val s3 = resourceOutputDir + o
                kotlin.runCatching {
                    InterruptResult(com.corrodinggames.rts.gameFramework.utility.j(
                        FileInputStream(s3), s3, o
                    ))
                }.getOrElse { InterruptResult(null) }
            }
        }

        com.corrodinggames.rts.gameFramework.f::class.setFunction {
            addProxy("f", Int::class) { i: Int ->
                val a2: String? = FClass.a(`R$drawable`::class.java, i)
                val resFileExist = File(resOutputDir).exists()
                if (a2 != null) {
                    return@addProxy com.corrodinggames.rts.gameFramework.e.a.a("${if(resFileExist) resOutputDir else "res/"}drawable", a2)

                }
                val a3: String? = FClass.a(`R$raw`::class.java, i)
                if (a3 != null) {
                    return@addProxy com.corrodinggames.rts.gameFramework.e.a.a("${if(resFileExist) resOutputDir else "res/"}raw", a3)

                }
                return@addProxy null

            }
        }

        com.corrodinggames.librocket.b::class.setFunction {
            addProxy("a", String::class, mode = InjectMode.InsertBefore) { str: String ->
                val o = FClass.o(str)
                val resFileExist = File(resOutputDir).exists()
                if(o.startsWith("drawable:") && resFileExist) {
                    InterruptResult(com.corrodinggames.librocket.b.b + resOutputDir + "drawable/" + o.removePrefix("drawable:"))
                } else Unit
            }
        }

        com.corrodinggames.rts.game.n::class.setFunction {
            addProxy("I", mode = InjectMode.InsertBefore) { self: com.corrodinggames.rts.game.n ->
                playerCacheMap[self]?.let { PlayerLeaveEvent(it).broadCastIn() }
                playerCacheMap.remove(self)
            }
        }

        GlobalEventChannel.filter(StartGameEvent::class).subscribeAlways {
            rwppVisibleSetter(false)
            gameCanvas.isVisible = true
            gameCanvas.requestFocus()
            isGaming = true
        }
    }

    override fun startNewMissionGame(difficulty: Difficulty, mission: Mission) {
        rwppVisibleSetter(false)
        gameCanvas.isVisible = true

        gameCanvas.requestFocus()

        isGaming = true
        container.post {
            val root = ScriptEngine.getInstance().root
            val libRocket = ScriptContext::class.java.getDeclaredField("libRocket").run {
                isAccessible = true
                get(root)
            } as com.corrodinggames.librocket.b
            val guiEngine = ScriptContext::class.java.getDeclaredField("guiEngine").run {
                isAccessible = true
                get(root)
            } as a
            //root.loadConfigAndStartNew("maps/normal/${mission.tmx.name}")
            val game = l.B()
            game.bQ.aiDifficulty = difficulty.ordinal - 2 // fuck code

            guiEngine.b(true)
            guiEngine.c(false)
            val met = IClass::class.java.getDeclaredMethod("a", String::class.java, Boolean::class.java, Int::class.java, Int::class.java, Boolean::class.java, Boolean::class.java)
            met.invoke(null, "maps/${mission.type.pathName()}/${(mission.mapName + mission.getMapSuffix())}", false, 0, 0, true, false)

            guiEngine.f()
            libRocket.closeActiveDocument()
            libRocket.clearHistory()
        }
    }

    override suspend fun load(context: LoadingContext): Unit = with(context) {

        gameThread = Thread {
            Display.setParent(gameCanvas)
            container.start()
        }

        gameThread.isDaemon = true
        gameThread.start()

        Main::class.java.declaredConstructors[0].apply {
            isAccessible = true
            main = newInstance() as Main
        }

        val receivedChannel = Channel<Unit>(1)

        container.post {
            val nHelper = object : n() {
                val i = com.corrodinggames.rts.java.i(main)

                override fun a(p0: String, p1: Int) {
                    if(p0.startsWith("kicked", ignoreCase = true)) {
                        KickedEvent(p0).broadCastIn()
                    } else {
                        i.a(p0, p1)
                    }
                }

                override fun a(p0: String, p1: String) {
                    if(p0.startsWith("Briefing", ignoreCase = true) || p0.startsWith("Players", ignoreCase = true)) {
                        i.a(p0, p1)
                    } else {
                        KickedEvent("$p0: $p1").broadCastIn()
                    }
                }

                override fun a(p0: String, p1: Boolean) {
                    println(p0)
                    message(p0)
                }

                override fun a(p0: Throwable?) {
                    p0?.let { throw it }
                }
            }

            //return@with
            //Display.create()

            // copy from Main.h
            // ignore noresources
            l.aU = true
            l.bb = true
            l.aX = true
            l.aW = true
            l.bg = EClass::class.java
            val openALAudio = OpenALAudio(20, 9, 512)

            com.corrodinggames.rts.gameFramework.a.e.c = com.corrodinggames.rts.java.o(openALAudio);
            AMClass.a = com.corrodinggames.rts.java.l(openALAudio)
            com.corrodinggames.rts.gameFramework.j.n.d = com.corrodinggames.rts.java.k()
            ac.b = VClass()

            main.g()

            with(main) {
                d = DClass()
                i = a.p()
                i.f = this
                p = AClass()
                i.a(p, d)
                p.debug = false
                p.setup()
                this.p.loadFont("font/Delicious-Roman.otf");
                this.p.loadFont("font/Delicious-Italic.otf");
                this.p.loadFont("font/Delicious-Bold.otf");
                this.p.loadFont("font/Delicious-BoldItalic.otf");
                this.p.loadFont("font/Roboto-Regular.ttf");
                this.p.loadFont("font/Roboto-Bold.ttf");
                println("NotoSansCJKsc start");
                this.p.loadFont("font/NotoSansCJKsc-Regular.otf", "notoSans");
                this.p.loadFont("font/DroidSansFallback.ttf", "fallback");
                println("NotoSansCJKsc end");
                this.i.c();
                println("end libRocket setup");
                b("GuiEngine");
                LClass.dz = this.e;
            }

            println("GameEngine")


            l.ck = Point(displaySize.width, displaySize.height)
            val serverContext = ServerContext()
            val a3 = l.a(serverContext, nHelper)

            with(main) {
                i.b()
                com.corrodinggames.rts.a.a.b()
                h = a3.bX
                a3.bQ.slick2dFullScreen = false
                a3.bQ.showZoomButton = false
                a3.bQ.showUnitGroups = false
                j = game
                com.corrodinggames.rts.java.u::class.java.getDeclaredField("c").apply {
                    isAccessible = true
                    set(game, container)
                }
                //container.post {
                    j.a(d)
                    j.a(displaySize.width, displaySize.height)
                //}
                println("----- Game init finished -----")
                a3.bX.d = this
                a3.bX.y = "unset"
            }

            u::class.java.getDeclaredField("r").apply {
                isAccessible = true
                set(game, true)
            }

            u::class.java.getDeclaredField("b").apply {
                isAccessible = true
                set(game, main)
            }


            l.B().bQ.sendReports = false // why luke should know
            receivedChannel.trySend(Unit)

            //Display.destroy()
        }

        //q = true
        // r = true // is reloaded
        receivedChannel.receive()
    }

    override fun hostStartWithPasswordAndMods(
        isPublic: Boolean,
        password: String?,
        useMods: Boolean,
    ) {
        val B = l.B()
        B.bX.n = password
        B.bX.q = isPublic
        B.bX.o = useMods
        gameRoom.isRWPPRoom = true
        if(B.bX.b(false)) {
            B.bX.ay.a = GameMapType.a
            B.bX.az = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
            B.bX.ay.b = "[z;p10]Crossing Large (10p).tmx"
        }
    }

    override fun hostNewSinglePlayer(sandbox: Boolean) {
        container.post {

            com.corrodinggames.rts.game.n.F()

            val root = ScriptEngine.getInstance().root
            val libRocket = ScriptContext::class.java.getDeclaredField("libRocket").run {
                isAccessible = true
                get(root)
            } as com.corrodinggames.librocket.b
            val guiEngine = ScriptContext::class.java.getDeclaredField("guiEngine").run {
                isAccessible = true
                get(root)
            } as a

            val game = l.B()
            game.bQ.aiDifficulty = Difficulty.Hard.ordinal - 2 // fuck code

            guiEngine.b(true)
            guiEngine.c(false)

            val met = IClass::class.java.getDeclaredMethod(
                "a",
                String::class.java,
                Boolean::class.java,
                Int::class.java,
                Int::class.java,
                Boolean::class.java,
                Boolean::class.java
            )
            met.invoke(null, "maps/skirmish/[z;p10]Crossing Large (10p).tmx", false, 0, 0, true, false)

            game.bX.ay.a = GameMapType.a
            game.bX.az = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
            game.bX.ay.b = "[z;p10]Crossing Large (10p).tmx"

            if (sandbox) {
                game.bL.E = false
                game.bS.y()
                game.bv = true
            } else {
                game.bv = false
            }


            game.bX.y = "You"
            game.bX.o = true
            val S: Boolean = if (sandbox) game.bX.R() else game.bX.S()

            if(S) {
                val e = game.bX.e()
                if(e != null) {
                    e.f = game.bQ.aiDifficulty
                    game.bX.a(e)
                }

                isSandboxGame = true
            }


            RefreshUIEvent().broadCastIn()
        }
    }

    override fun setUserName(name: String) {
        l.B().bX.a(name)
    }

    override suspend fun directJoinServer(address: String, uuid: String?, context: LoadingContext): Result<String> {
        context.message("Connecting...")

        val trim = restrictedString(address.trim())
        if(uuid == null) l.B().bQ.lastNetworkIP = trim
        l.B().bX.bw = uuid
        var result: Result<String>? = null
        val resultChannel = Channel<Unit>(1)
        threadConnector = l.B().bX.a(trim, true) {
            result = if(threadConnector!!.e != null) {
                Result.failure(IOException("Connection failed: ${threadConnector!!.e}."))
            } else Result.success("")
            resultChannel.trySend(Unit)
        }

        resultChannel.receive()

        if(result!!.isSuccess) {
            try {
                l.B().bX.b("staring new")
                l.B().bX.a(threadConnector!!.g)
            } catch(e: IOException) {
                e.printStackTrace()
                result = Result.failure(IOException("Connection failed"))
            }
        }

        threadConnector = null

        return result!!
    }

    override fun cancelJoinServer() {
        threadConnector?.a()
    }

    override fun onQuestionCallback(option: String) {
        rcnOption = option
    }

    override fun setTeamUnitCapHostGame(cap: Int) {
        l.B().bX.ax = cap
        l.B().bX.aw = cap
        l.B().bC = cap
    }

    override fun getAllMissionTypes(): List<MissionType> {
        return listOf(MissionType.Normal, MissionType.Challenge, MissionType.Survival)
    }

    override fun getAllMissions(): List<Mission> {
        if(_missions != null) return _missions!!
        val missions = mutableListOf<Mission>()

        getAllMissionTypes().forEach { type ->
            File("assets/maps/${type.pathName()}")
                .walk()
                .filter { it.name.endsWith(".tmx") }
                .forEachIndexed { i, f ->
                    missions.add(object : Mission {
                        override val id: Int = i
                        override val name: String =
                            if(type == MissionType.Normal)
                                f.name.split("__-__")[1].removeSuffix(".tmx")
                            else f.name.removeSuffix(".tmx")
                        override val type: MissionType
                            get() = type
                        override val image: Painter =
                            ImageIO.read(File(f.parent!! + "/" + f.name.removeSuffix(".tmx") + "_map.png")).toPainter()
                        override val mapName: String
                            get() = f.nameWithoutExtension
                        override fun displayName(): String {
                            return ScriptEngine.getInstance().root.convertMapName(name)
                        }
                        override val mapType: MapType
                            get() = MapType.SkirmishMap
                    })
                }
        }


        return missions.toList().also { _missions = it }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAllMaps(flush: Boolean): List<GameMap> {
        if(_maps.isEmpty() || flush) {
            val B = LClass.B()
            val levelDirs = com.corrodinggames.rts.gameFramework.e.a.a("/SD/rusted_warfare_maps", true)
            val mapFolders = mapOf(
                MapType.SkirmishMap to File("assets/maps/skirmish"),
                MapType.CustomMap to B.bZ.a(levelDirs, "/SD/rusted_warfare_maps"),
                MapType.SavedGame to File("saves")
            )
            for((type, folder) in mapFolders) {
                val maps = mutableListOf<GameMap>()
                if (folder is File) {
                    folder
                        .walk()
                        .filter { it.name.endsWith(".tmx") || it.name.endsWith(".rwsave") }
                        .forEachIndexed { i, f ->
                            maps.add(object : GameMap {
                                override val id: Int = i
                                override val image: Painter? =
                                    if(mapType != MapType.SavedGame) File(f.parent!! + "/" + f.name.removeSuffix(".tmx") + "_map.png")
                                        .let { if(it.exists()) ImageIO.read(it).toPainter() else null }
                                    else null
                                override val mapName: String
                                    get() = f.nameWithoutExtension
                                override val mapType: MapType
                                    get() = type
                            })
                        }
                } else if (folder is Array<*>) {
                    folder as Array<String>
                    folder.forEachIndexed { i, name ->
                        maps.add(object : GameMap {
                            override val id: Int = i
                            override val image: Painter? =
                                if(type == MapType.CustomMap) {
                                    if (name.contains("MOD|")) //TODO 实现mod图片
                                        null
                                    else loadImage(
                                        com.corrodinggames.rts.appFramework.c.c(name)
                                    )?.toPainter()
                                } else null
                            override val mapName: String
                                get() = name.removeSuffix(".tmx").removeSuffix(".rwsave")
                            override val mapType: MapType
                                get() = type

                            override fun displayName(): String {
                                return ScriptEngine.getInstance().root.convertMapName(name)
                            }
                        })
                    }
                }

                _maps[type] = maps.toList()
            }
        }

        return if (!flush && _allMaps != null)
            _allMaps!!
        else buildList { _maps.values.forEach(::addAll) }.also { _allMaps = it }
    }

    override fun getAllMapsByMapType(mapType: MapType): List<GameMap> {
        if(_maps.isEmpty()) getAllMaps()
        return _maps[mapType]!!
    }

    override fun getMissionsByType(type: MissionType): List<Mission> =
        getAllMissions().filter { it.type == type }

    override fun getStartingUnitOptions(): List<Pair<Int, String>> {
        val B: l = LClass.B()
        val list = mutableListOf<Pair<Int, String>>()
        val it: Iterator<*> = B.bX.i().iterator()
        while(it.hasNext()) {
            val num = it.next() as Int
            list.add(num to B.bX.d(num))
        }
        return list
    }
    @Suppress("UNCHECKED_CAST")
    override fun getAllUnits(): List<GameUnit> {
        val gameUnits = (com.corrodinggames.rts.game.units.ar.ae as ArrayList<com.corrodinggames.rts.game.units.`as`>)
        if(_units == null || lastAllUnits != gameUnits) {
            _units = gameUnits.map {
                object : GameUnit {
                    override val name: String
                        get() = it.v()
                    override val displayName: String
                        get() = it.e()
                    override val description: String
                        get() = it.f()
                    // Nevertheless, we can change it soon
//                    override val painter: Painter? = (it as? com.corrodinggames.rts.game.units.custom.l)?.ad?.let {
//                            runCatching {
//                                ImageIO.read(File(it.a().replace("/", "\\"))).toPainter()
//                            }.getOrElse { e ->
//                                println("error on reading path:${it.a()}")
//                                e.printStackTrace()
//                                null
//                            }
//                        }
                    override val movementType: MovementType
                        get() = MovementType.valueOf(it.o().name)
                    override val mod: Mod?
                        get() = (it as? com.corrodinggames.rts.game.units.custom.l)?.J?.s?.let(get<ModManager>()::getModByName)
                }
            }

            lastAllUnits = gameUnits
        }

        return _units!!
    }

    override fun onBanUnits(units: List<GameUnit>) {
        bannedUnitList = units.map(GameUnit::name)
        if(units.isNotEmpty())
            gameRoom.sendSystemMessage("Host has banned these units (房间已经ban以下单位): ${units.map(GameUnit::displayName).joinToString(", ")}")
    }

    override fun getAllReplays(): List<Replay> {
        return Reflect.call<com.corrodinggames.rts.appFramework.q, Array<String>>(
            null, "l", emptyList(), emptyList()
        )!!.mapIndexed { i, str ->
            object : Replay {
                override val id: Int = i
                override val name: String = str
                override fun displayName(): String {
                    return ScriptEngine.getInstance().root.convertMapName(name)
                }
            }
        }
    }

    override fun watchReplay(replay: Replay) {
        rwppVisibleSetter(false)
        gameCanvas.isVisible = true

        gameCanvas.requestFocus()

        isGaming = true

        container.post {
            ScriptEngine.getInstance().root.loadReplay(replay.name)
        }
    }

    override fun isGameCouldContinue(): Boolean {
        return ScriptEngine.getInstance().root.canResume()
    }

    override fun continueGame() {
        rwppVisibleSetter(false)
        gameCanvas.isVisible = true

        gameCanvas.requestFocus()

        isGaming = true

        container.post {
            ScriptEngine.getInstance().root.resumeNonMenu()
        }
    }


    private fun restrictedString(str: String?): String? {
        return str?.replace("'", ".")?.replace("\"", ".")?.replace("(", ".")?.replace(")", ".")?.replace(",", ".")
            ?.replace("<", ".")?.replace(">", ".")
    }

    private fun escapedString(str: String): String {
        return "'" + str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;")
            .replace("\"", "&quot;").replace("\${", "$ {") + "'"
    }

    private fun loadImage(path: String): BufferedImage? {
        val fileInputStream: InputStream?
        val bufferedInputStream: BufferedInputStream
        val a2 = ae.a(path)
        if (a2 != null) {
            fileInputStream = a2.b(path, true)
            if (fileInputStream == null) {
                println("Failed to open zipped file: $path")
                return null
            }
        } else {
            try {
                fileInputStream = FileInputStream("mods/maps/$path")
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        try {
            bufferedInputStream = BufferedInputStream(fileInputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        bufferedInputStream.use { stream ->
            return ImageIO.read(stream)
        }
    }


    internal companion object {
        internal lateinit var main: Main
        internal lateinit var gameThread: Thread
        val game = RwInternalGameImpl(Main.c)
        internal val container by lazy { RWPPContainer() }
        @Volatile
        internal var isGaming = false
        internal var rcnOption: String? = null
    }

    class RWPPContainer : b(game, displaySize.width, displaySize.height, false) {
        private val channel = Channel<() -> Unit>(Channel.UNLIMITED)

        /**
         * try to post an action to the game thread (running in Opengl context)
         */
        fun post(action: () -> Unit) {
            channel.trySend(action)
        }

        /**
         * post an action. It will return until the action was finished
         */
        suspend fun waitPost(action: () -> Unit) {
            val ec = Channel<Unit>(1)
            channel.send {
                action()
                ec.trySend(Unit)
            }
            ec.receive()
        }

        override fun updateAndRender(p0: Int) {
            val f = channel.tryReceive()
            f.getOrNull()?.invoke()

            //if(!gameCanvas.isVisible) return

            super.updateAndRender(p0)
        }
    }
}