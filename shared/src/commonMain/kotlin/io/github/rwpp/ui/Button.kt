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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MenuButton(content: String, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val colors by remember(isPressed) {
        mutableStateOf(listOf(Color(173, 243, 177), Color(162, 195, 171), Color(141, 221, 143)))
    }

    val gradient by remember(colors) { mutableStateOf(Brush.horizontalGradient(colors)) }

    Row(
        modifier =
        Modifier.fillMaxWidth()
            .padding(10.dp)
            .shadow(10.dp),
    ) {
        Image(
            painter = painterResource("btn_left.png"),
            contentDescription = null,
        )

        val pxValue = with(LocalDensity.current) { 40.toDp() }

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


