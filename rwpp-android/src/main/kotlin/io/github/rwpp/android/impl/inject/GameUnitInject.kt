/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

@InjectClass(com.corrodinggames.rts.game.units.ce::class)
object GameUnitInject {
    @Inject("n", InjectMode.InsertBefore)
    fun com.corrodinggames.rts.game.units.ce.onDraw(delta: Float) {
        (this as GameUnit).comp.forEach { it.onDraw(this, delta) }
    }

//    @RedirectMethod(
//        "a",
//        "(FZ)V",
//        "com.corrodinggames.rts.gameFramework.m.fi",
//        "a"
//    )
//    fun onDrawBar(rectF: RectF, param: Paint) {
//        (this as GameUnit).comp.onDrawBar(this, rectF as Rect, param as GamePaint)
//    }
}
