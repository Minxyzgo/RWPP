/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp

import io.github.rwpp.config.ConfigIO
import io.github.rwpp.core.Initialization
import io.github.rwpp.core.Logic
import io.github.rwpp.core.UI
import io.github.rwpp.scripts.Scripts
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface AppContext : KoinComponent {
    fun init() {
        get<ConfigIO>().readAllConfig()
        Logic.init()
        Scripts.init()
        UI.init()
        getKoin().getAll<Initialization>().forEach(Initialization::init)
    }


    fun onExit(action: () -> Unit)

    fun exit()
}