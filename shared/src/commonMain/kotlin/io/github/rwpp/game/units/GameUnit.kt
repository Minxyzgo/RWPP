/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.units

import androidx.compose.ui.graphics.painter.Painter
import io.github.rwpp.game.mod.Mod

interface GameUnit {
    val name: String
    val displayName: String
    val description: String
    val movementType: MovementType
    val mod: Mod?
}