/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.proxy

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.corrodinggames.rts.gameFramework.m.e
import com.corrodinggames.rts.gameFramework.utility.o
import com.github.minxyzgo.rwij.InjectMode
import com.github.minxyzgo.rwij.InterruptResult
import com.github.minxyzgo.rwij.setFunction
import io.github.rwpp.R
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.android.impl.getResourceFileName
import io.github.rwpp.appKoin
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.resOutputDir
import io.github.rwpp.resourceOutputDir
import io.github.rwpp.utils.Reflect
import java.io.File
import java.io.FileInputStream

object UnitPathProxy {
    init {
        com.corrodinggames.rts.gameFramework.e.c::class.setFunction {
//        addProxy("f", String::class, mode = InjectMode.InsertBefore) { _: Any?, str: String ->
//            if(controller.getUsingResource() == null
//                || str.contains("builtin_mods")
//                || (str.contains("maps") && !str.contains("bitmaps"))
//                || str.contains("translations")) return@addProxy Unit
//            val result = Reflect.call<com.corrodinggames.rts.gameFramework.e.c, String>(
//                com.corrodinggames.rts.gameFramework.e.a.b,
//                "__proxy__f",
//                listOf(String::class),
//                listOf(str)
//            )
//
//            println("p: $result")
//
//            result ?: return@addProxy InterruptResult(null)
//
//            if(!result.startsWith("music")) return@addProxy InterruptResult(result)
//
//            InterruptResult("assets/$result")
//        }

            addProxy("h", String::class, mode = InjectMode.InsertBefore) { _: Any?, str: String ->
                val externalHandler = appKoin.get<ExternalHandler>()
                if (externalHandler.getUsingResource() == null) return@addProxy Unit
                if(str.contains("builtin_mods")
                    || (str.contains("maps") && !str.contains("bitmaps"))
                    || str.contains("translations")) return@addProxy Unit
                val o = str.let {
                    if(it.startsWith("assets/") || it.startsWith("assets\\"))
                        it.removePrefix("assets/") else it
                }


                val s3 = resourceOutputDir + o
                kotlin.runCatching {
                    InterruptResult(
                        o(
                        FileInputStream(s3), s3, o
                    )
                    )
                }.getOrElse { InterruptResult(null) }
            }
        }

        com.corrodinggames.rts.gameFramework.bc::class.setFunction {
            addProxy("a", Boolean::class, mode = InjectMode.InsertBefore) { self: com.corrodinggames.rts.gameFramework.bc, z: Boolean ->
                val externalHandler = appKoin.get<ExternalHandler>()
                if (externalHandler.getUsingResource() == null) return@addProxy Unit

                val b = Reflect.get<com.corrodinggames.rts.gameFramework.bb>(self, "b")!!


                if(!b.b.startsWith("music")) return@addProxy Unit
                val a = Reflect.get<MediaPlayer>(self, "a")!!
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
                    InterruptResult(Unit)
                } catch (e: Exception) {
                    throw Error(e)
                }
            }
        }

        com.corrodinggames.rts.gameFramework.m.fh::class.setFunction {
            addProxy("a", Int::class, Boolean::class, mode = InjectMode.InsertBefore) { _: Any?, i: Int, bool: Boolean ->
                val externalHandler = appKoin.get<ExternalHandler>()
                if (externalHandler.getUsingResource() == null) return@addProxy Unit

                val resFileExist = File(resOutputDir).exists()
                if(!resFileExist) return@addProxy Unit

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
                    Log.i("RWPP", "drawable/path: $path")

                    val bitmap = BitmapFactory.decodeStream(
                        FileInputStream(path),
                        null, options
                    )

                    val eVar = e()
                    eVar.a(bitmap)

                    InterruptResult(eVar)
                } catch (e: Exception) {
                    throw Error(e)
                }
            }
        }

        com.corrodinggames.rts.gameFramework.a.a::class.setFunction {
            addProxy("a", Int::class, mode = InjectMode.InsertBefore) { self: com.corrodinggames.rts.gameFramework.a.a, i: Int, ->
                val externalHandler = appKoin.get<ExternalHandler>()
                if (externalHandler.getUsingResource() == null) return@addProxy Unit

                val resFileExist = File(resOutputDir).exists()
                if (!resFileExist) return@addProxy Unit


                try {
                    val bVar = com.corrodinggames.rts.gameFramework.a.b(
                        self,
                        R.raw::class.java.declaredFields.first { it.get(null) == i }.name,
                        self
                    )

                    val res = GameEngine.t().al.resources

                    val path = resOutputDir + res.getResourceFileName(i)
                    Log.i("RWPP", "raw/path: $path")
                    Reflect.set(bVar, "a", self)
                    val pool = Reflect.get<SoundPool>(self, "g")!!
                    Reflect.set(bVar, "b", pool.load(path, 1))
                    InterruptResult(bVar)
                } catch(e: Exception) {
                    throw Error(e)
                }
            }
        }
    }
}