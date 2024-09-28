/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwpp.config.Config
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.core.Initialization
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.Toml
import org.koin.core.annotation.Single
import java.io.File
import java.lang.reflect.Field
import kotlin.reflect.KClass

@Single(binds = [ConfigIO::class, Initialization::class])
class ConfigIOImpl : ConfigIO {
    private val fieldCache = mutableMapOf<String, Field>()

    @OptIn(InternalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    override fun saveConfig(config: Config) {
        val clazz = config::class
        val name = clazz.qualifiedName
        val file = File(System.getProperty("user.dir") + "$name.toml")
        if(!file.exists()) file.createNewFile()
        file.writeText(Toml.encodeToString(clazz.serializer() as KSerializer<Any>, config))
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Config> readConfig(clazz: KClass<T>): T? {
        val name = clazz.qualifiedName
        val file = File(System.getProperty("user.dir") + "$name.toml")
        if(!file.exists()) file.createNewFile()
        val src = file.readText()
        if(src.isBlank()) return null
        return Toml.decodeFromString(clazz.serializer(), src)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getGameConfig(name: String): T {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        return field.get(LClass.B().bQ) as T
    }

    override fun setGameConfig(name: String, value: Any?) {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        field.set(LClass.B().bQ, value)
    }

    override fun saveAllConfig() {
        super.saveAllConfig()
        LClass.B().bQ.save()
    }
}