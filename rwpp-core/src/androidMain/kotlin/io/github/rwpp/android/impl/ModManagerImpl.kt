/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import com.corrodinggames.rts.game.units.custom.ag
import com.corrodinggames.rts.gameFramework.e.a
import com.corrodinggames.rts.gameFramework.i.b
import com.corrodinggames.rts.gameFramework.k
import com.corrodinggames.rts.gameFramework.k.d
import io.github.rwpp.android.MainActivity
import io.github.rwpp.game.Game
import io.github.rwpp.game.mod.Mod
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.io.calculateSize
import io.github.rwpp.io.zipFolderToByte
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.koin.core.component.get
import java.io.File

@Single
class ModManagerImpl : ModManager {
    private val game: Game = get()
    private var mods: List<Mod>? = null

    override suspend fun modReload() = withContext(Dispatchers.IO) {
        val t = GameEngine.t()
        modSaveChange()
        val aVar = t.bW
        t.bo = true
        t.f()
        aVar.a(false, false)
        t.bo = false
        t.q()
    }
//
//    override fun modUpdate() {
//        //LClass.B().bZ.k()
//        GameEngine.t().bW.j()
//        mods = null
//        mods = getAllMods()
//    }

    override suspend fun modSaveChange() {
        val t = GameEngine.t()
        t.bW.d()
        t.bN.save()
        val a2: Int = t.bW.a()
        if(t.bU.C) {

        } else if(!ag.b(true)) {

        } else if(a2 == 0) {
            t.bW.b()
        }

        MainActivity.activityResume()
    }

    override fun getModByName(name: String): Mod? {
        mods = mods ?: getAllMods()
        return mods!!.firstOrNull { it.name == name }
    }

    @Suppress("unchecked_cast")
    override fun getAllMods(): List<Mod> {
        if(mods != null) return mods!!
        val mods = GameEngine.t().bW.e as ArrayList<com.corrodinggames.rts.gameFramework.i.b>

        return buildList {
            mods.forEach {
                add(object : Mod {
                    override val id: Int
                        get() = it.a
                    override val name: String
                        get() = it.q ?: ""
                    override val description: String
                        get() = it.s ?: ""
                    override val minVersion: String
                        get() = it.t ?: ""
                    override val errorMessage: String?
                        get() = it.P
                    override var isEnabled: Boolean
                        get() = !it.f
                        set(value) { it.f = !value }
                    override val path: String
                        get() = it.d()


                    override fun tryDelete(): Boolean {
                        val e = it.e()
                        return if (!it.l()) {
                            false
                        } else {
                            val file = File(e)
                            if (!a.i(file.absolutePath)) {
                                false
                            } else {
                                a.b(file)
                            }
                        }
                    }

                    override fun getRamUsed(): String {
                        return it.k()
                    }

                    override fun getSize(): Long {
                        return kotlin.runCatching {
                            File(modPath()).calculateSize()
                        }.getOrNull() ?: 0L
                    }

                    override fun getBytes(): ByteArray {
                        val file = File(modPath())
                        return if(file.isDirectory)
                            file.zipFolderToByte()
                        else file.readBytes()
                    }

                    private fun modPath(): String =
                        ("/storage/emulated/0/" + com.corrodinggames.rts.gameFramework.e.a.q(it.g()).removePrefix("/SD/"))
                })
            }
        }
    }
}