/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.net.packets

import io.github.rwpp.io.GameInputStream
import io.github.rwpp.io.GameOutputStream
import io.github.rwpp.net.InternalPacketType
import io.github.rwpp.net.Packet

@Suppress("MemberVisibilityCanBePrivate")
object GamePacket {
    fun getPacket(packetType: Int, action: (GameOutputStream) -> Unit): Packet {
        return object : Packet() {
            override val type: Int = packetType

            override fun readPacket(input: GameInputStream) {}

            override fun writePacket(output: GameOutputStream) {
                action(output)
            }
        }
    }

    fun getChatPacket(title: String?, message: String, color: Int): Packet {
        return getPacket(InternalPacketType.CHAT.type) { output ->
            output.writeUTF(message)
            output.writeByte(3)
            output.writeOptionalUTF(title)
            output.writeInt(0)
            output.writeInt(color)
        }
    }
}