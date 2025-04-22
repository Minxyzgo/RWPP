/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ksp

import javassist.ClassClassPath
import javassist.ClassPath
import javassist.ClassPool
import javassist.LoaderClassPath
import javassist.bytecode.ClassFile
import java.io.File
import java.io.FileNotFoundException

@Suppress(
    "EnumEntryName",
    "RemoveRedundantBackticks",
    "SpellCheckingInspection",
    "MemberVisibilityCanBePrivate"
)
enum class GameLibraries {
    `game-lib`,
    `android`,
    `android-game-lib`,
    `common-game-lib`,
    `android-platform-lib`,
    `commons-codec` { init {
        realName = "$name-1.6"
    } },
    `commons-logging` { init {
        realName = "$name-1.1.3"
    } },
    `fluent-hc` { init {
        realName = "$name-4.3.3"
    } },
    `httpclient` { init {
        realName = "$name-4.3.3"
    } },
    `httpclient-cache` { init {
        realName = "$name-4.3.3"
    } },
    `httpcore` { init {
        realName = "$name-4.3.2"
    } },
    `httpmime` { init {
        realName = "$name-4.3.3"
    } },
    `ibxm`,
    `jinput`,
    `jnlp`,
    `jogg` { init {
        realName = "$name-0.0.7"
    } },
    `jorbis` { init {
        realName = "$name-0.0.15"
    } },
    `lwjgl`,
    `lwjgl_util`,
    `lwjgl_util_applet`,
    `natives-linux`,
    `slick`,
    `tinylinepp`;

    var cp: Any? = null // ClassPath
    var realName = name
    var shouldLoad: Boolean = true

    lateinit var lib: File
    internal lateinit var classTree: ClassTree

    internal fun load(libPath: String, libName: String = realName) {
        isLoaded = true
        lib = File("${libPath}/$libName.jar")
        if(!lib.exists()) throw FileNotFoundException("cannot find lib: $name")
        removeClassPath()
        cp = defClassPool.appendClassPath(lib.absolutePath)
        if(this in includes) {
            classTree = ClassTree(ClassPool(defClassPool)).apply {
                defPool.childFirstLookup = true
                name = this@GameLibraries.name
                initByJarFile(lib)
            }
        }
    }

    companion object {
        @JvmStatic
        internal val defClassPool by lazy {
            ClassPool().apply {
                val cl = MainProcessorProvider::class.java.classLoader
                appendClassPath(LoaderClassPath(cl))
            }
        }

        /**
         * 需要被加载为[ClassTree]的lib
         */
        internal val includes by lazy { mutableSetOf<GameLibraries>() }
    }

    private var isLoaded = false

    private fun removeClassPath() {
        cp?.let { defClassPool.removeClassPath(cp as ClassPath) }
        cp = null
    }
}