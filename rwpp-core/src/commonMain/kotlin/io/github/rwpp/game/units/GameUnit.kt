/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.units

import io.github.rwpp.game.Player
import io.github.rwpp.game.comp.UnitComp

interface GameUnit {
    val player: Player
    val isDead: Boolean
    val type: UnitType
    val x: Float
    val y: Float
    val health: Float
    val maxHealth: Float
    val maxAttackRange: Float
    val target: GameUnit?

    var comp: UnitComp
}