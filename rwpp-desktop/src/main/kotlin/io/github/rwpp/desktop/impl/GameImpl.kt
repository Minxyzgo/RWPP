/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import android.content.ServerContext
import android.graphics.Point
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import com.corrodinggames.librocket.scripts.ScriptContext
import com.corrodinggames.librocket.scripts.ScriptEngine
import com.corrodinggames.rts.gameFramework.ac
import com.corrodinggames.rts.gameFramework.j.c
import com.corrodinggames.rts.gameFramework.l
import com.corrodinggames.rts.gameFramework.n
import com.corrodinggames.rts.gameFramework.utility.ae
import com.corrodinggames.rts.java.Main
import com.corrodinggames.rts.java.audio.lwjgl.OpenALAudio
import com.corrodinggames.rts.java.b
import com.corrodinggames.rts.java.b.a
import com.corrodinggames.rts.java.u
import io.github.rwpp.core.Logic
import io.github.rwpp.core.UI
import io.github.rwpp.desktop.*
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.*
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.data.RoomOption
import io.github.rwpp.game.map.*
import io.github.rwpp.game.mod.Mod
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.game.team.TeamMode
import io.github.rwpp.game.units.UnitType
import io.github.rwpp.game.units.MovementType
import io.github.rwpp.game.world.World
import io.github.rwpp.logger
import io.github.rwpp.net.Packet
import io.github.rwpp.net.packets.GamePacket
import io.github.rwpp.ui.LoadingContext
import io.github.rwpp.utils.Reflect
import kotlinx.coroutines.channels.Channel
import org.koin.core.annotation.Single
import org.koin.core.component.get
import org.lwjgl.opengl.Display
import java.awt.image.BufferedImage
import java.io.*
import java.util.TimerTask
import javax.imageio.ImageIO

