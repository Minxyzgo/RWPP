/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.io

import java.io.Closeable
import java.io.DataOutput
import java.io.DataOutputStream

class GameOutputStream(
    private val stream: DataOutputStream
) : DataOutput by stream, Closeable by stream {
    fun writeBytesWithSize(bytes: ByteArray) {
        writeInt(bytes.size)
        write(bytes)
    }

    fun writeOptionalUTF(string: String?) {
        writeBoolean(string != null)
        if (string != null) writeUTF(string)
    }
}