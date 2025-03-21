/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import com.corrodinggames.rts.game.units.am
import io.github.rwpp.game.Player
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.game.units.UnitType
import io.github.rwpp.inject.SetInterfaceOn

@SetInterfaceOn([am::class])
interface GameUnitImpl : GameUnit {
    val self: am

    override val player: Player
        get() = self.bX as Player

    override val isDead: Boolean
        get() = self.bV

    override val type: UnitType
        get() = self.dz as UnitType
}