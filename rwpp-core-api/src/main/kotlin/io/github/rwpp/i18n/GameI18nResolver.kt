/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */
package io.github.rwpp.i18n

import io.github.rwpp.core.Initialization
import org.koin.core.component.KoinComponent

interface GameI18nResolver : Initialization, KoinComponent {
    /**
     * Read RW translations (not RWPP)
     */
    fun i18n(str: String, vararg args: Any?): String
}
