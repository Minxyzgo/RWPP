/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import com.corrodinggames.librocket.scripts.ScriptEngine
import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwpp.ContextController
import io.github.rwpp.config.UIConfig
import io.github.rwpp.config.instance
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.game.Game
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.i18n.parseI18n
import io.github.rwpp.net.Net
import io.github.rwpp.net.registerListeners
import io.github.rwpp.utils.setPropertyFromObject
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.Toml
import okhttp3.OkHttpClient
import java.io.File
import java.lang.reflect.Field
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.system.exitProcess

class GameContextControllerImpl(private val _exit: () -> Unit)
    : ContextController,
    Game by GameImpl(),
    ModManager by ModManagerImpl(),
    Net by NetImpl(),
    ExternalHandler by ExternalHandlerImpl()
{
    private val fieldCache = mutableMapOf<String, Field>()
    private val exitActions = mutableListOf<() -> Unit>()

    init {
        registerListeners()
        runBlocking { parseI18n() }
        Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
        readAllConfig()
        File("mods/units")
            .walk()
            .forEach {
                if (it.name.contains(".network")) {
                    it.delete()
                } else if (it.name.endsWith(".netbak")) {
                    it.renameTo(File(it.absolutePath.removeSuffix(".netbak")))
                }
            }
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
        return field.get(LClass.B().bQ) as T
    }

    override fun setConfig(name: String, value: Any?) {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        field.set(LClass.B().bQ, value)
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> getRWPPConfig(clazz: KClass<T>): T? {
        val name = clazz.qualifiedName
        val file = File("$name.toml")
        if(!file.exists()) file.createNewFile()
        val src = file.readText()
        if(src.isBlank()) return null
        return Toml.decodeFromString(clazz.serializer(), src)
    }

    @OptIn(InternalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    override fun setRWPPConfig(value: Any) {
        val clazz = value::class
        val name = clazz.qualifiedName
        val file = File("$name.toml")
        if(!file.exists()) file.createNewFile()
        file.writeText(Toml.encodeToString(clazz.serializer() as KSerializer<Any>, value))
    }

    override fun saveConfig() {
        LClass.B().bQ.save()
    }

    override fun readAllConfig() {
        super.readAllConfig()
        getRWPPConfig(UIConfig::class)?.apply {
            UIConfig.instance.setPropertyFromObject(this)
        }
    }

    override fun saveAllConfig() {
        super.saveAllConfig()
        setRWPPConfig(UIConfig.instance)
    }

    override fun exit() {
        saveAllConfig()
        LClass.B().bQ.apply {
            numLoadsSinceRunningGameOrNormalExit = 0
            numIncompleteLoadAttempts = 0
            save()
        }
        exitActions.forEach { it.invoke() }
        ScriptEngine.getInstance().root.exit()
        GameImpl.gameThread.stop()

        _exit()
        exitProcess(0)
    }
}