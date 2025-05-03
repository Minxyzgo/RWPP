/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import io.github.rwpp.android.playerCacheMap
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.PlayerLeaveEvent
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

@InjectClass(com.corrodinggames.rts.gameFramework.j.c::class)
object ClientInject {
    @Inject("a", InjectMode.InsertBefore)
    fun com.corrodinggames.rts.gameFramework.j.c.onPlayerDisconnect(
        z1: Boolean,
        z2: Boolean,
        str: String?
    ) {
        if (this.A != null) {
            playerCacheMap[this.A]?.let { PlayerLeaveEvent(it, com.corrodinggames.rts.gameFramework.j.ae
                .g(str ?: "")).broadcastIn() }
            playerCacheMap.remove(this.A)
        }
    }
}