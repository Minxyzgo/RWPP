/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl.inject

import io.github.rwpp.impl.GameEngine
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult

@InjectClass(GameEngine::class)
object GameEngineInject {
    val interruptResult = InterruptResult()
    @Inject("c", injectMode = InjectMode.InsertBefore)
    fun clearLog(str: String): Any {
        return when (str) {
            "Fail safe menu" -> interruptResult
            "--- setRunning ---" -> interruptResult
            else -> Unit
        }
    }
}