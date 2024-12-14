/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject

import com.corrodinggames.librocket.b
import io.github.rwpp.desktop.impl.FClass
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult
import io.github.rwpp.resOutputDir
import java.io.File

@InjectClass(com.corrodinggames.librocket.b::class)
object DrawableInject {

    @Inject("a", injectMode = InjectMode.InsertBefore)
    fun redirectDrawableRes(str: String): Any {
        val o = FClass.o(str)
        val resFileExist = File(resOutputDir).exists()
        return if(o.startsWith("drawable:") && resFileExist) {
            InterruptResult(b.b + resOutputDir + "drawable/" + o.removePrefix("drawable:"))
        } else Unit
    }
}