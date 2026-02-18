/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.world

import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.game.base.Rect
import io.github.rwpp.game.units.GameUnit

interface World {
    // 暂且未知
    val cx: Float

    val cy: Float

    val cameraX: Float

    val cameraY: Float

    val flame: Int

    val displayWidth: Int

    val displayHeight: Int

    val zoom: Float

    fun getAllUnits(): List<GameUnit>

    fun drawText(text: String, x: Float, y: Float, paint: GamePaint)

    fun drawRect(rect: Rect, paint: GamePaint)

    fun drawCircle(x: Float, y: Float, radius: Float, paint: GamePaint)

    fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float, paint: GamePaint)
}