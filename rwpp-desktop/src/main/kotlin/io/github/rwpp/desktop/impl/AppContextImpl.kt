/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import com.corrodinggames.librocket.scripts.ScriptEngine
import io.github.rwpp.AppContext
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.desktop.GameEngine
import io.github.rwpp.impl.BaseAppContextImpl
import org.koin.core.annotation.Single
import org.koin.core.component.get
import kotlin.system.exitProcess

@Single([AppContext::class])
class AppContextImpl : BaseAppContextImpl() {

    private val exitActions = mutableListOf<() -> Unit>()


    override fun onExit(action: () -> Unit) {
        exitActions.add(action)
    }

    override fun isAndroid(): Boolean = false

    override fun isDesktop(): Boolean = true
    override fun externalStoragePath(path: String): String {
        return System.getProperty("user.dir") + "/$path"
    }


    override fun exit() {
        GameEngine.B().bO
        val configIO: ConfigIO = get()
        GameEngine.B().bQ.apply {
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