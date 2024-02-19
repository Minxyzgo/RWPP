/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game

import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.data.RoomOption
import io.github.rwpp.game.map.FogMode
import io.github.rwpp.game.map.GameMap
import io.github.rwpp.game.map.MapType

interface GameRoom {
    val maxPlayerCount: Int
    val isHost: Boolean
    val isHostServer: Boolean
    val localPlayer: Player
    val sharedControl: Boolean
    val randomSeed: Int
    val mapType: MapType
    var selectedMap: GameMap
    val startingCredits: Int
    val startingUnits: Int
    val fogMode: FogMode
    val revealedMap: Boolean
    val aiDifficulty: Difficulty
    val incomeMultiplier: Float
    val noNukes: Boolean
    val allowSpectators: Boolean
    var lockedRoom: Boolean
    val teamLock: Boolean
    val mods: Array<String>

    /**
     * Describe the current room is whether hosted by a RWPP protocol client (or server).
     */
    var isRWPPRoom: Boolean

    /**
     * The extra option of the RWPP room. If not, all the options are default.
     */
    var option: RoomOption

    /**
     * Describe whether client is connecting a network game.
     */
    val isConnecting: Boolean

    /**
     * Get all players from the room.
     */
    fun getPlayers(): List<Player>

    /**
     * Describe the detailed options of the room.
     */
    suspend fun roomDetails(): String

    /**
     * Send chat message by local player's own.
     */
    fun sendChatMessage(message: String)

    /**
     * Send system message. (if host)
     */
    fun sendSystemMessage(message: String)

    /**
     * Send quick game command.
     */
    fun sendQuickGameCommand(command: String)


    /**
     * Add an AI to the room. (if host)
     */
    fun addAI()

    /**
     * Apply the room config.
     *
     * @param teamMode available mods: 2t, 3t, FFA, spectators
     */
    fun applyRoomConfig(
        maxPlayerCount: Int,
        sharedControl: Boolean,
        startingCredits: Int,
        startingUnits: Int,
        fogMode: FogMode,
        aiDifficulty: Difficulty,
        incomeMultiplier: Float,
        noNukes: Boolean,
        allowSpectators: Boolean,
        teamLock: Boolean,
        teamMode: String?
    )

    /**
     * Kick a player. (is host)
     */
    fun kickPlayer(player: Player)

    /**
     * Disconnect from the room.
     */
    fun disconnect()

    /**
     * Start game. (if host)
     */
    fun startGame()
}