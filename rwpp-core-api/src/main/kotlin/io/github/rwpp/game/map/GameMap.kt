/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.map

import io.github.rwpp.ui.Imageable
import java.io.InputStream

interface GameMap : Imageable {
    val id: Int

    val mapName: String

    val mapType: MapType

    fun openInputStream(): InputStream

    fun displayName() = mapName.replace(mapPrefixRegex, "")

    fun getMapSuffix() = if(mapType == MapType.SavedGame) ".rwsave" else ".tmx"

    companion object {
        private val mapPrefixRegex = Regex("""^\[.*?\]""")
    }
}

