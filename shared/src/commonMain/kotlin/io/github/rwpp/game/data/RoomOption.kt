/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.data

import io.github.rwpp.protocolVersion as version
import kotlinx.serialization.Serializable

@Serializable
data class RoomOption(
    var canTransferMod: Boolean = false,
    val allModsSize: Int = 0,
    val protocolVersion: Int = version
)