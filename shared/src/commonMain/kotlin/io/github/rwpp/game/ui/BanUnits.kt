/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.game.Game
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.ui.*
import org.koin.compose.koinInject

@Composable
fun BanUnitViewDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    lastSelectedUnits: List<GameUnit>,
    onSelectedUnits: (List<GameUnit>) -> Unit
) {

    val game = koinInject<Game>()

    AnimatedAlertDialog(
        visible = visible, onDismissRequest = onDismissRequest
    ) { d ->
        val selectedUnits = remember(lastSelectedUnits) { mutableListOf<GameUnit>().apply { addAll(lastSelectedUnits) } }
        BorderCard(
            modifier = Modifier
               // .fillMaxSize(LargeProportion())
                .padding(10.dp)
                .autoClearFocus()
        ) {
            ExitButton(d)
            val state = rememberLazyListState()
            var filter by remember { mutableStateOf("") }

            val allUnits = remember(filter) { game.getAllUnits().filter { filter.isBlank() || it.displayName.contains(filter, ignoreCase = true) } }

            val current = LocalWindowManager.current

            if (current != WindowManager.Small) {
                //Fix the filter field
                FilterField(filter) { filter = it }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(5.dp), state = state) {

                if (current == WindowManager.Small) {
                    //When it is small, let the field can be scrolled
                    item { FilterField(filter) { filter = it } }
                }

                items(
                    allUnits.size,
                ) {
                    val unit by remember(allUnits) { mutableStateOf(allUnits[it]) }
                    var checked by remember(lastSelectedUnits, unit) { mutableStateOf(selectedUnits.contains(unit)) }
                    BanUnitItem(
                        it,
                        checked,
                        state,
                        unit
                    ) { c ->
                        checked = c
                        if(c) {
                            selectedUnits.add(unit)
                        } else {
                            selectedUnits.remove(unit)
                        }
                    }
                }
            }

            LargeDividingLine { 5.dp }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                RWTextButton("Clear", Modifier.padding(5.dp)) {
                    onSelectedUnits(listOf())
                    d()
                }

                RWTextButton("Apply", Modifier.padding(5.dp)) {
                    onSelectedUnits(selectedUnits)
                    d()
                }
            }
        }
    }
}

@Composable
fun BanUnitItem(
    index: Int,
    checked: Boolean,
    state: LazyListState,
    unit: GameUnit,
    onChanged: (checked: Boolean) -> Unit
) {
    val (_, easing) = state.calculateDelayAndEasing(index, 5)
    val animation = tween<Float>(durationMillis = 500, delayMillis = 0, easing = easing)
    val args = ScaleAndAlphaArgs(fromScale = 2f, toScale = 1f, fromAlpha = 0f, toAlpha = 1f)
    val (scale, alpha) = scaleAndAlpha(args = args, animation = animation)

    BorderCard(
        modifier = Modifier
            .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
            .fillMaxWidth()
            .padding(10.dp)
            .sizeIn(maxHeight = 200.dp, maxWidth = 200.dp),
        backgroundColor = Color.DarkGray.copy(.7f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            RWCheckbox(checked, modifier = Modifier.padding(5.dp), onCheckedChange = {
                onChanged(it)
            })

            Column {
                Text(
                    unit.displayName,
                    modifier = Modifier.padding(5.dp),
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    unit.name,
                    modifier = Modifier.padding(5.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun FilterField(filter: String, onFilterChange: (String) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        RWSingleOutlinedTextField(
            "Filter",
            filter,
            modifier = Modifier.fillMaxWidth(.5f).padding(5.dp),
            leadingIcon = { Icon(Icons.Default.Search, null) }
        ) {
            onFilterChange(it)
        }
    }
}