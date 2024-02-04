/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.utils.io

import java.io.Closeable
import java.io.DataInput
import java.io.DataInputStream

class GameInputStream(
    private val stream: DataInputStream
) : DataInput by stream, Closeable by stream {
    fun readNextBytes(): ByteArray {
        return readNBytes(readInt())
    }
    fun readNBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        readFully(bytes, 0, size)
        return bytes
    }
}