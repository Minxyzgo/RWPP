/*
 * Copyright 2023 RWPP contributors
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
import com.corrodinggames.rts.gameFramework.ac
import com.corrodinggames.rts.gameFramework.l
import com.corrodinggames.rts.gameFramework.n
import com.corrodinggames.rts.java.Main
import com.corrodinggames.rts.java.audio.lwjgl.OpenALAudio
import com.corrodinggames.rts.java.b
import com.corrodinggames.rts.java.b.a
import com.corrodinggames.rts.java.u
import com.github.minxyzgo.rwij.InjectMode
import com.github.minxyzgo.rwij.InterruptResult
import com.github.minxyzgo.rwij.setFunction
import io.github.rwpp.config.MultiplayerPreferences
import io.github.rwpp.config.instance
import io.github.rwpp.desktop.*
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.*
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.*
import io.github.rwpp.game.units.GameCommandActions
import io.github.rwpp.game.units.GameInternalUnits
import io.github.rwpp.ui.LoadingContext
import io.github.rwpp.welcomeMessage
import kotlinx.coroutines.channels.Channel
import org.lwjgl.opengl.Display
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.SwingUtilities


class GameImpl : Game {
    private val mapPrefixRegex = Regex("""^\[.*?\]""")
    private var _missions: List<Mission>? = null
    private var _maps = mutableMapOf<MapType, List<GameMap>>()
    private val asField = PlayerInternal::class.java.getDeclaredField("as")
        .apply { isAccessible = true }
    private var playerCacheMap = mutableMapOf<com.corrodinggames.rts.game.n, Player>()
    private var threadConnector: com.corrodinggames.rts.gameFramework.j.an? = null
    private var isSandboxGame: Boolean = false
    private var bannedUnitList: List<GameInternalUnits> = listOf()

    override val gameVersion: Int = 176

    override val gameRoom: GameRoom by lazy {
        val B = l.B()
        with(B.bX.ay) {
            object : GameRoom {
                override var maxPlayerCount: Int
                    get() = PlayerInternal.c
                    set(value) { PlayerInternal.b(value, true) }
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
                    get() = getAllMaps().firstOrNull { it.tmx!!.absolutePath.replace("\\", "/").endsWith(B.bX.ay.b ?: "") }
                        ?: NetworkMap(mapNameFormatMethod.invoke(null, B.bX.ay.b) as String)
                    set(value) {
                        val realPath = (
                                when(value.mapType) {
                                    MapType.SkirmishMap -> "maps/skirmish/"
                                    MapType.CustomMap -> "mods/maps/"
                                    else -> ""
                                }) + value.tmx!!.name.replace("\\", "/")
                        B.bX.az = realPath
                        B.bX.ay.a = com.corrodinggames.rts.gameFramework.j.ai.entries[value.mapType.ordinal]
                        b = value.tmx!!.name
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

                private val mapNameFormatMethod = com.corrodinggames.rts.appFramework.i::class.java.getDeclaredMethod("e", String::class.java)

                @Suppress("unchecked_cast")
                override fun getPlayers(): List<Player> {
                    return (asField.get(B.bX.ay) as Array<com.corrodinggames.rts.game.n?>).mapNotNull {
                        if(it == null) return@mapNotNull null
                        playerCacheMap.getOrPut(it) { PlayerImpl(it, this) }
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

                override fun addAI() {
                    B.bX.ap()
                }

                override fun applyTeamChange(mode: String) {
                    when(mode) {
                        "2t" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.a)
                        "3t" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.b)
                        "FFA" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.c)
                        "spectators" -> B.bX.a(com.corrodinggames.rts.gameFramework.j.am.d)
                        else -> throw IllegalArgumentException("mode: $mode")
                    }
                }

                override fun kickPlayer(player: Player) {
                    val p = (player as PlayerImpl).player
                    B.bX.e(p)
                    playerCacheMap.remove(p)
                }

                override fun disconnect() {
                    playerCacheMap.clear()
                    isSandboxGame = false
                    bannedUnitList = listOf()
                    B.bX.b("exited")
                }

                override fun startGame() {
                    rwppVisibleSetter(false)
                    gameCanvas.isVisible = true
                    gameCanvas.requestFocus()
                    isGaming = true

                    if(isHost || isSandboxGame) {
                        B.bX.ae()
                        isGaming = true
                    } else if(isHostServer) {
                        sendChatMessage("-qc -start")
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
                    sendMessageDialog.isVisible = true
                    val window = mainJFrame
                    sendMessageDialog.setLocation(window.x + window.width / 2, window.y + window.height / 2)
                }
            }

            addProxy(Root::makeSendTeamMessagePopupWithDefaultText) { _, str ->
                SwingUtilities.invokeLater {
                    sendMessageDialog.isVisible = true
                    val window = mainJFrame
                    sendMessageDialog.setLocation(window.x + window.width / 2, window.y + window.height / 2)
                }
            }
        }

        Main::class.setFunction {
            addProxy("c") {
                RefreshUIEvent().broadCastIn()
            }

            addProxy("c", com.corrodinggames.rts.gameFramework.j.c::class, String::class, String::class) { _: Any?, c: com.corrodinggames.rts.gameFramework.j.c, _: Any?, _: Any? ->
                if(MultiplayerPreferences.instance.showWelcomeMessage != true) return@addProxy Unit
                val rwOutputStream = RwOutputStream()
                rwOutputStream.c(welcomeMessage)
                rwOutputStream.c(3)
                rwOutputStream.b("RWPP")
                rwOutputStream.a(null as com.corrodinggames.rts.gameFramework.j.c?)
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

        com.corrodinggames.rts.gameFramework.j.ad::class.setFunction {
            addProxy("a", com.corrodinggames.rts.gameFramework.e::class, mode = InjectMode.InsertBefore) { _: Any?, b3: com.corrodinggames.rts.gameFramework.e ->
                val actionString = b3.k.a()
                if(actionString.startsWith("u_")) {
                    val realUnit = runCatching {
                        GameInternalUnits.valueOf(
                            actionString.removePrefix("u_").removePrefix("c_")
                        )
                    }.getOrNull()
                    if(realUnit != null && realUnit in bannedUnitList) {
                        return@addProxy InterruptResult(Unit)
                    }
                }
                if(b3.j == null) return@addProxy Unit
                val realAction = GameCommandActions.from(b3.j.d().ordinal)
                val u = b3.j.a()
                if(u is com.corrodinggames.rts.game.units.ar) {
                    val realUnit = GameInternalUnits.from(u.ordinal)
                    if(realAction == GameCommandActions.BUILD && realUnit in bannedUnitList) {
                        InterruptResult(Unit)
                    } else Unit
                } else Unit
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
            met.invoke(null, "maps/normal/${mission.tmx!!.name}", false, 0, 0, true, false)

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
                    if(p0.startsWith("Briefing", ignoreCase = true)) {
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
        B.bX.o = isPublic
        B.bX.q = !useMods
        if(B.bX.b(false)) {
            B.bX.ay.a = GameMapType.a
            B.bX.az = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
            B.bX.ay.b = "[z;p10]Crossing Large (10p).tmx"
        }
    }

    override fun hostNewSandbox() {
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

            game.bL.E = false
            game.bS.y()
            game.bv = true


            game.bX.y = "You"
            game.bX.o = true
            val S: Boolean = game.bX.R()

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
        return listOf(MissionType.Default)
    }

    override fun getAllMissions(): List<Mission> {
        if(_missions != null) return _missions!!

        val missionFolder = File("assets/maps/normal")
        val missions = mutableListOf<Mission>()

        missionFolder
            .walk()
            .filter { it.name.endsWith(".tmx") }
            .forEachIndexed { i, f ->
                missions.add(object : Mission {
                    override val id: Int = i
                    override val name: String = f.name.split("__-__")[1].removeSuffix(".tmx")
                    override val image: Painter =
                        ImageIO.read(File(f.parent!! + "/" + f.name.removeSuffix(".tmx") + "_map.png")).toPainter()
                    override val mapName: String
                        get() = tmx.nameWithoutExtension
                    override val tmx: File = f
                    override val mapType: MapType
                        get() = MapType.SkirmishMap
                })
            }
        return missions.toList().also { _missions = it }
    }

    override fun getAllMaps(): List<GameMap> {
        if(_maps.isEmpty()) {
            val mapFolders = mapOf(
                MapType.SkirmishMap to File("assets/maps/skirmish"),
                MapType.CustomMap to File("mods/maps"),
                MapType.SavedGame to File("saves")
            )
            for((type, folder) in mapFolders) {
                val maps = mutableListOf<GameMap>()
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
                                get() = tmx.nameWithoutExtension.let {
                                    if(type == MapType.SkirmishMap) it.replace(mapPrefixRegex, "") else it
                                }
                            override val tmx: File = f
                            override val mapType: MapType
                                get() = type
                        })
                    }
                _maps[type] = maps.toList()
            }
        }

        return buildList { _maps.values.forEach(::addAll) }
    }

    override fun getAllMapsByMapType(mapType: MapType): List<GameMap> {
        if(_maps.isEmpty()) getAllMaps()
        return _maps[mapType]!!
    }

    override fun getMissionsByType(type: MissionType): List<Mission> = getAllMissions()
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

    override fun onBanUnits(units: List<GameInternalUnits>) {
        bannedUnitList = units
        if(units.isNotEmpty()) gameRoom.sendSystemMessage("Host has banned these units (房间已经ban以下单位): ${units.joinToString(", ")}")
    }

    private fun restrictedString(str: String?): String? {
        return str?.replace("'", ".")?.replace("\"", ".")?.replace("(", ".")?.replace(")", ".")?.replace(",", ".")
            ?.replace("<", ".")?.replace(">", ".")
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

        private var cproperty: CClass? = null
        fun post(action: () -> Unit) {
            channel.trySend(action)
        }

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