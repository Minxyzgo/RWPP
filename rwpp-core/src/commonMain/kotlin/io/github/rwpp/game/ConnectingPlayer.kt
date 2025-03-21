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

object ConnectingPlayer : Player {
    override val connectHexId: String
        get() = ""
    override var spawnPoint: Int
        get() = 0
        set(_) {}
    override var name: String
        get() = "Connecting..."
        set(_) {}
    override val ping: String
        get() = ""
    override var team: Int
        get() = 0
        set(_) {}
    override var startingUnit: Int
        get() = 0
        set(_) {}
    override var color: Int
        get() = 0
        set(_) {}
    override val isSpectator: Boolean
        get() = false
    override val isAI: Boolean
        get() = false
    override var difficulty: Difficulty?
        get() = null
        set(_) {}
    override var credits: Int
        get() = 0
        set(_) {}
    override val statisticsData: PlayerStatisticsData
        get() = PlayerStatisticsData(0, 0 ,0 , 0, 0, 0)
    override val income: Int
        get() = 0
    override val isDefeated: Boolean
        get() = false
    override val isWipedOut: Boolean
        get() = false
    override val client: Client?
        get() = null

    override fun applyConfigChange(
        spawnPoint: Int,
        team: Int,
        color: Int?,
        startingUnits: Int?,
        aiDifficulty: Difficulty?,
        changeTeamFromSpawn: Boolean
    ) {}
}