package io.github.rwpp.game

import androidx.compose.ui.graphics.Color
import io.github.rwpp.game.base.Difficulty

interface Player {
    val connectHexId: String
    val spawnPoint: Int
    val name: String
    val ping: String
    val team: Int
    val startingUnit: Int
    val color: Int
    val isSpectator: Boolean
    val isAI: Boolean
    val difficulty: Difficulty?

    fun teamAlias() = when {
        team == -3 -> "S"
        team <= 10 -> Char('A'.code + team).toString()
        else -> team.toString()
    }

    fun applyConfigChange(
        spawnPoint: Int,
        team: Int,
        color: Int?,
        startingUnits: Int?,
        aiDifficulty: Difficulty?,
        changeTeamFromSpawn: Boolean
    )


    companion object {
        val teamColors = listOf(
            Color.Green,
            Color.Red,
            Color.Blue,
            Color.Yellow,
            Color.Cyan,
            Color(249, 246, 238),
            Color.Black,
            Color(255, 192, 203),
            Color(237, 112, 20),
            Color(51, 0, 102)
        )

        fun getTeamColor(team: Int): Color = teamColors.getOrNull(team) ?: Color.Gray
    }
}