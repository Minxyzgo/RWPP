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
import javassist.ClassMap

class MainProcessor(
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        //only run once
        if (finished) return emptyList()

        val classMap = ClassMap()

        for (file in resolver.getSymbolsWithAnnotation(RedirectTo::class.qualifiedName!!)) {
            if (!init) {
                init = true
                Builder.loadLib()
            }

            file.annotations.forEach { annotation ->
                if (annotation.shortName.asString() == RedirectTo::class.simpleName) {
                    val from = annotation.arguments.first().value as String
                    val to = annotation.arguments[1].value as String
                    logger.warn("redirecting $from to $to")
                    classMap[from] = to
                }
            }
        }

        if (classMap.isNotEmpty()) {
            InjectApi.redirectClassName(classMap)
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

        finished = true

        return emptyList()
    }

    override fun finish() {
        if (init) Builder.saveLib()
    }

    private fun processClass(
        clazz: KSClassDeclaration,
        injectClassNameProvider: () -> String?,
    ) {
        if (!init) {
            init = true
            Builder.loadLib()
        }

        if (clazz.classKind != ClassKind.OBJECT) throw IllegalArgumentException("Only objects can be annotated with @InjectClass")
        if (clazz.parentDeclaration != null) throw IllegalArgumentException("Object ${clazz.simpleName.asString()} must be top level")

        val injectClassName = injectClassNameProvider() ?: return

        if (injectClasses.contains(injectClassName)) {
            throw IllegalStateException("Duplicate @InjectClass annotation for $injectClassName")
        }

        injectClasses.add(injectClassName)

        logger.warn("processing class $injectClassName")

        clazz.declarations.forEach { declaration ->
            if (declaration is KSFunctionDeclaration) {
                declaration.annotations.forEach { annotation ->
                    when (annotation.shortName.asString()) {
                        Inject::class.simpleName -> {
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


                            //if (injectClass.declaration.parentDeclaration != null) MainProcessorProvider.logger.warn(injectClass.declaration.parentDeclaration!!::class.qualifiedName!!)
                            InjectApi.injectMethod(
                                injectClassName,
                                receiver != null,
                                annotation.arguments.first().value as String,
                                declaration.parameters.map { it.type.resolve().declaration.qualifiedName!!.asString() },
                                InjectMode.valueOf((annotation.arguments[1].value as KSType).declaration.simpleName.asString()),
                                declaration.returnType!!.resolve().declaration.qualifiedName!!.asString() == Unit::class.qualifiedName!!,
                                "${clazz.qualifiedName!!.asString()}.${declaration.simpleName.asString()}",
                                (annotation.arguments.getOrNull(2)?.value as? String) ?: "",
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
}