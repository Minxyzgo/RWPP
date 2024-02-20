/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

@Composable
fun AnimatedAlertDialog(
    visible: Boolean,
    enter: EnterTransition = fadeIn() + scaleIn(),
    exit: ExitTransition = shrinkOut() + scaleOut(),
    onDismissRequest: () -> Unit,
    enableDismiss: Boolean = true,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable (modifier: Modifier, dismiss: () -> Unit) -> Unit
) {
    if(visible) {
        var dismiss by remember { mutableStateOf(false) }

        Popup(popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset = IntOffset.Zero
        },
            onDismissRequest = { if(enableDismiss) dismiss = true },
            properties = PopupProperties(focusable = true)) {
            val scrimColor = Color.Black.copy(alpha = 0.32f) //todo configure scrim color in function arguments

            var dialogTrigger by remember { mutableStateOf(true) }
            LaunchedEffect(key1 = dismiss) {
                if(dismiss) {
                    dialogTrigger = false
                    delay(300)
                    onDismissRequest()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrimColor)
                    .pointerInput({ if(enableDismiss) dismiss = true }) {
                        detectTapGestures(onPress = { if(enableDismiss) dismiss = true })
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    dialogTrigger,
                    enter = enter,
                    exit = exit,
                ) {
                    content(
                        modifier
                            .shadow(elevation = 24.dp, shape = shape)
                            .pointerInput({ if(enableDismiss) dismiss = true }) {
                                detectTapGestures(onPress = {
                                    // Workaround to disable clicks on Surface background
                                    // https://github.com/JetBrains/compose-jb/issues/2581
                                })
                            }
                    ) { dismiss = true }
                }
            }
        }
    }
}