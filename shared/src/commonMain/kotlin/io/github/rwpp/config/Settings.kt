/*
 * Copyright 2023-2024 RWPP contributors
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
     *
     * Since windowing support is incomplete and extremely expensive, it defaults to true
     */
    var isFullscreen: Boolean = true,
    var showWelcomeMessage: Boolean? = null,
) : Config
