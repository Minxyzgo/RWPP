/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Context
import android.content.Intent
import com.corrodinggames.rts.appFramework.LevelGroupSelectActivity
import com.corrodinggames.rts.appFramework.LevelSelectActivity
import com.corrodinggames.rts.appFramework.LoadLevelActivity
import com.corrodinggames.rts.appFramework.ReplaySelectActivity
import com.corrodinggames.rts.gameFramework.j.ae
import com.corrodinggames.rts.gameFramework.k
import io.github.rwpp.android.*
import io.github.rwpp.android.MainActivity.Companion.gameView
import io.github.rwpp.appKoin
import io.github.rwpp.core.LoadingContext
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.*
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.*
import io.github.rwpp.game.ui.GUI
import io.github.rwpp.game.units.UnitType
import io.github.rwpp.game.world.World
import io.github.rwpp.logger
import kotlinx.coroutines.*
import org.koin.core.annotation.Single
import org.koin.core.component.get
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

@Single
class GameImpl : Game, CoroutineScope {

    private var isCancellingJob = AtomicBoolean(false)
    private var connectingJob: Deferred<String?>? = null
    private var _missions: List<Mission>? = null
    private var _allMaps: List<GameMap>? = null
    private var _maps = mutableMapOf<MapType, List<GameMap>>()

    override val gameRoom: GameRoom = GameRoomImpl(this)
    override val gui: GUI by lazy {
        GameEngine.t().bP as GUI
    }
    override val world: World = WorldImpl()

    override fun post(action: () -> Unit) {
        mainThreadChannel.trySend(action)
    }

    override fun startNewMissionGame(difficulty: Difficulty, mission: Mission) {
        val t = GameEngine.t()
        GameEngine.t().bU.b("starting singleplayer")
        t.bN.aiDifficulty = difficulty.ordinal - 2
        t.bN.save()
        LevelSelectActivity.loadSinglePlayerMapRaw("maps/${mission.type.pathName()}/${mission.mapName}.tmx", false, 0, 0, true, false)
        val intent = Intent(get(), CustomInGameActivity::class.java)
        intent.putExtra("level", t.di)
        gameLauncher.launch(intent)
    }

    override suspend fun load(context: LoadingContext) {
        initMap()
    }

    override fun hostStartWithPasswordAndMods(isPublic: Boolean, password: String?, useMods: Boolean) {
        val t: k = GameEngine.t()
        GameEngine.t().bU.b("starting new")
        t.bU.n = password
        t.bU.o = useMods
        t.bU.q = isPublic
        launch(Dispatchers.IO) {
            initMap(true)
            t.bU.t()
            MapChangedEvent(gameRoom.selectedMap.displayName()).broadcastIn()
            delay(100)
            RefreshUIEvent().broadcastIn()
            HostGameEvent().broadcastIn()
            PlayerJoinEvent(gameRoom.localPlayer).broadcastIn()
        }
    }

    override fun hostNewSinglePlayer(sandbox: Boolean) {
        val t = GameEngine.t()
        GameEngine.t().bU.b("starting singleplayer")
        LevelSelectActivity.loadSinglePlayerMapRaw("skirmish/[z;p10]Crossing Large (10p).tmx", true, 3, 1, true, true)
        t.bU.b("starting singleplayer")
        t.bU.y = "You"
        t.bU.o = true
        if (sandbox) t.bU.r() else t.bU.s()
        isSinglePlayerGame = true
        initMap(true)
        RefreshUIEvent().broadcastIn(delay = 200L)
        HostSinglePlayerGameEvent().broadcastIn()
    }

    override fun setUserName(name: String) {
        GameEngine.t().bU.a(name)
    }

    override suspend fun directJoinServer(address: String, uuid: String?, context: LoadingContext): Result<String> {
        GameEngine.t().a(appKoin.get(), gameView)

        isCancellingJob.set(false)

        initMap()

        GameEngine.t().bU.by = uuid
        connectingJob = withContext(Dispatchers.IO) {
            async { GameEngine.t().bU.c(address, false) }
        }

        val result = runCatching {
            connectingJob?.await()
        }

        return when {
            result.isSuccess && !isCancellingJob.get() -> {
//                val t = GameEngine.t()
//                t.bu = 0
                PlayerJoinEvent(gameRoom.localPlayer).broadcastIn()
                Result.success("")
            }
            ae.u() -> {
                isCancellingJob.set(false)
                Result.failure(IOException("Connection failed: Target server may not be open to the internet."))
            }
            else -> {
                isCancellingJob.set(false)
                Result.failure(IOException("Connection failed."))
            }
        }
    }

    override fun cancelJoinServer() {
        isCancellingJob.set(true)
        connectingJob?.cancel()
        connectingJob = null
    }

    override fun onQuestionCallback(option: String) {
        questionOption = option
    }

    override fun setTeamUnitCapHostGame(cap: Int) {
        GameEngine.t().bz = cap
        GameEngine.t().bU.az = cap
        GameEngine.t().bU.ay = cap
    }

    override fun getAllMissionTypes(): List<MissionType> {
        return listOf(MissionType.Normal, MissionType.Challenge, MissionType.Survival)
    }

