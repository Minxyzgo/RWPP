/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game

import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.data.PlayerData
import io.github.rwpp.game.data.PlayerStatisticsData
import io.github.rwpp.net.Client

interface Player {
    val connectHexId: String
    var spawnPoint: Int
    var name: String
    val ping: String
    var team: Int

    /**
     * Set player's starting unit.
     *
     * None if value equals -1
     */
    var startingUnit: Int

    /**
     * Set player's color.
     *
     * None if value equals -1
     */
    var color: Int

    /**
     * isSpectator: team == -3
     */
    val isSpectator: Boolean
    val isAI: Boolean
    var difficulty: Difficulty?

    /**
     * The player's credits.
     */
    var credits: Int

    /**
     * The player's statistics data.
     */
    val statisticsData: PlayerStatisticsData

    /**
     * The player's income.
     */
    val income: Int

    val isDefeated: Boolean
    val isWipedOut: Boolean

    val data: PlayerData

    val client: Client?

    fun applyConfigChange(
        spawnPoint: Int = this.spawnPoint,
        team: Int = this.team,
        color: Int? = null,
        startingUnits: Int? = null,
        aiDifficulty: Difficulty? = null,
        changeTeamFromSpawn: Boolean = false
    )

    companion object
}

fun Player.teamAlias() = when {
    team == -3 -> "S"
    team <= 10 -> Char('A'.code + team).toString()
    else -> team.toString()
}