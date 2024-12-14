/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.io

import java.io.File
import java.util.zip.ZipFile

fun File.calculateSize(): Long {
    var size = 0L
    if(this.isFile) {
        return this.length()
    } else {
        this.listFiles()?.forEach {
            size += if(it.isFile) it.length() else it.calculateSize()
        }
    }

    return size
}

fun File.unzipTo(targetFile: File) {
    val zipFile = ZipFile(this)
    for(entry in zipFile.entries()) {
        if(entry.isDirectory) {
            File(targetFile, entry.name).mkdirs()
        } else {
            val file = File(targetFile, entry.name)
            if(!file.exists()) {
                if(!file.parentFile.exists()) file.parentFile.mkdirs()
                file.createNewFile()
            }
            file.writeBytes(zipFile.getInputStream(entry).readBytes())
        }
    }
}