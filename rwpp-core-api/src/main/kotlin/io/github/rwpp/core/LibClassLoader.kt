/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.core

/**
 * Adapted from https://github.com/Anuken/Mindustry/tree/master/core/src/mindustry/mod/ModClassLoader.java
 */
class LibClassLoader : ClassLoader {
    private val children: MutableList<ClassLoader> = mutableListOf()
    private val inChild: ThreadLocal<Boolean> = object : ThreadLocal<Boolean>() {
        override fun initialValue(): Boolean {
            return false
        }
    }

    constructor(parent: ClassLoader) : super(parent)

    fun addChild(child: ClassLoader) {
        children.add(child)
    }

    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String?): Class<*> {
        //a child may try to delegate class loading to its parent, which is *this class loader* - do not let that happen
        if (inChild.get()) {
            inChild.set(false)
            throw ClassNotFoundException(name)
        }

        var last: ClassNotFoundException? = null
        val size: Int = children.size

        //if it doesn't exist in the main class loader, try all the children
        for (i in 0..<size) {
            try {
                try {
                    inChild.set(true)
                    return children[i].loadClass(name)
                } finally {
                    inChild.set(false)
                }
            } catch (e: ClassNotFoundException) {
                last = e
            }
        }

        throw (last ?: ClassNotFoundException(name))
    }
}