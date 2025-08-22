/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.config

import io.github.rwpp.io.GameInputStream
import io.github.rwpp.io.SizeUtils
import io.github.rwpp.net.InternalPacketType
import io.github.rwpp.net.packets.ServerPacket
import kotlinx.serialization.Serializable
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Describes the configuration of various types of servers
 *
 * [useAsDefaultList] works when [type] is [ServerType.RoomList],
 *
 * When it is set to true, then this list will be opened first every time you enter a multiplayer game
 */
@Serializable
data class ServerConfig @JvmOverloads constructor(
    var ip: String,
    var name: String,
    var type: ServerType,
    var useAsDefaultList: Boolean = false,
    var editable: Boolean = true,
    var customRoomListProvider: String? = null,
    var customRoomHostProtocol: String? = null
) {

    /**
     * Get a server information.
     *
     * Notice: [type] must be [ServerType.Server]
     *
     * This is a custom protocol for RW-PP and will not work on any server that does not implement such a protocol.
     */
    fun getServerInfo(): ServerPacket.ServerInfoReceivePacket {
        val list = ip.split(":")
        val address = list[0]
        val port = list.getOrNull(1)?.toIntOrNull()
        val socket = Socket()
        socket.soTimeout = 5000
        socket.connect(InetSocketAddress(address, port ?: 5123), 5000)

        // Normal packet
        DataOutputStream(socket.getOutputStream())
            .apply {
                val bytes = ServerPacket.ServerInfoGetPacket().toBytes()
                writeInt(bytes.size)
                writeInt(InternalPacketType.PRE_GET_SERVER_INFO_FROM_LIST.type)
                write(bytes)
            }

        val buffer = socket.source().buffer()

        while(true) {
            val size = buffer.readInt()
            val type = buffer.readInt()

            //limit image size
            val bytes = buffer.readByteArray(size.toLong().coerceAtMost(SizeUtils.kBToByte(10)))

            if(type == InternalPacketType.RECEIVE_SERVER_INFO_FROM_LIST.type) {
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