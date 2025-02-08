/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui.v2

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import io.github.rwpp.core.UI
import io.github.rwpp.ui.WindowManager

@Composable
fun ExpandedCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background.copy((UI.backgroundTransparency + 0.2f).coerceAtMost(1f)),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Card(
            shape = RectangleShape,
            elevation =  CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(if (LocalWindowManager.current == WindowManager.Small) 0.95f else 0.75f)
                .then(modifier),
            content = content
        )
    }
}

@Composable
fun RWIconButton(
    vector: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    tint: Color = MaterialTheme.colorScheme.surfaceTint,
    onClick: () -> Unit,
) {
    Card(
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.surfaceContainer),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier.then(modifier).bounceClick(onClick = onClick),
    ) {
        Icon(
            vector,
            null,
            tint = tint,
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
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.surfaceContainer),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier.then(modifier).bounceClick(onClick = onClick),
    ) {
        Icon(
            painter,
            null,
            tint = MaterialTheme.colorScheme.surfaceTint,
            modifier = Modifier.size(size).align(Alignment.CenterHorizontally).padding(10.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongPressFloatingActionButton(
    vector: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = androidx.compose.foundation.shape.CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier.then(modifier).combinedClickable(onLongClick = onLongClick, onClick = onClick),
    ) {
        Icon(
            vector,
            null,
            tint = MaterialTheme.colorScheme.surfaceTint,
            modifier = Modifier.size(size).align(Alignment.CenterHorizontally).padding(10.dp)
        )
    }
}

