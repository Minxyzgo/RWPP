/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalController
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.Mission
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.ui.*

@Composable
fun MissionView(onExit: () -> Unit) {
    BackHandler(true, onExit)

    BorderCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        ExitButton(onExit)

        val context = LocalController.current

        Row(
            modifier = Modifier.fillMaxWidth().scaleFit(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(readI18n("mission.title"), style = MaterialTheme.typography.displayLarge)
        }

        var selectedIndex0 by remember { mutableStateOf(0) }
        var selectedIndex1 by remember { mutableStateOf(context.getConfig<Int>("aiDifficulty") + 2) }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 5.dp)
                .scaleFit()
        ) {
            with(LocalController.current) {
                LargeDropdownMenu(
                    modifier = Modifier.wrapContentSize().padding(5.dp),
                    label = readI18n("mission.type"),
                    items = getAllMissionTypes(),
                    selectedIndex = selectedIndex0,
                    onItemSelected = { index, _ -> selectedIndex0 = index }
                )
            }

            LargeDropdownMenu(
                modifier = Modifier.wrapContentSize().padding(5.dp),
                label = readI18n("common.difficulty"),
                items = Difficulty.entries,
                selectedIndex = selectedIndex1,
                onItemSelected = { index, _ -> selectedIndex1 = index }
            )
        }

        LargeDividingLine { 5.dp }

        with(LocalController.current) {
            var missions by remember { mutableStateOf(listOf<Mission>()) }
            LaunchedEffect(selectedIndex0) {
                missions = getMissionsByType(getAllMissionTypes()[selectedIndex0])
            }

            val state = rememberLazyListState()

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
            ) {
                items(
                    count = missions.size,
                    key = { missions[it].id }
                ) {
                    val mission = missions[it]
                    val difficulty = Difficulty.entries[selectedIndex1]
                    MapItem(it, state, mission.name, mission.image) { startNewMissionGame(difficulty, mission) }
                }
            }
        }
    }
}

