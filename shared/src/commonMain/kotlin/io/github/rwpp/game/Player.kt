/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game

import androidx.compose.ui.graphics.Color
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.data.PlayerData
import io.github.rwpp.net.Client

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

    /**
     * The player extra data.
     */
    val data: PlayerData

    val client: Client

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
        @Suppress("MemberVisibilityCanBePrivate")
        val teamColors = listOf(
            Color.Green,
            Color.Red,
            Color.Blue,
            Color.Yellow,
            Color.Cyan,
            Color(249, 246, 238),
            Color.Black,
            Color(255, 0, 255),
            Color(237, 112, 20),
            Color(51, 0, 102)
        )

        fun getTeamColor(team: Int): Color = teamColors.getOrNull(team) ?: Color.Gray
    }
}