/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.external

import io.github.rwpp.projectVersion
import kotlinx.serialization.Serializable

@Serializable
data class ExtensionConfig(
    @Deprecated("Use id instead")
    private val name: String = "",

    /**
     * The unique identifier of the extension.
     */
    val id: String = name,

    /**
     * The version of the extension.
     */
    val version: String = "1.0.0",

    /**
     * The author of the extension.
     */
    val author: String = "",

    /**
     * The display name of the extension.
     */
    val displayName: String = id,

    /**
     * The icon path of the extension.
     */
    val icon: String = "",

    /**
     * The description of the extension.
     */
    val description: String = "",

    /**
     * Whether the extension has some resources.
     */
    val hasResource: Boolean = false,

    /**
     * Whether the extension has some inject info.
     * This is used to determine whether the extension needs to be injected into the game.
     */
    val hasInjectInfo: Boolean = false,

    /**
     * The minimum game version that the extension can run on.
     */
    val minGameVersion: String = projectVersion,

    /**
     * The dependencies of the extension.
     */
    val dependencies: List<String> = emptyList(),

    /**
     * The supported platforms of the extension.
     *
     * If it is empty, it means that the extension can run on all platforms.
     *
     * Supported platforms: Android, Desktop
     */
    val supportedPlatforms: List<String> = emptyList(),
)
