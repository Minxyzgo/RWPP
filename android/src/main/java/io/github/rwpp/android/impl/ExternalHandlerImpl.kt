/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import io.github.rwpp.R
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.external.Resource
import io.github.rwpp.external.ResourceConfig
import io.github.rwpp.impl.BaseExternalHandlerImpl
import io.github.rwpp.resOutputDir
import io.github.rwpp.resourceOutputDir
import io.github.rwpp.utils.io.unzipTo
import org.koin.core.annotation.Single
import org.koin.core.component.get
import java.io.File
import java.util.zip.ZipFile

@Single(binds = [ExternalHandler::class])
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


        val resourceList = listOf("units", "tilesets", "music", "shaders")
        resourceList.forEach {
            copyAssets(get(), it, resourceOutputDir + it)
        }

        val resList = listOf(
            R.drawable::class.java,
            R.raw::class.java
        )

        val resources = get<Context>().resources

        resList.forEach { clazz ->
            clazz.declaredFields.forEach {
                val i = (it.get(null) as Int)
                val bytes = resources.openRawResource(i).use { res -> res.readBytes() }
                val fi = File(resOutputDir + resources.getResourceFileName(i))
                fi.parentFile!!.run { if (!exists()) mkdirs() }
                if (!fi.exists()) fi.createNewFile()
                fi.writeBytes(bytes)
            }
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
                    BitmapPainter(
                        BitmapFactory.decodeStream(zipFile.getInputStream(iconEntry))
                          .asImageBitmap()
                    )
                }
            }
        }
    }

    override fun getUsingResource(): Resource? {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) return null
        }

        return super.getUsingResource()
    }
}