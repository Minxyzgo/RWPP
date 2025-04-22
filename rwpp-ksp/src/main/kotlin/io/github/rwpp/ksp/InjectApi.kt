/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.rwpp.ksp

import io.github.rwpp.inject.InjectMode
import javassist.*
import javassist.bytecode.*
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

internal object InjectApi {

    fun redirectClassName(classMap: ClassMap) {
        GameLibraries.includes.first().classTree.allClasses.forEach {
            it.replaceClassName(classMap)
        }
    }

    fun injectInterface(
        interfaceName: String,
        targetClassName: String,
        newFields: List<Pair<String, String>>, // Pair<fieldName, fieldType>
        accessors: List<Pair<String, String>>, // Pair<propertyName, fieldName>
        hasSelfField: Boolean = false,
    ) {
        val classPool = GameLibraries.includes.first().classTree.defPool
        val targetClass = classPool.get(targetClassName)

        targetClass.classFile.addInterface(interfaceName)

        newFields.forEach { (name, type) ->
            val field = CtField.make("${transformClass(type)} $name;", targetClass)
            targetClass.addField(field)
            targetClass.addMethod(CtNewMethod.getter("get${name.replaceFirstChar { it.uppercase() }}", field))
            targetClass.addMethod(CtNewMethod.setter("set${name.replaceFirstChar { it.uppercase() }}", field))
        }

        accessors.forEach { (propertyName, fieldName) ->
            val field = targetClass.getField(fieldName)
            targetClass.addMethod(CtNewMethod.getter("get${propertyName.replaceFirstChar { it.uppercase() }}", field))
            targetClass.addMethod(CtNewMethod.setter("set${propertyName.replaceFirstChar { it.uppercase() }}", field))
        }

        if (hasSelfField) {
            targetClass.addMethod(CtNewMethod.make("""
                public $targetClassName getSelf() {
                    return this;
                }
            """.trimIndent(), targetClass))
        }
    }

    fun injectMethod(
        className: String,
        hasReceiver: Boolean,
        methodName: String,
        methodArgs: List<String>,
        injectMode: InjectMode,
        returnClassIsVoid: Boolean,
        injectFunctionPath: String,
        desc: String
    ) {
        @Suppress("NAME_SHADOWING")
        val methodArgs = transformClasses(methodArgs)

        val classPool = GameLibraries.includes.first().classTree.defPool
        val clazz = classPool.get(className)

        val method = if (desc.isNotBlank())
            clazz.getMethod(methodName, desc)
        else clazz.getDeclaredMethod(methodName, methodArgs.map(classPool::get).toTypedArray())

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
                    if (returnClassIsVoid) "V"
                    else if (injectMode == InjectMode.Override) Descriptor.of(method.returnType)
                    else "Ljava/lang/Object;"
        )

        if (!returnClassIsVoid && injectMode != InjectMode.Override) {
            bytecode.addAstore(codeAttribute.maxLocals)
            //bytecode.add(Bytecode.POP)
        }

        //此处并非真实返回值，只是给javassist标记为此处方法已经结束
        //若在Override模式下，则应直接返回结果
        if (method.returnType != CtClass.voidType && injectMode != InjectMode.Override) {
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

        if (!returnClassIsVoid && injectMode != InjectMode.Override) {
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
                        $returnCode $originalCode
                    """.trimIndent())
                }

            }

            InjectMode.Override -> {
//                if (returnClassIsVoid) return
//                method.insertAfter(
//                    """
//                        $returnCode result;
//                    """.trimIndent()
//                )
            }

            InjectMode.InsertAfter -> {
                method.addLocalVariable("result2", method.returnType)
                method.insertBefore("\$r result2 = $originalCode")
                if (returnClassIsVoid) {
                    method.insertAfter(
                        """
                        if(result != kotlin.Unit.INSTANCE) {
                            $returnCode result;
                        } else {
                            $returnCode result2;
                        }
                    """.trimIndent()
                    )
                } else {
                    method.insertAfter(
                        """
                        $returnCode result2;
                        """.trimIndent())
                }
            }
        }
    }

    fun redirect(
        className: String,
        hasReceiver: Boolean,
        methodName: String,
        methodDesc: String,
        targetClassName: String,
        targetMethodName: String,
        targetMethodArgs: List<String>,
        returnType: String,
        injectFunctionPath: String,
    ) {

        val classPool = GameLibraries.includes.first().classTree.defPool
        val clazz = classPool.get(className)
        val methodArgs = Descriptor.getParameterTypes(methodDesc, classPool)

        @Suppress("NAME_SHADOWING")
        val targetMethodArgs = transformClasses(targetMethodArgs).map { classPool.get(it) }.toTypedArray()
        val targetMethodReturnType = classPool[transformClasses(listOf(returnType)).first()]

        MainProcessor.logger.warn("targetMethodArgs: ${targetMethodArgs.joinToString(",")}")
        val method = clazz.getDeclaredMethod(methodName, methodArgs)
        val targetMethod = classPool.get(targetClassName).getDeclaredMethod(targetMethodName, targetMethodArgs)

        val newMethodName = "__redirect__${methodName}__$targetMethodName"

        val newMethod = CtNewMethod.make(targetMethodReturnType,
            newMethodName,
            targetMethodArgs,
            arrayOf(),
            "{ }",
            clazz
        )

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

        for (param in newMethod.parameterTypes) {
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

        MainProcessor.logger.warn(newMethod.parameterTypes.joinToString("") { it.name })
        MainProcessor.logger.warn("i: $i")

        bytecode.addInvokevirtual(
            injectClass,
            injectFunctionPath.substringAfterLast("."),
            "(" +
                    (if (hasReceiver) Descriptor.of(clazz) else "") +
                    targetMethodArgs.joinToString("") { Descriptor.of(it) } +
                    ")" +
                    Descriptor.of(targetMethodReturnType)
        )

        bytecode.addReturn(targetMethodReturnType)

        newMethod.methodInfo.codeAttribute = bytecode.toCodeAttribute()
        newMethod.methodInfo.codeAttribute.maxLocals = i
        newMethod.methodInfo.rebuildStackMapIf6(clazz.classPool, clazz.classFile2)

        clazz.addMethod(newMethod)

        method.instrument(object : ExprEditor() {
            override fun edit(m: MethodCall) {
                if(m.signature == targetMethod.signature
                    && m.methodName == targetMethodName
                    && m.className == targetClassName) {
                    MainProcessor.logger.warn("redirect $className.$methodName")
                    m.replace("{  $newMethodName($$); }")
                }
                super.edit(m)
            }
        })
    }

    private fun transformClasses(methodArgs: List<String>): List<String> {
        @Suppress("NAME_SHADOWING")
        val methodArgs = methodArgs.toMutableList()

        for ((i, v) in methodArgs.withIndex()) {
            methodArgs[i] = transformClass(v)
        }

        return methodArgs
    }
    
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
}

