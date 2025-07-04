/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.CloseUIPanelEvent
import io.github.rwpp.game.Game
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.widget.BorderCard
import io.github.rwpp.widget.ExitButton
import io.github.rwpp.widget.LargeDividingLine
import io.github.rwpp.widget.scaleFit
import org.koin.compose.koinInject

@Composable
fun ReplaysViewDialog(
    onExit: () -> Unit
) {
    BackHandler(true, onExit)
    DisposableEffect(Unit) {
        onDispose {
            CloseUIPanelEvent("replays").broadcastIn()
        }
    }

    BorderCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Box {
            ExitButton(onExit)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().scaleFit(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Replay", style = MaterialTheme.typography.headlineLarge)
                }

                LargeDividingLine { 0.dp }

                with(koinInject<Game>()) {
                    val replays by remember { mutableStateOf(getAllReplays()) }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                    ) {
                        items(
                            count = replays.size,
                            key = { replays[it].id }
                        ) {
                            val replay = replays[it]
                            MapItem(replay.displayName(), null, false) {
                                watchReplay(replay)
                            }
                        }
                    }
                }
            }
        }
    }
}
