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
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.ui.LoadingContext

interface Game {
    /**
     * 1.15 -> 176
     */
    val gameVersion: Int

    /**
     * Current game room.
     */
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

    /**
     * Host a new multiplayer game room.
     * @param isPublic whether the room will be added to the room list.
     * @param password the password of the room.
     * @param useMods whether the mod will be used or not in the room.
     */
    fun hostStartWithPasswordAndMods(
        isPublic: Boolean,
        password: String?,
        useMods: Boolean,
    )

    /**
     * Host a new sandbox game.
     */
    fun hostNewSandbox()

    /**
     * Set the name of the local player.
     */
    fun setUserName(name: String)

    /**
     * Direct join a server by specific address.
     * @param uuid room uuid.
     * @param context describes the loading context.
     */
    suspend fun directJoinServer(
        address: String,
        uuid: String?,
        context: LoadingContext
    ): Result<String>

    /**
     * Interrupt directly joining Server
     */
    fun cancelJoinServer()

    /**
     * Set the option, and it will be used automatically to reply the next question.
     */
    fun onQuestionCallback(option: String)

    /**
     * Set the team unit capacity.
     */
    fun setTeamUnitCapHostGame(cap: Int)

    /**
     * Get all mission Types.
     */
    fun getAllMissionTypes(): List<MissionType>

    /**
     * Get all missions.
     */
    fun getAllMissions(): List<Mission>

    /**
     * Get all missions by given type.
     */
    fun getMissionsByType(type: MissionType): List<Mission>

    /**
     * Get all maps.
     */
    fun getAllMaps(): List<GameMap>

    /**
     * Get all maps by given map type.
     * @param mapType the map type.
     */
    fun getAllMapsByMapType(mapType: MapType): List<GameMap>

    /**
     * Get starting unit option list.
     *
     * It will return like (index -> option name).
     *
     * You can use the index to set [GameRoom.startingUnits].
     */
    fun getStartingUnitOptions(): List<Pair<Int, String>>

    /**
     * Get all the unit list.
     */
    fun getAllUnits(): List<GameUnit>

    /**
     * Ban given unit, and all actions about the unit will not executed.
     */
    fun onBanUnits(units: List<GameUnit>)
}