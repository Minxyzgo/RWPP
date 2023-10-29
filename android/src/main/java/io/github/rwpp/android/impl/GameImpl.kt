/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import com.corrodinggames.rts.appFramework.*
import com.corrodinggames.rts.gameFramework.j.ae
import com.corrodinggames.rts.gameFramework.j.at
import com.corrodinggames.rts.gameFramework.k
import io.github.rwpp.android.*
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.RefreshUIEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.GameMap
import io.github.rwpp.game.map.MapType
import io.github.rwpp.game.map.Mission
import io.github.rwpp.game.map.MissionType
import io.github.rwpp.game.units.GameInternalUnits
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.ui.LoadingContext
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import kotlin.coroutines.CoroutineContext


class GameImpl : Game, CoroutineScope {

    private var connectingJob: Deferred<String?>? = null
    private var _missions: List<Mission>? = null
    private var _maps = mutableMapOf<MapType, List<GameMap>>()

    override val gameVersion: Int
        get() = 176
    override val gameRoom: GameRoom = GameRoomImpl(this)

    override fun startNewMissionGame(difficulty: Difficulty, mission: Mission) {
        val t = GameEngine.t()
        t.bU.aA.f = difficulty.ordinal - 2
        LevelSelectActivity.loadSinglePlayerMapRaw("maps/normal/${mission.mapName}.tmx", false, 0, 0, true, false)
        val intent = Intent(MainActivity.instance, InGameActivity::class.java)
        intent.putExtra("level", t.di)
        gameLauncher.launch(intent)
    }

    override suspend fun load(context: LoadingContext) = withContext(Dispatchers.IO) {
        launch {
            while(GameEngine.t()?.bg != true) {
                context.message(GameEngine.t()?.dF ?: "loading...")
            }
        }

        GameEngine.c(MainActivity.instance)
        GameEngine.t().bU.aA.a = at.a
        GameEngine.t().bU.aB = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
        GameEngine.t().bU.aA.b = "[z;p10]Crossing Large (10p).tmx"
        Unit
    }

    override fun hostStartWithPasswordAndMods(isPublic: Boolean, password: String?, useMods: Boolean) {
        val t: k = GameEngine.t()
        t.bU.n = password
        t.bU.o = useMods
        t.bU.q = isPublic
        launch(Dispatchers.IO) {
            t.bU.t()
            GameEngine.t().bU.aA.a = at.a
            GameEngine.t().bU.aB = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
            GameEngine.t().bU.aA.b = "[z;p10]Crossing Large (10p).tmx"
            delay(100)
            RefreshUIEvent().broadCastIn()
        }
    }

    override fun hostNewSandbox() {
        val t = GameEngine.t()
        LevelSelectActivity.loadSinglePlayerMapRaw("skirmish/[z;p10]Crossing Large (10p).tmx", true, 3, 1, true, true)
        t.bU.b("starting singleplayer (sandbox)")
        t.bU.y = "You"
        t.bU.o = true
        t.bU.r()
        isSandboxGame = true
        GameEngine.t().bU.aA.a = at.a
        GameEngine.t().bU.aB = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
        GameEngine.t().bU.aA.b = "[z;p10]Crossing Large (10p).tmx"
        RefreshUIEvent().broadCastIn()
    }

    override fun setUserName(name: String) {
        GameEngine.t().bU.a(name)
    }

    override suspend fun directJoinServer(address: String, uuid: String?, context: LoadingContext): Result<String> {
        GameEngine.t().bU.by = uuid
        connectingJob = withContext(Dispatchers.IO) {
            async { GameEngine.t().bU.c(address, false) }
        }

        val result = connectingJob?.await()

        return when {
            result == null -> {
                Result.success("")
            }
            ae.u() -> Result.failure(IOException("Connection failed: Target server may not be open to the internet."))
            else -> Result.failure(IOException("Connection failed."))
        }
    }

    override fun cancelJoinServer() {
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
        return listOf(MissionType.Default)
    }

    override fun getAllMissions(): List<Mission> {
        if(_missions != null) return _missions!!
        val assets = MainActivity.instance.assets

        val missions = mutableListOf<Mission>()

        assets.list("maps/normal")!!
            .filter { it.endsWith(".tmx") }
            .forEachIndexed { i, f ->
                missions.add(object : Mission {
                    override val id: Int = i
                    override val name: String = f.split("__-__")[1].removeSuffix(".tmx")
                    override val image: Painter =
                        BitmapPainter(BitmapFactory.decodeStream(
                            assets.open("maps/normal/${f.removeSuffix(".tmx") + "_map.png"}")
                        ).asImageBitmap())
                    override val mapName: String
                        get() = f.removeSuffix(".tmx")
                    override val tmx: File? = null
                    override val mapType: MapType
                        get() = MapType.SkirmishMap
                })
            }
        return missions.toList().also { _missions = it }
    }

    override fun getAllMaps(): List<GameMap> {
        val assets = MainActivity.instance.assets
        if(_maps.isEmpty()) {
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
                        override val image: Painter =
                            BitmapPainter(BitmapFactory.decodeStream(
                                assets.open("maps/skirmish/${f.removeSuffix(".tmx") + "_map.png"}")
                            ).asImageBitmap())
                        override val mapName: String
                            get() = f.removeSuffix(".tmx")
                        override val tmx: File? = null
                        override val mapType: MapType
                            get() = MapType.SkirmishMap
                    })
                }
            _maps[MapType.SkirmishMap]= assetMaps

            for((type, path) in mapPaths) {
                val maps = mutableListOf<GameMap>()
                path?.forEachIndexed { i, name ->
                        maps.add(object : GameMap {
                            override val id: Int = i
                            override val image: Painter? =
                                if(type == MapType.CustomMap) {
                                    com.corrodinggames.rts.appFramework.d.d(
                                        "/SD/rusted_warfare_maps/$name"
                                    )?.asImageBitmap()?.let { BitmapPainter(it) }
                                } else null
                            override val mapName: String
                                get() = name.removeSuffix(".tmx").removeSuffix(".rwsave")
                            override val tmx: File? = null
                            override val mapType: MapType
                                get() = type

                            override fun displayName(): String = LevelSelectActivity.convertLevelFileNameForDisplay(mapName)
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
        val list = mutableListOf<Pair<Int, String>>()
        val it: Iterator<*> = ae.d().iterator()
        while(it.hasNext()) {
            val num = it.next() as Int
            list.add(num to ae.c(num))
        }
        return list
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAllUnits(): List<GameUnit> {
        return (com.corrodinggames.rts.game.units.cj.ae as ArrayList<com.corrodinggames.rts.game.units.el>).map {
            object : GameUnit {
                override val name: String = it.i()
                override val displayName: String = it.e()
                override val description: String = it.f()
            }
        }
    }

    override fun onBanUnits(units: List<GameUnit>) {
        bannedUnitList = units.map { it.name }
        if(units.isNotEmpty()) gameRoom.sendSystemMessage("Host has banned these units (房间已经ban以下单位): ${
            units.joinToString(
                ", "
            ) { it.displayName }
        }")
    }

    override val coroutineContext: CoroutineContext = Job()

}