/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.external

import io.github.rwpp.core.Initialization
import io.github.rwpp.core.LibClassLoader
import javassist.ClassPath
import org.koin.core.component.KoinComponent
import java.io.File

interface ExternalHandler : KoinComponent, Initialization {
    val mainLoader: LibClassLoader

    fun getAllExtensions(update: Boolean = false): Result<List<Extension>>

    @Suppress("unused")
    fun getExtensionById(id: String): Extension? {
        return getAllExtensions().getOrThrow().firstOrNull { ext -> ext.config.id == id }
    }

    fun canEnable(extension: Extension): Boolean

    fun enableResource(resource: Extension?)

    fun getUsingResource(): Extension?

    fun openFileChooser(onChooseFile: (File) -> Unit)

    fun loadExtensionClass(extension: Extension): ClassLoader?

    fun loadJar(jar: File, parent: ClassLoader): ClassLoader

    fun getMultiplatformClassPath(parent: ClassLoader): ClassPath

    fun newExtension(
        isEnabled: Boolean,
        isZip: Boolean,
        extensionFile: File,
        config: ExtensionConfig
    ): Extension
}