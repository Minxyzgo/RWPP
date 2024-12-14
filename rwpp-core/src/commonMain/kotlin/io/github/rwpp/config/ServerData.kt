/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.config

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.rwpp.net.packets.ServerPacket

@Stable
data class ServerData(private val _config: ServerConfig) {
    val config by mutableStateOf(_config)
    var isLoading by mutableStateOf(false)
    var infoPacket by mutableStateOf<ServerPacket.ServerInfoReceivePacket?>(null)
}