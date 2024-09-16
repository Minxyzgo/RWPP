/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import io.github.rwpp.core.Initialization
import io.github.rwpp.net.Client
import io.github.rwpp.net.Net
import io.github.rwpp.net.Packet
import io.github.rwpp.net.PacketType
import okhttp3.OkHttpClient
import org.koin.core.annotation.Single
import java.awt.Desktop
import java.io.DataInputStream
import java.net.URI

@Single(binds = [Net::class, Initialization::class])
class NetImpl : Net {
    override val packetDecoders: MutableMap<PacketType, (DataInputStream) -> Packet> = mutableMapOf()
    override val listeners: MutableMap<PacketType, (Client, Packet) -> Unit> = mutableMapOf()
    override val client: OkHttpClient = OkHttpClient()

    override fun sendPacketToServer(packet: Packet) {
        LClass.B().bX.f(packet.asGamePacket())
    }

    override fun sendPacketToClients(packet: Packet) {
        LClass.B().bX.g(packet.asGamePacket())
    }

    override fun openUriInBrowser(uri: String) {
        Desktop.getDesktop().browse(URI(uri))
    }
}