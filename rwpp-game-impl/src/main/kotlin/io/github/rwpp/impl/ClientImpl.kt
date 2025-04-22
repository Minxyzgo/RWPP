/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl

import io.github.rwpp.inject.SetInterfaceOn
import io.github.rwpp.net.Client
import io.github.rwpp.net.Packet

@SetInterfaceOn([com.corrodinggames.rts.gameFramework.j.c::class])
interface ClientImpl : Client {
    val self: com.corrodinggames.rts.gameFramework.j.c

    override fun sendPacketToClient(packet: Packet) {
        GameEngine.B().bX.a(self, packet.asGamePacket())
    }
}