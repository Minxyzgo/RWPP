/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import android.graphics.Paint
import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.inject.SetInterfaceOn

@SetInterfaceOn([Paint::class])
interface PaintImpl : GamePaint {
    val self: Paint
    override var argb: Int
        get() = self.e()
        set(value) { self.b(value) }

    override val style: GamePaint.Style
        get() = GamePaint.Style.entries[(self.d() as Enum<*>).ordinal]
}