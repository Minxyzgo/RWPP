/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject

import com.corrodinggames.rts.game.units.a.p
import com.corrodinggames.rts.gameFramework.ad
import io.github.rwpp.desktop.impl.GameEngine
import io.github.rwpp.desktop.impl.GameView
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult
import io.github.rwpp.utils.Reflect
//
//@InjectClass(GameView::class)
//object GameViewInject {
//    var buttons: java.util.ArrayList<Any?>? = null
//    var logic: com.corrodinggames.rts.gameFramework.f.g? = null
//    val teamChat: Any? by lazy { Reflect.get(logic!!, "q") }
//    val mapPing: Any? by lazy { Reflect.get(logic!!, "r") }
//    @Inject("a", InjectMode.InsertBefore)
//    fun GameView.onAddGameAction(am: com.corrodinggames.rts.game.units.am?, arrayList: java.util.ArrayList<Any?>?): Any {
//        buttons = buttons ?: Reflect.get(this, "aq")
//        logic = logic ?: Reflect.get(this, "a")
//        buttons?.clear()
//        val q: Int = logic!!.q()
//        if (q == 0) {
//            if (GameEngine.B().bQ.showChatAndPingShortcuts && GameEngine.B().M()) {
//                buttons!!.add(0, teamChat)
//                buttons!!.add(0, mapPing)
//                buttons!!.add(1, SelectBuild)
//            }
//            return InterruptResult(buttons!!)
//        }
//
//        return buttons!!
//    }
//
//    object SelectBuild : p("c__cut_chat") {
//        // com.corrodinggames.rts.game.units.a.s
//        override fun b(): String {
//            return "Select Buildings"
//        }
//
//        override fun compareTo(other: Any?): Int {
//            return 0
//        }
//
//        // com.corrodinggames.rts.game.units.a.s
//        override fun a(): String {
//            return "Select Buildings"
//        }
//
//        // com.corrodinggames.rts.game.units.a.s
//        override fun c(amVar: com.corrodinggames.rts.game.units.am?, z: Boolean): Boolean {
//            GameEngine.B().bS.g.n()
//            return true
//        }
//
//        // com.corrodinggames.rts.game.units.a.s
//        override fun M(): ad {
//            return GameEngine.B().bT.u
//        }
//    }
//}