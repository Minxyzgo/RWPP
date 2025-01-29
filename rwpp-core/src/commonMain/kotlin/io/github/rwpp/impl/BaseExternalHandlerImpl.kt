/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl

import io.github.rwpp.appKoin
import io.github.rwpp.config.EnabledExtensions
import io.github.rwpp.extensionPath
import io.github.rwpp.external.Extension
import io.github.rwpp.external.ExtensionConfig
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.logger
import io.github.rwpp.resourceOutputDir
import io.github.rwpp.scripts.Scripts
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromNativeReader
import java.io.File
import java.io.FileNotFoundException

abstract class BaseExternalHandlerImpl : ExternalHandler {
    protected var _usingResource: Extension? = null
    protected var extensions: List<Extension>? = null

    private val fileExists by lazy {
        File(resourceOutputDir).exists()
    }

    override fun getAllExtensions(update: Boolean): Result<List<Extension>> {
        return Result.success(
            if (extensions != null && !update)
                extensions!!
            else File(extensionPath).let { file ->
                if (file.exists()) {
                    val enabledExtension =
                        appKoin.get<EnabledExtensions>().values.also { logger.info("Enabled extensions: ${it.joinToString()}") }
                    mutableListOf<Extension>()
                        .also { extensions = it }
                        .apply {
                            file
                                .walk()
                                .filter { it.name.endsWith(".rwext") || it.name.endsWith(".rwres") || it.isDirectory }
                                .forEachIndexed { _, fi ->
                                    var config: ExtensionConfig? = null

                                    logger.info("File: ${fi.absolutePath}")
                                    if (fi.absolutePath == extensionPath) return@forEachIndexed

                                    if (fi.isDirectory) {
                                        val infoTomlFile = File(fi, "info.toml")
                                        if (!infoTomlFile.exists()) return@forEachIndexed
                                        config = Toml.decodeFromNativeReader(infoTomlFile.reader())
                                    } else {
                                        val zipFile = java.util.zip.ZipFile(fi)
                                        val entry = zipFile.getEntry("info.toml")

                                        if (entry != null) {
                                            val input = zipFile.getInputStream(entry)
                                            config = Toml.decodeFromNativeReader(input.reader())
                                        }
                                    }


                                    logger.info("Config: $config")

                                    config
                                        ?: return Result.failure(FileNotFoundException("No info.toml found in extension: ${fi.absolutePath}"))

                                    if (extensions!!.any { it.config.id == config.id }) return Result.failure(
                                        IllegalStateException("Duplicate extension id found: ${config.id}")
                                    )

                                    logger.info("add extension")
                                    add(
                                        newExtension(
                                            enabledExtension.contains(config.id),
                                            !fi.isDirectory,
                                            fi,
                                            config
                                        )
                                    )
                                }
                        }
                } else emptyList()
            })
    }

    override fun getUsingResource(): Extension? {
        return _usingResource ?: run {
            val infoTomlFile = File(resourceOutputDir + "info.toml")

            if (!fileExists || !infoTomlFile.exists()) return@run null

            val info = Toml.decodeFromNativeReader<ExtensionConfig>(
                infoTomlFile.reader()
            )

            getAllExtensions().getOrNull()?.first { it.config.displayName == info.displayName }
        }.also { _usingResource = it }
    }

    override fun init() {
        logger.info("Init extensions...")
        getAllExtensions().getOrThrow().forEach { extension ->
            if (extension.isEnabled) {
                try {
                    logger.info("Init for ${extension.config.id}")

                    Scripts.loadScript(
                        extension.config.id,
                        extension.zipFile?.let { zip ->
                            val entry = zip.getEntry("scripts/main.lua")
                            zip.getInputStream(entry).reader().readText()
                        } ?: File(extension.file, "scripts/main.lua").readText()
                    )
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }
}