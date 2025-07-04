/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import io.github.rwpp.net.Client
import io.github.rwpp.net.Packet

class ClientImpl(
    internal val client: com.corrodinggames.rts.gameFramework.j.c?
) : Client {
    override fun sendPacketToClient(packet: Packet) {
        client?.a(packet.asGamePacket())
    }
}