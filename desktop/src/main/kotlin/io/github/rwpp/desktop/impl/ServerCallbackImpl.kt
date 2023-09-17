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