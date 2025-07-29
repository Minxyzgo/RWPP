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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.CloseUIPanelEvent
import io.github.rwpp.game.Game
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.widget.BorderCard
import io.github.rwpp.widget.ExitButton
import io.github.rwpp.widget.LargeDividingLine
import io.github.rwpp.widget.RWSingleOutlinedTextField
import io.github.rwpp.widget.scaleFit
import my.nanihadesuka.compose.LazyVerticalGridScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
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

    val game = koinInject<Game>()
    var filter by remember { mutableStateOf("") }
    val allReplays = remember { game.getAllReplays() }
    val replays = remember(filter) { allReplays.filter { it.name.contains(filter, ignoreCase = true) } }

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

                RWSingleOutlinedTextField(
                    "Filter",
                    filter,
                    modifier = Modifier.fillMaxWidth(.5f).align(Alignment.CenterHorizontally),
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                ) {
                    filter = it
                }

                LargeDividingLine { 0.dp }
                val state = rememberLazyGridState()
                LazyVerticalGridScrollbar(
                    state,
                    settings = ScrollbarSettings.Default.copy(
                        thumbSelectedColor = MaterialTheme.colorScheme.primary,
                        thumbUnselectedColor = MaterialTheme.colorScheme.inversePrimary,
                    ),
                ) {
                    LazyVerticalGrid(
                        state = state,
                        columns = GridCells.Fixed(5),
                    ) {
                        items(
                            replays,
                            key = { it.id }
                        ) { replay ->
                            val mapName = rememberSaveable { replay.displayName() }
                            MapItem(mapName, null, false) {
                                game.watchReplay(replay)
                            }
                        }
                    }
                }
            }
        }
    }
}
