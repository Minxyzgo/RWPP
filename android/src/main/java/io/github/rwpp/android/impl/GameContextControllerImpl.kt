/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Context
import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwpp.ContextController
import io.github.rwpp.android.MainActivity
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.game.Game
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.net.Net
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.Toml
import okhttp3.OkHttpClient
import java.lang.reflect.Field
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass

class GameContextControllerImpl(private val _exit: () -> Unit)
    : ContextController,
    Game by GameImpl(),
    ModManager by ModManagerImpl(),
    Net by NetImpl(),
    ExternalHandler by ExternalHandlerImpl()
{
    private val fieldCache = mutableMapOf<String, Field>()
    private val exitActions = mutableListOf<() -> Unit>()
    override val client: OkHttpClient = OkHttpClient()

    init {
        Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
    }

    override fun i18n(str: String, vararg args: Any?): String {
        return com.corrodinggames.rts.gameFramework.h.a.a(str, args)
    }

    override fun onExit(action: () -> Unit) {
        exitActions.add(action)
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

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> getRWPPConfig(clazz: KClass<T>): T? {
        val name = clazz.qualifiedName
        val preferences = MainActivity.instance.getSharedPreferences(name, Context.MODE_PRIVATE)
        val src = preferences.getString("src", "")
        if(src.isNullOrBlank()) return null
        return Toml.decodeFromString(clazz.serializer(), src)
    }

    @OptIn(InternalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    override fun setRWPPConfig(value: Any) {
        val clazz = value::class
        val name = clazz.qualifiedName
        val preferences = MainActivity.instance.getSharedPreferences(name, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("src", Toml.encodeToString(clazz.serializer() as KSerializer<Any>, value))
        editor.commit()
    }

    override fun saveConfig() {
        GameEngine.t().bN.save()
    }

    override fun exit() {
        saveAllConfig()
        GameEngine.t().bN.apply {
            numLoadsSinceRunningGameOrNormalExit = 0
            numIncompleteLoadAttempts = 0
            save()
        }
        exitActions.forEach { it.invoke() }
        _exit()
    }
}