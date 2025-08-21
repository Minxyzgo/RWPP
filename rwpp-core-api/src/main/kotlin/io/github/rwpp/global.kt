/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp

import io.github.rwpp.command.CommandHandler
import io.github.rwpp.command.CommandHandler.Command
import io.github.rwpp.game.Game
import io.github.rwpp.game.Player
import io.github.rwpp.io.SizeUtils
import io.github.rwpp.rwpp_core_api.BuildConfig
import org.koin.core.Koin
import org.slf4j.Logger
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * The version of the project.
 */
const val projectVersion = "v" + BuildConfig.VERSION

/**
 * The version of the game core.
 */
const val coreVersion = "v1.15"

/**
 * The protocol version.
 * The clients which have different protocol version can not join to each other
 */
const val protocolVersion = 3

/**
 * 1.15 -> 176
 */
const val gameVersion: Int = 176

/**
 * Koin init flag.
 */
var koinInit = false

/**
 * global koin module.
 */
lateinit var appKoin: Koin

val welcomeMessage =
    """
        这是一个使用[RWPP]所创建的房间
        [RWPP]是在github上开源的多平台RW启动器, 支持多种拓展功能
        QQ交流群: 927597495
        开源地址请访问 https://github.com/Minxyzgo/RWPP 
        当前版本: $projectVersion (core $coreVersion)
        Copyright 2023-2025 RWPP contributors
    """.trimIndent()

const val packageName = "io.github.rwpp"

/**
 * 48 is the maximum size of Rusted Warfare packet in MB.
 */
val maxModSize = SizeUtils.mBToByte(48)

/**
 * global logger.
 */
lateinit var logger: Logger

/**
 * global command handler.
 */
val commands = CommandHandler("/").apply {
    register<Player>("help", "[page]", "Lists all commands.") { args, player ->
        val room = appKoin.get<Game>().gameRoom
        if (args.isNotEmpty() && args[0].toIntOrNull() == null) {
            room.sendMessageToPlayer(player, "RWPP", "'page' must be a number.")
            return@register
        }
        val commandsPerPage = 6
        var page = if (args.isNotEmpty()) args[0].toInt() else 1
        val pages = ceil(commandList.size.toDouble() / commandsPerPage).roundToInt()

        page--

        if (page >= pages || page < 0) {
            room.sendMessageToPlayer(player, "RWPP", "'page' must be a number between 1 and $pages.")
            return@register
        }

        val result = StringBuilder()
        result.append("--- Commands Page ${(page + 1)}/${pages} ---\n")

        for (i in commandsPerPage * page..<(commandsPerPage * (page + 1)).coerceAtMost(commandList.size)) {
            val command: Command = commandList[i]
            result.append("- /").append(command.text).append(" ").append(command.paramText)
                .append(" - ").append(command.description).append("\n")

        }
        room.sendMessageToPlayer(player, "RWPP", result.toString())
    }
}


val extensionPath by lazy {
    if (appKoin.get<AppContext>().isAndroid()) {
        "/storage/emulated/0/rustedWarfare/extension/"
    } else System.getProperty("user.dir") + "/extension/"
}

val resourceOutputDir by lazy {
    if (appKoin.get<AppContext>().isAndroid()) {
        "/storage/emulated/0/rustedWarfare/resource_generated/"
    } else System.getProperty("user.dir") + "/resource_generated/"
}

val resOutputDir by lazy {
    if (appKoin.get<AppContext>().isAndroid()) {
        "/storage/emulated/0/rustedWarfare/resource_generated/res/"
    } else System.getProperty("user.dir") + "/resource_generated/res/"
}

val mapDir by lazy {
    if (appKoin.get<AppContext>().isAndroid()) {
        "/storage/emulated/0/rustedWarfare/maps"
    } else System.getProperty("user.dir") + "/mods/maps"
}

val modDir by lazy {
    if (appKoin.get<AppContext>().isAndroid()) {
        "/storage/emulated/0/rustedWarfare/units"
    } else System.getProperty("user.dir") + "/mods/units"
}

val generatedLibDir by lazy {
    if (appKoin.get<AppContext>().isAndroid()) {
        "/storage/emulated/0/rustedWarfare/generated_lib"
    } else System.getProperty("user.dir") + "/generated_lib"
}