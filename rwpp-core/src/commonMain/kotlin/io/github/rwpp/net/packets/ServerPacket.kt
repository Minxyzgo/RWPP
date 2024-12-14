/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.net.packets

import io.github.rwpp.net.Packet
import io.github.rwpp.net.PacketType
import io.github.rwpp.net.ServerStatus
import io.github.rwpp.io.GameInputStream
import io.github.rwpp.io.GameOutputStream

sealed class ServerPacket(type: PacketType) : Packet(type) {

    class ServerInfoGetPacket : ServerPacket(PacketType.PRE_GET_SERVER_INFO_FROM_LIST) {
        override fun readPacket(input: GameInputStream) {
            input.readInt()
        }

        override fun writePacket(output: GameOutputStream) {
            output.writeInt(1) // buf must > 6
        }
    }

    class ServerInfoReceivePacket(
        var name: String = "",
        var currentPlayer: Int = 0,
        var maxPlayerSize: Int = 0,
        var mapName: String = "",
        var description: String = "",
        var version: String = "",
        var mods: String = "",
        var status: ServerStatus = ServerStatus.BattleRoom,
        var iconBytes: ByteArray = byteArrayOf(),
    ): ServerPacket(PacketType.RECEIVE_SERVER_INFO_FROM_LIST) {
        var ping: Int = 0
        override fun readPacket(input: GameInputStream) {
            name = input.readUTF()
            currentPlayer = input.readInt()
            maxPlayerSize = input.readInt()
            mapName = input.readUTF()
            description = input.readUTF()
            version = input.readUTF()
            mods = input.readUTF()
            iconBytes = input.readNextBytes()
            status = ServerStatus.entries[input.readInt()]
        }

        override fun writePacket(output: GameOutputStream) {
            output.writeUTF(name)
            output.writeInt(currentPlayer)
            output.writeInt(maxPlayerSize)
            output.writeUTF(mapName)
            output.writeUTF(description)
            output.writeUTF(version)
            output.writeUTF(mods)
            output.writeBytesWithSize(iconBytes)
            output.writeInt(status.ordinal)
        }
    }
}