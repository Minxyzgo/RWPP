/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.net.packets

import io.github.rwpp.io.GameInputStream
import io.github.rwpp.io.GameOutputStream
import io.github.rwpp.net.Packet

@Suppress("MemberVisibilityCanBePrivate")
sealed class ModPacket : Packet() {

    class RequestPacket : ModPacket() {

        var mods: String = ""

        override val type: Int = MOD_DOWNLOAD_REQUEST

        override fun readPacket(input: GameInputStream) {
            mods = input.readUTF()
        }

        override fun writePacket(output: GameOutputStream) {
            output.writeUTF(mods)
        }
    }

    class ModPackPacket : ModPacket() {
        var index: Int = 0
        var name: String = ""
        var modBytes: ByteArray = byteArrayOf()

        override val type: Int = DOWNLOAD_MOD_PACK

        override fun readPacket(input: GameInputStream) {
            index = input.readInt()
            name = input.readUTF()
            modBytes = input.readNextBytes()
        }

        override fun writePacket(output: GameOutputStream) {
            output.writeInt(index)
            output.writeUTF(name)
            output.writeBytesWithSize(modBytes)
        }
    }

    class ModReloadFinishPacket : ModPacket() {
       override val type: Int = MOD_RELOAD_FINISH

        override fun readPacket(input: GameInputStream) {
            input.readInt()
        }

        override fun writePacket(output: GameOutputStream) {
            output.writeInt(1)
        }
    }

    companion object {
        const val MOD_DOWNLOAD_REQUEST = 500
        const val DOWNLOAD_MOD_PACK = 510
        const val MOD_RELOAD_FINISH = 502
    }
}