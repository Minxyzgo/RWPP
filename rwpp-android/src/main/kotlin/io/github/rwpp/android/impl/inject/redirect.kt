/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

//@file:RedirectTo("com.corrodinggames.rts.R", "io.github.rwpp.R")
@file:RedirectTo("com.corrodinggames.rts.R\$anim", "io.github.rwpp.R\$anim")
@file:RedirectTo("com.corrodinggames.rts.R\$array", "io.github.rwpp.R\$array")
@file:RedirectTo("com.corrodinggames.rts.R\$attr", "io.github.rwpp.R\$attr")
@file:RedirectTo("com.corrodinggames.rts.R\$color", "io.github.rwpp.R\$color")
@file:RedirectTo("com.corrodinggames.rts.R\$drawable", "io.github.rwpp.R\$drawable")
@file:RedirectTo("com.corrodinggames.rts.R\$id", "io.github.rwpp.R\$id")
@file:RedirectTo("com.corrodinggames.rts.R\$layout", "io.github.rwpp.R\$layout")
@file:RedirectTo("com.corrodinggames.rts.R\$raw", "io.github.rwpp.R\$raw")
@file:RedirectTo("com.corrodinggames.rts.R\$string", "io.github.rwpp.R\$string")
@file:RedirectTo("com.corrodinggames.rts.R\$style", "io.github.rwpp.R\$style")
@file:RedirectTo("com.corrodinggames.rts.R\$styleable", "io.github.rwpp.R\$styleable")
@file:RedirectTo("com.corrodinggames.rts.R\$xml", "io.github.rwpp.R\$xml")

package io.github.rwpp.android.impl.inject

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.SoundPool
import com.corrodinggames.rts.gameFramework.m.e
import com.corrodinggames.rts.gameFramework.utility.o
import io.github.rwpp.R
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.android.impl.getResourceFileName
import io.github.rwpp.appKoin
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.inject.*
import io.github.rwpp.resOutputDir
import io.github.rwpp.resourceOutputDir
import io.github.rwpp.utils.Reflect
import java.io.File
import java.io.FileInputStream


@InjectClass(com.corrodinggames.rts.gameFramework.e.c::class)
object AssetInject {
    @Inject("h", InjectMode.InsertBefore)
    fun redirectAsset(str: String): Any {
        val externalHandler = appKoin.get<ExternalHandler>()
        if (externalHandler.getUsingResource() == null) return Unit
        if(str.contains("builtin_mods")
            || (str.contains("maps") && !str.contains("bitmaps"))
            || str.contains("translations")) return Unit
        val o = str.let {
            if(it.startsWith("assets/") || it.startsWith("assets\\"))
                it.removePrefix("assets/") else it
        }

        val s3 = resourceOutputDir + o
        return kotlin.runCatching {
            InterruptResult(
                o(
                    FileInputStream(s3), s3, o
                )
            )
        }.getOrElse { InterruptResult(null) }
    }
}


@InjectClass(com.corrodinggames.rts.gameFramework.bc::class)
object MusicInject {
    @Inject("a", InjectMode.InsertBefore)
    fun com.corrodinggames.rts.gameFramework.bc.redirectMusic(z: Boolean): Any {
        val externalHandler = appKoin.get<ExternalHandler>()
        if (externalHandler.getUsingResource() == null) return Unit

        val b = Reflect.get<com.corrodinggames.rts.gameFramework.bb>(this, "b")!!


        if(!b.b.startsWith("music")) return Unit
        val a = Reflect.get<MediaPlayer>(this, "a")!!
        try {
            a.reset()
            val input = FileInputStream(resourceOutputDir + b.b)
            a.setDataSource(input.fd, 0L, input.available().toLong())
            input.close()
            if (z) {
                a.isLooping = true
            }
            a.setVolume(0.0f, 0.0f)
            a.setOnInfoListener { _, _, _ -> true }
            a.setOnPreparedListener { a.start() }
            a.prepareAsync()
            return InterruptResult.Unit
        } catch (e: Exception) {
            throw Error(e)
        }
    }
}

@InjectClass(com.corrodinggames.rts.gameFramework.m.fh::class)
object ImageInject {
    @Inject("a", InjectMode.InsertBefore)
    fun redirectBitmap(i: Int, bool: Boolean): Any {
        val externalHandler = appKoin.get<ExternalHandler>()
        if (externalHandler.getUsingResource() == null) return Unit

        val resFileExist = File(resOutputDir).exists()
        if(!resFileExist) return Unit

        try {
            val res = GameEngine.t().al.resources

            val options = BitmapFactory.Options()
            if (bool) {
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
            } else {
                options.inPreferredConfig = Bitmap.Config.RGB_565
            }
            options.inScaled = false

            val path = resOutputDir + res.getResourceFileName(i)

            val bitmap = BitmapFactory.decodeStream(
                FileInputStream(path),
                null, options
            )

            val eVar = e()
            eVar.a(bitmap)

            return InterruptResult(eVar)
        } catch (e: Exception) {
            throw Error(e)
        }
    }
}

@InjectClass( com.corrodinggames.rts.gameFramework.a.a::class)
object ResInject {
    @Inject("a", InjectMode.InsertBefore)
    fun com.corrodinggames.rts.gameFramework.a.a.redirectRes(i: Int): Any {
        val externalHandler = appKoin.get<ExternalHandler>()
        if (externalHandler.getUsingResource() == null) return Unit

        val resFileExist = File(resOutputDir).exists()
        if (!resFileExist) return Unit


        try {
            val bVar = com.corrodinggames.rts.gameFramework.a.b(
                this,
                R.raw::class.java.declaredFields.first { it.get(null) == i }.name,
                this
            )

            val res = GameEngine.t().al.resources

            val path = resOutputDir + res.getResourceFileName(i)
            Reflect.set(bVar, "a", this)
            val pool = Reflect.get<SoundPool>(this, "g")!!
            Reflect.set(bVar, "b", pool.load(path, 1))
            return InterruptResult(bVar)
        } catch(e: Exception) {
            throw Error(e)
        }
    }
}