/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.ui

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
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import io.github.rwpp.net.Net
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.ui.BorderCard
import io.github.rwpp.ui.ExitButton
import io.github.rwpp.ui.LargeDividingLine
import io.github.rwpp.ui.v2.ExpandedCard
import org.koin.compose.koinInject

/**
 * Simple contributor list,
 *
 * It will be changed at any time.
 */
@Composable
fun ContributorList(
    onExit: () -> Unit,
) = ExpandedCard(modifier = Modifier
    .verticalScroll(rememberScrollState())
) {
    ExitButton(onExit)
    BackHandler(true, onExit)

    val net = koinInject<Net>()

    val header1 = buildAnnotatedString {
        append("赞助者列表")

        withLink(
            link = LinkAnnotation
                .Clickable(
                    tag = "contributor",
                    linkInteractionListener = { net.openUriInBrowser("https://afdian.com/a/minxyzgo") },
                    styles = TextLinkStyles(style = SpanStyle(color = Color.Red))
                )
        ) {
            append(" (为RWPP赞助一下？)")
        }
    }

    Text(header1, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp))
    LargeDividingLine { 5.dp }
    BorderCard(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
        Text(
            "铁锈盒子 (ww.rtsbox.cn)",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.onSurface
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