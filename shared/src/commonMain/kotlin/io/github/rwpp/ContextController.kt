package io.github.rwpp

import io.github.rwpp.game.Game
import io.github.rwpp.game.config.ConfigHandler
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.net.Net

interface ContextController : ConfigHandler, Game, Net, ModManager {
    fun i18n(str: String, vararg args: Any?): String

    fun exit()
}