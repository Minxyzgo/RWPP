/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop

import io.github.rwpp.desktop.impl.PlayerInternal
import io.github.rwpp.game.Player
import java.util.concurrent.CopyOnWriteArraySet

@Volatile
internal var isGaming = false
@Volatile
internal var gameOver = false
     set(value) {
         field = value
         if (!value) {
             defeatedPlayerSet.clear()
         }
     }
internal var roomMods: Array<String> = arrayOf()
internal var rcnOption: String? = null
internal var isSandboxGame: Boolean = false
internal var bannedUnitList: List<String> = listOf()
internal val defeatedPlayerSet = CopyOnWriteArraySet<PlayerInternal>()