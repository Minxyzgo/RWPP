/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp

import io.github.rwpp.utils.io.SizeUtils

const val projectVersion = "1.0.6-alpha"
val welcomeMessage =
    """
        这是一个使用[RWPP]所开始的房间
        [RWPP]是在github上开源的多平台RW启动器, 支持多种拓展功能
        开源地址请访问 https://github.com/Minxyzgo/RWPP 
        bug反馈与交流加入群: 150450999
        当前版本: $projectVersion (不稳定)
        Copyright 2023 RWPP contributors
    """.trimIndent()

const val packageName = "io.github.rwpp"

val maxModSize = SizeUtils.mBToByte(3)