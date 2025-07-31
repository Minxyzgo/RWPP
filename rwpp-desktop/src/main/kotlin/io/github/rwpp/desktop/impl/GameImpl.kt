/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import android.content.ServerContext
import android.graphics.Point
import com.corrodinggames.librocket.scripts.ScriptContext
import com.corrodinggames.librocket.scripts.ScriptEngine
import com.corrodinggames.rts.gameFramework.ac
import com.corrodinggames.rts.gameFramework.l
import com.corrodinggames.rts.gameFramework.n
import com.corrodinggames.rts.gameFramework.utility.ae
import com.corrodinggames.rts.java.Main
import com.corrodinggames.rts.java.audio.lwjgl.OpenALAudio
import com.corrodinggames.rts.java.b
import com.corrodinggames.rts.java.b.a
import com.corrodinggames.rts.java.u
import io.github.rwpp.appKoin
import io.github.rwpp.config.Settings
import io.github.rwpp.core.LoadingContext
import io.github.rwpp.desktop.*
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.events.StartGameEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.*
import io.github.rwpp.game.ui.GUI
import io.github.rwpp.game.world.World
import io.github.rwpp.logger
import io.github.rwpp.ui.UI
import io.github.rwpp.utils.Reflect
import io.github.rwpp.widget.loadingMessage
import kotlinx.coroutines.channels.Channel
import org.koin.core.annotation.Single
import org.lwjgl.opengl.Display
import java.io.*

@Single(binds = [Game::class])
class GameImpl : AbstractGame() {
    private var _missions: List<Mission>? = null
    private var _allMaps: List<GameMap>? = null
    private var _maps = mutableMapOf<MapType, List<GameMap>>()
    override val gameRoom: GameRoom = object : AbstractGameRoom() {
        override fun startGame() {
            rwppVisibleSetter(false)
            gameCanvas.isVisible = true
            gameCanvas.requestFocus()
            super.startGame()
        }
    }

    override val gui: GUI
        get() = GameEngine.B().bS as GUI
    override val world: World = WorldImpl()

    override fun post(action: () -> Unit) {
        container.post(action)
    }

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
                    message(p0)
                    loadingMessage = p0
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


            main.d = object : DClass() {
                //返回是否全屏，以便能处理边缘移动
                override fun f(): Boolean {
                    val B = GameEngine.B()
                    return B != null && appKoin.get<Settings>().isFullscreen
                }
            }
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
                //a3.bQ.showUnitGroups = false
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

            //此处忽略dpi来正确设置librocket的显示大小
            val root = ScriptEngine.getInstance().root
            val libRocket = ScriptContext::class.java.getDeclaredField("libRocket").run {
                isAccessible = true
                get(root)
            } as com.corrodinggames.librocket.b
            val scale = getDPIScale()
            libRocket.setDimensionsWrap((displaySize.width / scale).toInt(), (displaySize.height / scale).toInt())

            receivedChannel.trySend(Unit)

            //Display.destroy()
        }

        //q = true
        // r = true // is reloaded
        receivedChannel.receive()
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
                        override val mapName: String
                            get() = f.nameWithoutExtension
                        private val _displayName = ScriptEngine.getInstance().root.convertMapName(name)
                        override fun displayName(): String = _displayName
                        override val mapType: MapType
                            get() = MapType.SkirmishMap

                        override fun openImageInputStream(): InputStream? {
                            return File(f.parent!! + "/" + f.name.removeSuffix(".tmx") + "_map.png").inputStream()
                        }

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
                                override val mapName: String
                                    get() = f.nameWithoutExtension
                                override val mapType: MapType
                                    get() = type

                                override fun openImageInputStream(): InputStream? {
                                    return if(mapType != MapType.SavedGame)
                                        File(f.parent!! + "/" + f.name.removeSuffix(".tmx") + "_map.png").inputStream()
                                    else null
                                }

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
                            override val mapName: String
                                get() = name.removeSuffix(".tmx").removeSuffix(".rwsave")
                            override val mapType: MapType
                                get() = type
                            private val _displayName = ScriptEngine.getInstance().root.convertMapName(name)

                            override fun openImageInputStream(): InputStream? {
                                return if(type == MapType.CustomMap) {
                                    if (name.contains("MOD|")) //TODO 实现mod图片
                                        null
                                    else loadInputStream(
                                        com.corrodinggames.rts.appFramework.c.c(name)
                                    )
                                } else null
                            }

                            override fun openInputStream(): InputStream {
                                return com.corrodinggames.rts.game.b.b.b("/SD/rusted_warfare_maps/$name")
                            }

                            override fun displayName(): String {
                                return _displayName
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



    override fun getAllReplays(): List<Replay> {
        return Reflect.call<com.corrodinggames.rts.appFramework.q, Array<String>>(
            null, "l", emptyList(), emptyList()
        )!!.mapIndexed { i, str ->
            object : Replay {
                override val id: Int = i
                override val name: String = str
                private val _displayName = ScriptEngine.getInstance().root.convertMapName(name)
                override fun displayName(): String = _displayName
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




    private fun loadInputStream(path: String): InputStream? {
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

        return fileInputStream
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