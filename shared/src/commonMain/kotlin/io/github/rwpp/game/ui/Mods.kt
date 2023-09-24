/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalController
import io.github.rwpp.ui.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModsView(onExit: () -> Unit) = with(LocalController.current) {
    var mods by remember { mutableStateOf(getAllMods()) }
    var updated by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingAction by remember { mutableStateOf<suspend () -> Unit>({}) }
    var disableAll by remember { mutableStateOf(false) }

    LoadingView(isLoading, onLoaded = { isLoading = false }) {
        loadingAction()
        true
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.Center) {
                RWTextButton(
                    "Update",
                    modifier = Modifier.padding(5.dp),
                )  { loadingAction = { modUpdate(); mods = getAllMods(); updated = true }; isLoading = true }

                RWTextButton(
                    label = "Reload",
                    modifier = Modifier.padding(5.dp),
                ) { loadingAction = { modReload() }; isLoading = true }

                RWTextButton(
                    "disableAll",
                    modifier = Modifier.padding(5.dp)
                ) { disableAll = true }

                RWTextButton(
                    label = "Apply",
                    modifier = Modifier.padding(5.dp),
                ) { loadingAction = { modSaveChange(); onExit() }; isLoading = true }
            }
        }
    ) {
        BorderCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Column {
                ExitButton(onExit)
                var filter by remember { mutableStateOf("") }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    RWSingleOutlinedTextField(
                        "Filter",
                        filter,
                        modifier = Modifier.fillMaxWidth(.4f),
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    ) {
                        filter = it
                    }
                }


                remember(filter, updated) {
                    if(filter.isNotBlank() || updated) mods = getAllMods().filter { it.name.contains(filter) }
                    if(filter.isBlank()) mods = getAllMods()
                    updated = false
                }

                Spacer(modifier = Modifier.size(20.dp))

                val state = rememberLazyListState()

                LazyColumnWithScrollbar(
                    state = state,
                    data = mods,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        key = { mods[it].id },
                        count = mods.size
                    ) { index ->
                        val mod = mods[index]
                        val (delay, easing) = state.calculateDelayAndEasing(index, 1)
                        val animation = tween<Float>(durationMillis = 500, delayMillis = delay, easing = easing)
                        val args = ScaleAndAlphaArgs(fromScale = 2f, toScale = 1f, fromAlpha = 0f, toAlpha = 1f)
                        val (scale, alpha) = scaleAndAlpha(args = args, animation = animation)
                        BorderCard(
                            backgroundColor = Color.DarkGray.copy(.6f),
                            modifier = Modifier
                                .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
                                .animateItemPlacement()
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(5.dp)
                        ) {
                            Column {
                                var checked by remember { mutableStateOf(mod.isEnabled) }
                                remember(disableAll) {
                                    if(disableAll) {
                                        checked = false
                                        mod.isEnabled = false
                                        disableAll = false
                                    }
                                }

                                Row {
                                    RWCheckbox(checked, onCheckedChange = {
                                        checked = !checked
                                        mod.isEnabled = checked
                                    }, modifier = Modifier.padding(5.dp))
                                    Text(mod.name, modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.headlineLarge, color = Color(151, 188, 98))
                                }

                                SelectionContainer {
                                    Text(mod.description, style = MaterialTheme.typography.bodyLarge)
                                }

                                Spacer(modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}