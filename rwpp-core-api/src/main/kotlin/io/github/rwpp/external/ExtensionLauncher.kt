/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.external

import io.github.rwpp.appKoin
import io.github.rwpp.config.Config
import io.github.rwpp.config.ConfigIO
import java.io.InputStream
import kotlin.reflect.KClass

/**
 * Abstract class for launching external jar extensions.
 */
abstract class ExtensionLauncher {
    /**
     * The extension this launcher is for.
     */
    val extension by lazy {
        appKoin.get<ExternalHandler>().getAllExtensions().getOrThrow().first { it.launcher == this }
    }

    /**
     * Saves the specified config to the config file.
     *
     * The config must be serializable using the [Config] interface.
     */
    fun saveConfig(config: Config) {
        appKoin.get<ConfigIO>().saveConfig(config)
    }

    /**
     * Gets the specified config from the config file.
     */
    fun <C : Config> getConfig(clazz: KClass<C>): Config? {
        return appKoin.get<ConfigIO>().readConfig(clazz)
    }

    /**
     * Gets the specified single config from the config file.
     */
    fun getSingleConfig(key: String): String? {
        return appKoin.get<ConfigIO>().readSingleConfig(extension.config.id, key)
    }

    /**
     * Saves the specified single config to the config file.
     */
    fun saveSingleConfig(key: String, value: Any?) {
        appKoin.get<ConfigIO>().saveSingleConfig(extension.config.id, key, value)
    }

    /**
     * Opens an input stream for the specified entry name.
     *
     * You should use this method instead of calling [ClassLoader.getResourceAsStream] on the extension's classloader,
     * otherwise you may get null if the extension run on android platform.
     */
    fun openInputStream(entryName: String): InputStream? {
        return extension.openInputStream(entryName)
    }

    /**
     * Called after all extensions have been loaded.
     */
    abstract fun init()
}