package io.github.rwpp.game

import io.github.rwpp.game.base.Difficulty
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


    fun getPlayers(): List<Player>

    fun roomDetails(): String

    fun sendChatMessage(message: String)

    fun sendSystemMessage(message: String)

    fun addAI()

    fun applyTeamChange(mode: String)

    fun kickPlayer(player: Player)

    fun disconnect()

    fun startGame()
}