/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.os.Environment
import io.github.rwpp.AppContext
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.graphics.GL
import io.github.rwpp.impl.BaseAppContextImpl
import okhttp3.OkHttpClient
import org.koin.core.annotation.Single
import org.koin.core.component.get
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

@Single([AppContext::class])
class AppContextImpl : BaseAppContextImpl() {
    private val exitActions = mutableListOf<() -> Unit>()

    init {
        Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
    }

    override fun onExit(action: () -> Unit) {
        exitActions.add(action)
    }

    override fun isAndroid(): Boolean = true

    override fun isDesktop(): Boolean = false
    override fun externalStoragePath(path: String): String {
        return Environment.getExternalStorageDirectory().absolutePath + "/rustedWarfare/$path"
    }

    override fun init() {
        super.init()
        GL.gameCanvas = GameCanvasImpl()
    }

    override fun exit() {
        get<ConfigIO>().saveAllConfig()
        GameEngine.t().bN.apply {
            numLoadsSinceRunningGameOrNormalExit = 0
            numIncompleteLoadAttempts = 0
            save()
        }
        exitActions.forEach { it.invoke() }
        exitProcess(0)
    }
}