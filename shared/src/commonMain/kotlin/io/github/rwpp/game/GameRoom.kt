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
    var maxPlayerCount: Int
    val isHost: Boolean
    val isHostServer: Boolean
    val localPlayer: Player
    var sharedControl: Boolean
    val randomSeed: Int
    val mapType: MapType
    var selectedMap: GameMap
    var startingCredits: Int
    var startingUnits: Int
    var fogMode: FogMode
    val revealedMap: Boolean
    var aiDifficulty: Difficulty
    var incomeMultiplier: Float
    var noNukes: Boolean
    var allowSpectators: Boolean
    var lockedRoom: Boolean
    var teamLock: Boolean
    val mods: Array<String>

    var isRWPPRoom: Boolean
    var option: RoomOption

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
     * Add an AI to the room. (if host)
     */
    fun addAI()

    /**
     * Apply team change mod.
     *
     * @param mode available mods: 2t, 3t, FFA, spectators
     */
    fun applyTeamChange(mode: String)

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