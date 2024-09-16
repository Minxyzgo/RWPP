/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.config

import io.github.rwpp.core.Initialization
import io.github.rwpp.utils.setPropertyFromObject
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

/**
 * An interface that describes all operations related to configuring reads and writes.
 *
 * To get it, use `koinInject<ConfigIO>()` or `appKoin.get<ConfigIO>()`
 */
interface ConfigIO : KoinComponent, Initialization {
    /**
     * Save a rw-pp configuration.
     */
    fun saveConfig(config: Config)

    /**
     * Read a rw-pp configuration from the file.
     */
    fun <T : Config> readConfig(clazz: KClass<T>): T?

    /**
     * Get a game configuration.
     */
    fun <T> getGameConfig(name: String): T

    /**
     * Set a game configuration.
     */
    fun setGameConfig(name: String, value: Any?)

    /**
     * Save all the configurations, which should also include the game's.
     *
     * It should be called when exciting the game.
     */
    fun saveAllConfig() {
        // save config
        val configs = getKoin().getAll<Config>()
        configs.forEach {
            saveConfig(it)
        }
    }

    /**
     * Read all rw-pp configurations from files.
     *
     * It should be called before the game starts.
     */
    fun readAllConfig() {
        val configs = getKoin().getAll<Config>()
        configs.forEach { config ->
            runCatching { readConfig(config::class)?.let { config.setPropertyFromObject(it) } }
                .onFailure { it.printStackTrace() }
        }
    }

    override fun init() {
        readAllConfig()
    }
}