/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Context
import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwpp.config.Config
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.core.Initialization
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.Toml
import org.koin.core.annotation.Single
import org.koin.core.component.get
import java.lang.reflect.Field
import kotlin.reflect.KClass

@Single(binds = [ConfigIO::class])
class ConfigIOImpl : ConfigIO {
    private val fieldCache = mutableMapOf<String, Field>()

    @Suppress("unchecked_cast")
    @OptIn(InternalSerializationApi::class)
    override fun saveConfig(config: Config) {
        val clazz = config::class
        val name = clazz.qualifiedName
        val preferences = get<Context>().getSharedPreferences(name, Context.MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putString("src", Toml.encodeToString(clazz.serializer() as KSerializer<Any>, config))
        editor.commit()
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Config> readConfig(clazz: KClass<T>): T? {
        val name = clazz.qualifiedName

        val preferences = get<Context>().getSharedPreferences(name, Context.MODE_PRIVATE)
        val src = preferences.getString("src", "")
        if(src.isNullOrBlank()) return null

        return Toml.decodeFromString(clazz.serializer(), src)
    }

    override fun <T : Config> deleteConfig(clazz: KClass<T>) {
        val name = clazz.qualifiedName
        val preferences = get<Context>().getSharedPreferences(name, Context.MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putString("src", "")
        editor.commit()
    }

    override fun saveSingleConfig(group: String, key: String, value: Any?) {
        val preferences = get<Context>().getSharedPreferences(group, Context.MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putString(key, value.toString())
        editor.commit()
    }

    override fun readSingleConfig(group: String, key: String): String? {
        val preferences = get<Context>().getSharedPreferences(group, Context.MODE_PRIVATE)
        return preferences.getString(key, "")
    }

    @Suppress("unchecked_cast")
    override fun <T> getGameConfig(name: String): T {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        return field.get(GameEngine.t().bN) as T
    }

    override fun setGameConfig(name: String, value: Any?) {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        field.set(GameEngine.t().bN, value)
    }


    override fun saveAllConfig() {
        super.saveAllConfig()
        GameEngine.t().bN.save()
    }
}