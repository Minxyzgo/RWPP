/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Context
import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwpp.android.MainActivity
import io.github.rwpp.config.Config
import io.github.rwpp.game.team.ConfigHandler
import io.github.rwpp.utils.setPropertyFromObject
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.Toml
import java.lang.reflect.Field
import kotlin.reflect.KClass

class ConfigHandlerImpl : ConfigHandler {
    private val fieldCache = mutableMapOf<String, Field>()
    private val allConfig: List<Config> by lazy { getKoin().getAll<Config>() }

    @Suppress("unchecked_cast")
    override fun <T> getGameConfig(name: String): T {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        return field.get(GameEngine.t().bN) as T
    }

    override fun setGameConfig(name: String, value: Any?) {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        field.set(GameEngine.t().bN, value)
    }

    override fun readAllConfig() {
        allConfig.forEach { config ->
            getRWPPConfig(config::class)?.let { config.setPropertyFromObject(it) }
        }
    }

    override fun saveAllConfig() {
        allConfig.forEach { config ->
            setRWPPConfig(config)
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun getRWPPConfig(clazz: KClass<*>) : Any? {
        val name = clazz.qualifiedName
        val preferences = MainActivity.instance.getSharedPreferences(name, Context.MODE_PRIVATE)
        val src = preferences.getString("src", "")
        if(src.isNullOrBlank()) return null

        return Toml.decodeFromString(clazz.serializer(), src)
    }

    @OptIn(InternalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    private fun setRWPPConfig(value: Any) {
        val clazz = value::class
        val name = clazz.qualifiedName
        val preferences = MainActivity.instance.getSharedPreferences(name, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("src", Toml.encodeToString(clazz.serializer() as KSerializer<Any>, value))
        editor.commit()
    }
}