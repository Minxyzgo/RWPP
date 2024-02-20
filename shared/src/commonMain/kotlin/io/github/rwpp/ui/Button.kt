/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MenuButton(
    content: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val colors by remember(isPressed) {
        mutableStateOf(
            if(enabled) listOf(Color(173, 243, 177), Color(162, 195, 171), Color(141, 221, 143))
            else listOf(Color.DarkGray, Color.Gray, Color.DarkGray)
        )
    }

    val gradient by remember(colors) { mutableStateOf(Brush.horizontalGradient(colors)) }
    val pxValue = with(LocalDensity.current) {
        (MaterialTheme.typography.headlineLarge.fontSize.value + 5).toDp() + 20.dp
    }

    Row(
        modifier =
        Modifier.fillMaxWidth()
            .padding(10.dp)
            .shadow(10.dp),
    ) {
        Image(
            modifier = Modifier.requiredHeight(pxValue),
            contentScale = ContentScale.FillHeight,
            painter = painterResource("btn_left.png"),
            contentDescription = null,
        )

        Button(
            modifier = Modifier
                .requiredHeight(pxValue)
                .weight(1f),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            onClick = { onClick() },
        ) {
            Box(
                modifier = Modifier
                    .background(gradient)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }, onTap = { onClick() })
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = content, style = MaterialTheme.typography.headlineLarge)
            }
        }

        Image(
            modifier = Modifier.requiredHeight(pxValue),
            contentScale = ContentScale.FillHeight,
            painter = painterResource("btn_right.png"),
            contentDescription = null,
        )
    }
}

@Composable
fun ExitButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(0.8f)),
            modifier = Modifier.size(30.dp),
            contentPadding = PaddingValues(0.dp),
            onClick = { onClick() },
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
        }
    }
}


