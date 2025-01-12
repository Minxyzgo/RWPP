/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl

import io.github.rwpp.appKoin
import io.github.rwpp.config.EnabledExtensions
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.external.Extension
import io.github.rwpp.external.ExtensionConfig
import io.github.rwpp.resourceOutputDir
import io.github.rwpp.extensionPath
import io.github.rwpp.logger
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromNativeReader
import io.github.rwpp.scripts.Scripts
import java.io.File
import java.io.FileNotFoundException

abstract class BaseExternalHandlerImpl : ExternalHandler {
    protected var _usingExtension: Extension? = null
    protected var extensions: List<Extension>? = null

    override fun getAllExtensions(): Result<List<Extension>> {
        return Result.success(extensions ?: File(extensionPath).let { file ->
            if (file.exists()) {
                mutableListOf<Extension>()
                    .also { extensions = it }
                    .apply {
                        file
                            .walk()
                            .filter { it.name.endsWith(".rwext") || it.name.endsWith(".rwres") }
                            .forEachIndexed { i, zip ->
                                val zipFile = java.util.zip.ZipFile(zip)
                                val entry = zipFile.getEntry("info.toml")
                                var config: ExtensionConfig? = null


                                if (entry != null) {
                                    val input = zipFile.getInputStream(entry)
                                    config = Toml.decodeFromNativeReader(input.reader())
                                }

                                config
                                    ?: return Result.failure(FileNotFoundException("No info.toml found in extension: ${zip.absolutePath}"))

                                if (extensions!!.any { it.config.id == config.id }) return Result.failure(
                                    IllegalStateException("Duplicate extension id found: ${config.id}")
                                )

                                add(
                                    newExtension(
                                        appKoin.get<EnabledExtensions>().values.contains(config.id),
                                        zip,
                                        config
                                    )
                                )
                            }
                    }
            } else emptyList()
        })
    }

    override fun init() {
        logger.info("Init extensions...")
        getAllExtensions().getOrNull()?.forEach { extension ->
            if (extension.isEnabled) {
                try {
                    logger.info("Init for ${extension.config.id}")
                    val entry = extension.zipFile.getEntry("scripts/main.lua")
                    Scripts.loadScript(extension.config.id, extension.zipFile.getInputStream(entry).reader().readText())
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }
}