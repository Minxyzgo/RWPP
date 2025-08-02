/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.runtime.Composable

abstract class ComposeWidget : Widget() {
    @Composable
    abstract fun Content()
}

fun composeWidget(content: @Composable () -> Unit): ComposeWidget {
    return object : ComposeWidget() {
        @Composable
        override fun Content() {
            content()
        }
    }
}