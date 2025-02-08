/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.github.rwpp.android.impl.PlayerInternal
import io.github.rwpp.game.Player
import org.koin.core.KoinApplication
import java.io.File
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger

lateinit var gameLauncher: ActivityResultLauncher<Intent>
lateinit var fileChooser: ActivityResultLauncher<Intent>
var gameOver = false
val defeatedPlayerSet = CopyOnWriteArraySet<PlayerInternal>()
var questionOption: String? = null
var isSinglePlayerGame: Boolean = false
var isGaming = false
var isReturnToBattleRoom = false
var roomMods = arrayOf<String>()
var bannedUnitList: List<String> = listOf()
val cacheModSize = AtomicInteger(0)
var playerCacheMap = mutableMapOf<PlayerInternal, Player>()
lateinit var koinApplication: KoinApplication
val pickFileActions = mutableListOf<(File) -> Unit>()
var gameLoaded = false

