/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.team

import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player


abstract class TeamMode(val name: String) {
    open val displayName: String
        get() = name

    abstract fun onPlayerJoin(gameRoom: GameRoom, player: Player)

    abstract fun onInit(gameRoom: GameRoom)

    /**
     * @return the team number of the player will be assigned to.
     */
    open fun autoTeamAssign(gameRoom: GameRoom, targetSpawnPoint: Int, player: Player): Int {
        return player.team
    }

    companion object {
        val modes = mutableListOf<TeamMode>(
            InternalTeamMode2t,
            InternalTeamMode3t,
            InternalTeamModeFFA,
            InternalTeamModeSpectators,
            RandomTeamMode,
            AllVsAI,
            AllVs2,
        )
    }
}