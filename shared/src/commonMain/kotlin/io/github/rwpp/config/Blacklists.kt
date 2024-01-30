/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.config

import io.github.rwpp.ContextController
import kotlinx.serialization.Serializable

@Serializable
private class BlacklistsSerializer(
    val nameArray: Array<String>,
    val urlArray: Array<String>
)

object Blacklists : ConfigSerializer {
    lateinit var blacklists: MutableList<Blacklist>

    override fun readFromContext(context: ContextController) {
        val se = context.getRWPPConfig(BlacklistsSerializer::class)
        if(se == null) {
            blacklists = mutableListOf()
            return
        }
        blacklists = mutableListOf<Blacklist>().apply {
            se.nameArray.forEachIndexed { i, name ->
                add(Blacklist(name, se.urlArray[i]))
            }
        }
    }

    override fun writeFromContext(context: ContextController) {
        context.setRWPPConfig(
            BlacklistsSerializer(
                blacklists.map { it.name }.toTypedArray(),
                blacklists.map { it.uuid }.toTypedArray()
            )
        )
    }
}