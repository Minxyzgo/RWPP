/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.comp

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * This module is used to define the components of the game.
 */
@Module
@ComponentScan("io.github.rwpp.game.comp")
class CompModule