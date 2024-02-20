/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import io.github.rwpp.desktop.gameContext
import io.github.rwpp.external.Resource
import io.github.rwpp.external.ResourceConfig
import io.github.rwpp.impl.BaseExternalHandlerImpl
import io.github.rwpp.resOutputDir
import io.github.rwpp.resourceOutputDir
import io.github.rwpp.utils.io.unzipTo
import java.io.File
import java.util.zip.ZipFile
import javax.imageio.ImageIO

class ExternalHandlerImpl : BaseExternalHandlerImpl() {
    override fun enableResource(resource: Resource?) {
        _usingResource = resource
        resource ?: run {
            File(resourceOutputDir).let {
                if (it.exists()) it.deleteRecursively()
            }
            File(resOutputDir).let {
                if (it.exists()) it.deleteRecursively()
            }
            return
        }

        val resourceList = listOf("gui", "units", "tilesets", "music", "shaders")
        resourceList.forEach {
            File("assets/$it").copyRecursively(
                File(resourceOutputDir + it), true
            )
        }

        val resList = listOf("drawable", "raw")

        resList.forEach {
            File("res/$it").copyRecursively(
                File(resOutputDir + it), true
            )
        }

        resource.resourceFile.unzipTo(File(resourceOutputDir))
    }


    override fun newResource(id: Int, resourceFile: File, config: ResourceConfig): Resource {
        return object : Resource(
            id, resourceFile, config
        ) {
            override val iconPainter: Painter? by lazy {
                if (config.icon.isBlank())
                    null
                else {
                    val zipFile = ZipFile(resourceFile)
                    val iconEntry = zipFile.getEntry(config.icon)
                    ImageIO.read(zipFile.getInputStream(iconEntry))
                        .toPainter()
                }
            }
        }
    }
}