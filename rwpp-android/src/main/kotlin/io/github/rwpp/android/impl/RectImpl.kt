/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import io.github.rwpp.game.base.Rect

class RectImpl(
    val rect: android.graphics.Rect
) : Rect {
    override val bottom: Float
        get() = rect.bottom.toFloat()

    override val left: Float
        get() = rect.left.toFloat()

    override val right: Float
        get() = rect.right.toFloat()

    override val top: Float
        get() = rect.top.toFloat()
}