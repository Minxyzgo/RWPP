package io.github.rwpp.game.map

import androidx.compose.ui.graphics.painter.Painter
import java.io.File

interface GameMap {
    val id: Int

    val image: Painter?

    val mapName: String

    val tmx: File

    val mapType: MapType
}

