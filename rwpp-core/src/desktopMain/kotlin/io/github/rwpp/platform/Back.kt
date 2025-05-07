/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.events.KeyboardEvent
import io.github.rwpp.event.onDispose

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
//    BackHandler {
//        if (enabled) onBack()
//    }
    GlobalEventChannel.filter(KeyboardEvent::class).onDispose {
        subscribeAlways {
            //ESC
            if (it.keyCode == 0x1B ) {
                onBack()
                it.intercept()
            }
        }
    }
}