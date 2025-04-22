/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl


import com.corrodinggames.rts.gameFramework.j.au
import io.github.rwpp.io.GameOutputStream
import io.github.rwpp.net.Packet
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream


fun initMap(force: Boolean = false) {
    val B = GameEngine.B()
    if (B.bX.ay.b == null || B.bX.az == null || force) B.bX.ay.a = GameMapType.a
    if (B.bX.az == null || force) B.bX.az = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
    if (B.bX.ay.b == null || force) B.bX.ay.b = "[z;p10]Crossing Large (10p).tmx"
}

fun Packet.asGamePacket(): au = au(type!!).also { pack ->
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

//
//fun GamePaint.asPaint(): Paint {
//    this.aC = ag()
//    if (this.bN) {
//        this.aC.a(
//            255,
//            SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE,
//            SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE,
//            SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE
//        )
//    } else {
//        this.aC.a(255, 30, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, 30)
//    }
//    this.aC.a(Paint.Align.CENTER)
//    this.aC.c(true)
//    this.aC.a(true)
//
//}