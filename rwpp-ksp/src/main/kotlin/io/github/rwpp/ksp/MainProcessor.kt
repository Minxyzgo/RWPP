/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import io.github.rwpp.inject.*
import io.github.rwpp.inject.runtime.Builder
import javassist.ClassPool

class MainProcessor(
    private val logger: KSPLogger,
    private val requiredPathType: PathType
) : SymbolProcessor {

    lateinit var classPool: ClassPool
    val injectInfos = mutableSetOf<InjectInfo>()
    val setInterfaceOnInfos = mutableSetOf<SetInterfaceOnInfo>()
    val redirectToInfos = mutableSetOf<RedirectToInfo>()
    val redirectMethodInfos = mutableSetOf<RedirectMethodInfo>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        MainProcessor.logger = logger
        //only run once
        if (finished) return emptyList()


        if (!init) {
            init = true
            Builder.loadLib()
            classPool = GameLibraries.includes.first().classTree.defPool
        }

        for (file in resolver.getSymbolsWithAnnotation(RedirectTo::class.qualifiedName!!)) {
            file.annotations.forEach { annotation ->
                if (annotation.shortName.asString() == RedirectTo::class.simpleName) {
                    val from = annotation.arguments.first().value as String
                    val to = annotation.arguments[1].value as String
                    redirectToInfos.add(RedirectToInfo(from, to))
                }
            }
        }

        for (clazz in resolver.getSymbolsWithAnnotation(InjectClass::class.qualifiedName!!)) {
           processClass(clazz as KSClassDeclaration) {
               val injectClass = clazz.annotations.first { it.shortName.asString() == InjectClass::class.simpleName }.arguments.first().value as KSType
               injectClass.declaration.qualifiedName?.asString()
           }
        }

        for (clazz in resolver.getSymbolsWithAnnotation(InjectClassByString::class.qualifiedName!!)) {
            processClass(clazz as KSClassDeclaration) {
                clazz.annotations.first { it.shortName.asString() == InjectClassByString::class.simpleName }.arguments.first().value as String
            }
        }

        for (clazz in resolver.getSymbolsWithAnnotation(SetInterfaceOn::class.qualifiedName!!)) {
            clazz as KSClassDeclaration
            require(clazz.classKind == ClassKind.INTERFACE) {  "Only interfaces can be annotated with @SetInterfaceOn" }
            val annotation = clazz.annotations.first {  it.shortName.asString() == SetInterfaceOn::class.simpleName }
            val classes = (annotation.arguments.first().value as ArrayList<KSType>).map { it.declaration.qualifiedName!!.asString() }
            classes.forEach { className ->
                val hasSelfProperty = clazz.declarations.any {
                    it is KSPropertyDeclaration && it.simpleName.asString() == "self"
                }

                setInterfaceOnInfos.add(
                    SetInterfaceOnInfo(
                        clazz.qualifiedName!!.asString(),
                        className,
                        clazz.declarations.filter { anno ->
                            anno is KSPropertyDeclaration && anno.annotations.any { it.shortName.asString() == NewField::class.simpleName }
                        }.map { property ->
                            property as KSPropertyDeclaration
                            property.simpleName.asString() to transformClass(property.type.resolve().declaration.qualifiedName!!.asString())
                        }.toList(),
                        clazz.declarations.filter { anno ->
                            anno is KSPropertyDeclaration && anno.annotations.any { it.shortName.asString() == Accessor::class.simpleName }
                        }.map { property ->
                            property as KSPropertyDeclaration
                            property.simpleName.asString() to property.annotations.first { it.shortName.asString() == Accessor::class.simpleName }.arguments.first().value as String
                        }.toList(),
                        hasSelfProperty
                    )
                )
            }
        }

        finished = true

        return emptyList()
    }

    override fun finish() {
        if (init) {
            Builder.rootInfo = RootInfo(
                injectInfos,
                setInterfaceOnInfos,
                redirectToInfos,
                redirectMethodInfos,
            )
            Builder.saveConfig(Builder.rootInfo!!, Builder.configFile)
        }
    }

    private fun processClass(
        clazz: KSClassDeclaration,
        injectClassNameProvider: () -> String?,
    ) {
        if (!init) {
            init = true
            Builder.loadLib()
        }

        require(clazz.classKind == ClassKind.OBJECT) { "Only objects can be annotated with @InjectClass" }
        require(clazz.parentDeclaration == null) { "Object ${clazz.simpleName.asString()} must be top level" }

        val injectClassName = injectClassNameProvider() ?: return

        if (injectClasses.contains(injectClassName)) {
            throw IllegalStateException("Duplicate @InjectClass annotation for $injectClassName")
        }

        injectClasses.add(injectClassName)

        logger.warn("processing class $injectClassName")

        clazz.declarations.forEach { declaration ->
            if (declaration is KSFunctionDeclaration) {
                declaration.annotations.filter {
                    it.shortName.asString() in listOf(Inject::class.simpleName, RedirectMethod::class.simpleName)
                }.sortedBy {
                    if (it.shortName.asString() == RedirectMethod::class.simpleName) 0 else 1
                }.forEach { annotation ->
                    var receiver = declaration
                        .extensionReceiver?.
                        resolve()

                    if (receiver != null) {
                        if (receiver.declaration is KSTypeAlias) receiver = (receiver.declaration as KSTypeAlias).type.resolve()
                    }

                    val receiverType = receiver?.declaration?.qualifiedName?.asString() ?: ""
                    if (receiver != null && receiverType != injectClassName && receiverType != "kotlin.Any")
                        throw IllegalArgumentException(
                            "Receiver (${receiver.declaration.qualifiedName!!.asString()})" +
                                    " of ${declaration.simpleName.asString()} must be of type $injectClassName"
                        )

                    val methodName = annotation.arguments.first().value as String

                    val thisCode = if (receiver != null) "this," else ""

                    val injectFunctionPath = if (requiredPathType == PathType.Path)
                        "${clazz.qualifiedName!!.asString()}.${declaration.simpleName.asString()}"
                    else
                        "${clazz.qualifiedName!!.asString()}.${declaration.simpleName.asString()}($thisCode$$);"

                    val args by lazy {
                        declaration.parameters.map { param ->
                            val type = param.type.resolve()
                            val paramDeclaration = type.declaration
                            val qualifiedName = paramDeclaration.qualifiedName!!.asString()
                            val className = if (qualifiedName == "kotlin.Array") {
                                type.arguments.first().type!!.resolve().declaration.qualifiedName!!.asString() + "[]"
                            } else transformClass(qualifiedName)
                            classPool[className]
                        }
                    }

                    when (annotation.shortName.asString()) {
                        RedirectMethod::class.simpleName -> {
                            val method = classPool[injectClassName].getMethod(methodName, annotation.arguments[1].value as String)
                            val targetClassName = annotation.arguments[2].value as String
                            val targetMethodName = annotation.arguments[3].value as String
                            val targetMethod = classPool[targetClassName].getDeclaredMethod(targetMethodName, args.toTypedArray())
                            redirectMethodInfos.add(
                                RedirectMethodInfo(
                                    receiver != null,
                                    injectClassName,
                                    methodName,
                                    method.signature,
                                    targetClassName,
                                    targetMethodName,
                                    targetMethod.signature,
                                    injectFunctionPath,
                                    requiredPathType
                                )
                            )
                        }

                        Inject::class.simpleName -> {
                            val methodDesc = ((annotation.arguments.getOrNull(2)?.value as? String) ?: "")
                                .ifBlank {
                                    classPool[injectClassName]
                                        .getDeclaredMethod(methodName, args.toTypedArray()).signature
                                }

                            injectInfos.add(
                                InjectInfo(
                                    injectClassName,
                                    receiver != null,
                                    methodName,
                                    methodDesc,
                                    injectFunctionPath,
                                    requiredPathType,
                                    declaration.returnType!!.resolve().declaration.qualifiedName!!.asString() == Unit::class.qualifiedName!!,
                                    InjectMode.valueOf((annotation.arguments[1].value as KSType).declaration.simpleName.asString())
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private var init = false
    private var finished = false
    private val injectClasses = mutableSetOf<String>()

    private fun transformClass(className: String): String = when (className) {
        "kotlin.Any" -> "java.lang.Object"
        "kotlin.Int" -> "int"
        "kotlin.Long" -> "long"
        "kotlin.Float" -> "float"
        "kotlin.Double" -> "double"
        "kotlin.Boolean" -> "boolean"
        "kotlin.Byte" -> "byte"
        "kotlin.Char" -> "char"
        "kotlin.Short" -> "short"
        "kotlin.String" -> "java.lang.String"
        "kotlin.Array" -> "java.lang.Object[]"
        "kotlin.IntArray" -> "int[]"
        "kotlin.LongArray" -> "long[]"
        "kotlin.FloatArray" -> "float[]"
        "kotlin.DoubleArray" -> "double[]"
        "kotlin.BooleanArray" -> "boolean[]"
        "kotlin.ByteArray" -> "byte[]"
        "kotlin.CharArray" -> "char[]"
        "kotlin.ShortArray" -> "short[]"
        "kotlin.collections.List" -> "java.util.List"
        "kotlin.collections.Map" -> "java.util.Map"
        "kotlin.collections.Set" -> "java.util.Set"
        "kotlin.collections.MutableList" -> "java.util.List"
        "kotlin.collections.MutableMap" -> "java.util.Map"
        "kotlin.collections.MutableSet" -> "java.util.Set"
        "kotlin.Unit" -> "void"
        else -> className
    }

    companion object {
        lateinit var logger: KSPLogger
    }
}