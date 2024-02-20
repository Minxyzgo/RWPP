/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

class GameStream(
    private val stream: com.corrodinggames.rts.gameFramework.j.k
) {
    fun readByte() = stream.d()
    fun readBool() = stream.e()
    fun readInt() = stream.f()
    fun readFloat() = stream.g()
    fun readDouble() = stream.h()
    fun readLong() = stream.i()
    fun readUTF() = stream.l()
}