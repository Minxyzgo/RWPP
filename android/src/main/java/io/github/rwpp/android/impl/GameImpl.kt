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
import com.corrodinggames.rts.appFramework.InGameActivity
import com.corrodinggames.rts.appFramework.LevelSelectActivity
import com.corrodinggames.rts.gameFramework.j.at
import com.corrodinggames.rts.gameFramework.k
import io.github.rwpp.android.MainActivity
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.GameMap
import io.github.rwpp.game.map.MapType
import io.github.rwpp.game.map.Mission
import io.github.rwpp.game.map.MissionType
import io.github.rwpp.ui.LoadingContext
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import kotlin.coroutines.CoroutineContext


class GameImpl : Game, CoroutineScope {
    private val mapPrefixRegex = Regex("""^\[.*?\]""")
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
        MainActivity.instance.startActivityForResult(intent, 0)
    }

    override suspend fun load(context: LoadingContext) {
        context.message("loading")
        GameEngine.c(MainActivity.instance)
    }

    override fun hostStartWithPasswordAndMods(isPublic: Boolean, password: String?, useMods: Boolean) {
        val t: k = GameEngine.t()
        t.bU.n = password
        t.bU.o = useMods
        t.bU.g = isPublic
        launch(Dispatchers.IO) {
            t.bU.t()
        }

        GameEngine.t().bU.aA.a = at.a
        GameEngine.t().bU.aB = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
        GameEngine.t().bU.aA.b = "[z;p10]Crossing Large (10p).tmx"
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
            result == null -> Result.success("")
            com.corrodinggames.rts.gameFramework.j.ae.u() -> Result.failure(IOException("Connection failed: Target server may not be open to the internet."))
            else -> Result.failure(IOException("Connection failed."))
        }
    }

    override fun cancelJoinServer() {
        connectingJob?.cancel()
        connectingJob = null
    }

    override fun onRcnCallback(option: String) {
        TODO("Not yet implemented")
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
        if(_maps.isEmpty()) {
            val mapFolders = mapOf(
                MapType.SkirmishMap to File("maps/skirmish"),
                MapType.CustomMap to File("/SD/rustedWarfare/maps"),
                MapType.SavedGame to File("/SD/rustedWarfare/saves")
            )

            for((type, folder) in mapFolders) {
                val maps = mutableListOf<GameMap>()
                folder
                    .walk()
                    .filter { it.name.endsWith(".tmx") }
                    .forEachIndexed { i, f ->
                        maps.add(object : GameMap {
                            override val id: Int = i
                            override val image: Painter? =
                                if(mapType != MapType.SavedGame) File(f.parent!! + "/" + f.name.removeSuffix(".tmx") + "_map.png")
                                    .let {
                                        if(it.exists()) {
                                            BitmapPainter(BitmapFactory.decodeFile(it.absolutePath).asImageBitmap())
                                        } else null }
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
    override val coroutineContext: CoroutineContext = Job()

}