/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject

import com.corrodinggames.rts.game.n
import com.corrodinggames.rts.gameFramework.j.ac
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.PlayerJoinEvent
import io.github.rwpp.game.Player
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

@InjectClass(ac::class)
object NetworkCallbackInject {
    @Inject("a", InjectMode.Override)
    fun onPlayerJoin(
        player: n
    ) {
        PlayerJoinEvent(player as Player).broadcastIn()
    }
}