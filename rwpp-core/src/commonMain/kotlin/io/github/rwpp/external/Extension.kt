/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.external

import androidx.compose.ui.graphics.painter.Painter
import io.github.rwpp.logger
import io.github.rwpp.scripts.LuaWidget
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

abstract class Extension(
    var isEnabled: Boolean = false,
    val file: File,

    /**
     * Null if the extension is a folder.
     */
    val zipFile: ZipFile?,
    val config: ExtensionConfig,
) {
    abstract val iconPainter: Painter?

    @JvmField
    var settingPanel = mutableListOf<LuaWidget>()

    @Suppress("unused")
    fun openInputStream(entryName: String): InputStream {
        return if (zipFile != null) {
            logger.debug("Opening input stream for $entryName from zip file")
            zipFile.getInputStream(zipFile.getEntry(entryName))
        } else {
            File(file, entryName).inputStream()
        }
    }
}