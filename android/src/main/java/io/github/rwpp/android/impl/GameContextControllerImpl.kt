package io.github.rwpp.android.impl

import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwpp.ContextController
import io.github.rwpp.game.Game
import io.github.rwpp.game.mod.ModManager
import okhttp3.OkHttpClient
import java.lang.reflect.Field
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.jvm.jvmName

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
        return field.get(KClass.t().bN) as T
    }

    override fun setConfig(name: String, value: Any?) {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        field.set(KClass.t().bN, value)
    }

    override fun saveConfig() {
        KClass.t().bN.save()
    }

    override fun exit() {
        KClass.t().bN.apply {
            numLoadsSinceRunningGameOrNormalExit = 0
            numIncompleteLoadAttempts = 0
            save()
        }
        _exit()
    }
}