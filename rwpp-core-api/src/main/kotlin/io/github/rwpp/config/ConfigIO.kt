/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.config

import io.github.rwpp.logger
import io.github.rwpp.utils.setPropertyFromObject
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

/**
 * An interface that describes all operations related to configuring reads and writes.
 *
 * To get it, use `koinInject<ConfigIO>()` or `appKoin.get<ConfigIO>()`
 */
interface ConfigIO : KoinComponent {
    /**
     * Save a rw-pp configuration.
     */
    fun saveConfig(config: Config)

    /**
     * Read a rw-pp configuration from the file.
     */
    fun <T : Config> readConfig(clazz: KClass<T>): T?

    /**
     * Delete the file of a rw-pp configuration
     */
    fun <T : Config> deleteConfig(clazz: KClass<T>)

    /**
     * Save a single configuration.
     */
    fun saveSingleConfig(group: String, key: String, value: Any?)

    /**
     * Read a single configuration.
     */
    fun readSingleConfig(group: String, key: String): String?

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
            runCatching {
                logger.info("Reading config ${config::class.simpleName}...")
                readConfig(config::class)?.let { config.setPropertyFromObject(it) }
            }.onFailure {
                logger.error("Failed to read config ${config::class.simpleName}. Delete it.", it)
                deleteConfig(config::class)
            }
        }
    }
}