@Single
class GameImpl : Game {
    private var _missions: List<Mission>? = null
    private var _allMaps: List<GameMap>? = null
    private var _maps = mutableMapOf<MapType, List<GameMap>>()
    private var _units: List<UnitType>? = null
    private var lastAllUnits: java.util.ArrayList<*>? = null
    private val asField = PlayerInternal::class.java.getDeclaredField("as")
        .apply { isAccessible = true }
    private var threadConnector: com.corrodinggames.rts.gameFramework.j.an? = null

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
                        return playerCacheMap[B.bX.z] ?: PlayerImpl(B.bs)
                    }
                override var sharedControl: Boolean
                    get() = l
                    set(value) { l = value }
                override val randomSeed: Int
                    get() = q
                override val mapType: MapType
                    get() = MapType.entries[a.ordinal]
                override var selectedMap: GameMap
                    get() = getAllMaps().firstOrNull {
                        (if (isHost) B.bX.az else B.bX.ay.b)?.endsWith((it.mapName + it.getMapSuffix()).replace("\\", "/")) ?: false
                    } ?: NetworkMap(mapNameFormatMethod.invoke(null, B.bX.ay.b) as String)
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
                            MapChangedEvent(value.displayName()).broadcastIn()
                            updateUI()
                        }
                    }
                override var displayMapName: String
                    get() = mapNameFormatMethod.invoke(null, B.bX.ay.b) as String
                    set(value) { B.bX.ay.b = value }
                override var startingCredits: Int
                    get() = c
                    set(value) { c = value }
                override var startingUnits: Int
                    get() = g
                    set(value) { g = value }
                override var fogMode: FogMode
                    get() = FogMode.entries[d]
                    set(value) { d = value.ordinal }
                override var revealedMap: Boolean
                    get() = e
                    set(value) { e = value }
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
                    get() = GameEngine.B().bX.B
                override val isStartGame: Boolean
                    get() = isGaming
                override var teamMode: TeamMode? = null

                override var gameMapTransformer: ((XMLMap) -> Unit)? = null
                private val mapNameFormatMethod = com.corrodinggames.rts.appFramework.i::class.java.getDeclaredMethod("e", String::class.java)

                @Suppress("unchecked_cast")
                override fun getPlayers(): List<Player> {
                    return (asField.get(B.bX.ay) as Array<com.corrodinggames.rts.game.n?>).mapNotNull {
                        if(it == null) return@mapNotNull null
                        playerCacheMap.getOrPut(it) { PlayerImpl(it).also { p -> if (it != B.bX.z) PlayerJoinEvent(p).broadcastIn() } }
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

                override fun sendMessageToPlayer(player: Player?, title: String?, message: String, color: Int) {
                    if (player != null && player != localPlayer) {
                        player.client?.sendPacketToClient(GamePacket.getChatPacket(title, message, color))
                    } else {
                        // New Message:
                        Reflect.call(
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
                    if (player == localPlayer) {
                        sendChatMessage("-surrender")
                    } else if (player.client != null) {
                        GameEngine.B().bX.b(
                            (player.client as ClientImpl).client, (player as PlayerImpl).player, player.name, "-surrender"
                        )
                    }

                }

                override fun syncAllPlayer() {
                    if (isHost) {
                        container.post {
                            GameEngine.B().bX.a(false, false, true)
                        }
                    }
                }

                override fun addCommandPacket(packet: Packet) {
                    val p = GameEngine.B().cf.b()
                    p.a(com.corrodinggames.rts.gameFramework.j.k(
                        DataInputStream(ByteArrayInputStream(packet.toBytes())))
                    )
                    GameEngine.B().cf.b.add(p)
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
                        val e = GameEngine.B().bX.e()
                        e.l = sharedControl
                        e.c = startingCredits
                        e.g = startingUnits
                        e.f = aiDifficulty.ordinal - 2
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
                            else -> synchronized(Logic) { teamMode.onInit(this) }
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
                    val p = (player as PlayerImpl).player
                    B.bX.e(p)
                    // playerCacheMap.remove(p) maybe kick didn't work out
                }

                override fun disconnect(reason: String) {
                    playerCacheMap.clear()
                    isSandboxGame = false
                    isRWPPRoom = false
                    option = RoomOption()
                    bannedUnitList = listOf()
                    roomMods = arrayOf()
                    teamMode = null
                    gameOver = false

                    if(isConnecting) B.bX.b(reason)
                    DisconnectEvent(reason).broadcastIn()
                }

                override fun updateUI() {
                    GameEngine.B().bX.f()
                    GameEngine.B().bX.P()
                    GameEngine.B().bX.L()
                    com.corrodinggames.rts.appFramework.n::class.java.getDeclaredMethod("o").invoke(null)
                }

                override fun startGame() {
                    rwppVisibleSetter(false)
                    gameCanvas.isVisible = true
                    gameCanvas.requestFocus()
                    gameOver = false
                    isGaming = true

                    if(isHost || isSandboxGame) {
                        if (!isSandboxGame && gameMapTransformer != null) {
                            val xmlMap = XMLMap(selectedMap)
                            gameMapTransformer!!.invoke(xmlMap)
                            val path = "mods/maps/generated_${selectedMap.mapName + selectedMap.getMapSuffix()}"
                            val file = xmlMap.saveToFile(path)
                            B.bX.az = path
                            GlobalEventChannel.filter(DisconnectEvent::class).subscribeOnce {
                                file.delete()
                            }
                            B.bX.ay.a = com.corrodinggames.rts.gameFramework.j.ai.entries[1]
                            updateUI()
                        }
                        B.bX.ae()
                    }
                }
            }
        }
    }
    override val world: World
        get() = TODO("Not yet implemented")

    init {
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

        gameOver = false
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
            //root.loadConfigAndStartNew("maps/normal/${mission.tmx.displayName}")
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
                    if (p0.startsWith("kicked", ignoreCase = true)) {
                        UI.showWarning(p0, true)
                    } else {
                        i.a(p0, p1)
                    }
                }

                override fun a(p0: String, p1: String) {
                    if (p0.startsWith("Briefing", ignoreCase = true) || p0.startsWith("Players", ignoreCase = true)) {
                        i.a(p0, p1)
                    } else {
                        UI.showWarning("$p0: $p1", true)
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

            if (native) {
//                l.aH = true
//                com.corrodinggames.rts.java.c.b().b()
//                println("Early steam init")
//                com.corrodinggames.rts.gameFramework.o.a.a().b()
//                println("Early steam init done.")
            }

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


            main.d = DClass()
            val i = com.corrodinggames.rts.java.b.a()
            com.corrodinggames.librocket.a::class.java.getDeclaredField("a").apply {
                isAccessible = true
            }.set(null, i)
            Reflect.set(main, "i", i)
            i.f = main
            val p1 = AClass()
            Reflect.set(main, "p", p1)
            i.a(p1, main.d)
            val p = Reflect.get<com.corrodinggames.rts.java.d.a>(main, "p")!!
            p.debug = false
            p.setup()
            p.loadFont("font/Delicious-Roman.otf");
            p.loadFont("font/Delicious-Italic.otf");
            p.loadFont("font/Delicious-Bold.otf");
            p.loadFont("font/Delicious-BoldItalic.otf");
            p.loadFont("font/Roboto-Regular.ttf");
            p.loadFont("font/Roboto-Bold.ttf");
            println("NotoSansCJKsc start");
            p.loadFont("font/NotoSansCJKsc-Regular.otf", "notoSans");
            p.loadFont("font/DroidSansFallback.ttf", "fallback");
            println("NotoSansCJKsc end");
            i.c();
            println("end libRocket setup");
            main.b("GuiEngine");
            GameEngine.dz = main.e;


            println("GameEngine")


            l.ck = Point(displaySize.width, displaySize.height)
            val serverContext = ServerContext()
            val a3 = l.a(serverContext, nHelper)

            with(main) {
                val i = Reflect.get<a>(this, "i")!!
                GameEngine.B().a(null, i.c, true);
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
            initMap(true)
            MapChangedEvent(gameRoom.selectedMap.displayName()).broadcastIn()
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

            initMap(true)

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


            RefreshUIEvent().broadcastIn()
        }
    }

    override fun setUserName(name: String) {
        l.B().bX.a(name)
    }

    override suspend fun directJoinServer(address: String, uuid: String?, context: LoadingContext): Result<String> {
        val B = GameEngine.B()
        initMap()

        context.message("Connecting...")

        val trim = restrictedString(address.trim())
        if(uuid == null) l.B().bQ.lastNetworkIP = trim
        l.B().bX.bw = uuid
        var result: Result<String>? = null
        val resultChannel = Channel<Unit>(1)
        threadConnector = l.B().bX.a(trim, true) {
            result = if(threadConnector!!.e != null) {
                rcnOption = null
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
                logger.error(e.stackTraceToString())
                result = Result.failure(IOException("Connection failed"))
            }
        }

        threadConnector = null

        return result!!
    }

    override fun cancelJoinServer() {
        threadConnector?.a()
        rcnOption = null
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

                        override fun openInputStream(): InputStream {
                            throw RuntimeException("not supported")
                        }
                    })
                }
        }


        return missions.toList().also { _missions = it }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAllMaps(flush: Boolean): List<GameMap> {
        if(_maps.isEmpty() || flush) {
            val B = GameEngine.B()
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

                                override fun openInputStream(): InputStream {
                                    return f.inputStream()
                                }
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

                            override fun openInputStream(): InputStream {
                                return com.corrodinggames.rts.game.b.b.b("/SD/rusted_warfare_maps/$name")
                            }

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
        val B: l = GameEngine.B()
        val list = mutableListOf<Pair<Int, String>>()
        val it: Iterator<*> = B.bX.i().iterator()
        while(it.hasNext()) {
            val num = it.next() as Int
            list.add(num to B.bX.d(num))
        }
        return list
    }
    @Suppress("UNCHECKED_CAST")
    override fun getAllUnits(): List<UnitType> {
        val gameUnits = (com.corrodinggames.rts.game.units.ar.ae as ArrayList<com.corrodinggames.rts.game.units.`as`>)
        if(_units == null || lastAllUnits != gameUnits) {
            _units = gameUnits.map {
                object : UnitType {
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

    override fun onBanUnits(units: List<UnitType>) {
        bannedUnitList = units.map(UnitType::name)
        if(units.isNotEmpty())
            gameRoom.sendSystemMessage("Host has banned these units (房间已经ban以下单位): ${units.map(UnitType::displayName).joinToString(", ")}")
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
        gameOver = false

        isGaming = true

        container.post {
            ScriptEngine.getInstance().root.loadReplay(replay.name)
        }
    }

    override fun isGameCouldContinue(): Boolean {
        return kotlin.runCatching { ScriptEngine.getInstance().root.canResume() }.getOrDefault(false)
    }

    override fun continueGame() {
        rwppVisibleSetter(false)
        gameCanvas.isVisible = true

        gameCanvas.requestFocus()

        isGaming = true
        gameOver = false

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
                logger.error(e.stackTraceToString())
                return null
            }
        }

        try {
            bufferedInputStream = BufferedInputStream(fileInputStream)
        } catch (e: Exception) {
            logger.error(e.stackTraceToString())
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