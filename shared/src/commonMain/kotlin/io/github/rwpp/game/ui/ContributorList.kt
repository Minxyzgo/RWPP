/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.ui

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.ui.*
import io.github.rwpp.ui.v2.ExpandedCard

/**
 * Simple contributor list,
 *
 * It will be changed at any time.
 */
@Composable
fun ContributorList(
    onExit: () -> Unit,
) = ExpandedCard(modifier = Modifier
    .fillMaxHeight()
    .fillMaxWidth(if (LocalWindowManager.current == WindowManager.Small) 0.95f else 0.75f)
    .verticalScroll(rememberScrollState())
) {
    ExitButton(onExit)
    BackHandler(true, onExit)

    Text("赞助者列表", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp))
    LargeDividingLine { 5.dp }
    BorderCard(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
        Text(
            "铁锈盒子 (ww.rtsbox.cn)",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color(151, 188, 98)
        )
    }

    Text("贡献者列表", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp))

    LargeDividingLine { 5.dp }

    val contributors = "Minxyzgo;Dr"
    BorderCard(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
        contributors.split(";").forEach {
            Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
        }

    }

    Text("特别鸣谢", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp))

    LargeDividingLine { 5.dp }

    BorderCard(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
        Text("@Corroding Games @LukeHoschke Rusted Warfare", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
    }

}