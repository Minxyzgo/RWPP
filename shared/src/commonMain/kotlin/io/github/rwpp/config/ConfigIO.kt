/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.config

import io.github.rwpp.utils.setPropertyFromObject
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

interface ConfigIO : KoinComponent {
    fun saveConfig(config: Config)

    fun <T : Config> readConfig(clazz: KClass<T>): T?

    fun <T> getGameConfig(name: String): T

    fun setGameConfig(name: String, value: Any?)

    fun saveAllConfig() {
        // save config
        val configs = getKoin().getAll<Config>()
        configs.forEach {
            saveConfig(it)
        }
    }

    fun readAllConfig() {
        val configs = getKoin().getAll<Config>()
        configs.forEach { config ->
            println("read: ${config::class.qualifiedName}")
            runCatching { readConfig(config::class)?.let {
                config.setPropertyFromObject(it)
            } }
                .onFailure { it.printStackTrace() }
        }
    }
}