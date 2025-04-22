/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl

import com.corrodinggames.rts.game.units.am
import io.github.rwpp.game.Player
import io.github.rwpp.game.comp.UnitComp
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.game.units.UnitType
import io.github.rwpp.inject.NewField
import io.github.rwpp.inject.SetInterfaceOn

@SetInterfaceOn([am::class])
interface GameUnitImpl : GameUnit {
    val self: am

    @NewField
    var _comp: Any

    override var comp: UnitComp
        get() = _comp as UnitComp
        set(value) { _comp = value }

    override val player: Player
        get() = self.bX as Player

    override val isDead: Boolean
        get() = self.bV

    override val type: UnitType
        get() = self.dz as UnitType

    override val x: Float
        get() = self.eo

    override val y: Float
        get() = self.ep

    override val target: GameUnit?
        get() = (this as? com.corrodinggames.rts.game.units.y)?.R as? GameUnit

    override val health: Float
        get() = self.cu

    override val maxAttackRange: Float
        get() = (this as? com.corrodinggames.rts.game.units.y)?.m() ?: 0f

    override val maxHealth: Float
        get() = self.cv
}