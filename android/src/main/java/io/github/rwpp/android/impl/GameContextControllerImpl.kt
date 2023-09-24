/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwpp.ContextController
import io.github.rwpp.game.Game
import io.github.rwpp.game.mod.ModManager
import okhttp3.OkHttpClient
import java.lang.reflect.Field
import java.util.logging.Level
import java.util.logging.Logger

class GameContextControllerImpl(private val _exit: () -> Unit)
    : ContextController, Game by GameImpl(), ModManager by ModManagerImpl() {
    private val fieldCache = mutableMapOf<String, Field>()
    override val client: OkHttpClient = OkHttpClient()

    init {
        Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
    }

    override fun i18n(str: String, vararg args: Any?): String {
        return com.corrodinggames.rts.gameFramework.h.a.a(str, args)
    }

    @Suppress("unchecked_cast")
    override fun <T> getConfig(name: String): T {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        return field.get(GameEngine.t().bN) as T
    }

    override fun setConfig(name: String, value: Any?) {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        field.set(GameEngine.t().bN, value)
    }

    override fun saveConfig() {
        GameEngine.t().bN.save()
    }

    override fun exit() {
        GameEngine.t().bN.apply {
            numLoadsSinceRunningGameOrNormalExit = 0
            numIncompleteLoadAttempts = 0
            save()
        }
        _exit()
    }
}