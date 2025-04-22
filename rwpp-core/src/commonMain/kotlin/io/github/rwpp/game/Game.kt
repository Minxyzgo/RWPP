/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game

import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.*
import io.github.rwpp.game.ui.GUI
import io.github.rwpp.game.units.UnitType
import io.github.rwpp.game.world.World
import io.github.rwpp.widget.LoadingContext
import org.koin.core.component.KoinComponent

interface Game : KoinComponent {
    /**
     * Current game room.
     */
    val gameRoom: GameRoom

    val gui: GUI


    //TODO Not Implemented
    val world: World

    /**
     * Post an action to the main thread.
     */
    fun post(action: () -> Unit)

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
     * Host a new single player game.
     */
    fun hostNewSinglePlayer(sandbox: Boolean)

    /**
     * Set the displayName of the local player.
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
    fun getAllMaps(flush: Boolean = false): List<GameMap>

    /**
     * Get all maps by given map type.
     * @param mapType the map type.
     */
    fun getAllMapsByMapType(mapType: MapType): List<GameMap>

    /**
     * Get starting unit option list.
     *
     * It will return like (index -> option displayName).
     *
     * You can use the index to set [GameRoom.startingUnits].
     */
    fun getStartingUnitOptions(): List<Pair<Int, String>>

    /**
     * Get all the unit list.
     */
    fun getAllUnits(): List<UnitType>

    /**
     * Ban given unit, and all actions about the unit will not executed.
     */
    fun onBanUnits(units: List<UnitType>)

    /**
     * Get all replays.
     */
    fun getAllReplays(): List<Replay>

    /**
     * Watch the given replay.
     */
    fun watchReplay(replay: Replay)

    /**
     * Describe if the game could continue.
     */
    fun isGameCouldContinue(): Boolean

    /**
     * Continue the game, make sure [isGameCouldContinue] return true.
     */
    fun continueGame()
}