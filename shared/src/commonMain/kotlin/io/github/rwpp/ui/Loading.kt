/*
 * Copyright 2023 RWPP contributors
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
    val message by remember { loadingMessage }
    var cancel by remember { mutableStateOf(false) }

    AnimatedAlertDialog(visible, onDismissRequest = onLoaded, enableDismiss = cancellable) { modifier, dismiss ->
        BorderCard(
            modifier = Modifier.fillMaxSize(GeneralProportion()).then(modifier),
            backgroundColor = Color.Gray
        ) {
            LaunchedEffect(Unit) {
                if(loadContent(LoadingContext { loadingMessage.value = it })) {
                    loadingMessage.value = ""
                    dismiss()
                } else cancel = true

            }

            if(cancellable || cancel) ExitButton(dismiss)

            Column(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(enableAnimation && !cancel) LoadingAnimation(modifier = Modifier.padding(20.dp))
                Text(message, modifier = Modifier.padding(20.dp))
            }
        }
    }
}

@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    indicatorSize: Dp = 100.dp,
    circleColors: List<Color> = listOf(
        Color(0xFF5851D8),
        Color(0xFF833AB4),
        Color(0xFFC13584),
        Color(0xFFE1306C),
        Color(0xFFFD1D1D),
        Color(0xFFF56040),
        Color(0xFFF77737),
        Color(0xFFFCAF45),
        Color(0xFFFFDC80),
        Color(0xFF5851D8)
    ),
    animationDuration: Int = 360
) {

    val infiniteTransition = rememberInfiniteTransition()

    val rotateAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = LinearEasing
            )
        )
    )

    CircularProgressIndicator(
        modifier = Modifier
            .size(size = indicatorSize)
            .rotate(degrees = rotateAnimation)
            .border(
                width = 4.dp,
                brush = Brush.sweepGradient(circleColors),
                shape = CircleShape
            ).then(modifier),
        progress = 1f,
        strokeWidth = 1.dp,
        color = Color.Transparent // Set background color
    )
}