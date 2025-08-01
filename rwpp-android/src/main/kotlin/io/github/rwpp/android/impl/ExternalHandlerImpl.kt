/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Context
import android.content.Intent
import dalvik.system.PathClassLoader
import io.github.rwpp.R
import io.github.rwpp.android.dexFolder
import io.github.rwpp.android.fileChooser
import io.github.rwpp.android.pickFileActions
import io.github.rwpp.core.Initialization
import io.github.rwpp.external.Extension
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.impl.BaseExternalHandlerImpl
import io.github.rwpp.io.unzipTo
import io.github.rwpp.resOutputDir
import io.github.rwpp.resourceOutputDir
import org.koin.core.annotation.Single
import org.koin.core.component.get
import java.io.File
import java.util.zip.ZipFile


@Single(binds = [ExternalHandler::class, Initialization::class])
class ExternalHandlerImpl : BaseExternalHandlerImpl() {
    override fun enableResource(resource: Extension?) {
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

        resource.file.unzipTo(File(resourceOutputDir))
    }

    override fun openFileChooser(onChooseFile: (File) -> Unit) {
        pickFileActions += onChooseFile
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("*/*") // 设置文件类型为任意类型
        intent.addCategory(Intent.CATEGORY_OPENABLE) // 添加可打开的文件分类
        fileChooser.launch(intent)
    }

    override fun loadJar(jar: File, parent: ClassLoader): ClassLoader {
        val targetFile = File(dexFolder, "classes-${jar.nameWithoutExtension}.dex")
        val zip = ZipFile(jar)
        val dex = zip.getEntry("classes.dex")
        val inputStream = zip.getInputStream(dex)
        if (targetFile.exists()) {
            targetFile.delete()
        }
        targetFile.setReadOnly()
        targetFile.writeBytes(inputStream.readBytes())
        return object : PathClassLoader(targetFile.absolutePath, parent) {
            @Throws(ClassNotFoundException::class)
            override fun loadClass(name: String?, resolve: Boolean): Class<*>? {
                //check for loaded state
                var loadedClass = findLoadedClass(name)
                if (loadedClass == null) {
                    try {
                        //try to load own class first
                        loadedClass = findClass(name)
                    } catch (_: ClassNotFoundException) {
                        //use parent if not found
                        return parent.loadClass(name)
                    }
                }

                if (resolve) {
                    resolveClass(loadedClass)
                }
                return loadedClass
            }
        }
    }


    //private val exFilePicker = ExFilePicker()
}