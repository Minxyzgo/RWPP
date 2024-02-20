/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl


import com.corrodinggames.rts.gameFramework.j.au
import io.github.rwpp.desktop.gameContext
import io.github.rwpp.net.Packet
import io.github.rwpp.utils.io.GameOutputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

fun Packet.asGamePacket(): au = au(type.type).also { pack ->
    val byteArrayOutput = ByteArrayOutputStream()
    val gameOutput = GameOutputStream(
        DataOutputStream(byteArrayOutput)
    )
    gameOutput.use { writePacket(it) }
    val bytes = byteArrayOutput.toByteArray()
    byteArrayOutput.close()
    pack.c = bytes
    pack.d = -1
}
