/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dalvik.system.BaseDexClassLoader
import dalvik.system.PathClassLoader
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.android.impl.PlayerInternal
import io.github.rwpp.game.Player
import io.github.rwpp.utils.Reflect
import io.github.rwpp.widget.loadingMessage
import kotlinx.coroutines.channels.Channel
import org.koin.core.KoinApplication
import java.io.File
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger
import kotlin.getValue

var _gameSpeed = 1f

@Volatile
var init = false
var requireReloadingLib = false
var message by mutableStateOf("loading")
lateinit var gameLauncher: ActivityResultLauncher<Intent>
lateinit var fileChooser: ActivityResultLauncher<Intent>
val mainThreadChannel = Channel<() -> Unit>(Channel.UNLIMITED)
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
lateinit var dexFolder: File
val pickFileActions = mutableListOf<(File) -> Unit>()
var gameLoaded = false
val uiHandler by lazy {
    Handler(Looper.getMainLooper())
}
val loadingThread by lazy {
    Thread({
        while (true) {
            val msg = GameEngine.t()?.dF
            if (msg != null) {
                message = msg
                loadingMessage = msg
            }
        }
    }, "LoadingContextThread").apply { start() }
}

private var libClassLoader: PathClassLoader? = null

fun loadDex(context: Context) {
    val pathClassLoader = context.classLoader as PathClassLoader
    val dexPath = "${dexFolder.absolutePath}/classes.dex"

    val cl = libClassLoader ?: PathClassLoader(dexPath, pathClassLoader)
    val systemPathList = Reflect.reifiedGet<BaseDexClassLoader, Any>(pathClassLoader, "pathList")!!
    val pathList = Reflect.reifiedGet<BaseDexClassLoader, Any>(cl, "pathList")!!

    val systemDexElements = Reflect.get<Array<*>>(systemPathList, "dexElements")!!
    val dexElements = Reflect.get<Array<*>>(pathList, "dexElements")!!

    val elements = java.lang.reflect.Array
        .newInstance(
            dexElements.javaClass.componentType,
            systemDexElements.size + dexElements.size
        )

    systemDexElements.toMutableList()
        .apply { addAll(dexElements) }
        .forEachIndexed { i, v -> java.lang.reflect.Array.set(elements, i, v) }

    libClassLoader = cl
    Reflect.set(systemPathList, "dexElements", elements)
}