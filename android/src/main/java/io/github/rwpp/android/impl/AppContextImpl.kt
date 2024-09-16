/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.app.Activity
import android.content.Context
import io.github.rwpp.AppContext
import io.github.rwpp.config.ConfigIO
import okhttp3.OkHttpClient
import org.koin.core.annotation.Single
import org.koin.core.component.get
import java.util.logging.Level
import java.util.logging.Logger

@Single
class AppContextImpl : AppContext {
    private val exitActions = mutableListOf<() -> Unit>()

    init {
        Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
    }


    override fun onExit(action: () -> Unit) {
        exitActions.add(action)
    }

    override fun exit() {
        get<ConfigIO>().saveAllConfig()
        GameEngine.t().bN.apply {
            numLoadsSinceRunningGameOrNormalExit = 0
            numIncompleteLoadAttempts = 0
            save()
        }
        exitActions.forEach { it.invoke() }
        (get<Context>() as Activity).finish()
    }
}