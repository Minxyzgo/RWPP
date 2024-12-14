/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ksp

import javassist.ClassPool
import javassist.CtClass
import java.io.File
import java.util.zip.ZipFile

/**
 * 以流模式实现的ClassTree, 便于获取任意jar包内的class等信息
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
internal class ClassTree(
    val defPool: ClassPool,
) {
    /**
     * 该包下所有的子级包
     */
    val packages = mutableSetOf<ClassTree>()

    /**
     * 该包下所有的class的name组成的集合
     */
    val classesName = mutableSetOf<String>()

    /**
     * 该包的全称名
     */
    var longName: String = ""
        private set

    /**
     * 该包的简称名，如想获取全名使用[longName]
     *
     * 调用set函数会同时更改[longName], 并且同时重定向所有子包
     */
    var name: String = ""
        set(value) {
            field = value

            if(this == root) {
                return
            }

            val newLongName = longName.split(".").toMutableList().apply {
                this[lastIndex] = value
            }.joinToString(".")

            allPackages.forEach {
                it.longName = newLongName + it.longName.removePrefix(longName)
            }

            longName = newLongName
        }

    /**
     * 获取该包所在的jar的root节点
     */
    var root: ClassTree = this

    /**
     * 获取该包下所有的class(包括子包下）
     *
     * 注意：由于tree结构可能发生更改，因此这不会自动进行缓存
     */
    val allClasses: Set<CtClass>
        get() {
            val result = mutableSetOf<CtClass>()
            var next = this

            fun add() {
                result.addAll(next.classesName.map { defPool["${next.longName}.$it"] })
                next.packages.forEach {
                    next = it
                    add()
                }
            }

            add()
            return result.toSet()
        }


    /**
     * 获取该包下所有子包
     *
     * 注意：由于tree结构可能发生更改，因此这不会自动进行缓存
     */
    val allPackages: Set<ClassTree>
        get() =
            if(this == root) {
                root.allPackageFromRoot.values.toSet()
            }else {
                val result = mutableSetOf<ClassTree>()
                var next = this

                fun add() {
                    result.addAll(next.packages)
                    next.packages.forEach {
                        next = it
                        add()
                    }
                }

                add()
                result.toSet()
            }

    /**
     * 获取该包所在的jar下所包含的所有包
     */
    val allPackageFromRoot: MutableMap<String, ClassTree> by lazy {
        if(this != root) {
            root.allPackageFromRoot
        } else {
            mutableMapOf()
        }
    }

    /**
     * 以包的全称名获取包 （从[allPackageFromRoot]中获取）
     */
    fun findTreeByPackage(
        packageName: String,
    ): ClassTree {
        return allPackageFromRoot[packageName]!!
    }

    /**
     * 通过class名获取该class所在包的tree，从根包开始查找
     */
    fun getTreeByCtClassName(name: String): ClassTree {
        val (tree, _) = root.getTreeByCtClassName0(name)
        return tree
    }

    fun forEachAllClassesWithTree(
        action: (CtClass, ClassTree) -> Unit,
    ) {
        var next = this

        fun each() {
            next.packages.forEach { tree ->
                next = tree
                tree.classesName.toSet().forEach {
                    val path = "${tree.longName}.$it"
                    action(defPool.getOrNull(path) ?: defPool["${tree.longNameCache}.$it"], tree)
                }
                each()
            }
        }

        each()
    }

    /**
     * 通过jar文件初始化该classTree，执行任意操作前这都是必须的
     */
    fun initByJarFile(jar: File) {
        defPool.appendClassPath(jar.absolutePath)
        ZipFile(jar).entries().toList().mapNotNull {
            if(it.name.contains("META-INF")) {
                return@mapNotNull null
            }

            if(it.name.endsWith(".class")) it.name.removeSuffix(".class").replace("/", ".") else null
        }.forEach {
            val packageName = it.substring(0, it.lastIndexOf('.'))
            addPackageTree(packageName)
            allPackageFromRoot[packageName]!!.classesName.add(it.substring(it.lastIndexOf('.') + 1, it.lastIndex + 1))
        }
    }

    /**
     * 当tree变更完毕时调用此函数刷新tree结构
     */
    fun flushAllPackages() {
        allPackageFromRoot.toMap().forEach { (t, u) ->
            if(t != u.longName) {
                allPackageFromRoot.remove(t)
                allPackageFromRoot[u.longName] = u
            }

            u.longNameCache = u.longName
        }
    }

    /** longName缓存，防止包名更改无法找到类 */
    private var longNameCache = ""

    private fun addPackageTree(name: String) {
        if(this.root != this) throw RuntimeException()
        if(allPackageFromRoot[name] != null) return

        fun newTree(str: String, fullName: String): ClassTree {
            val tree = ClassTree(defPool).apply {
                this.name = str
                this@apply.root = this@ClassTree.root
                this.longName = fullName
                longNameCache = fullName
            }

            allPackageFromRoot[fullName] = tree
            return tree
        }

        var nextTree = root

        val arr = name.split(".")
        for((i ,str) in arr.withIndex()) {
            val fullName = arr.take(i + 1).joinToString(".")
            nextTree = nextTree.packages.firstOrNull { it.name == str } ?: newTree(str, fullName).also { nextTree.packages.add(it) }
        }
    }

    private fun getTreeByCtClassName0(name: String): Pair<ClassTree, String?> {
        var realName: String? = null
        var currentTree: ClassTree = this
        if(name in classesName) realName = name
        if(realName == null) {
            for(tree in packages) {
                val (t, n) = tree.getTreeByCtClassName0(name)
                currentTree = t
                realName = n
                if(realName != null) break
            }
        }

        return currentTree to realName
    }
}