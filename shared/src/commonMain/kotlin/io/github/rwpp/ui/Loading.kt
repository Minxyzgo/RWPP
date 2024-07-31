/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.rwpp.ui.v2.LineSpinFadeLoaderIndicator
import kotlinx.coroutines.launch

val loadingMessage = mutableStateOf("")

@JvmInline
value class LoadingContext(val message: (str: String) -> Unit)

@Composable
fun LoadingView(
    visible: Boolean,
    onLoaded: () -> Unit,
    enableAnimation: Boolean = true,
    cancellable: Boolean = false,
    loadContent: suspend LoadingContext.() -> Boolean
) {

    val scope = rememberCoroutineScope()

    AnimatedAlertDialog(visible, onDismissRequest = onLoaded, enableDismiss = cancellable) { dismiss ->

        val message by remember { loadingMessage }
        var cancel by remember { mutableStateOf(false) }

        BorderCard(
            modifier = Modifier.size(500.dp),
        ) {
            LaunchedEffect(Unit) {
                scope.launch {
                    if(loadContent(LoadingContext { loadingMessage.value = it })) {
                        loadingMessage.value = ""
                        dismiss()
                    } else cancel = true
                }
            }

            if(cancellable || cancel) ExitButton(dismiss)

            Column(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(enableAnimation && !cancel) LineSpinFadeLoaderIndicator(Color(151, 188, 98))
                Text(message, modifier = Modifier.padding(20.dp).offset(y = 50.dp), color = Color.White)
            }
        }
    }
}