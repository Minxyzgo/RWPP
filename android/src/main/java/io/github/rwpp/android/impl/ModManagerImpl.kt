package io.github.rwpp.android.impl

import com.corrodinggames.rts.game.units.custom.ag
import io.github.rwpp.game.mod.Mod
import io.github.rwpp.game.mod.ModManager

class ModManagerImpl : ModManager {
    private var mods: List<Mod>? = null

    override suspend fun modReload() {
//        GameImpl.container.waitPost {
//            val B = LClass.B()
//            B.bZ.e()
//            B.bQ.save()
//            B.bZ.l()
//        }
        TODO("Not yet implemented")
    }

    override fun modUpdate() {
        //LClass.B().bZ.k()
        KClass.t().bW.j()
        mods = null
        mods = getAllMods()
    }

    override suspend fun modSaveChange() {
        val t = KClass.t()
        t.bW.d()
        t.bN.save()
        val a2: Int = t.bW.a()
        if(t.bU.C)
            return
        ag.b(true)
    }

    override fun getModByName(name: String): Mod {
        mods = mods ?: getAllMods()
        return mods!!.first { it.name == name }
    }

    @Suppress("unchecked_cast")
    override fun getAllMods(): List<Mod> {
        if(mods != null) return mods!!
        val mods = KClass.t().bW.e as ArrayList<com.corrodinggames.rts.gameFramework.i.b>

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
                    override var isEnabled: Boolean
                        get() = !it.A
                        set(value) { it.A = !value }
                })
            }
        }
    }
}