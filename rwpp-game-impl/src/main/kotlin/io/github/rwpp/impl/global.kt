/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl

import java.util.concurrent.CopyOnWriteArraySet

@Volatile
var isGaming = false
@Volatile
var gameOver = false
    set(value) {
        field = value
        if (!value) {
            defeatedPlayerSet.clear()
        }
    }
var roomMods: Array<String> = arrayOf()
var rcnOption: String? = null
var singlePlayer: Boolean = false
var bannedUnitList: List<String> = listOf()
val defeatedPlayerSet = CopyOnWriteArraySet<PlayerInternal>()