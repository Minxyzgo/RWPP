/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.corrodinggames.rts.game.units.ce
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope
import com.corrodinggames.rts.gameFramework.ah
import com.corrodinggames.rts.gameFramework.f.a
import com.corrodinggames.rts.gameFramework.f.a.i
import com.corrodinggames.rts.gameFramework.f.av
import com.corrodinggames.rts.gameFramework.utility.`SlickToAndroidKeycodes$AndroidCodes`
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.appKoin
import io.github.rwpp.config.Settings
import io.github.rwpp.game.Game
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.inject.Accessor
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.SetInterfaceOn
import io.github.rwpp.utils.Reflect


typealias GUI = com.corrodinggames.rts.gameFramework.f.a
typealias FClass = com.corrodinggames.rts.gameFramework.f

@SetInterfaceOn([com.corrodinggames.rts.gameFramework.f.i::class])
interface RenderAccessor {
    @Accessor("I")
    val IFlag: Boolean
    @Accessor("T")
    val TFlag: Boolean
}

@InjectClass(GUI::class)
object GuiInject {
    var buttons: java.util.ArrayList<Any?>? = null
    val room by lazy { appKoin.get<Game>().gameRoom }
    val settings by lazy { appKoin.get<Settings>() }
    var unitGroups: ArrayList<ce>? = null

    private val rect = Rect()
    private val sRect = Rect()
    private val paintI = Paint()

    @Inject(method = "a", injectMode = InjectMode.InsertAfter)
    fun GUI.onAddGameActions(unit: ce?, arr: java.util.ArrayList<*>?) {
        buttons = buttons ?: Reflect.get(this, "aq")
        if (unit == null && settings.showExtraButton) {
            buttons!!.add(ShowAttackRangeBuilding)
            buttons!!.add(ShowAttackRangeUnits)
        }
        if (unit != null && settings.showExtraButton && (unit as GameUnit).player.team != room.localPlayer.team) {
            buttons!!.add(ShowAttackRange)
        }
    }

    @Inject(method = "f", injectMode = InjectMode.Override)
    fun GUI.onDrawUnitGroups(f: Float) {
        unitGroups = unitGroups
            ?: Reflect.get(this, "aA")
        var sb: String?
        var z: Boolean
        var f2: Float
        val engine = GameEngine.t()
        val i = (engine.cE - (engine.cg * 30.0f)).toInt()
        val i3 = ((engine.cn - 20.0f).toInt()) / 3
        var i2 = ((engine.ci - engine.cn) - i3 * (settings.maxDisplayUnitGroupCount - 3) - settings.displayUnitGroupXOffset + 10).toInt()
        val i4 = i3 - 5
        var i5 = 0
        val render = GameEngine.t().bP
        while (true) {
            val i6 = i5
            if (i6 >= unitGroups!!.size) {
                return
            }
            val avVar: av = unitGroups!![i6] as av
            if (avVar.h) {
                if (avVar.a.isNotEmpty()) {
                    val arrayList = ArrayList<Any?>()
                    val it = avVar.a.iterator()
                    while (it.hasNext()) {
                        val a2 = ah.a((it.next() as ce).ej, true)
                        if (a2 != null && !a2.bX) {
                            arrayList.add(a2)
                        }
                    }
                    avVar.a = arrayList
                }
                avVar.h = false
            }
            avVar.c()
            if (engine.bN.keyboardSupport && i6 < engine.bQ.ai.size) {
                if (engine.bQ.ak[i6].a()) {
                    avVar.a.clear()
                    avVar.b()
                }
                if (engine.bQ.aj[i6].a()) {
                    render.e()
                    avVar.a()
                }
                if (engine.bQ.ai[i6].a()) {
                    render.e()
                    render.h()
                    avVar.a()
                }
            }
            if (engine.bN.showUnitGroups && i6 < settings.maxDisplayUnitGroupCount) {
                if (avVar.a.isEmpty()) {
                    if (render.bN) {
                        sb = "Empty"
                    } else {
                        sb = "(" + (i6 + 1) + ")"
                    }
                } else {
                    sb = StringBuilder().append(avVar.a.size).toString()
                }
                avVar.d = FClass.a(avVar.d, 0.01f * f)
                avVar.e = FClass.a(avVar.e, 0.01f * f)
                avVar.f = FClass.a(avVar.f, 0.01f * f)
                val argb: Int = Color.argb(
                    50,
                    (100.0f + (avVar.f * 100.0f)).toInt(),
                    (100.0f + (avVar.e * 100.0f)).toInt(),
                    (100.0f + (avVar.d * 100.0f)).toInt()
                )

                val i7 = (31.0f * engine.cg).toInt()
                val i8: Int = com.corrodinggames.rts.gameFramework.f.k.a
                if (render.a(
                        i2,
                        i,
                        i4,
                        i7,
                        sb,
                        true,
                        argb,
                        render.aC,
                        false,
                        null as i?
                    ) && render.ac == null && !(render as RenderAccessor).TFlag
                ) {
                    avVar.b += f
                    render.a()
                    paintI.reset()
                    paintI.setColor(Color.argb(120, 200, 0, 0))
                    if (avVar.b < 50.0f) {
                        f2 = avVar.b / 50.0f
                        paintI.setColor(Color.argb((150.0f + (40.0f * f2)).toInt(), 0, 200, 0))
                        drawGroupText(i2, i, i4, "Select Group", "(Hold for more..)", paintI, f2)
                    } else if (avVar.b < 100.0f) {
                        f2 = (avVar.b - 50.0f) / 50.0f
                        paintI.setColor(Color.argb((150.0f + (40.0f * f2)).toInt(), 200, 0, 0))
                        drawGroupText(i2, i, i4, "Add to Group", "(Hold for more..)", paintI, f2)
                    } else {
                        drawGroupText(i2, i, i4, "Replace Group", VariableScope.nullOrMissingString, paintI, 0.0f)
                        f2 = 1.0f
                    }
                    val i9 = (31.0f * engine.cg).toInt()
                    sRect.set(i2, ((i + i9) - (i9 * f2)).toInt(), i2 + i4, i9 + i)
                    engine.bL.b(sRect, paintI)
                    z = true
                } else {
                    z = false
                }
                if (!z) {
                    if (avVar.b != 0.0f && !(render as RenderAccessor).IFlag) {
                        if (avVar.b > 100.0f) {
                            avVar.a.clear()
                            avVar.b()
                            avVar.f = 1.0f
                        } else if (avVar.b > 50.0f) {
                            avVar.b()
                            render.e()
                            render.h()
                            avVar.a()
                            avVar.e = 1.0f
                        } else if (avVar.a.isNotEmpty()) {
                            render.e()
                            render.h()
                            avVar.a()
                            avVar.d = 1.0f
                        } else {
                            avVar.a.clear()
                            avVar.b()
                            avVar.e = 1.0f
                        }
                    }
                    if (!z) {
                        avVar.b = 0.0f
                    }
                }
                i2 += i3
            }
            i5 = i6 + 1
        }

    }

