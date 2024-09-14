/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import com.corrodinggames.librocket.scripts.ScriptEngine
import io.github.rwpp.AppContext
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.i18n.parseI18n
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.koin.core.annotation.Single
import org.koin.core.component.get
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

@Single
class AppContextImpl : AppContext {

    private val exitActions = mutableListOf<() -> Unit>()


    override fun onExit(action: () -> Unit) {
        exitActions.add(action)
    }


    override fun exit() {
        val configIO: ConfigIO = get()
        LClass.B().bQ.apply {
            numLoadsSinceRunningGameOrNormalExit = 0
            numIncompleteLoadAttempts = 0
        }
        configIO.saveAllConfig()
        exitActions.forEach { it.invoke() }
        ScriptEngine.getInstance().root.exit()
        GameImpl.gameThread.stop()
        exitProcess(0)
    }
}