    override fun getAllMissions(): List<Mission> {
        if(_missions != null) return _missions!!
        val assets = get<Context>().assets

        val missions = mutableListOf<Mission>()
        getAllMissionTypes().forEach { type ->
            assets.list("maps/${type.pathName()}")!!
                .filter { it.endsWith(".tmx") }
                .forEachIndexed { i, f ->
                    missions.add(object : Mission {
                        override val id: Int = i
                        override val name: String =
                            if(type == MissionType.Normal)
                                f.split("__-__")[1].removeSuffix(".tmx")
                            else f.removeSuffix(".tmx")
                        override val type: MissionType
                            get() = type
                        override val mapName: String
                            get() = f.removeSuffix(".tmx")
                        override val mapType: MapType
                            get() = MapType.SkirmishMap

                        private val _displayName = LevelSelectActivity.convertLevelFileNameForDisplay(name)
                        override fun displayName(): String = _displayName

                        override fun openImageInputStream(): InputStream? {
                            return assets.open("maps/${type.pathName()}/${f.removeSuffix(".tmx") + "_map.png"}")
                        }

                        override fun openInputStream(): InputStream {
                            throw RuntimeException("Not implemented")
                        }
                    })
                }
        }


        return missions.toList().also { _missions = it }
    }

    override fun getAllMaps(flush: Boolean): List<GameMap> {
        val assets = get<Context>().assets
        if(_maps.isEmpty() || flush) {
            val t = GameEngine.t()
            val levelDirs = com.corrodinggames.rts.gameFramework.e.a.a(LevelGroupSelectActivity.customLevelsDir, true)
            val mapPaths = mapOf<MapType, Array<String>?>(
                MapType.CustomMap to t.bW.a(levelDirs, LevelGroupSelectActivity.customLevelsDir),
                MapType.SavedGame to LoadLevelActivity.getGameSaves()
            )


            val assetMaps = mutableListOf<GameMap>()
            assets.list("maps/skirmish")!!
                .filter { it.endsWith(".tmx") }
                .forEachIndexed { i, f ->
                    assetMaps.add(object : GameMap {
                        override val id: Int = i
                        override val mapName: String
                            get() = f.removeSuffix(".tmx")
                        override val mapType: MapType
                            get() = MapType.SkirmishMap

                        override fun openImageInputStream(): InputStream? {
                            return assets.open("maps/skirmish/${f.removeSuffix(".tmx") + "_map.png"}")
                        }

                        override fun openInputStream(): InputStream {
                            return com.corrodinggames.rts.game.b.b.a("maps/skirmish/$f")
                        }
                    })
                }
            _maps[MapType.SkirmishMap]= assetMaps

            for((type, path) in mapPaths) {
                val maps = mutableListOf<GameMap>()
                path?.forEachIndexed { i, name ->
                        maps.add(object : GameMap {
                            override val id: Int = i
                            override val mapName: String
                                get() = name.removeSuffix(".tmx").removeSuffix(".rwsave")
                            override val mapType: MapType
                                get() = type

                            private val _displayName = LevelSelectActivity.convertLevelFileNameForDisplay(mapName)

                            override fun openImageInputStream(): InputStream? {
                                return if(type == MapType.CustomMap) {
                                    com.corrodinggames.rts.appFramework.d.c(
                                        "/SD/rusted_warfare_maps/$name"
                                    )
                                } else null
                            }

                            override fun openInputStream(): InputStream {
                                return com.corrodinggames.rts.game.b.b.a("/SD/rusted_warfare_maps/$name")
                            }

                            override fun displayName(): String = _displayName
                        })
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

    override fun getMissionsByType(type: MissionType): List<Mission> = getAllMissions().filter { it.type == type }
    override fun getStartingUnitOptions(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        val it: Iterator<*> = ae.d().iterator()
        while(it.hasNext()) {
            val num = it.next() as Int
            list.add(num to ae.c(num))
        }
        return list
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAllUnitTypes(): List<UnitType> {
        return com.corrodinggames.rts.game.units.cj.ae as ArrayList<UnitType>
    }

    override fun onBanUnits(units: List<UnitType>) {
        bannedUnitList = units.map { it.name }
        if(units.isNotEmpty()) gameRoom.sendSystemMessage("Host has banned these units (房间已经ban以下单位): ${
            units.joinToString(
                ", "
            ) { it.displayName }
        }")
    }

    override fun getAllReplays(): List<Replay> {
        val eStr = com.corrodinggames.rts.gameFramework.e.a.e("/SD/rustedWarfare/replays/")
        logger.info("replay eStr: $eStr")
        logger.info("b: ${com.corrodinggames.rts.gameFramework.e.a.e()}")
        logger.info("replay b: ${com.corrodinggames.rts.gameFramework.e.a.c(eStr)}")
        logger.info("saflink: ${com.corrodinggames.rts.gameFramework.utility.ah.a(eStr)}")
        return ReplaySelectActivity.getGameSaves()?.mapIndexed { i, str ->
            object : Replay {
                override val id: Int = i
                override val name: String = str

                private val _displayName = LoadLevelActivity.convertDataFileNameForDisplay(com.corrodinggames.rts.gameFramework.e.a.q(name))

                override fun displayName(): String {
                    return _displayName
                }
            }
        } ?: listOf()
    }

    override fun watchReplay(replay: Replay) {
        if (GameEngine.t().bY.b(replay.name)) {
            gameLauncher.launch(
                Intent(get(), CustomInGameActivity::class.java)
            )
        }
    }

    override fun setEffectLimitForAllEffects(limit: Int) {
        val effectEngine = GameEngine.t().bO
        effectEngine.apply {
            b = limit
            c = limit
            d = limit
            e = limit
        }
    }

    override fun isGameCouldContinue(): Boolean {
        val c = GameEngine.t()
        return !(c == null || !c.bD || c.bE)
    }

    override fun continueGame() {
        val intent = Intent(get(), CustomInGameActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        gameLauncher.launch(intent)
    }


    override val coroutineContext: CoroutineContext = Job()

}