/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import io.github.rwpp.net.Packet

fun Packet.asGamePacket(): com.corrodinggames.rts.gameFramework.j.bi {
    return com.corrodinggames.rts.gameFramework.j.bi(type).apply {
        c = bytes
    }
}

fun sendKickToClient(c: com.corrodinggames.rts.gameFramework.j.c, reason: String) {
    val t = GameEngine.t()
    t.bU::class.java.getDeclaredMethod("b", com.corrodinggames.rts.gameFramework.j.c::class.java, String::class.java).apply {
        isAccessible = true
    }.invoke(t.bU, c, reason)
}

fun sendPacketToClient(c: com.corrodinggames.rts.gameFramework.j.c, bi: com.corrodinggames.rts.gameFramework.j.bi) {
    val t = GameEngine.t()
    t.bU::class.java.getDeclaredMethod("a", com.corrodinggames.rts.gameFramework.j.c::class.java, com.corrodinggames.rts.gameFramework.j.bi::class.java).apply {
        isAccessible = true
    }.invoke(t.bU, c, bi)
}