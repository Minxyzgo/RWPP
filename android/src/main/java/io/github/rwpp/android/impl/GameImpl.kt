package io.github.rwpp.android.impl

import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.GameMap
import io.github.rwpp.game.map.MapType
import io.github.rwpp.game.map.Mission
import io.github.rwpp.game.map.MissionType
import io.github.rwpp.ui.LoadingContext


class GameImpl : Game {
    override val gameVersion: Int
        get() = 176
    override val gameRoom: GameRoom
        get() = TODO("Not yet implemented")

    override fun startNewMissionGame(difficulty: Difficulty, mission: Mission) {
        TODO("Not yet implemented")
    }

    override suspend fun load(context: LoadingContext) {
        // do nothing
        context.message("finished.")
    }

    override fun hostStartWithPasswordAndMods(isPublic: Boolean, password: String?, useMods: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setUserName(name: String) {
        TODO("Not yet implemented")
    }

    override suspend fun directJoinServer(address: String, uuid: String?, context: LoadingContext): Result<String> {
        TODO("Not yet implemented")
    }

    override fun cancelJoinServer() {
        TODO("Not yet implemented")
    }

    override fun onRcnCallback(option: String) {
        TODO("Not yet implemented")
    }

    override fun setTeamUnitCapHostGame(cap: Int) {
        TODO("Not yet implemented")
    }

    override fun getAllMissionTypes(): List<MissionType> {
        TODO("Not yet implemented")
    }

    override fun getAllMissions(): List<Mission> {
        TODO("Not yet implemented")
    }

    override fun getAllMaps(): List<GameMap> {
        TODO("Not yet implemented")
    }

    override fun getAllMapsByMapType(mapType: MapType): List<GameMap> {
        TODO("Not yet implemented")
    }

    override fun getMissionsByType(type: MissionType): List<Mission> {
        TODO("Not yet implemented")
    }
}