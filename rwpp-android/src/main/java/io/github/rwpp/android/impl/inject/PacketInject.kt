/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import io.github.rwpp.android.impl.NetPacket
import io.github.rwpp.appKoin
import io.github.rwpp.config.Settings
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

@InjectClass(NetPacket::class)
object PacketInject {
    @Inject("a", InjectMode.InsertBefore)
    fun NetPacket.onSendGameCommand(a: com.corrodinggames.rts.gameFramework.j.bg) {
        if (settings.enhancedReinforceTroops) {
            val actionString = this.k.b
            if (actionString != "-1") {
                val l = wField.get(this) as List<*>
                val m = com.corrodinggames.rts.gameFramework.utility.p(l.sortedBy {
                    (it as? com.corrodinggames.rts.game.units.d.s)?.cY()?.size ?: 0
                })
                wField.set(this, m)
            }
        }
    }

    private val wField = NetPacket::class.java.getDeclaredField("w").apply {
        isAccessible = true
    }

    private val settings = appKoin.get<Settings>()
}