/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

class MainProcessor : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        //only run once
        if (finished) return emptyList()

        for (clazz in resolver.getSymbolsWithAnnotation(InjectClass::class.qualifiedName!!)) {
            clazz as KSClassDeclaration
            if (clazz.classKind != ClassKind.OBJECT) throw IllegalStateException("Only objects can be annotated with @InjectClass")
            if (clazz.parentDeclaration != null) throw IllegalStateException("Object ${clazz.simpleName.asString()} must be top level")
            val injectClass = clazz.annotations.first { it.shortName.asString() == InjectClass::class.simpleName }.arguments.first().value as KSType

            val injectClassName = injectClass.declaration.qualifiedName?.asString()?.also {
                if (injectClasses.contains(it)) {
                    throw IllegalStateException("Duplicate @InjectClass annotation for $it")
                }

                injectClasses.add(it)
            } ?: continue


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

                                if (receiver != null && receiver.declaration.qualifiedName?.asString() != injectClassName)
                                    throw IllegalStateException(
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
                                    "${clazz.qualifiedName!!.asString()}.${declaration.simpleName.asString()}"
                                )
                            }
                        }
                    }
                }
            }
        }

        finished = true

        return emptyList()
    }

    override fun finish() {
        Builder.saveLib()
    }

    private var finished = false
    private val injectClasses = mutableSetOf<String>()
}