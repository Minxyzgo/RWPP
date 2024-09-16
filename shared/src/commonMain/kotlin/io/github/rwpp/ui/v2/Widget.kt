/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui.v2

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.ui.WindowManager
import io.github.rwpp.ui.autoClearFocus

@Composable
fun ExpandedCard(
    modifier: Modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth(if (LocalWindowManager.current == WindowManager.Small) 0.95f else 0.75f),
    backgroundColor: Color = Color(53, 57, 53).copy(0.9f),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Card(
            shape = RectangleShape,
            elevation =  CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            modifier = modifier.autoClearFocus(),
            content = content
        )
    }
}

@Composable
fun RWIconButton(
    vector: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    onClick: () -> Unit,
) {
    Card(
        border = BorderStroke(3.dp, Color.DarkGray),
        colors = CardDefaults.cardColors(containerColor = Color(27, 18, 18)),
        shape = RoundedCornerShape(5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier.then(modifier).bounceClick(onClick = onClick),
    ) {
        Icon(
            vector,
            null,
            modifier = Modifier.size(size).align(Alignment.CenterHorizontally).padding(10.dp)
        )
    }
}

@Composable
fun RWIconButton(
    painter: Painter,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    onClick: () -> Unit,
) {
    Card(
        border = BorderStroke(3.dp, Color.DarkGray),
        colors = CardDefaults.cardColors(containerColor = Color(27, 18, 18)),
        shape = RoundedCornerShape(5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier.then(modifier).bounceClick(onClick = onClick),
    ) {
        Icon(
            painter,
            null,
            modifier = Modifier.size(size).align(Alignment.CenterHorizontally).padding(10.dp)
        )
    }
}


