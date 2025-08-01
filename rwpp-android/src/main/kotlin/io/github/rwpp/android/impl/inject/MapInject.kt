/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.TriggerActivateEvent
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

typealias GameMapInternal = com.corrodinggames.rts.gameFramework.n.a
@InjectClass(GameMapInternal::class)
object MapInject {
    @Inject("b", InjectMode.InsertBefore)
    fun GameMapInternal.onReadStringProperty(name: String) {
        //此方法仅在触发Trigger时会调用一次， 因此可用于判断是否触发
        if (name == "debugMessage") TriggerActivateEvent(b).broadcastIn()
    }
}