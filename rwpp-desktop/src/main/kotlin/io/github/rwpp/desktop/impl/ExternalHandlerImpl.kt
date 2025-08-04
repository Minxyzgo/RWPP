/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import io.github.rwpp.core.Initialization
import io.github.rwpp.external.Extension
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.impl.BaseExternalHandlerImpl
import io.github.rwpp.io.unzipTo
import io.github.rwpp.resOutputDir
import io.github.rwpp.resourceOutputDir
import javassist.LoaderClassPath
import org.koin.core.annotation.Single
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import javax.swing.JFileChooser

@Single(binds = [ExternalHandler::class, Initialization::class])
class ExternalHandlerImpl : BaseExternalHandlerImpl() {
    override fun enableResource(resource: Extension?) {
        if (resource?.config?.hasResource == false) return
        _usingResource = resource
        resource ?: run {
            File(resourceOutputDir).let {
                it.setWritable(true)
                if (it.exists()) it.deleteRecursively()
            }
            File(resOutputDir).let {
                it.setWritable(true)
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

    override fun loadJar(jar: File, parent: ClassLoader): ClassLoader {
        return object : URLClassLoader(arrayOf<URL?>(jar.toURI().toURL()), parent) {
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

    override fun getMultiplatformClassPath(parent: ClassLoader): javassist.ClassPath {
        return LoaderClassPath(parent)
    }
}