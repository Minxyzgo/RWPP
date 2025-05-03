/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.mod

import java.io.File

interface Mod {
    val id: Int
    val name: String
    val description: String
    val minVersion: String
    val errorMessage: String?
    var isEnabled: Boolean
    val path: String
    //var isNetworkMod: Boolean

    fun tryDelete(): Boolean {
        return runCatching {
            File(path).delete()
        }.getOrNull() == true
    }

    fun getRamUsed(): String

    fun getSize(): Long

    fun getBytes(): ByteArray
}