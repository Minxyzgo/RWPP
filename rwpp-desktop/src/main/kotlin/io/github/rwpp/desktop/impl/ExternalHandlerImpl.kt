/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import io.github.rwpp.core.Initialization
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.external.Extension
import io.github.rwpp.external.ExtensionConfig
import io.github.rwpp.impl.BaseExternalHandlerImpl
import io.github.rwpp.resOutputDir
import io.github.rwpp.resourceOutputDir
import io.github.rwpp.io.unzipTo
import org.koin.core.annotation.Single
import java.io.File
import java.util.zip.ZipFile
import javax.imageio.ImageIO
import javax.swing.JFileChooser

@Single(binds = [ExternalHandler::class, Initialization::class])
class ExternalHandlerImpl : BaseExternalHandlerImpl() {
    override fun enableResource(resource: Extension?) {
        if (resource?.config?.hasResource == false) return
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


        resource.file.unzipTo(File(resourceOutputDir))
    }

    override fun openFileChooser(onChooseFile: (File) -> Unit) {
        val fileChooser = JFileChooser()
        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            onChooseFile(fileChooser.selectedFile)
        }
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
            override val iconPainter: Painter? by lazy {
                if (config.icon.isBlank())
                    null
                else {
                    ImageIO.read(
                        zipFile?.let { it.getInputStream(it.getEntry(config.icon)) }
                            ?: File(extensionFile, config.icon).inputStream()
                    ).toPainter()
                }
            }
        }
    }
}