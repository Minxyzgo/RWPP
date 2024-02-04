/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game

import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.data.PlayerData

object ConnectingPlayer : Player {
    override val connectHexId: String
        get() = ""
    override val spawnPoint: Int
        get() = 0
    override val name: String
        get() = "Connecting..."
    override val ping: String
        get() = ""
    override val team: Int
        get() = 0
    override val startingUnit: Int
        get() = 0
    override val color: Int
        get() = 0
    override val isSpectator: Boolean
        get() = false
    override val isAI: Boolean
        get() = false
    override val difficulty: Difficulty?
        get() = null
    override val data: PlayerData = PlayerData()

    override fun applyConfigChange(
        spawnPoint: Int,
        team: Int,
        color: Int?,
        startingUnits: Int?,
        aiDifficulty: Difficulty?,
        changeTeamFromSpawn: Boolean
    ) {}
}