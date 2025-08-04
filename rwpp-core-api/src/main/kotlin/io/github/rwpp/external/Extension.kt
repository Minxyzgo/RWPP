/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.external

import io.github.rwpp.AppContext
import io.github.rwpp.appKoin
import io.github.rwpp.inject.RootInfo
import io.github.rwpp.logger
import io.github.rwpp.ui.Imageable
import io.github.rwpp.ui.Widget
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

abstract class Extension(
    /**
     * Whether the extension is enabled or not.
     */
    var isEnabled: Boolean = false,

    /**
     * The file or folder that contains the extension.
     */
    val file: File,

    /**
     * Null if the extension is a folder.
     */
    val zipFile: ZipFile?,

    /**
     * The configuration of the extension.
     */
    val config: ExtensionConfig,
) : Imageable {
    /**
     * The UI widgets that will be added to the settings panel.
     */
    @JvmField
    val settingPanel = mutableListOf<Widget>()

    /**
     * The UI widgets that will be added to the player options panel.
     */
    @JvmField
    val extraPlayerOptions = mutableListOf<Widget>()

    /**
     * The UI widgets that will be added to the room options panel.
     */
    @JvmField
    val extraRoomOptions = mutableListOf<Widget>()

    /**
     * The jvm launcher of the extension.
     */
    var launcher: ExtensionLauncher? = null

    /**
     * The class loader of the extension.
     */
    var classLoader: ClassLoader? = null

    /**
     * The inject info of the extension.
     *
     * It will load the inject info from the file "inject_(platform).toml" or "inject_lua.toml" in the extension file.
     *
     * Null if the extension does not have an inject info.
     * @see [ExtensionConfig.hasInjectInfo]
     */
    abstract val injectInfo: RootInfo?

    val isSupportedForCurrentPlatform: Boolean by lazy {
        val app = appKoin.get<AppContext>()
        config.supportedPlatforms.isEmpty() ||
                (config.supportedPlatforms.any { it.equals("android", ignoreCase = true) } && app.isAndroid()) ||
                (config.supportedPlatforms.any { it.equals("desktop", ignoreCase = true) } && app.isDesktop())
    }

    override fun openImageInputStream(): InputStream? {
        return  if (config.icon.isNotBlank()) {
            runCatching { openInputStream(config.icon) }.getOrNull()
        } else {
            null
        }
    }

    /**
     * Opens an input stream for the given entry name.
     */
    @Suppress("unused")
    fun openInputStream(entryName: String): InputStream? {
        return runCatching {
            if (zipFile != null) {
                logger.debug("Opening input stream for $entryName from zip file")
                zipFile.getInputStream(zipFile.getEntry(entryName))
            } else {
                File(file, entryName).inputStream()
            }
        }.getOrNull()
    }
}