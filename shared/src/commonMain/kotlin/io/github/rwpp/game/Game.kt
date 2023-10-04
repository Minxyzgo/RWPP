/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game

import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.GameMap
import io.github.rwpp.game.map.MapType
import io.github.rwpp.game.map.Mission
import io.github.rwpp.game.map.MissionType
import io.github.rwpp.game.units.GameInternalUnits
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

    fun hostNewSandbox()

    fun setUserName(name: String)

    suspend fun directJoinServer(address: String, uuid: String?, context: LoadingContext): Result<String>

    fun cancelJoinServer()

    fun onQuestionCallback(option: String)

    fun setTeamUnitCapHostGame(cap: Int)

    fun getAllMissionTypes(): List<MissionType>

    fun getAllMissions(): List<Mission>

    fun getAllMaps(): List<GameMap>

    fun getAllMapsByMapType(mapType: MapType): List<GameMap>

    fun getMissionsByType(type: MissionType): List<Mission>

    fun onBanUnits(units: List<GameInternalUnits>)
}