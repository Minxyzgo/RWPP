package io.github.rwpp.desktop.impl

import com.corrodinggames.librocket.scripts.ScriptEngine
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
        return field.get(LClass.B().bQ) as T
    }

    override fun setConfig(name: String, value: Any?) {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        field.set(LClass.B().bQ, value)
    }

    override fun saveConfig() {
        LClass.B().bQ.save()
    }

    override fun exit() {
        LClass.B().bQ.apply {
            numLoadsSinceRunningGameOrNormalExit = 0
            numIncompleteLoadAttempts = 0
            save()
        }
        ScriptEngine.getInstance().root.exit()
        GameImpl.gameThread.stop()
        _exit()
    }
}