package io.github.rwpp.desktop.impl

import com.corrodinggames.rts.game.units.custom.ag
import io.github.rwpp.game.mod.Mod
import io.github.rwpp.game.mod.ModManager

class ModManagerImpl : ModManager {
    private var mods: List<Mod>? = null

    override suspend fun modReload() = GameImpl.container.waitPost {
        val B = LClass.B()
        B.bZ.e()
        B.bQ.save()
        B.bZ.l()
    }

    override fun modUpdate() {
        LClass.B().bZ.k()
        mods = null
        mods = getAllMods()
    }

    override suspend fun modSaveChange() {
        val b = LClass.B()
        b.bZ.e()
        b.bQ.save()
        val a2: Int = b.bZ.a(false)
        if(b.bX.B)
            return
        ag.c(true)
    }

    override fun getModByName(name: String): Mod {
        mods = mods ?: getAllMods()
        return mods!!.first { it.name == name }
    }

    @Suppress("unchecked_cast")
    override fun getAllMods(): List<Mod> {
        if(mods != null) return mods!!
        val mods = IAClass::class.java.getDeclaredField("e").run {
            isAccessible = true
            get(LClass.B().bZ)
        } as ArrayList<com.corrodinggames.rts.gameFramework.i.b>

        return buildList {
            mods.forEach {
                add(object : Mod {
                    override val id: Int
                        get() = it.a
                    override val name: String
                        get() = it.s ?: ""
                    override val description: String
                        get() = it.u ?: ""
                    override val minVersion: String
                        get() = it.v ?: ""
                    override var isEnabled: Boolean
                        get() = !it.f
                        set(value) { it.f = !value }
                })
            }
        }
    }
}