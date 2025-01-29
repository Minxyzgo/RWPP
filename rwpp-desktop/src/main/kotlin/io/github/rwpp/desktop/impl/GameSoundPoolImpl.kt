/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import io.github.rwpp.game.audio.GameSound
import io.github.rwpp.game.audio.GameSoundPool
import org.koin.core.annotation.Single
import java.io.InputStream

@Single
class GameSoundPoolImpl : GameSoundPool {
    private val soundMap = mutableMapOf<String, GameSound>()

    override fun newSound(input: InputStream, name: String): GameSound {

        if (soundMap.containsKey(name)) {
            return soundMap[name]!!
        }

        val sound =
            (com.corrodinggames.rts.gameFramework.a.e.c as com.corrodinggames.rts.java.o).f.newSound(
                com.corrodinggames.rts.java.audio.a.a(input, name)
            )

        return object : GameSound {
            override fun play() {
                sound.play()
            }
        }.also { soundMap[name] = it }
    }
}