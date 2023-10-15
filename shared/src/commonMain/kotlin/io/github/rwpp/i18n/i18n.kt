/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.i18n

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Resource
import org.jetbrains.compose.resources.resource
import java.util.Locale

@OptIn(ExperimentalResourceApi::class)
private var i18nResource: Resource? = null

@Composable
@OptIn(ExperimentalResourceApi::class)
fun parseI18n() {
    i18nResource = i18nResource ?: resource(Locale.getDefault().language)
}