/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.scripts

import io.github.rwpp.inject.InjectMode
import kotlinx.serialization.Serializable

@Serializable
data class LuaRootInfo(
    val injectInfos: Set<LuaInjectInfo> = emptySet(),
    val redirectInfos: Set<LuaRedirectMethodInfo> = emptySet(),
)

@Serializable
data class LuaInjectInfo(
    val className: String,
    val methodName: String,
    val methodDesc: String,
    val alias: String = "$className.$methodName$methodDesc",
    val injectMode: InjectMode = InjectMode.Override,
    val platform: String,
)

@Serializable
data class LuaRedirectMethodInfo(
    val className: String,
    val method: String,
    val methodDesc: String,
    val targetClassName: String,
    val targetMethod: String,
    val targetMethodDesc: String,
    val alias: String = "$className.$method$methodDesc:$targetClassName.$targetMethod$targetMethodDesc",
    val platform: String,
)