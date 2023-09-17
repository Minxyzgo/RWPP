package io.github.rwpp.game.mod

interface ModManager {
    suspend fun modReload()

    fun modUpdate()

    suspend fun modSaveChange()

    fun getModByName(name: String): Mod

    fun getAllMods(): List<Mod>
}