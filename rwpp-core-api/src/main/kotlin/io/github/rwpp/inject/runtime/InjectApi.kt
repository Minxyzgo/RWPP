/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.inject.runtime

import io.github.rwpp.inject.GameLibraries
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.PathType
import javassist.*
import javassist.bytecode.Bytecode
import javassist.bytecode.ConstPool
import javassist.bytecode.Descriptor
import javassist.bytecode.LocalVariableAttribute
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

object InjectApi {
    fun redirectClassName(classMap: ClassMap) {
        GameLibraries.includes.first().classTree.allClasses.forEach { clazz ->
            classMap.entries.forEach {
                clazz.replaceClassName(it.key, it.value)
            }
        }
    }

    fun injectInterface(
        interfaceName: String,
        targetClassName: String,
        newFields: List<Pair<String, String>>, // Pair<fieldName, fieldType>
        accessors: List<Pair<String, String>>, // Pair<propertyName, fieldName>
        hasSelfField: Boolean = false,
    ) {
        Builder.logger?.info("Injecting interface $interfaceName to $targetClassName")

        val classPool = GameLibraries.includes.first().classTree.defPool
        val targetClass = classPool.get(targetClassName)

        targetClass.classFile.addInterface(interfaceName)

        newFields.forEach { (name, type) ->
            val field = CtField.make("$type $name;", targetClass)
            targetClass.addField(field)
            targetClass.addMethod(CtNewMethod.getter("get${name.replaceFirstChar { it.uppercase() }}", field))
            targetClass.addMethod(CtNewMethod.setter("set${name.replaceFirstChar { it.uppercase() }}", field))
        }

        accessors.forEach { (propertyName, fieldName) ->
            val field = targetClass.getField(fieldName)
            targetClass.addMethod(CtNewMethod.getter("get${propertyName.replaceFirstChar { it.uppercase() }}", field))
            targetClass.addMethod(CtNewMethod.setter("set${propertyName.replaceFirstChar { it.uppercase() }}", field))
        }

        if (hasSelfField && targetClass.declaredMethods.none { it.name == "getSelf" }) {
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
        methodDesc: String,
        injectMode: InjectMode,
        injectFunctionPath: String,
        returnClassIsVoid: Boolean,
        pathType: PathType,
    ) {
        Builder.logger?.info("Injecting method $className.$methodName$methodDesc")

        val classPool = GameLibraries.includes.first().classTree.defPool
        val clazz = classPool.get(className)
        val method = clazz.getMethod(methodName, methodDesc)
        val returnCode = if (method.returnType != CtClass.voidType) "return (\$r)" else ""

        if (pathType == PathType.Path) {
            val originalName = "__original__${method.name}"
            val isNative = Modifier.isNative(method.modifiers)
            if (!isNative && clazz.declaredMethods.none {
                it.name == originalName && it.parameterTypes.contentEquals(method.parameterTypes)
            }) {
                val proceed = CtNewMethod.copy(method, originalName, clazz, null)
                proceed.modifiers = Modifier.setProtected(method.modifiers)
                clazz.addMethod(proceed)
            } else {
                method.modifiers = method.modifiers and Modifier.NATIVE.inv()
            }

            var codeAttribute = method.methodInfo.codeAttribute
            val injectClass = injectFunctionPath.substringBeforeLast(".")
            val bytecode = Bytecode(clazz.classFile.constPool)
            bytecode.setMaxStack(codeAttribute.maxStack)
            bytecode.setMaxLocals(codeAttribute.maxLocals)
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
                        Descriptor.getParamDescriptor(methodDesc).removePrefix("(").removeSuffix(")") +
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

            //如果模式为Override，则直接返回结果，否则此处仅是标记方法结束
            bytecode.addReturn(method.returnType)

            method.methodInfo.codeAttribute = bytecode.toCodeAttribute()
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

                //添加变量result，方便下面返回
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

            when (injectMode) {
                InjectMode.InsertBefore -> {
                    if (!returnClassIsVoid) {
                        method.insertAfter(
                            """
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
                        method.insertAfter(
                            """
                            $returnCode $originalCode
                        """.trimIndent()
                        )
                    }

                }

                InjectMode.Override -> {
                }

                InjectMode.InsertAfter -> {
                    if (!returnClassIsVoid) {
                        method.addLocalVariable("result2", method.returnType)
                        method.insertBefore("result2 = $originalCode")
                        method.insertAfter(/* src = */ """
                            $returnCode result2;
                        """.trimIndent()
                        )
                    } else {
                        method.insertBefore(originalCode)
                    }
                }
            }
        } else if (pathType == PathType.JavaCode) {
            val javaCode = injectFunctionPath.let {
                if (Modifier.isStatic(method.modifiers))
                    it.replace("this", "null")
                else it
            }

            Builder.logger?.info("Java code: $javaCode")
            when (injectMode) {
                InjectMode.InsertBefore -> {
                    if (returnClassIsVoid) {
                        method.insertBefore(javaCode)
                    } else {
                        method.addLocalVariable("result", classPool.get("java.lang.Object"))
                        method.insertBefore("""
                            result = $javaCode
                            if(result instanceof io.github.rwpp.inject.InterruptResult) {
                                $returnCode ((io.github.rwpp.inject.InterruptResult)result).getResult();
                                ${if (method.returnType == CtClass.voidType) "return;" else ""}
                            }
                        """.trimIndent()
                        )
                        method.insertAfter("""
                            if(result != kotlin.Unit.INSTANCE) {
                                $returnCode result;
                            }
                        """.trimIndent()
                        )
                    }
                }

                InjectMode.Override -> {
                    method.setBody("""
                        $returnCode $javaCode
                    """.trimIndent()
                    )
                }

                InjectMode.InsertAfter -> {
                    if (returnClassIsVoid) {
                        method.insertAfter(javaCode)
                    } else {
                        method.insertAfter("""
                            $returnCode $javaCode
                        """.trimIndent()
                        )
                    }
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
        targetMethodDesc: String,
        injectFunctionPath: String,
        pathType: PathType
    ) {
        Builder.logger?.info("Redirect $targetClassName.$targetMethodName$targetMethodDesc in $className.$methodName$methodDesc ")

        val classPool = GameLibraries.includes.first().classTree.defPool
        val clazz = classPool.get(className)
        val methodArgs = Descriptor.getParameterTypes(methodDesc, classPool)
        val method = clazz.getDeclaredMethod(methodName, methodArgs)
        val targetMethod = classPool.get(targetClassName).getMethod(targetMethodName, targetMethodDesc)
        val targetMethodReturnType = Descriptor.getReturnType(targetMethodDesc, classPool)
        val targetMethodArgs = Descriptor.getParameterTypes(targetMethodDesc, classPool)

        val newMethodName = "__redirect__${methodName}__$targetMethodName"

        if (pathType == PathType.Path) {
            val newMethod = CtNewMethod.make(
                targetMethodReturnType,
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

            Builder.logger?.warn(newMethod.parameterTypes.joinToString("") { it.name })
            Builder.logger?.warn("i: $i")

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
        }

        method.instrument(object : ExprEditor() {
            override fun edit(m: MethodCall) {
                if(m.signature == targetMethod.signature
                    && m.methodName == targetMethodName
                    && m.className == targetClassName) {
                    Builder.logger?.warn("redirect $className.$methodName")
                    if (pathType == PathType.Path) {
                        m.replace("{  $newMethodName($$); }")
                    } else {
                        val javaCode = injectFunctionPath.let {
                            if (Modifier.isStatic(method.modifiers))
                                it.replace("this", "null")
                            else it
                        }
                        m.replace("{ $javaCode }")
                    }
                }
                super.edit(m)
            }
        })
    }

//    private fun CtBehavior.setBodyModified(src: String) {
//        val cc = declaringClass
//        try {
//            val jv = Javac(cc)
//            val gen = Reflect.get<JvstCodeGen>(jv, "gen")!!
//            gen.setTypeChecker(CustomJvstTypeChecker(cc, cc.classPool, gen))
//            val b = jv.compileBody(this, src)
//            methodInfo.setCodeAttribute(b.toCodeAttribute())
//            methodInfo.setAccessFlags(
//                methodInfo.getAccessFlags()
//                        and AccessFlag.ABSTRACT.inv()
//            )
//            methodInfo.rebuildStackMapIf6(cc.classPool, cc.classFile2)
//            declaringClass.rebuildClassFile()
//        } catch (e: CompileError) {
//            throw CannotCompileException(e)
//        } catch (e: BadBytecode) {
//            throw CannotCompileException(e)
//        }
//    }
//
//    private fun CtBehavior.insertBeforeModified(src: String, rebuild: Boolean = true) {
//        val cc = declaringClass
//        val ca = methodInfo.getCodeAttribute()
//        if (ca == null) throw CannotCompileException("no method body")
//
//        val iterator = ca.iterator()
//        val jv = Javac(cc)
//        val gen = Reflect.get<JvstCodeGen>(jv, "gen")!!
//        gen.setTypeChecker(CustomJvstTypeChecker(cc, cc.classPool, gen))
//        try {
//            val nvars = jv.recordParams(
//                parameterTypes,
//                Modifier.isStatic(modifiers)
//            )
//            jv.recordParamNames(ca, nvars)
//            jv.recordLocalVariables(ca, 0)
//            jv.recordReturnType(getReturnType0(), false)
//            jv.compileStmnt(src)
//            val b = jv.bytecode
//            val stack = b.getMaxStack()
//            val locals = b.getMaxLocals()
//
//            if (stack > ca.maxStack) ca.maxStack = stack
//
//            if (locals > ca.maxLocals) ca.maxLocals = locals
//
//            val pos = iterator.insertEx(b.get())
//            iterator.insert(b.exceptionTable, pos)
//            if (rebuild) methodInfo.rebuildStackMapIf6(cc.classPool, cc.classFile2)
//        } catch (e: NotFoundException) {
//            throw CannotCompileException(e)
//        } catch (e: CompileError) {
//            throw CannotCompileException(e)
//        } catch (e: BadBytecode) {
//            throw CannotCompileException(e)
//        }
//    }
//
//    @Throws(CannotCompileException::class)
//    fun CtBehavior.insertAfterModified(src: String?) {
//        val cc: CtClass = declaringClass
//        val pool: ConstPool = methodInfo.getConstPool()
//        val ca: CodeAttribute = methodInfo.getCodeAttribute()
//
//        val iterator = ca.iterator()
//        val retAddr = ca.maxLocals
//        val b = Bytecode(pool, 0, retAddr + 1)
//        b.setStackDepth(ca.maxStack + 1)
//        val jv = Javac(b, cc)
//        val gen = Reflect.get<JvstCodeGen>(jv, "gen")!!
//        gen.setTypeChecker(CustomJvstTypeChecker(cc, cc.classPool, gen))
//        try {
//            val nvars = jv.recordParams(
//                parameterTypes,
//                Modifier.isStatic(modifiers)
//            )
//            jv.recordParamNames(ca, nvars)
//            val rtype = getReturnType0()
//            val varNo = jv.recordReturnType(rtype, true)
//            jv.recordLocalVariables(ca, 0)
//
//            // finally clause for exceptions
//            var handlerLen = 0
//            var handlerPos = iterator.codeLength
//
//            var adviceLen = 0
//            var advicePos = 0
//            var noReturn = true
//            while (iterator.hasNext()) {
//                val pos = iterator.next()
//                if (pos >= handlerPos) break
//
//                val c = iterator.byteAt(pos)
//                if (c == Opcode.ARETURN || c == Opcode.IRETURN || c == Opcode.FRETURN || c == Opcode.LRETURN || c == Opcode.DRETURN || c == Opcode.RETURN) {
//                    if (noReturn) {
//                        // finally clause for normal termination
//                        adviceLen = Reflect.callNotNull(this, "insertAfterAdvice", args = listOf(b, jv, src, pool, rtype, varNo))
//                        handlerPos = iterator.append(b.get())
//                        iterator.append(b.exceptionTable, handlerPos)
//                        advicePos = iterator.codeLength - adviceLen
//                        handlerLen = advicePos - handlerPos
//                        noReturn = false
//                    }
//
//                    Reflect.callVoid(this, "insertGoto", args = listOf(iterator, handlerPos, pos))
//                    advicePos = iterator.codeLength - adviceLen
//                    handlerPos = advicePos - handlerLen
//                }
//
//            }
//
//            if (noReturn) {
//                handlerPos = iterator.append(b.get())
//                iterator.append(b.exceptionTable, handlerPos)
//            }
//
//            ca.maxStack = b.getMaxStack()
//            ca.maxLocals = b.getMaxLocals()
//            methodInfo.rebuildStackMapIf6(cc.classPool, cc.classFile2)
//        } catch (e: NotFoundException) {
//            throw CannotCompileException(e)
//        } catch (e: CompileError) {
//            throw CannotCompileException(e)
//        } catch (e: BadBytecode) {
//            throw CannotCompileException(e)
//        }
//    }

//    private fun getCheckerMethodName(checker: String): String {
//        val start = checker.lastIndexOf('.')
//        val end = checker.indexOf('(')
//        return checker.substring(start + 1, end)
//    }
//
//    private fun getCheckerMethodDesc(checker: String): String {
//        return checker.substring(checker.indexOf('('))
//    }
//
//    private fun getCheckerWithoutDesc(checker: String): String {
//        val end = checker.indexOf('(')
//        return checker.substring(0, end)
//    }
//
//
//    private fun CtBehavior.getReturnType0(): CtClass {
//        return Descriptor.getReturnType(
//            methodInfo.getDescriptor(),
//            declaringClass.classPool
//        )
//    }
//
//    private class CustomJvstTypeChecker(
//        cc: CtClass,
//        cp: ClassPool,
//        gen: JvstCodeGen,
//    ) : JvstTypeChecker(cc, cp, gen) {
//        init {
//            resolver = CustomMemberResolver(cp)
//        }
//    }
//
//    private class CustomMemberResolver(cp: ClassPool) : MemberResolver(cp) {
//        override fun lookupMethod(
//            clazz: CtClass,
//            currentClass: CtClass?,
//            current: MethodInfo?,
//            methodName: String?,
//            argTypes: IntArray?,
//            argDims: IntArray?,
//            argClassNames: Array<out String?>
//        ): Method? {
//            /*Builder.logger?.warn("lookupMethod ${clazz?.name}.$methodName")
//            val name = clazz.name + "." + methodName
//            val checker = forceCheckers.firstOrNull { getCheckerWithoutDesc(it).also { Builder.logger?.warn("checker $it") } == name }
//            Builder.logger?.warn("checker $checker")*/
//            return /*if (checker != null)
//                Method(clazz, MethodInfo(cacheConstPool, methodName, getCheckerMethodDesc(checker)), 0)
//            else*/ super.lookupMethod(clazz, currentClass, current, methodName, argTypes, argDims, argClassNames)
//        }
//    }
}