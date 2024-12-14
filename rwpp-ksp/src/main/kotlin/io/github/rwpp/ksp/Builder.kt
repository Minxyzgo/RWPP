/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ksp

import javassist.CtClass
import javassist.bytecode.Descriptor
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Suppress("MemberVisibilityCanBePrivate")
internal object Builder {
    var libDir = "lib"
    var releaseLibActions = mutableMapOf<GameLibraries, (GameLibraries, File, ClassLoader) -> Unit>()

    /**
     * 保存现有已修改的lib到[libDir]
     */
    fun saveLib() {
         GameLibraries.includes.forEach { v ->
             val jarFile = File("$libDir/${v.realName}.jar")
             buildJar(jarFile, v.classTree.allClasses)
         }
    }

    /**
     * 根据[libDir]加载lib，若[libDir]不存在，则返回[FileNotFoundException]
     */
    fun loadLib() {
        releaseLibs()

        val libFile = File(libDir)
        if(!libFile.exists()) {
            throw FileNotFoundException("libFile: $libDir is not exists")
        }

        GameLibraries.entries.filter { it.shouldLoad }.forEach {
            it.load(libDir)
        }
    }

    /**
     * 释放包内的lib资源到[libDir]
     */
    fun releaseLibs(
        cl: ClassLoader = Thread.currentThread().contextClassLoader,
        targetDir: String = libDir,
    ) {
        GameLibraries.entries.forEach { releaseLib(cl, it, targetDir) }
    }

    /**
     * 释放包内指定的lib资源到[libDir]
     */
    fun releaseLib(
        cl: ClassLoader = Builder::class.java.classLoader,
        lib: GameLibraries,
        targetDir: String,
        libName: String = lib.realName
    ) {
        val jarFile = File("$targetDir/${libName}.jar")
        releaseLibActions[lib]?.let {
            it(lib, jarFile, cl)
        } ?: cl.getResourceAsStream("${libName}.jar")!!.use {
            if(!jarFile.exists()) {
                jarFile.parentFile.mkdirs()
                jarFile.createNewFile()
            }

            jarFile.writeBytes(it.readBytes())
        }
    }

    /**
     * 给定文件和类列表生成jar
     * @param jar 生成jar的文件
     * @param classes 给定的类列表
     */
     fun buildJar(jar: File, classes: Iterable<CtClass>) {
        val tempFile = File.createTempFile("temp-${jar.name}", ".jar")
        val zipOut = ZipOutputStream(
            tempFile.outputStream()
        )

        zipOut.use { zip ->
            classes.forEach {
                zip.putNextEntry(ZipEntry(Descriptor.toJvmName(it) + ".class"))
                zip.write(it.toBytecode())
                it.defrost()
            }
        }

        val arr = tempFile.readBytes()
        jar.writeBytes(arr)
        tempFile.delete()
    }
}
