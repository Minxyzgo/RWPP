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
    override val tmx: File
        get() = null!!
    override val mapType: MapType
        get() = MapType.CustomMap
}