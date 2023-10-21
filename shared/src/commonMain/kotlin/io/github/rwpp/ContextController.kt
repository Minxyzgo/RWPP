/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp

import io.github.rwpp.config.Blacklists
import io.github.rwpp.config.MultiplayerPreferences
import io.github.rwpp.config.instance
import io.github.rwpp.game.Game
import io.github.rwpp.game.config.ConfigHandler
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.net.Net

interface ContextController : ConfigHandler, Game, Net, ModManager {
    fun i18n(str: String, vararg args: Any?): String

    fun readAllConfig() {
        Blacklists.readFromContext(this)
        getRWPPConfig(MultiplayerPreferences::class)?.apply {
            val instance = MultiplayerPreferences.instance
            instance.creatorNameFilter = creatorNameFilter
            instance.joinServerAddress = joinServerAddress
            instance.mapNameFilter = mapNameFilter
            instance.playerLimitRangeTo = playerLimitRangeTo
            instance.playerLimitRangeFrom = playerLimitRangeFrom
            instance.showWelcomeMessage = showWelcomeMessage
        }
    }

    fun saveAllConfig() {
        Blacklists.writeFromContext(this)
        setRWPPConfig(MultiplayerPreferences.instance)
    }

    fun exit()
}