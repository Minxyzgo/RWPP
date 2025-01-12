/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.external

import kotlinx.serialization.Serializable

@Serializable
data class ExtensionConfig(
    val id: String,
    val version: String = "1.0.0",
    val author: String = "",
    val displayName: String = id,
    val icon: String = "",
    val description: String = "",
    val hasResource: Boolean = false,
    val dependencies: List<String> = emptyList()
)
