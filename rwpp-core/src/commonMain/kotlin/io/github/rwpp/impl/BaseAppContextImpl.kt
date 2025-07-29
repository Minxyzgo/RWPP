/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl

import io.github.rwpp.AppContext
import io.github.rwpp.appKoin
import io.github.rwpp.core.Initialization
import io.github.rwpp.core.Logic
import io.github.rwpp.scripts.Scripts
import io.github.rwpp.ui.IUserInterface
import io.github.rwpp.ui.UI

abstract class BaseAppContextImpl : AppContext {
    override fun init() {
        Logic.init()
        Scripts.init()
        UI.init()
        appKoin.declare(UI, secondaryTypes = listOf(IUserInterface::class))
        getKoin().getAll<Initialization>().forEach(Initialization::init)
    }
}