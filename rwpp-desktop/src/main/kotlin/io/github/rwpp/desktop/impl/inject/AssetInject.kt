/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl.inject

import io.github.rwpp.appKoin
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult
import io.github.rwpp.resourceOutputDir
import io.github.rwpp.utils.Reflect
import java.io.File
import java.io.FileInputStream

@InjectClass(com.corrodinggames.rts.gameFramework.e.c::class)
object AssetInject {
    private val usingResource by lazy {
        File(resourceOutputDir).exists()
    }
    @Inject("f", injectMode = InjectMode.InsertBefore)
    fun redirectAsset(str: String): Any {
        if(!usingResource
            || str.contains("builtin_mods")
            || (str.contains("maps") && !str.contains("bitmaps"))
            || str.contains("translations")) return Unit
        val result = Reflect.call<com.corrodinggames.rts.gameFramework.e.c, String>(
            com.corrodinggames.rts.gameFramework.e.a.b,
            "__original__f",
            listOf(String::class),
            listOf(str)
        )

        if(result?.contains("assets") != true) return InterruptResult(result)

        return InterruptResult(resourceOutputDir + result.removePrefix("assets/"))
    }

    @Inject("i", injectMode = InjectMode.InsertBefore)
    fun redirectImageAsset(str: String): Any {
        if(!usingResource
            || str.contains("builtin_mods")
            || (str.contains("maps") && !str.contains("bitmaps"))
            || str.contains("translations")) return Unit
        val o = str.let {
            if(it.startsWith("assets/") || it.startsWith("assets\\"))
                it.removePrefix("assets/") else it
        }


        val s3 = resourceOutputDir + o
        return kotlin.runCatching {
            InterruptResult(com.corrodinggames.rts.gameFramework.utility.j(
                FileInputStream(s3), s3, o
            ))
        }.getOrElse { InterruptResult(null) }
    }
}