/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop

import com.corrodinggames.rts.gameFramework.SettingsEngine
import io.github.rwpp.config.ConfigIO
import java.lang.reflect.Field

abstract class AbstractConfigIO : ConfigIO {
    private val fieldCache = mutableMapOf<String, Field>()

    @Suppress("UNCHECKED_CAST")
    override fun <T> getGameConfig(name: String): T {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        return field.get(GameEngine.B().bQ) as T
    }

    override fun setGameConfig(name: String, value: Any?) {
        val field = fieldCache.getOrPut(name) { SettingsEngine::class.java.getDeclaredField(name) }
        field.set(GameEngine.B().bQ, value)
    }

    override fun saveAllConfig() {
        super.saveAllConfig()
        GameEngine.B().bQ.save()
    }
}