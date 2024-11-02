/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.annotations

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.rwpp.utils.io.GameInputStream
import io.github.rwpp.utils.io.GameOutputStream

class MainProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
//
//        resolver.getAllFiles().forEach {
//            logger.warn(it.fileName)
//        }
//
//        val providerMap = resolver.getSymbolsWithAnnotation(TypeProvider::class.qualifiedName!!)
//            .filterIsInstance<KSClassDeclaration>()
//            .associate { ksClass ->
//                ksClass.simpleName.getShortName() to ksClass.annotations.firstNotNullOf {
//                    if (it.shortName.getShortName() == TypeProvider::class.simpleName)
//                        it.arguments[0].value.toString() + "," to it.arguments[1].value.toString()
//                    else null
//                }
//            }
//
//        val annotatedClasses = resolver.getSymbolsWithAnnotation(GamePacket::class.qualifiedName!!)
//            .filterIsInstance<KSClassDeclaration>()
//
//
//        for (aClass in annotatedClasses) {
//
//            val writerFunSpec = FunSpec.builder("writePacket")
//                .receiver(aClass.toClassName())
//                .addParameter("output", GameOutputStream::class)
//
//            val inputFunSpec = FunSpec.builder("readPacket")
//                .receiver(aClass.toClassName())
//                .addParameter("input", GameInputStream::class)
//                .returns(aClass.toClassName())
//
//            val inputFunBody = StringBuilder()
//            inputFunBody.append("return ${aClass.toClassName().canonicalName}(")
//
//            inputFunBody.appendLine()
//
//            val properties = aClass.getAllProperties()
//            properties.forEach { property ->
//                if (property.simpleName.getShortName() == "type") return@forEach
//
//
//                val typeProvider = providerMap[property.type.resolve().toClassName().simpleName]
//
//                val methodInput = when (val prefix = property.type.resolve().toClassName().simpleName) {
//                    "ByteArray" -> "readNextBytes"
//                    "String" -> "readUTF"
//                    else -> "read${prefix.replaceFirstChar { it.uppercase() }}"
//                }
//
//                val methodOutput = when (val prefix = property.type.resolve().toClassName().simpleName) {
//                    "ByteArray" -> "writeBytesWithSize"
//                    "String" -> "writeUTF"
//                    else -> "write${prefix.replaceFirstChar { it.uppercase() }}"
//                }
//
//                val shortName = property.simpleName.getShortName()
//                writerFunSpec.addStatement(typeProvider?.second?.replace("#", shortName) ?: "output.$methodOutput($shortName)")
//                inputFunBody.appendLine(tab.repeat(2) + (typeProvider?.first?.replace("#", shortName) ?: "input.$methodInput(),"))
//            }
//
//            inputFunBody.append("$tab)")
//
//            inputFunSpec.addCode(inputFunBody.toString())
//
//            val toBytesFunSpec = FunSpec.builder("toBytes")
//                .receiver(aClass.toClassName())
//                .returns(ByteArray::class)
//                .addCode(CodeBlock.of("""
//                    val byteArrayOutput = java.io.ByteArrayOutputStream()
//                    val gameOutput = io.github.rwpp.utils.io.GameOutputStream(
//                        java.io.DataOutputStream(byteArrayOutput)
//                    )
//                    gameOutput.use { writePacket(it) }
//
//                    val bytes = byteArrayOutput.toByteArray()
//                    byteArrayOutput.close()
//
//                    return bytes
//                """.trimIndent()))
//                .build()
//
//            scriptSpec.addFunction(writerFunSpec.build())
//            scriptSpec.addFunction(inputFunSpec.build())
//            scriptSpec.addFunction(toBytesFunSpec)
//        }

        return emptyList()
    }

    override fun finish() {
        scriptSpec.build().writeTo(codeGenerator,
            Dependencies(
                false
            )
        )
    }

    private val packageName = "io.github.rwpp.generated"
    private val tab = "  "
    private val scriptSpec = FileSpec.scriptBuilder("GeneratedPackets", packageName).apply {
        indent(tab)
    }
}