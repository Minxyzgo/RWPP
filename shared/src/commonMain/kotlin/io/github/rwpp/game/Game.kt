package io.github.rwpp.game

import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.GameMap
import io.github.rwpp.game.map.MapType
import io.github.rwpp.game.map.Mission
import io.github.rwpp.game.map.MissionType
import io.github.rwpp.ui.LoadingContext

interface Game {
    val gameVersion: Int

    val gameRoom: GameRoom

    /**
     * Start a mission game with specific difficulty.
     */
    fun startNewMissionGame(difficulty: Difficulty, mission: Mission)

    /**
     * Init rw engine.
     * @param context describes how the loading information show.
     */
    suspend fun load(context: LoadingContext)

    fun hostStartWithPasswordAndMods(
        isPublic: Boolean,
        password: String?,
        useMods: Boolean,
    )

    fun setUserName(name: String)

    suspend fun directJoinServer(address: String, uuid: String?, context: LoadingContext): Result<String>

    fun cancelJoinServer()

    fun onRcnCallback(option: String)

    fun setTeamUnitCapHostGame(cap: Int)

    fun getAllMissionTypes(): List<MissionType>

    fun getAllMissions(): List<Mission>

    fun getAllMaps(): List<GameMap>

    fun getAllMapsByMapType(mapType: MapType): List<GameMap>

    fun getMissionsByType(type: MissionType): List<Mission>
}