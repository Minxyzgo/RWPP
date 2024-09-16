/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MenuButton(
    content: String,
    icon: Any? = null,
    modifier: Modifier = Modifier.size(140.dp).padding(10.dp),
    onClick: () -> Unit
) {
    Surface(
        color = Color(27, 18, 18),
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 10.dp,
        tonalElevation = 10.dp,
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(5.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon is Painter)
                Icon(icon, null, tint = Color.White, modifier = Modifier.padding(5.dp))
            else if (icon is ImageVector)
                Icon(icon, null, tint = Color.White, modifier = Modifier.padding(5.dp))

            Text(
                content,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(5.dp)
            )
        }
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
            Icon(Icons.Default.Close, tint = Color.White, contentDescription = null)
        }
    }
}


