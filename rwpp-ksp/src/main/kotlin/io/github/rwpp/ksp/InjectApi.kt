/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.rwpp.ksp

import io.github.rwpp.inject.InjectMode
import javassist.CtClass
import javassist.CtNewMethod
import javassist.Modifier
import javassist.bytecode.Bytecode
import javassist.bytecode.ConstPool
import javassist.bytecode.Descriptor
import javassist.bytecode.LocalVariableAttribute

internal object InjectApi {

    fun injectMethod(
        className: String,
        hasReceiver: Boolean,
        methodName: String,
        methodArgs: List<String>,
        injectMode: InjectMode,
        returnClassIsVoid: Boolean,
        injectFunctionPath: String,
    ) {

        @Suppress("NAME_SHADOWING")
        val methodArgs = methodArgs.toMutableList()

        //TODO int[] float[] etc.
        for ((i, v) in methodArgs.withIndex()) {
            when (v) {
                "kotlin.Int" -> methodArgs[i] = "int"
                "kotlin.Long" -> methodArgs[i] = "long"
                "kotlin.Float" -> methodArgs[i] = "float"
                "kotlin.Double" -> methodArgs[i] = "double"
                "kotlin.Boolean" -> methodArgs[i] = "boolean"
                "kotlin.Byte" -> methodArgs[i] = "byte"
                "kotlin.Char" -> methodArgs[i] = "char"
                "kotlin.Short" -> methodArgs[i] = "short"
                "kotlin.String" -> methodArgs[i] = "java.lang.String"
                "kotlin.Array" -> methodArgs[i] = "java.lang.Object[]"
                "kotlin.collections.List" -> methodArgs[i] = "java.util.List"
                "kotlin.collections.Map" -> methodArgs[i] = "java.util.Map"
                "kotlin.collections.Set" -> methodArgs[i] = "java.util.Set"
                "kotlin.collections.MutableList" -> methodArgs[i] = "java.util.List"
                "kotlin.collections.MutableMap" -> methodArgs[i] = "java.util.Map"
                "kotlin.collections.MutableSet" -> methodArgs[i] = "java.util.Set"
                "kotlin.Unit" -> methodArgs[i] = "void"
            }
        }

        val classPool = GameLibraries.`game-lib`.classTree.defPool
        val clazz = classPool.get(className)

        val method = clazz.getDeclaredMethod(methodName, methodArgs.map(classPool::get).toTypedArray())
        val isNative = Modifier.isNative(method.modifiers)

        val originalName = "__original__${method.name}"
        if (!isNative) {
            val proceed = CtNewMethod.copy(method, originalName, clazz, null)
            proceed.modifiers = Modifier.setProtected(method.modifiers)
            clazz.addMethod(proceed)
        } else {
            method.modifiers = method.modifiers and Modifier.NATIVE.inv()
        }

        var codeAttribute = method.methodInfo.codeAttribute

        val injectClass = injectFunctionPath.substringBeforeLast(".")

        val bytecode = Bytecode(clazz.classFile.constPool)

        bytecode.addGetstatic(injectClass, "INSTANCE", Descriptor.of(injectClass))

        var i = 0
        if (!Modifier.isStatic(method.modifiers)) {
            if (hasReceiver) bytecode.addAload(0)
            i++
        } else if (hasReceiver) {
            bytecode.add(Bytecode.ACONST_NULL)
        }

        for (param in method.parameterTypes) {
            when (param) {
                CtClass.booleanType -> bytecode.addIload(i)
                CtClass.byteType -> bytecode.addIload(i)
                CtClass.charType -> bytecode.addIload(i)
                CtClass.shortType -> bytecode.addIload(i)
                CtClass.intType -> bytecode.addIload(i)
                CtClass.longType -> bytecode.addLload(i)
                CtClass.floatType -> bytecode.addFload(i)
                CtClass.doubleType -> bytecode.addDload(i)
                else -> bytecode.addAload(i)
            }

            i++
        }

        bytecode.addInvokevirtual(
            injectClass,
            injectFunctionPath.substringAfterLast("."),
            "(" +
                    (if (hasReceiver) Descriptor.of(clazz) else "") +
                    methodArgs.joinToString("") { Descriptor.of(it) } +
                    ")" +
                    if (returnClassIsVoid) "V" else "Ljava/lang/Object;"
        )

        if (!returnClassIsVoid) {
            bytecode.addAstore(codeAttribute.maxLocals)
            //bytecode.add(Bytecode.POP)
        }

        if (method.returnType != CtClass.voidType) {
            when (method.returnType) {
                CtClass.booleanType -> bytecode.addIconst(0)
                CtClass.byteType -> bytecode.addIconst(0)
                CtClass.charType -> bytecode.addIconst(0)
                CtClass.shortType -> bytecode.addIconst(0)
                CtClass.intType -> bytecode.addIconst(0)
                CtClass.longType -> bytecode.addLconst(0)
                CtClass.floatType -> bytecode.addFconst(0.0f)
                CtClass.doubleType -> bytecode.addDconst(0.0)
                else -> bytecode.add(Bytecode.ACONST_NULL)
            }
        }

        bytecode.addReturn(method.returnType)

        method.methodInfo.codeAttribute = bytecode.toCodeAttribute()
        method.methodInfo.codeAttribute.maxLocals = codeAttribute.maxLocals
        method.methodInfo.rebuildStackMapIf6(method.declaringClass.classPool, method.declaringClass.classFile2)

        codeAttribute = method.methodInfo.codeAttribute
        //codeAttribute.maxStack = i + 1

        if (!returnClassIsVoid) {
            val maxLocals = codeAttribute.maxLocals
            val cp: ConstPool = method.methodInfo.constPool

            var va = codeAttribute.getAttribute(
                LocalVariableAttribute.tag
            ) as LocalVariableAttribute?
            if (va == null) {
                va = LocalVariableAttribute(cp)
                codeAttribute.attributes.add(va)
            }

            val desc: String = Descriptor.of(classPool.get("java.lang.Object"))

            va.addEntry(
                0, codeAttribute.codeLength,
                cp.addUtf8Info("result"), cp.addUtf8Info(desc), maxLocals
            )

            codeAttribute.maxLocals += Descriptor.dataSize(desc)
        }

        val originalCode = if (!Modifier.isNative(method.modifiers))
            "${if (Modifier.isStatic(method.modifiers)) "" else "this."}$originalName($$);"
        else "null"

       // MainProcessorProvider.logger.warn(originalCode)
        val returnCode = if (method.returnType != CtClass.voidType) "return (\$r)" else ""

        when (injectMode) {
            InjectMode.InsertBefore -> {
                if(!returnClassIsVoid) {
                    method.insertAfter("""
                        if(result instanceof io.github.rwpp.inject.InterruptResult) {
                            $returnCode ((io.github.rwpp.inject.InterruptResult)result).getResult();
                        } else if(result != kotlin.Unit.INSTANCE) {
                            $originalCode
                            $returnCode result;
                        } else {
                            $returnCode $originalCode
                        }   
                    """.trimIndent()
                    )
                } else {
                    method.insertAfter("""
                        $originalCode
                    """.trimIndent())
                }

            }

            InjectMode.Override -> {
                if (returnClassIsVoid) return
                method.insertAfter(
                    """
                        $returnCode result;
                    """.trimIndent()
                )
            }

            InjectMode.InsertAfter -> {
                if (returnClassIsVoid) return
                method.addLocalVariable("result2", method.returnType)
                method.insertBefore("\$r result2 = $originalCode")
                method.insertAfter(
                    """
                        if(result != kotlin.Unit.INSTANCE) {
                            $returnCode result;
                        } else {
                            $returnCode result2;
                        }
                    """.trimIndent()
                )
            }
        }
    }
}