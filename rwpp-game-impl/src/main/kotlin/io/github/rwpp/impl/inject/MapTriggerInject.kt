/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl.inject

import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.TriggerActivateEvent
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

@InjectClass(com.corrodinggames.rts.gameFramework.n.d::class)
object MapTriggerInject {
    @Inject("a", InjectMode.InsertBefore)
    fun onTrigger(f: com.corrodinggames.rts.gameFramework.n.f, a: com.corrodinggames.rts.gameFramework.n.a) {
        TriggerActivateEvent(a.b).broadcastIn()
    }
}