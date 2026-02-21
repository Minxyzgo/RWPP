/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import com.corrodinggames.rts.game.units.custom.l
import io.github.rwpp.appKoin
import io.github.rwpp.game.mod.Mod
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.game.units.MovementType
import io.github.rwpp.game.units.UnitType
import io.github.rwpp.inject.SetInterfaceOn

@SetInterfaceOn([com.corrodinggames.rts.game.units.`as`::class])
interface UnitTypeImpl : UnitType {
    val self: com.corrodinggames.rts.game.units.`as`

    override val name: String
        get() = self.v()
    override val displayName: String
        get() = self.e()
    override val description: String
        get() = self.f()
    // Nevertheless, we can change it soon
//                    override val painter: Painter? = (it as? com.corrodinggames.rts.game.units.custom.l)?.ad?.let {
//                            runCatching {
//                                ImageIO.read(File(it.a().replace("/", "\\"))).toPainter()
//                            }.getOrElse { e ->
//                                println("error on reading path:${it.a()}")
//                                e.printStackTrace()
//                                null
//                            }
//                        }
    override val movementType: MovementType
        get() = MovementType.valueOf(self.o().name)

    override val isBuilder: Boolean
        get() = self.l()

    override val mod: Mod?
        get() = (self as? l)?.J?.s?.let(appKoin.get<ModManager>()::getModByName)
}