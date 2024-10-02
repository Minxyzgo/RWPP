/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.i18n

import androidx.compose.runtime.Composable
import io.github.rwpp.shared.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.asTomlLiteral
import net.peanuuutz.tomlkt.asTomlTable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject
import java.text.MessageFormat
import java.util.*

internal lateinit var i18nTable: TomlTable
private val cacheMap = mutableMapOf<String, String>()

@OptIn(ExperimentalResourceApi::class)
suspend fun parseI18n() {
    i18nTable = Toml.parseToTomlTable(withContext(Dispatchers.IO) {
        runCatching { Res.readBytes("files/bundle_${Locale.getDefault().language}.toml") }.getOrNull() ?: Res.readBytes("files/bundle_en.toml")
    }.decodeToString().replace("\r", "\n"))
}

@Composable
fun readI18n(path: String, i18nType: I18nType = I18nType.RWPP, vararg arg: String): String {
    if (i18nType == I18nType.RWPP) {
        cacheMap[path]?.let { return MessageFormat.format(it, *arg) }
        val strArray = path.split(".")
        val iterator = strArray.iterator()
        var table: TomlTable = i18nTable
        while(iterator.hasNext()) {
            val next = iterator.next()
            if(!iterator.hasNext()) {
                return table[next]!!.asTomlLiteral().content.let {
                    cacheMap[path] = it
                    MessageFormat.format(it, *arg)
                }
            } else table = table[next]!!.asTomlTable()
        }
    } else if (i18nType == I18nType.RW) {
        val resolver: GameI18nResolver = koinInject()
        return resolver.i18n(path, *arg)
    }

    return "null"
}