    private fun GUI.drawGroupText(i: Int, i2: Int, i3: Int, str: String?, str2: String?, paint: Paint?, f: Float) {
        val i4 = (i3 * 2.5).toInt()
        val render = GameEngine.t().bP
        val engine = GameEngine.t()
        val i5 = (40.0f * engine.cg).toInt()
        val i6: Float = (i + (i3 / 2)).toFloat()
        val i7 = ((i2 - i5) - (35.0f * engine.cg)).toInt()
        rect.set((i6 - (i4 / 2)).toInt(), i7, i4, i5)
        render.a(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            VariableScope.nullOrMissingString,
            Color.argb(`SlickToAndroidKeycodes$AndroidCodes`.KEYCODE_STB_INPUT, 100, 100, 100),
            render.aC,
            false,
            null as i?,
            0
        )
        sRect.set(rect.left, rect.top, rect.right, rect.bottom)
        sRect.right = (sRect.right * f).toInt()
        engine.bL.c(sRect, paint)
        engine.bL.a(str, i6, i7 + ((render.aC.textSize + 5.0f) * 1.0f), render.aC)
        engine.bL.a(str2, i6, i7 + ((render.aC.textSize + 5.0f) * 2.0f), render.aC)
    }


    object ShowAttackRange : com.corrodinggames.rts.game.units.a.p("c_show_attack_range") {
        override fun b(): String? {
            return readI18n("settings.showAttackRange")
        }

        override fun a(): String? {
            return "Show Attack Range"
        }

        override fun compareTo(other: Any?): Int {
            return 0
        }

        override fun c(unit: com.corrodinggames.rts.game.units.ce, z: Boolean): Boolean {
            //GameEngine.B().bS.g.n()
            val unit = GameEngine.t().bP.bZ.firstOrNull()
            if (unit != null) {
                (unit as GameUnit).comp.showAttackRange = !unit.comp.showAttackRange
            }
            return true
        }
    }

    object ShowAttackRangeBuilding : com.corrodinggames.rts.game.units.a.p("c_show_attack_range_building") {
        override fun b(): String? {
            return "显示建筑攻击范围"
        }

        override fun a(): String? {
            return "Show Attack Range Building"
        }

        override fun compareTo(other: Any?): Int {
            return 0
        }

        override fun c(unit: com.corrodinggames.rts.game.units.ce, z: Boolean): Boolean {
            //GameEngine.B().bS.g.n()

            settings.showBuildingAttackRange = !settings.showBuildingAttackRange
            return true
        }
    }

    object ShowAttackRangeUnits : com.corrodinggames.rts.game.units.a.p("c_show_attack_range_units") {

        override fun b(): String? {
            return "显示单位攻击范围\n${settings.showAttackRangeUnit}"
        }

        override fun a(): String? {
            return "Show Attack Range Units"
        }

        override fun compareTo(other: Any?): Int {
            return 0
        }

        override fun c(unit: com.corrodinggames.rts.game.units.ce, z: Boolean): Boolean {
            //GameEngine.B().bS.g.n()
            val index = Settings.unitAttackRangeTypes.indexOf(settings.showAttackRangeUnit)
            settings.showAttackRangeUnit = Settings.unitAttackRangeTypes.getOrNull(index + 1) ?: Settings.unitAttackRangeTypes.first()
            return true
        }
    }
}