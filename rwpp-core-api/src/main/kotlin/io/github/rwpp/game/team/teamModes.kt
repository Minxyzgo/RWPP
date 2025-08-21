/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.team

import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player

sealed class InternalTeamMode(name: String) : TeamMode(name) {
    override fun onInit(gameRoom: GameRoom) {
    }
}

object InternalTeamMode2t : InternalTeamMode("2t") {
    override val displayName: String = "2 Teams (eg 5v5)"

    override fun autoTeamAssign(gameRoom: GameRoom, targetSpawnPoint: Int, player: Player): Int {
        return targetSpawnPoint % 2
    }

    override fun onPlayerJoin(gameRoom: GameRoom, player: Player) {
    }
}

object InternalTeamMode3t : InternalTeamMode("3t") {
    override val displayName: String = "3 Teams (eg 1v1v1)"

    override fun autoTeamAssign(gameRoom: GameRoom, targetSpawnPoint: Int, player: Player): Int {
        return targetSpawnPoint % 3
    }

    override fun onPlayerJoin(gameRoom: GameRoom, player: Player) {
        player.applyConfigChange(
            team = player.spawnPoint % 3 + 1
        )
    }
}

object InternalTeamModeFFA : InternalTeamMode("FFA") {
    override val displayName: String = "No teams (FFA)"

    override fun autoTeamAssign(gameRoom: GameRoom, targetSpawnPoint: Int, player: Player): Int {
        return targetSpawnPoint
    }

    override fun onPlayerJoin(gameRoom: GameRoom, player: Player) {
        player.applyConfigChange(
            team = player.spawnPoint + 1
        )
    }
}

object InternalTeamModeSpectators : InternalTeamMode("spectators") {
    override val displayName: String = "All spectators"

    override fun autoTeamAssign(gameRoom: GameRoom, targetSpawnPoint: Int, player: Player): Int {
        return -3
    }

    override fun onPlayerJoin(gameRoom: GameRoom, player: Player) {
        // It doesn't seem necessary
    }
}

object RandomTeamMode : TeamMode("random-team") {
    override val displayName: String = "Random Team"

    override fun onPlayerJoin(gameRoom: GameRoom, player: Player) {
        // do nothing
    }

    override fun onInit(gameRoom: GameRoom) {
        val playerSize = gameRoom.getPlayers().size
        val shuffled = gameRoom.getPlayers().shuffled()

        val teamCount = when(playerSize) {
            in 1.. 8 -> 2
            in 9..21 -> 3
            in 22..32 -> 4
            else -> 5
        }

        var leftCount = teamCount
        var currentTeam = 0

        shuffled.forEach { player ->
            player.team = currentTeam

            leftCount--

            if(leftCount == 0) {
                leftCount = teamCount
                currentTeam++
            }
        }
    }
}

object AllVsAI : TeamMode("all-vs-ai") {
    override val displayName: String = "All vs AI"

    override fun autoTeamAssign(gameRoom: GameRoom, targetSpawnPoint: Int, player: Player): Int {
        return if (player.isAI) 2 else 1
    }

    override fun onPlayerJoin(gameRoom: GameRoom, player: Player) {
        player.applyConfigChange(
            team = if (player.isAI) 1 else 0,
        )
    }

    override fun onInit(gameRoom: GameRoom) {
        gameRoom.getPlayers().forEach { player ->
            if (player.isAI) {
                player.team = 1
            } else {
                player.team = 0
            }
        }
    }
}

object AllVs2 : TeamMode("all-vs-2") {
    override val displayName: String = "All vs 2 (survival)"
    override fun autoTeamAssign(gameRoom: GameRoom, targetSpawnPoint: Int, player: Player): Int {
        return if (player.spawnPoint == 1) 1 else 0
    }

    override fun onPlayerJoin(gameRoom: GameRoom, player: Player) {
        player.applyConfigChange(
            team = if (player.spawnPoint == 1) 2 else 1,
        )
    }

    override fun onInit(gameRoom: GameRoom) {
        gameRoom.getPlayers().forEach { player ->
            if (player.spawnPoint == 1) {
                player.team = 1
            } else {
                player.team = 0
            }
        }
    }
}
