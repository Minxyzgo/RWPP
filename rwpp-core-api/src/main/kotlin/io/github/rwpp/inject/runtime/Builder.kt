/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.inject.runtime

import io.github.rwpp.appKoin
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.inject.*
import io.github.rwpp.inject.runtime.Builder.configFile
import io.github.rwpp.inject.runtime.Builder.libDir
import io.github.rwpp.inject.runtime.Builder.outputDir
import javassist.ClassMap
import javassist.CtClass
import javassist.LoaderClassPath
import javassist.bytecode.Descriptor
import kotlinx.serialization.encodeToString
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromNativeReader
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.Reader
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


@Suppress("MemberVisibilityCanBePrivate")
object Builder {
    var libDir = "lib"
    var outputDir = "output"
    val configFile
        get() = "$outputDir/config.toml"
    var rootInfo: RootInfo? = null
    var logger: BuildLogger? = null
    var configToml: Toml = Toml {}
    var releaseLibActions = mutableMapOf<GameLibraries, (GameLibraries, File, ClassLoader) -> Unit>()

    fun init(lib: GameLibraries, libFile: File) {
        GameLibraries.includes.add(lib)
        lib.load(libFile)
        val externalHandler = appKoin.get<ExternalHandler>()
        val extensions = externalHandler.getAllExtensions().getOrNull()
        saveConfig(rootInfo!!, configFile)
        extensions?.forEach {
            if (it.config.hasInjectInfo && it.isEnabled) {
                checkAndMergeConfig(it.injectInfo!!)
                val loader = externalHandler.loadExtensionClass(it)
                GameLibraries.defClassPool.appendClassPath(externalHandler.getMultiplatformClassPath(loader!!))
            }
        }
        applyConfig()
        extensions?.forEach {
            if (it.config.hasInjectInfo && it.isEnabled) {
                saveConfig(it.injectInfo!!, "$outputDir/config_${it.config.id}.toml")
            }
        }
        saveLib()

    }

    fun prepareReloadingLib(): Boolean {
        val resourceConfig = loadConfig(getConfigInputStream(null).reader())
        val extensions = appKoin.get<ExternalHandler>().getAllExtensions().getOrNull()
        var hasUnenabledExtensions = false
        extensions?.forEach {
            val configFile = File(outputDir, "config_${it.config.id}.toml")
            if (!it.isEnabled && configFile.exists()) {
                hasUnenabledExtensions = true
                configFile.delete()
            }
        }
        val extensionConfigNotEquals = extensions?.any {
            val configFile = File(outputDir, "config_${it.config.id}.toml")
            it.config.hasInjectInfo && it.isEnabled &&
                    (!configFile.exists() || it.injectInfo != loadConfig(configFile.reader()))
        } ?: false
        val gameLibNotExists = File(outputDir).let { file ->
            !file.exists() || !file.walk().any { it.extension == "jar" && it.name.contains("game-lib") }
        }
        val resourceConfigNotEquals = File(configFile).let { file ->
            val currentConfig = loadConfig(getConfigInputStream(file).reader())
            file.exists() && resourceConfig != currentConfig
        }
        rootInfo = resourceConfig

        return hasUnenabledExtensions || extensionConfigNotEquals || gameLibNotExists || resourceConfigNotEquals
    }

    fun getConfigInputStream(file: File? = null): InputStream {
        return if (file?.exists() == true)
            file.inputStream()
        else
            Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream("config.toml")
    }


    /**
     * 保存现有已修改的lib到[outputDir]
     */
    fun saveLib() {
        logger?.info("Save lib to $outputDir")
        GameLibraries.includes.forEach { v ->
            val jarFile = File("$outputDir/${v.realName}.jar")
            buildJar(jarFile, v.classTree.allClasses)
        }
    }

    /**
     * 根据[libDir]加载lib，若[libDir]不存在，则返回[FileNotFoundException]
     */
    fun loadLib() {
        logger?.info("Load lib from $libDir")
        val libFile = File(libDir)
        if (!libFile.exists()) {
            throw FileNotFoundException("libFile: $libDir is not exists")
        }

        GameLibraries.entries.filter { it.shouldLoad }.forEach {
            it.load(File(libDir, it.realName + ".jar"))
        }
    }

    /**
     * 加载配置文件[configFile]
     */
    fun loadConfig(reader: Reader? = null): RootInfo {
        logger?.info("Load config from $configFile")
        val configFile = File(Builder.configFile)
        if (!configFile.exists() && reader == null) {
            throw FileNotFoundException("configFile: $configFile is not exists")
        }

        return configToml.decodeFromNativeReader(RootInfo.serializer(), reader ?: configFile.reader())
    }

