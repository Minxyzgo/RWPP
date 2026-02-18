/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import io.github.rwpp.appKoin
import io.github.rwpp.game.Player
import io.github.rwpp.game.units.comp.EntityRangeUnitComp
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.game.units.UnitType
import io.github.rwpp.game.units.comp.UnitComp
import io.github.rwpp.inject.NewField
import io.github.rwpp.inject.SetInterfaceOn

@SetInterfaceOn([com.corrodinggames.rts.game.units.ce::class])
interface GameUnitImpl : GameUnit {
    val self: com.corrodinggames.rts.game.units.ce

    @NewField
    var _comp: List<UnitComp>?

    override val comp: List<UnitComp>
        get(){
            _comp = _comp ?: appKoin.getAll<UnitComp>()
            return _comp!!
        }

    override val player: Player
        get() = self.bZ as Player

    override val isDead: Boolean
        get() = self.bX

    override val type: UnitType
        get() = self.dB as UnitType

    override val health: Float
        get() = self.cw

    override val maxAttackRange: Float
        get() = (self as? com.corrodinggames.rts.game.units.bp)?.l() ?: 0f

    override val maxHealth: Float
        get() = self.cx

    override val target: GameUnit?
        get() = (self as? com.corrodinggames.rts.game.units.bp)?.T as GameUnit?

    override val x: Float
        get() = self.eq

    override val y: Float
        get() = self.er
}