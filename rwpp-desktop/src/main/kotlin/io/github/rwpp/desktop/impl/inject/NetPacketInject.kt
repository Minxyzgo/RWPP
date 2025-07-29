/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject

import com.corrodinggames.rts.game.units.d.l
import com.corrodinggames.rts.game.units.y
import com.corrodinggames.rts.gameFramework.e
import com.corrodinggames.rts.gameFramework.utility.m
import io.github.rwpp.appKoin
import io.github.rwpp.config.Settings
import io.github.rwpp.desktop.NetPacket
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

@InjectClass(NetPacket::class)
object NetPacketInject {
    @Suppress("UNCHECKED_CAST")
    @Inject("a", injectMode = InjectMode.InsertBefore)
    fun e.onEnhancedReinforceTroops(): Any {
        val settings = appKoin.get<Settings>()
        if (settings.enhancedReinforceTroops) {
            val actionString = k.a()
            if (actionString != "-1") {
                val l = vField.get(this) as List<y>
                val m = m(l.sortedBy { (it as? l)?.dx()?.size ?: 0 })
                vField.set(this, m)
            }
        }

        return Unit
    }

    private val vField = NetPacket::class.java.getDeclaredField("v").apply { isAccessible = true }
}
