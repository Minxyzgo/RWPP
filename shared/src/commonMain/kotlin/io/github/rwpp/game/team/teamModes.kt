/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.team

import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import org.koin.core.annotation.Single


@Single(binds = [TeamMode::class])
class InternalTeamMode2t : InternalTeamMode {
    override val name: String = "2t"

    override val displayName: String = "2 Teams (eg 5v5)"

    override fun onPlayerJoin(gameRoom: GameRoom, player: Player) {
        // normal , do nothing
    }
}

@Single(binds = [TeamMode::class])
class InternalTeamMode3t : InternalTeamMode {
    override val name: String = "3t"

    override val displayName: String = "3 Teams (eg 1v1v1)"

    override fun onPlayerJoin(gameRoom: GameRoom, player: Player) {
        player.applyConfigChange(
            team = player.spawnPoint % 3 + 1
        )
    }
}

@Single(binds = [TeamMode::class])
class InternalTeamModeFFA : InternalTeamMode {
    override val name: String = "FFA"

    override val displayName: String = "No teams (FFA)"

    override fun onPlayerJoin(gameRoom: GameRoom, player: Player) {
        player.applyConfigChange(
            team = player.spawnPoint + 1
        )
    }
}

@Single(binds = [TeamMode::class])
class InternalTeamModeSpectators : InternalTeamMode {
    override val name: String = "spectators"

    override val displayName: String = "All spectators"

    override fun onPlayerJoin(gameRoom: GameRoom, player: Player) {
        // It doesn't seem necessary
    }
}