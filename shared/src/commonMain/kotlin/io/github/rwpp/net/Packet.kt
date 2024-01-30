/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.net

import java.io.ByteArrayOutputStream
import java.io.DataOutput
import java.io.DataOutputStream

data class Packet internal constructor(
    val type: Int,
    val bytes: ByteArray
) {

    companion object {
        fun createPacket(
            type: Int,
            content: (DataOutput) -> Unit
        ): Packet {
            val byteArrayOutput = ByteArrayOutputStream()
            val dataOutput = DataOutputStream(byteArrayOutput)
            dataOutput.use(content)
            val bytes = byteArrayOutput.toByteArray()
            byteArrayOutput.close()
            return Packet(type, bytes)
        }
    }
}