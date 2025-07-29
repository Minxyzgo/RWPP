/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl

import io.github.rwpp.AppContext
import io.github.rwpp.appKoin
import io.github.rwpp.config.EnabledExtensions
import io.github.rwpp.extensionPath
import io.github.rwpp.external.Extension
import io.github.rwpp.external.ExtensionConfig
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.generatedLibDir
import io.github.rwpp.inject.InjectInfo
import io.github.rwpp.inject.PathType
import io.github.rwpp.inject.RedirectMethodInfo
import io.github.rwpp.inject.RootInfo
import io.github.rwpp.logger
import io.github.rwpp.projectVersion
import io.github.rwpp.resourceOutputDir
import io.github.rwpp.scripts.LuaRootInfo
import io.github.rwpp.scripts.Scripts
import io.github.rwpp.ui.UI
import io.github.rwpp.utils.compareVersions
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromNativeReader
import org.koin.core.component.get
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipFile

abstract class BaseExternalHandlerImpl : ExternalHandler {
    protected var _usingResource: Extension? = null
    protected var extensions: List<Extension>? = null

    private val fileExists by lazy {
        File(resourceOutputDir).exists()
    }

    override fun newExtension(
        isEnabled: Boolean,
        isZip: Boolean,
        extensionFile: File,
        config: ExtensionConfig
    ): Extension {
        return object : Extension(
            isEnabled, extensionFile, if (isZip) ZipFile(extensionFile) else null, config
        ) {
            override val injectInfo: RootInfo? by lazy {
                var isLuaInfo = false
                val inputStream = openInputStream("inject_lua.toml")
                    .also { if (it != null) isLuaInfo = true }
                    ?: if (appKoin.get<AppContext>().isDesktop())
                        openInputStream("inject_desktop.toml")
                    else
                        openInputStream("inject_android.toml")
                    ?: openInputStream("inject.toml")
                return@lazy if (inputStream != null) {
                    if (isLuaInfo) {
                        val luaRootInfo = Toml.decodeFromNativeReader<LuaRootInfo>(inputStream.reader())

                        RootInfo(
                            injectInfos = luaRootInfo.injectInfos.mapNotNull {
                                if (isSupportCurrentGamePlatform(it.platform)) {
                                    InjectInfo(
                                        it.className,
                                        true,
                                        it.methodName,
                                        it.methodDesc,
                                        "io.github.rwpp.scripts.Scripts.callGlobalFunction(\"__global__${config.id}__${it.alias}\", this, \$args);",
                                        PathType.JavaCode,
                                        false,
                                        it.injectMode,
                                    )
                                } else {
                                    null
                                }
                            }.toSet(),
                            redirectMethodInfos = luaRootInfo.redirectInfos.mapNotNull {
                                if (isSupportCurrentGamePlatform(it.platform)) {
                                    RedirectMethodInfo(
                                        true,
                                        it.className,
                                        it.method,
                                        it.methodDesc,
                                        it.targetClassName,
                                        it.targetMethod,
                                        it.targetMethodDesc,
                                        "io.github.rwpp.scripts.Scripts.callGlobalFunction(\"__global__${config.id}__${it.alias}\", this, \$args);",
                                        PathType.JavaCode,
                                    )
                                } else {
                                     null
                                }
                            }.toSet(),
                        )
                    } else {
                        Toml.decodeFromNativeReader<RootInfo>(inputStream.reader())
                    }
                } else {
                    null
                }
            }
        }
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
                                        val zipFile = ZipFile(fi)
                                        val entry = zipFile.getEntry("info.toml")

                                        if (entry != null) {
                                            val input = zipFile.getInputStream(entry)
                                            config = Toml.decodeFromNativeReader(input.reader())
                                        }
                                    }


                                    logger.info("Config: $config")

                                    config
                                        ?: run {
                                            extensions = listOf()
                                            return Result.failure(FileNotFoundException("No info.toml found in extension: ${fi.absolutePath}"))
                                        }

                                    if (extensions!!.any { it.config.id == config.id }) {
                                        extensions = listOf()
                                        return Result.failure(
                                            IllegalStateException("Duplicate extension id found: ${config.id}")
                                        )
                                    }

                                    logger.info("add extension")
                                    add(
                                        newExtension(
                                            enabledExtension.contains(config.id),
                                            !fi.isDirectory,
                                            fi,
                                            config.copy(hasResource = config.hasResource || fi.name.endsWith(".rwres"))
                                        )
                                    )
                                }
                        }
                } else emptyList()
            })
    }

    override fun canEnable(extension: Extension): Boolean {
        return extension.isSupportedForCurrentPlatform &&
                compareVersions(extension.config.minGameVersion, projectVersion) != -1 &&
                (extension.config.dependencies.isEmpty() || extension.config.dependencies
                    .all { getExtensionById(it)?.isEnabled == true })
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
        getAllExtensions().onFailure {
            UI.showWarning(it.message ?: "Load extensions failed.")
        }.getOrNull()?.forEach { extension ->
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
                } catch (e: Exception) { logger.error(e.stackTraceToString()) }
            }
        }
    }

    private fun isSupportCurrentGamePlatform(platform: String): Boolean {
        val app = get<AppContext>()
        return (platform.equals("android", ignoreCase = true) && app.isAndroid())
                || (platform.equals("desktop", ignoreCase = true) && app.isDesktop())
    }
}