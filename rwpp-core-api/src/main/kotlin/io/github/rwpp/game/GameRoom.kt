/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game

import io.github.rwpp.commands
import io.github.rwpp.game.data.RoomOption
import io.github.rwpp.game.map.FogMode
import io.github.rwpp.game.map.GameMap
import io.github.rwpp.game.map.MapType
import io.github.rwpp.game.map.XMLMap
import io.github.rwpp.game.team.TeamMode
import io.github.rwpp.game.units.UnitType
import io.github.rwpp.net.Client

interface GameRoom {
    val maxPlayerCount: Int
    val isHost: Boolean
    val isHostServer: Boolean
    val localPlayer: Player
    val sharedControl: Boolean
    val randomSeed: Int
    val mapType: MapType
    var selectedMap: GameMap
    var displayMapName: String
    var startingCredits: Int
    var startingUnits: Int
    var fogMode: FogMode
    var revealedMap: Boolean
    var aiDifficulty: Int
    var incomeMultiplier: Float
    var noNukes: Boolean
    var allowSpectators: Boolean
    var lockedRoom: Boolean
    var teamLock: Boolean
    val mods: Array<String>
    val isStartGame: Boolean
    var teamMode: TeamMode?
    val isSinglePlayerGame: Boolean

    /**
     * Experimental
     */
    var gameSpeed: Float

    /**
     * Transform the map before loading. (if host)
     */
    var gameMapTransformer: ((XMLMap) -> Unit)?

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
     * @return all players from the room.
     */
    fun getPlayers(): List<Player>

    /**
     * Get a player by its client.
     * @param client the client of the player.
     */
    fun getPlayerByClient(client: Client): Player? {
        return getPlayers().firstOrNull { it.client == client }
    }

    /**
     * Describe the detailed options of the room.
     * @return the detailed options of the room.
     */
    suspend fun roomDetails(): String

    /**
     * Send chat message by local player's own.
     * @param message the content of the message.
     */
    fun sendChatMessage(message: String)

    /**
     * Send system message. (if host)
     * @param message the content of the message.
     */
    fun sendSystemMessage(message: String)

    /**
     * Send quick game command.
     * @param command the command to send.
     */
    fun sendQuickGameCommand(command: String)

    /**
     * Pause or resume the game.
     * @param pause true to pause the game, false to resume the game.
     */
    fun pauseOrResumeGame(pause: Boolean)

    /**
     * Send message to a player. (if host)
     * @param player the target player to send message. if null, send to local player.
     * @param title the title of the message.
     * @param message the content of the message.
     */
    fun sendMessageToPlayer(player: Player?, title: String?, message: String, color: Int = -1)

    /**
     * Let a player surrender. (if host)
     * @param player the target player to surrender.
     */
    fun sendSurrender(player: Player)

    /**
     * Spawn a unit to the map. (if host)
     * @param player the player who spawns the unit.
     * @param unitType the type of the unit to spawn.
     * @param x the x position of the unit.
     * @param y the y position of the unit.
     * @param size the size of the spawn radius.
     */
    fun spawnUnit(
        player: Player,
        unitType: UnitType,
        x: Float,
        y: Float,
        size: Int = 0
    )

    /**
     * Sync all players. (if host)
     */
    fun syncAllPlayer()

    /**
     * Add AIs to the room with the specific count. (if host)
     * @param count the count of AIs to add.r
     */
    fun addAI(count: Int = 1)

    /**
     * Apply the room config.
     */
    fun applyRoomConfig(
        maxPlayerCount: Int,
        sharedControl: Boolean,
        startingCredits: Int,
        startingUnits: Int,
        fogMode: FogMode,
        aiDifficulty: Int,
        incomeMultiplier: Float,
        noNukes: Boolean,
        allowSpectators: Boolean,
        teamLock: Boolean,
        teamMode: TeamMode?
    )

    /**
     * Get the remaining players count.
     * @return the remaining players count.
     */
    fun remainingPlayersCount(): Int

    /**
     * Kick a player. (if host)
     * @param player the target player to kick.
     */
    fun kickPlayer(player: Player)

    /**
     * Disconnect from the room.
     * @param reason the reason of the disconnect.
     */
    fun disconnect(reason: String = "excited")

    /**
     * Update UI. Contains local refresh and sending info packet (if host)
     */
    fun updateUI()

    /**
     * Start game. (if host)
     */
    fun startGame()
}

fun GameRoom.sendChatMessageOrCommand(message: String) {
    if (isHost && message.startsWith(commands.prefix)) {
        commands.handleCommandMessage(message, localPlayer) { sendMessageToPlayer(localPlayer, "RWPP", it) }
    } else sendChatMessage(message)
}