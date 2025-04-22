/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl.inject

import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.PlayerLeaveEvent
import io.github.rwpp.game.Player
import io.github.rwpp.impl.Client
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

@InjectClass(Client::class)
object ClientInject {
    @Inject("a", InjectMode.InsertBefore)
    fun Client.onPlayerDisconnect(
        z1: Boolean,
        z2: Boolean,
        str: String?
    ) {
        if (this.z != null) {
            PlayerLeaveEvent(
                this.z as Player, com.corrodinggames.rts.gameFramework.j.ad
                    .i(str ?: "")
            ).broadcastIn()
        }
    }
}