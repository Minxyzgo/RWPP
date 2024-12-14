/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.net.packets

import io.github.rwpp.net.Packet
import io.github.rwpp.net.PacketType
import io.github.rwpp.io.GameInputStream
import io.github.rwpp.io.GameOutputStream

/**
 * Don't register them, they are implemented in platform.
 */
//@Suppress("MemberVisibilityCanBePrivate")
//sealed class ModPacket(type: PacketType) : Packet(type) {
//    class RequestPacket(
//        var mods: String
//    ) : ModPacket(PacketType.MOD_DOWNLOAD_REQUEST) {
//
//        override fun readPacket(input: GameInputStream) {
//            mods = input.readUTF()
//        }
//
//        override fun writePacket(output: GameOutputStream) {
//            output.writeUTF(mods)
//        }
//    }
//
//    class ModPackPacket(
//        var size: Int = 0,
//        var index: Int = 0,
//        var name: String = "",
//        var modBytes: ByteArray = byteArrayOf()
//    ) : ModPacket(PacketType.DOWNLOAD_MOD_PACK) {
//        override fun readPacket(input: GameInputStream) {
//            size = input.readInt()
//            index = input.readInt()
//            name = input.readUTF()
//            modBytes = input.readNextBytes()
//        }
//
//        override fun writePacket(output: GameOutputStream) {
//            output.writeInt(size)
//            output.writeInt(index)
//            output.writeUTF(name)
//            output.writeBytesWithSize(modBytes)
//        }
//
//    }
//
//    class ModReloadFinishPacket : ModPacket(PacketType.MOD_RELOAD_FINISH) {
//        override fun readPacket(input: GameInputStream) {
//            input.readInt()
//        }
//
//        override fun writePacket(output: GameOutputStream) {
//            output.writeInt(1)
//        }
//    }
//}