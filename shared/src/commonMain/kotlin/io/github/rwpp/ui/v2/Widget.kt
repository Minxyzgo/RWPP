/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui.v2

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.ui.WindowManager

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
            modifier = modifier,
            content = content
        )
    }
}


