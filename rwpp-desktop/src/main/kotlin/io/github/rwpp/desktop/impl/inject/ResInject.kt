/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject

import com.corrodinggames.rts.`R$drawable`
import com.corrodinggames.rts.`R$raw`
import io.github.rwpp.desktop.impl.Graphics
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.resOutputDir
import java.io.File

@InjectClass(Graphics::class)
object ResInject {
    @Inject("f", InjectMode.Override)
    fun redirectRes(i: Int): String? {
        val a2: String? = Graphics.a(`R$drawable`::class.java, i)
        val resFileExist = File(resOutputDir).exists()
        if (a2 != null) {
            return com.corrodinggames.rts.gameFramework.e.a.a("${if(resFileExist) resOutputDir else "res/"}drawable", a2)

        }
        val a3: String? = Graphics.a(`R$raw`::class.java, i)
        if (a3 != null) {
            return com.corrodinggames.rts.gameFramework.e.a.a("${if(resFileExist) resOutputDir else "res/"}raw", a3)

        }
        return null
    }
}