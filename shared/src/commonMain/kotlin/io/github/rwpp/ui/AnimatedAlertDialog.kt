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
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.awt.event.KeyEvent

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
    var dialogTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if(visible) {
            delay(10)
            dialogTrigger = true
        }
    }

    if(visible) {
        var dismiss by remember { mutableStateOf(false) }
        val flow = remember { MutableStateFlow<Any?>(null) }

        Popup(
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset = IntOffset.Zero
            },
            focusable = true,
            onDismissRequest = { if(enableDismiss) dismiss = true },
            onKeyEvent = {
                if(it.type == KeyEventType.KeyDown && it.awtEventOrNull?.keyCode == KeyEvent.VK_ESCAPE) {
                    dialogTrigger = false
                    if(enableDismiss) dismiss = true
                    true
                } else {
                    false
                }
            },
        ) {
            val scrimColor = Color.Black.copy(alpha = 0.32f) //todo configure scrim color in function arguments

            LaunchedEffect(Unit) {
                flow.collectLatest {
                    if(it != null) {
                        dismiss = true
                    }
                }
            }

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
                    ) { runBlocking { flow.emit(Unit) } }
                }
            }
        }
    }
}