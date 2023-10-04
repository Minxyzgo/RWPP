/*
 * Copyright 2023 RWPP contributors
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
import io.github.rwpp.game.units.GameInternalUnits
import io.github.rwpp.ui.*

@Composable
fun BanUnitViewDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    lastSelectedUnits: List<GameInternalUnits>,
    onSelectedUnits: (List<GameInternalUnits>) -> Unit
) {
    AnimatedAlertDialog(
        visible = visible, onDismissRequest = onDismissRequest
    ) { m, d ->
        val selectedUnits = remember { mutableListOf<GameInternalUnits>().apply { addAll(lastSelectedUnits) } }
        BorderCard(
            backgroundColor = Color.Gray,
            modifier = Modifier
                .fillMaxSize(if(LocalWindowManager.current == WindowManager.Large) 0.5f else 0.85f)
                .padding(10.dp)
                .then(m)
        ) {
            ExitButton(d)
            val state = rememberLazyListState()
            var filter by remember { mutableStateOf("") }
            RWSingleOutlinedTextField(
                "Filter",
                filter,
                modifier = Modifier.fillMaxWidth(.5f).padding(5.dp).align(Alignment.CenterHorizontally),
                leadingIcon = { Icon(Icons.Default.Search, null) }
            ) {
                filter = it
            }

            val allUnits = remember(filter) { GameInternalUnits.entries.filter { filter.isBlank() || it.name.contains(filter, ignoreCase = true) } }

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(5.dp), state = state) {
                items(
                    allUnits.size,
                    key = { allUnits[it].ordinal }
                ) {
                    val unit = allUnits[it]
                    var checked by remember { mutableStateOf(selectedUnits.contains(unit)) }
                    BanUnitItem(
                        it,
                        checked,
                        state,
                        unit.name
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
    name: String,
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

            Text(
                name,
                modifier = Modifier.padding(5.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}