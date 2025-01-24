/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import android.util.Log
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.android.isReturnToBattleRoom
import io.github.rwpp.core.UI
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult

@InjectClass(GameEngine::class)
object GameEngineInject {
    @Inject("g", InjectMode.Override)
    fun onSendWarning(message: String) {
        if (message.startsWith("Kicked") || message.startsWith("Missing")) UI.showWarning(message, true)
    }

    @Inject("d", InjectMode.InsertBefore)
    fun log(str: String): Any {
        if(str == "----- returnToBattleroom -----") {
            isReturnToBattleRoom = true
        }

        return Unit
    }

    @Inject("c", InjectMode.Override)
    fun logI(str: String?) {
        Log.i("RustedWarfare", str ?: "")
    }

    @Inject("b", InjectMode.InsertBefore)
    fun showToast(title: String, message: String): Any {
        return if (title == "Players" || title == "Briefing") {
            Unit
        } else {
            InterruptResult(Unit)
        }
    }
}