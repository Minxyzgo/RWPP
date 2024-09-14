/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.config

import io.github.rwpp.net.PacketType
import io.github.rwpp.net.packets.ServerPacket
import io.github.rwpp.utils.io.GameInputStream
import io.github.rwpp.utils.io.SizeUtils
import kotlinx.serialization.Serializable
import okio.buffer
import okio.source
import org.koin.core.annotation.Single
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket

@Serializable
data class ServerConfig(
    var ip: String,
    var name: String,
    var type: ServerType,
    var useAsDefaultList: Boolean = false,
) {
    fun getServerInfo(): ServerPacket.ServerInfoReceivePacket {
        val list = ip.split(":")
        val address = list[0]
        val port = list.getOrNull(1)?.toIntOrNull()
        val socket = Socket()
        socket.soTimeout = 5000
        socket.connect(InetSocketAddress(address, port ?: 5123), 5000)

        DataOutputStream(socket.getOutputStream())
            .apply {
                val bytes = ServerPacket.ServerInfoGetPacket().toBytes()
                writeInt(bytes.size)
                writeInt(PacketType.PRE_GET_SERVER_INFO_FROM_LIST.type)
                write(bytes)
            }

        val buffer = socket.source().buffer()

        while(true) {
            val size = buffer.readInt()
            val type = buffer.readInt()
            val bytes = buffer.readByteArray(size.toLong().coerceAtMost(SizeUtils.kBToByte(10)))

            if(type == PacketType.RECEIVE_SERVER_INFO_FROM_LIST.type) {
                socket.close()
                return ServerPacket.ServerInfoReceivePacket().apply {
                    readPacket(
                        GameInputStream(
                            DataInputStream(ByteArrayInputStream(bytes))
                        )
                    )
                }
            }
        }
    }
}