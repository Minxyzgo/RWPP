/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal const val ANIMATION_TIME = 500L
internal const val DIALOG_BUILD_TIME = 300L

// Inspired by https://medium.com/tech-takeaways/ios-like-modal-view-dialog-animation-in-jetpack-compose-fac5778969af

@Composable
internal fun AnimatedModalBottomSheetTransition(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(ANIMATION_TIME.toInt()),
            initialOffsetY = { fullHeight -> fullHeight }
        ),
        exit = slideOutVertically(
            animationSpec = tween(ANIMATION_TIME.toInt()),
            targetOffsetY = { fullHeight -> fullHeight }
        ),
        content = content
    )
}

@Composable
internal fun AnimatedScaleInTransition(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = tween(ANIMATION_TIME.toInt())
        ),
        exit = scaleOut(
            animationSpec = tween(ANIMATION_TIME.toInt())
        ),
        content = content
    )
}

@Composable
fun AnimatedTransitionDialog(
    onDismissRequest: () -> Unit,
    enableDismiss: Boolean,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable (AnimatedTransitionDialogHelper) -> Unit
) {
    val onDismissSharedFlow: MutableSharedFlow<Any> = remember { MutableSharedFlow() }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val animateTrigger = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        launch {
            delay(DIALOG_BUILD_TIME)
            animateTrigger.value = true
        }
        launch {
            onDismissSharedFlow.asSharedFlow().collectLatest {
                startDismissWithExitAnimation(animateTrigger, onDismissRequest)
            }
        }
    }

//    Popup(alignment = Alignment.Center,
//        onDismissRequest = {
//            if (enableDismiss) {
//                coroutineScope.launch {
//                    startDismissWithExitAnimation(animateTrigger, onDismissRequest)
//                }
//            }
//        }
//    ) {
//        Box(
//            contentAlignment = contentAlignment,
//            modifier = Modifier.fillMaxSize()
//        ) {
//            AnimatedScaleInTransition(visible = animateTrigger.value) {
//
//                content(AnimatedTransitionDialogHelper(coroutineScope, onDismissSharedFlow))
//
//            }
//        }
//    }

    Dialog(
        onDismissRequest = {
            if (enableDismiss) {
                coroutineScope.launch {
                    startDismissWithExitAnimation(animateTrigger, onDismissRequest)
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            contentAlignment = contentAlignment,
        ) {
            AnimatedScaleInTransition(visible = animateTrigger.value) {
                content(AnimatedTransitionDialogHelper(coroutineScope, onDismissSharedFlow))
            }
        }
    }
}


class AnimatedTransitionDialogHelper(
    private val coroutineScope: CoroutineScope,
    private val onDismissFlow: MutableSharedFlow<Any>
) {

    fun triggerAnimatedDismiss() {
        coroutineScope.launch {
            onDismissFlow.emit(Any())
        }
    }
}

suspend fun startDismissWithExitAnimation(
    animateTrigger: MutableState<Boolean>,
    onDismissRequest: () -> Unit
) {
    animateTrigger.value = false
    delay(ANIMATION_TIME)
    onDismissRequest()
}

@Composable
fun AnimatedAlertDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    enableDismiss: Boolean = true,
    content: @Composable (dismiss: () -> Unit) -> Unit
) {
    if(visible) {
        AnimatedTransitionDialog(onDismissRequest = onDismissRequest, enableDismiss = enableDismiss) { animatedTransitionDialogHelper ->
            content(animatedTransitionDialogHelper::triggerAnimatedDismiss)
        }
    }
}