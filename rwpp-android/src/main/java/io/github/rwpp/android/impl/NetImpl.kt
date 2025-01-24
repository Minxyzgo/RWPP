/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.rwpp.core.Initialization
import io.github.rwpp.net.Client
import io.github.rwpp.net.Net
import io.github.rwpp.net.Packet
import io.github.rwpp.net.PacketType
import okhttp3.OkHttpClient
import org.koin.core.annotation.Single
import org.koin.core.component.get
import java.io.DataInputStream

@Single(binds = [Net::class, Initialization::class])
class NetImpl : Net {
    override val packetDecoders: MutableMap<PacketType, (DataInputStream) -> Packet> = mutableMapOf()
    override val listeners: MutableMap<PacketType, (Client, Packet) -> Unit> = mutableMapOf()
    override val client: OkHttpClient = OkHttpClient()

    override fun sendPacketToServer(packet: Packet) {
        GameEngine.t().bU.b(packet.asGamePacket())
    }

    override fun sendPacketToClients(packet: Packet) {
        GameEngine.t().bU.c(packet.asGamePacket())
    }

    override fun openUriInBrowser(uri: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        get<Context>().startActivity(browserIntent)
    }
}