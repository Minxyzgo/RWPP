/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import com.corrodinggames.rts.gameFramework.j.ae
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.QuestionDialogEvent
import io.github.rwpp.event.events.QuestionReplyEvent

class ServerCallbackImpl : com.corrodinggames.rts.java.b.a() {

    init {
        a = this
    }
    override fun a(ae: ae) {
        if(GameImpl.rcnOption != null) {
            ae.a(GameImpl.rcnOption)
            GameImpl.rcnOption = null
            return
        }
        QuestionDialogEvent(
            if(ae.b != null)
                "Server Question"
            else ae.e ?: "Password Required",
            if(ae.b != null)
                com.corrodinggames.rts.gameFramework.h.a.c(ae.b)
            else "This server requires a password to join"
        ).broadCastIn()
        GlobalEventChannel.filter(QuestionReplyEvent::class).subscribeOnce {
            if(!it.cancel) {
                ae.a(it.message!!)
            } else {
                ae.a()
            }
        }
    }
}