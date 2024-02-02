/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.utils.io

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun File.zipFolderToByte(): ByteArray {
    val byteOut = ByteArrayOutputStream()
    val zipOut = ZipOutputStream(byteOut)
    zipRecursive(this, Paths.get(this.absolutePath), zipOut)
    val bytes = byteOut.toByteArray()
    byteOut.close()
    zipOut.close()
    return bytes
}

private fun zipRecursive(sourceFile: File, base: Path, zip: ZipOutputStream) {
    if(sourceFile.isDirectory) {
        val fileList = sourceFile.listFiles() ?: return
        for(file in fileList) {
            zipRecursive(file, base, zip)
        }
    } else {
        val entryName = base.relativize(Paths.get(sourceFile.absolutePath)).toString()
        zip.putNextEntry(ZipEntry(entryName))
        sourceFile.inputStream().use { it.copyTo(zip) }
    }
}