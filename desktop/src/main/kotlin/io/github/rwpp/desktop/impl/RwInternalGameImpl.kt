/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import com.corrodinggames.rts.java.u
import org.newdawn.slick.GameContainer

class RwInternalGameImpl(str: String) : u(str) {

    override fun update(p0: GameContainer?, p1: Int) {
        try {
            super.update(p0, p1)
        } catch (_: Exception){}
    }


    override fun b() {
    }


//    override fun keyPressed(p0: Int, p1: Char) {
//        l.B()?.b(SlickToAndroidKeycodes.b(p0), true)
//    }
//
//    override fun keyReleased(p0: Int, p1: Char) {
//        // 暂时不实现
//    }
}