/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.map

import androidx.compose.ui.graphics.painter.Painter
import java.io.File

class NetworkMap(private val _mapName: String) : GameMap {
    override val id: Int
        get() = -1
    override val image: Painter?
        get() = null
    override val mapName: String
        get() = _mapName
    override val mapType: MapType
        get() = MapType.CustomMap
}