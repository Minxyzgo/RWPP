/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.github.rwpp.AppContext
import io.github.rwpp.config.ConfigModule
import io.github.rwpp.game.team.TeamModeModule
import org.koin.android.ext.koin.androidLogger
import org.koin.compose.KoinApplication
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.ksp.generated.module
import java.util.concurrent.atomic.AtomicInteger

lateinit var gameLauncher: ActivityResultLauncher<Intent>
var questionOption: String? = null
var isSandboxGame: Boolean = false
var isGaming = false
var isReturnToBattleRoom = false
var roomMods = arrayOf<String>()
var bannedUnitList: List<String> = listOf()
val cacheModSize = AtomicInteger(0)
lateinit var koinApplication: KoinApplication

