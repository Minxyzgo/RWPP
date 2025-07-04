/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl.inject

import android.graphics.Color
import com.corrodinggames.rts.game.units.a.p
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope
import com.corrodinggames.rts.gameFramework.f.am
import io.github.rwpp.appKoin
import io.github.rwpp.config.Settings
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.impl.FClass
import io.github.rwpp.impl.GameEngine
import io.github.rwpp.impl.GameView
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult
import io.github.rwpp.utils.Reflect
import kotlin.math.roundToInt


@InjectClass(GameView::class)
object GameViewInject {
    var buttons: java.util.ArrayList<Any?>? = null

    val teamChat: Any? by lazy { Reflect.get(render!!, "q") }
    val mapPing: Any? by lazy { Reflect.get(render!!, "r") }
    @Inject("a", InjectMode.InsertBefore)
    fun GameView.onAddGameAction(am: com.corrodinggames.rts.game.units.am?, arrayList: java.util.ArrayList<Any?>?): Any {
        buttons = buttons ?: Reflect.get(this, "aq")
        render = render ?: Reflect.get(this, "a")
        buttons?.clear()
        val q: Int = render!!.q()
        if (q == 0) {
            if (GameEngine.B().bQ.showChatAndPingShortcuts && GameEngine.B().M()) {
                buttons!!.add(
                    teamChat
                )
                buttons!!.add(
                    mapPing
                )

                if (appKoin.get<Settings>().showExtraButton) {
                    buttons!!.add(
                        SelectBuild
                    )
                }
            }
            return InterruptResult(buttons!!)
        }

        return buttons!!
    }

    object SelectBuild : p("c__cut_enable") {
        // com.corrodinggames.rts.game.units.a.s
        override fun b(): String {
            return readI18n("settings.showAttackRange")
        }

        override fun compareTo(other: Any?): Int {
            return 0
        }

        // com.corrodinggames.rts.game.units.a.s
        override fun a(): String {
            return "Select Buildings"
        }

        // com.corrodinggames.rts.game.units.a.s
        override fun c(amVar: com.corrodinggames.rts.game.units.am?, z: Boolean): Boolean {
            //GameEngine.B().bS.g.n()
            settings.showBuildingAttackRange = !settings.showBuildingAttackRange
            return true
        }
    }

    @Inject("e", InjectMode.Override)
    fun GameView.unitGroupUI(f: Float) {
        render = render ?: Reflect.get(this, "a")
        io.github.rwpp.impl.inject.GameViewInject.unitGroups = io.github.rwpp.impl.inject.GameViewInject.unitGroups ?: Reflect.get(this, "aA")
        val B = GameEngine.B()
        val i = (B.cH - (30.0f * B.cj))
        val i3 = ((B.cq - 20.0f).roundToInt()) / 3
        var i2 = ((B.cl - B.cq) - i3 * (io.github.rwpp.impl.inject.GameViewInject.settings.maxDisplayUnitGroupCount - 3) + 10)
        val i4 = i3 - 5
        for (i5 in 0..<io.github.rwpp.impl.inject.GameViewInject.unitGroups!!.size) {
            val amVar = io.github.rwpp.impl.inject.GameViewInject.unitGroups!![i5]
            if (amVar.h) {
                amVar.e()
                amVar.h = false
            }
            amVar.d()
            if (B.bQ.keyboardSupport && i5 < B.bT.ai.size) {
                if (B.bT.ak.get(i5).a()) {
                    amVar.b()
                    amVar.c()
                }
                if (B.bT.aj.get(i5).a()) {
                    render!!.l()
                    amVar.a()
                }
                if (B.bT.ai.get(i5).a()) {
                    render!!.l()
                    render!!.y()
                    amVar.a()
                }
            }
            if (B.bQ.showUnitGroups && i5 < io.github.rwpp.impl.inject.GameViewInject.settings.maxDisplayUnitGroupCount) {
                val str = if (amVar.a.size == 0) {
                    if (render!!.bN) {
                        "Empty"
                    } else {
                        "(" + (i5 + 1) + ")"
                    }
                } else {
                    VariableScope.nullOrMissingString + amVar.a.size
                }
                amVar.d = FClass.a(amVar.d, 0.01f * f)
                amVar.e = FClass.a(amVar.e, 0.01f * f)
                amVar.f = FClass.a(amVar.f, 0.01f * f)

                render!!.a(
                    i2.roundToInt(),
                    i.roundToInt(),
                    i4,
                    (31.0f * B.cj).roundToInt(),
                    str,
                    com.corrodinggames.rts.gameFramework.f.i.a,
                    true,
                    Color.a(
                        50,
                        (100.0f + (amVar.f * 100.0f)).roundToInt(),
                        (100.0f + (amVar.e * 100.0f)).roundToInt(),
                        (100.0f + (amVar.d * 100.0f)).roundToInt()
                    )
                )
                i2 += i3
            }
        }
    }

    var render: com.corrodinggames.rts.gameFramework.f.g? = null
    var unitGroups: ArrayList<am>? = null
    val settings by lazy { appKoin.get<Settings>() }
}