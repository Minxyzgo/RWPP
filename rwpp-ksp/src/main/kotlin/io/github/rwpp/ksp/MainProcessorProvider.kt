/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import io.github.rwpp.rwpp_ksp.BuildConfig

class MainProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        Builder.libDir = BuildConfig.DEFAULT_LIB_DIR
        Builder.outputDir = environment.options["outputDir"].toString()
        GameLibraries.includes.add(GameLibraries.valueOf(environment.options["lib"].toString()))
        environment.logger.warn("RWPP-KSP: libs: ${GameLibraries.includes.joinToString(",")}")
        return MainProcessor(environment.logger)
    }
}