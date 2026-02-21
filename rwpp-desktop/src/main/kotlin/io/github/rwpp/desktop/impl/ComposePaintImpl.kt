/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import android.graphics.Paint
import android.graphics.`Paint$Style`
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.toArgb
import io.github.rwpp.inject.NewField
import io.github.rwpp.inject.SetInterfaceOn

@SetInterfaceOn([Paint::class])
interface ComposePaintImpl : androidx.compose.ui.graphics.Paint {
    val self: Paint

    @NewField
    var _color: Color?

    override var color: Color
        get() {
            if (_color == null) {
                _color = Color(self.e())
            }

            return _color!!
        }
        set(value) {
            self.b(value.toArgb())
        }

    override var style: PaintingStyle
        get() {
            return when (val value = (self.d() as Enum<*>).ordinal) {
                0 -> PaintingStyle.Fill
                1 -> PaintingStyle.Stroke
                else -> throw IllegalStateException("value: $value is not supported.")
            }
        }

        set(value) {
            when (value) {
                PaintingStyle.Fill -> self.a(`Paint$Style`.a)
                PaintingStyle.Stroke -> self.a(`Paint$Style`.b)
                else -> throw IllegalStateException("style: $value is not supported.")
            }
        }
}