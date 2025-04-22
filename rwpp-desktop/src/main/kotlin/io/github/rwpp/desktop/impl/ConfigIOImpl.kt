/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import io.github.rwpp.config.Config
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.impl.AbstractConfigIO
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.Toml
import org.koin.core.annotation.Single
import java.io.File
import java.util.*
import kotlin.reflect.KClass

@Single(binds = [ConfigIO::class])
class ConfigIOImpl : AbstractConfigIO() {
    private val propertiesCache = mutableMapOf<String, Properties>()

    @OptIn(InternalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    override fun saveConfig(config: Config) {
        val clazz = config::class
        val name = clazz.qualifiedName
        val file = File(System.getProperty("user.dir") + "/$name.toml")
        if(!file.exists()) file.createNewFile()
        file.writeText(Toml.encodeToString(clazz.serializer() as KSerializer<Any>, config))
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Config> readConfig(clazz: KClass<T>): T? {
        val name = clazz.qualifiedName
        val file = File(System.getProperty("user.dir") + "/$name.toml")
        if(!file.exists()) file.createNewFile()
        val src = file.readText()
        if(src.isBlank()) return null
        return Toml.decodeFromString(clazz.serializer(), src)
    }

    override fun <T : Config> deleteConfig(clazz: KClass<T>) {
        val name = clazz.qualifiedName
        val file = File(System.getProperty("user.dir") + "/$name.toml")
        if(file.exists()) file.delete()
    }

    override fun saveSingleConfig(group: String, key: String, value: Any?) {
        val properties = propertiesCache[group] ?: Properties().apply {
            val file = File(System.getProperty("user.dir") + "/$group.properties")
            if (file.exists()) load(file.inputStream())
            propertiesCache[group] = this
        }

        properties.setProperty(key, value.toString())
        properties.store(File(System.getProperty("user.dir") + "/$group.properties").outputStream(), null)
    }

    override fun readSingleConfig(group: String, key: String): String? {
        val properties = propertiesCache[group] ?: Properties().apply {
            val file = File(System.getProperty("user.dir") + "/$group.properties")
            if (file.exists()) load(file.inputStream())
            propertiesCache[group] = this
        }

        return properties.getProperty(key)
    }
}