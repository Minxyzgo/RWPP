/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.i18n

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.asTomlLiteral
import net.peanuuutz.tomlkt.asTomlTable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource
import java.util.*

internal lateinit var i18nTable: TomlTable

@OptIn(ExperimentalResourceApi::class)
suspend fun parseI18n() {
    val res = runCatching { resource("bundles/bundle_${Locale.getDefault().language}.toml") }
        .getOrNull() ?: resource("bundles/bundle_en.toml")
    i18nTable = Toml.parseToTomlTable(withContext(Dispatchers.IO) {
        res.readBytes()
    }.decodeToString().replace("\r", "\n"))
}

fun readI18n(path: String): String {
    val strArray = path.split(".")
    val iter = strArray.iterator()
    var table: TomlTable = i18nTable
    while(iter.hasNext()) {
        val next = iter.next()
        println(next)
        if(!iter.hasNext()) {
            return table[next]!!.asTomlLiteral().content
        } else table = table[next]!!.asTomlTable()
    }

    return "null"
}