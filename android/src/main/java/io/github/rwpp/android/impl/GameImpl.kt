/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.content.ContextCompat.startActivity
import com.corrodinggames.rts.appFramework.*
import com.corrodinggames.rts.gameFramework.j.ae
import com.corrodinggames.rts.gameFramework.j.at
import com.corrodinggames.rts.gameFramework.k
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import io.github.rwpp.android.*
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.RefreshUIEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.*
import io.github.rwpp.game.mod.Mod
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.game.units.MovementType
import io.github.rwpp.ui.LoadingContext
import kotlinx.coroutines.*
import org.koin.core.annotation.Single
import org.koin.core.component.get
import java.io.File
import java.io.IOException
import kotlin.coroutines.CoroutineContext


@Single
class GameImpl : Game, CoroutineScope {

    private var connectingJob: Deferred<String?>? = null
    private var _missions: List<Mission>? = null
    private var _maps = mutableMapOf<MapType, List<GameMap>>()
    private var _units: List<GameUnit>? = null
    private var cacheUnits: ArrayList<*>? = null

    override val gameRoom: GameRoom = GameRoomImpl(this)

    override fun startNewMissionGame(difficulty: Difficulty, mission: Mission) {
        val t = GameEngine.t()
        t.bN.aiDifficulty = difficulty.ordinal - 2
        t.bN.save()
        LevelSelectActivity.loadSinglePlayerMapRaw("maps/${mission.type.pathName()}/${mission.mapName}.tmx", false, 0, 0, true, false)
        val intent = Intent(get(), InGameActivity::class.java)
        intent.putExtra("level", t.di)
        gameLauncher.launch(intent)
    }

    override suspend fun load(context: LoadingContext) {
        // init map
        GameEngine.t().bU.aA.a = at.a
        GameEngine.t().bU.aB = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
        GameEngine.t().bU.aA.b = "[z;p10]Crossing Large (10p).tmx"
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
//                val t = GameEngine.t()
//                t.bu = 0
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
                        override val image: Painter =
                            BitmapPainter(BitmapFactory.decodeStream(
                                assets.open("maps/${type.pathName()}/${f.removeSuffix(".tmx") + "_map.png"}")
                            ).asImageBitmap())
                        override val mapName: String
                            get() = f.removeSuffix(".tmx")
                        override val mapType: MapType
                            get() = MapType.SkirmishMap
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
                        override val image: Painter =
                            BitmapPainter(BitmapFactory.decodeStream(
                                assets.open("maps/skirmish/${f.removeSuffix(".tmx") + "_map.png"}")
                            ).asImageBitmap())
                        override val mapName: String
                            get() = f.removeSuffix(".tmx")
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
    override fun getAllUnits(): List<GameUnit> {
        val units = (com.corrodinggames.rts.game.units.cj.ae as ArrayList<com.corrodinggames.rts.game.units.el>)
        if(cacheUnits != units || _units == null) {
            _units = units.map {
                object : GameUnit {
                    override val name: String = it.i()
                    override val displayName: String = it.e()
                    override val description: String = it.f()
                    override val movementType: MovementType
                        get() = MovementType.valueOf(it.o().name)
                    override val mod: Mod?
                        get() = (it as? com.corrodinggames.rts.game.units.custom.l)?.J?.q?.let(get<ModManager>()::getModByName)
                }
            }

            cacheUnits = units
        }
        return _units!!
    }

    override fun onBanUnits(units: List<GameUnit>) {
        bannedUnitList = units.map { it.name }
        if(units.isNotEmpty()) gameRoom.sendSystemMessage("Host has banned these units (房间已经ban以下单位): ${
            units.joinToString(
                ", "
            ) { it.displayName }
        }")
    }

    override fun getAllReplays(): List<Replay> {
        return ReplaySelectActivity.getGameSaves()?.mapIndexed { i, str ->
            object : Replay {
                override val id: Int = i
                override val name: String = str
                override fun displayName(): String {
                    return LoadLevelActivity.convertDataFileNameForDisplay(com.corrodinggames.rts.gameFramework.e.a.q(name));
                }
            }
        } ?: listOf()
    }

    override fun watchReplay(replay: Replay) {
        if (GameEngine.t().bY.b(replay.name)) {
            gameLauncher.launch(
                Intent(get(), InGameActivity::class.java)
            )
        }
    }

    override fun isGameCouldContinue(): Boolean {
        val c = GameEngine.t()
        return !(c == null || !c.bD || c.bE)
    }

    override fun continueGame() {
        val intent = Intent(get(), InGameActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        gameLauncher.launch(intent)
    }




    override val coroutineContext: CoroutineContext = Job()

}