/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp

import io.github.rwpp.config.ConfigIO
import io.github.rwpp.core.Logic
import io.github.rwpp.i18n.parseI18n
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface AppContext : KoinComponent {
    fun init() {
        runBlocking { parseI18n() }
        get<ConfigIO>().readAllConfig()
        Logic
    }


    fun onExit(action: () -> Unit)

    fun exit()
}