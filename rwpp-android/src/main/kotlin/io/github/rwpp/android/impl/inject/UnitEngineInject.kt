/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import io.github.rwpp.event.broadcast
import io.github.rwpp.event.events.ModCheckEvent
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult
import io.github.rwpp.utils.Reflect
import kotlinx.coroutines.runBlocking

@InjectClass(com.corrodinggames.rts.game.units.custom.l::class)
object UnitEngineInject {
    @Inject("a", InjectMode.InsertBefore)
    fun onCheckUnitData(ab: com.corrodinggames.rts.game.units.custom.ab, map: java.util.HashMap<*, com.corrodinggames.rts.game.units.custom.ac>): Any {
        val requiredMods = map.values.mapNotNull { Reflect.get<String>(it, "a")  }
        val event = runBlocking { ModCheckEvent(requiredMods).broadcast() }
        return if (event.isIntercepted) InterruptResult() else Unit
    }
}