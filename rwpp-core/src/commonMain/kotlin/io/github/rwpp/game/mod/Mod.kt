/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.mod

interface Mod {
    val id: Int
    val name: String
    val description: String
    val minVersion: String
    val errorMessage: String?
    var isEnabled: Boolean
    //var isNetworkMod: Boolean


    fun getRamUsed(): String

    fun getSize(): Long

    fun getBytes(): ByteArray
}