    /**
     * 检查并合并配置
     */
    fun checkAndMergeConfig(otherRootInfo: RootInfo) {
        if (rootInfo == null) {
            throw IllegalStateException("rootInfo is null, please loadConfig() first")
        }

        val overrideInfos = rootInfo!!.injectInfos
            .filter { it.injectMode == InjectMode.Override }
            .associateBy(InjectInfo::signature)
        val redirectToInfos = rootInfo!!.redirectToInfos.associateBy { it.from }
        val redirectMethodInfos = rootInfo!!.redirectMethodInfos.associateBy(RedirectMethodInfo::signature)

        otherRootInfo.let { root ->
            root.injectInfos.forEach {
                if (it.injectMode == InjectMode.Override && it.signature() in overrideInfos) {
                    throw IllegalArgumentException("Cannot override ${it.signature()} twice")
                }
            }

            root.redirectToInfos.forEach {
                if (it.from in redirectToInfos) {
                    throw IllegalArgumentException("redirectToInfo from: ${it.from} is already exists")
                }
            }
            root.redirectMethodInfos.forEach {
                if (it.signature() in redirectMethodInfos) {
                    throw IllegalArgumentException("redirectMethodInfo signature: ${it.signature()} is already exists")
                }
            }
        }

        rootInfo = rootInfo!!.copy(
            injectInfos = rootInfo!!.injectInfos + otherRootInfo.injectInfos,
            redirectToInfos = rootInfo!!.redirectToInfos + otherRootInfo.redirectToInfos,
            redirectMethodInfos = rootInfo!!.redirectMethodInfos + otherRootInfo.redirectMethodInfos
        )
    }

    fun applyConfig() {
        logger?.info("Apply config")
        require(rootInfo != null) { "rootInfo is null, please loadConfig() first" }
        rootInfo!!.let { root ->
            val classMap = ClassMap()
            root.redirectToInfos.forEach { info ->
                classMap[info.from] = info.to
                logger?.info("Redirect ${info.from} to ${info.to}")
            }
            InjectApi.redirectClassName(classMap)

            root.injectInfos.forEach { info ->
                InjectApi.injectMethod(
                    info.className,
                    info.hasReceiver,
                    info.methodName,
                    info.methodDesc,
                    info.injectMode,
                    info.path,
                    info.returnClassIsVoid,
                    info.pathType
                )
            }

            root.setInterfaceOnInfos.forEach { info ->
                InjectApi.injectInterface(
                    info.interfaceName,
                    info.targetClassName,
                    info.newFields,
                    info.accessors,
                    info.hasSelfField,
                )
            }

            root.redirectMethodInfos.forEach { info ->
                InjectApi.redirect(
                    info.className,
                    info.hasReceiver,
                    info.method,
                    info.methodDesc,
                    info.targetClassName,
                    info.targetMethod,
                    info.targetMethodDesc,
                    info.path,
                    info.pathType
                )
            }
        }
    }

    /**
     * 保存配置到[configFile]
     */
    fun saveConfig(config: RootInfo, path: String) {
        logger?.info("Save config to $path")
        val configFile = File(path.replace("\\", "/"))
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            configFile.createNewFile()
        }
        configFile.writeText(configToml.encodeToString(config))
    }

    /**
     * 释放包内的lib资源到[targetDir]
     */
    fun releaseLibs(
        cl: ClassLoader = Thread.currentThread().contextClassLoader,
        targetDir: String = outputDir,
    ) {
        GameLibraries.entries.forEach { releaseLib(cl, it, targetDir) }
    }

    /**
     * 释放包内指定的lib资源到[targetDir]
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
            if (!jarFile.exists()) {
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
        if (!jar.exists()) {
            jar.parentFile.mkdirs()
            jar.createNewFile()
        }
        val tempFile = File.createTempFile("temp-${jar.name}", ".jar")
        val zipOut = ZipOutputStream(
            tempFile.outputStream()
        )

        zipOut.use { zip ->
            classes.forEach {
                zip.putNextEntry(ZipEntry(Descriptor.toJvmName(it) + ".class"))
                zip.write(it.toBytecode())
            }
        }

        val arr = tempFile.readBytes()
        jar.writeBytes(arr)
        tempFile.delete()
    }
}