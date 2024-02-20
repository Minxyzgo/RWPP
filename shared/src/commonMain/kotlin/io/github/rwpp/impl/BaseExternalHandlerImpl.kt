/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl

import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.external.Resource
import io.github.rwpp.external.ResourceConfig
import io.github.rwpp.resourceOutputDir
import io.github.rwpp.resourcePath
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromNativeReader
import java.io.File
import java.io.FileNotFoundException

abstract class BaseExternalHandlerImpl : ExternalHandler {
    protected var _usingResource: Resource? = null
    protected var resources: List<Resource>? = null

    override fun getAllResources(): List<Resource> {
        return resources ?: File(resourcePath).let { file ->
            if (file.exists()) {
                buildList {
                    file
                        .walk()
                        .filter { it.name.endsWith(".rwres") }
                        .forEachIndexed { i, zip ->
                            val zipFile = java.util.zip.ZipFile(zip)
                            val entry = zipFile.getEntry("info.toml")
                            var config: ResourceConfig? = null


                            if (entry != null) {
                                val input = zipFile.getInputStream(entry)
                                config = Toml.decodeFromNativeReader(input.reader())
                            }

                            config ?: throw FileNotFoundException("No info.toml found in resource: ${zip.absolutePath}")

                            add(
                                newResource(i, zip, config)
                            )
                        }
                }.also { resources = it }
            } else emptyList()
        }
    }

    override fun getUsingResource(): Resource? {
        return _usingResource ?: run {
            if(!File(resourceOutputDir).exists()) return@run null
            val info = Toml.decodeFromNativeReader<ResourceConfig>(
                File(resourceOutputDir + "info.toml").reader()
            )

            getAllResources().first { it.config.name == info.name }
        }
    }
}