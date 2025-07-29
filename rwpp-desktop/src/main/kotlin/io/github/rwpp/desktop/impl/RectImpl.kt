/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import android.graphics.RectF
import io.github.rwpp.game.base.Rect
import io.github.rwpp.inject.SetInterfaceOn
import io.github.rwpp.utils.Reflect

@SetInterfaceOn([RectF::class])
interface RectImpl : Rect {
    val self: RectF
    override val left: Float
        get() = Reflect.get(self, "a")!!
    override val top: Float
        get() = Reflect.get(self, "b")!!
    override val right: Float
        get() = Reflect.get(self, "c")!!
    override val bottom: Float
        get() = Reflect.get(self, "d")!!
}