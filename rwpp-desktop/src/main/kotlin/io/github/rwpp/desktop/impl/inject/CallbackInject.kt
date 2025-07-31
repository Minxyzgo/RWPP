/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject

import android.app.Activity
import com.corrodinggames.rts.gameFramework.h.a
import com.corrodinggames.rts.gameFramework.j.ae
import io.github.rwpp.desktop.GameEngine
import io.github.rwpp.desktop.rcnOption
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult
import io.github.rwpp.ui.UI


@InjectClass(com.corrodinggames.librocket.a::class)
object CallbackInject {
    @Inject("a", InjectMode.InsertBefore)
    fun onSetCallback(ae: ae): Any? {
        if(rcnOption != null) {
            ae.a(rcnOption)
            rcnOption = null
            return InterruptResult()
        }


        if (ae.e == "Search units") return Unit

        UI.showQuestion(
            if(ae.b != null)
                "Server Question"
            else ae.e ?: "Password Required",
            if(ae.b != null)
                a.c(ae.b)
            else "This server requires a password to join"
        ) {
            if (it == null) {
                ae.a()
            } else {
                ae.a(it)
            }
        }

        return InterruptResult()
    }

    @Inject("b", InjectMode.Override)
    fun com.corrodinggames.librocket.a.noMainMenu() {
        GameEngine.B()?.a(null as Activity?, this.c, true)
    }
}