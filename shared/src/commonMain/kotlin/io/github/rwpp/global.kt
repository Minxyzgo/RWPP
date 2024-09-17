/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp

import io.github.rwpp.utils.io.SizeUtils
import io.github.rwpp.platform.Platform
import org.koin.core.Koin

const val projectVersion = "1.3.0-beta01 (core v1.15)"

/**
 * The protocol version.
 * The clients which have different protocol version can not join to each other
 */
const val protocolVersion = 1

/**
 * 1.15 -> 176
 */
const val gameVersion: Int = 176

/**
 * global koin module.
 */
lateinit var appKoin: Koin

val welcomeMessage =
    """
        这是一个使用[RWPP]所创建的房间
        [RWPP]是在github上开源的多平台RW启动器, 支持多种拓展功能
        开源地址请访问 https://github.com/Minxyzgo/RWPP 
        当前版本: $projectVersion
        Copyright 2023-2024 RWPP contributors
    """.trimIndent()

const val packageName = "io.github.rwpp"

val maxModSize = SizeUtils.mBToByte(16)

val resourcePath = if(Platform.isAndroid()) {
    "/storage/emulated/0/rustedWarfare/resource/"
} else "resource/"

val resourceOutputDir = if(Platform.isAndroid()) {
    "/storage/emulated/0/rustedWarfare/resource_generated/"
} else "resource_generated/"

val resOutputDir = if(Platform.isAndroid()) {
    "/storage/emulated/0/rustedWarfare/resource_generated/res/"
} else "resource_generated/res/"