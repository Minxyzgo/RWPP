/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Intent
import android.net.Uri
import io.github.rwpp.AppContext
import io.github.rwpp.android.MainActivity
import io.github.rwpp.net.*
import okhttp3.OkHttpClient
import java.io.DataInputStream

class NetImpl : Net {
    override val packetDecoders: MutableMap<PacketType, (DataInputStream) -> Packet> = mutableMapOf()
    override val listeners: MutableMap<PacketType, (AppContext, Client, Packet) -> Unit> = mutableMapOf()
    override val client: OkHttpClient = OkHttpClient()

    init {
        registerListeners()
    }

    override fun sendPacketToServer(packet: Packet) {
        GameEngine.t().bU.b(packet.asGamePacket())
    }

    override fun sendPacketToClients(packet: Packet) {
        GameEngine.t().bU.c(packet.asGamePacket())
    }

    override fun openUriInBrowser(uri: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        MainActivity.instance.startActivity(browserIntent)
    }
}