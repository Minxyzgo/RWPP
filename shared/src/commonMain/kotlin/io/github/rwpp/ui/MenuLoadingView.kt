/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.rwpp.shared.generated.resources.Res
import io.github.rwpp.shared.generated.resources.title
import io.github.rwpp.ui.v2.LineSpinFadeLoaderIndicator
import org.jetbrains.compose.resources.painterResource

@Composable
fun MenuLoadingView(
    message: String
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painterResource(Res.drawable.title), contentDescription = "Menu", modifier = Modifier.scale(1.7f).padding(10.dp))
        Spacer(modifier = Modifier.size(100.dp))
        LineSpinFadeLoaderIndicator(
            radius = 25f,
            penThickness = 8f,
            color = Color(151, 188, 98)
        )
        Text(
            message,
            modifier = Modifier.padding(20.dp).offset(y = 25.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}