/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.net

import io.github.rwpp.io.GameInputStream
import io.github.rwpp.io.GameOutputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

interface Packet {
    val type: Int?

    fun readPacket(input: GameInputStream)

    fun writePacket(output: GameOutputStream)

    fun toBytes(): ByteArray {
        val byteArrayOutput = ByteArrayOutputStream()
        val gameOutput = GameOutputStream(
            DataOutputStream(byteArrayOutput)
        )
        gameOutput.use { writePacket(it) }

        val bytes = byteArrayOutput.toByteArray()
        byteArrayOutput.close()

        return bytes
    }
}