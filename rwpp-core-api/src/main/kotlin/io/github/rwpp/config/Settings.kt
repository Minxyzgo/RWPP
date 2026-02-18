/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.config

import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single
@Serializable
data class Settings(
    /**
     * Decide whether to allow game in full screen (Only PC)
     */
    var isFullscreen: Boolean = true,
    var showWelcomeMessage: Boolean? = null,
    var ignoreVersion: String? = null,
    var autoCheckUpdate: Boolean = true,
    var enhancedReinforceTroops: Boolean = false,
    var backgroundImagePath: String? = null,
    var selectedTheme: String? = null,
    var backgroundTransparency: Float = 0.7f,
    var backgroundImageTransparency: Float = 1f,
    var showBuildingAttackRange: Boolean = false,
    var showExtraButton: Boolean = false,
    /** @see unitAttackRangeTypes */
    var showAttackRangeUnit: String = "Never",
    var enableAnimations: Boolean = true,
    var maxDisplayUnitGroupCount: Int = 7,
    var displayUnitGroupXOffset: Int = 0,
    var changeGameTheme: Boolean = false,
    var showUnitTargetLine: Boolean = false,
    var improvedHealthBar: Boolean = false,
    var mouseMoveView: Boolean = false,
    //var pathfindingOptimization: Boolean = false,
    var boldText: Boolean = false,
    var renderingBackend: String = "OpenGL", // Default, Software, OpenGL
    var forceEnglish: Boolean = false,
) : Config {
    companion object {
        val unitAttackRangeTypes = listOf("Never", "Land", "Air", "All")
    }
}
