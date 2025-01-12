/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.rwpp.app.PermissionHelper
import io.github.rwpp.game.Game
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.ui.*
import io.github.rwpp.ui.v2.LazyColumnScrollbar
import org.koin.compose.koinInject

@Composable
fun ModsView(onExit: () -> Unit) {
    BackHandler(true, onExit)
    val permissionHelper = koinInject<PermissionHelper>()
    val modManager = koinInject<ModManager>()
    val game = koinInject<Game>()

    var mods by remember { mutableStateOf(modManager.getAllMods()) }
    var updated by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingAction by remember { mutableStateOf<suspend () -> Unit>({}) }
    var disableAll by remember { mutableStateOf(false) }

    LoadingView(isLoading, onLoaded = { isLoading = false }) {
        loadingAction()
        true
    }

    LaunchedEffect(Unit) {
        permissionHelper.requestExternalStoragePermission()
    }

    fun updateMods() {
        loadingAction = {
            modManager.modUpdate()
            mods = modManager.getAllMods()
            updated = true
        }

        isLoading = true
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.Center) {
                RWTextButton(
                    readI18n("mod.update"),
                    modifier = Modifier.padding(5.dp),
                )  { updateMods() }

                RWTextButton(
                    readI18n("mod.reload"),
                    modifier = Modifier.padding(5.dp),
                ) { loadingAction = {
                    modManager.modReload()
                    game.getAllMaps(true)
                    mods = modManager.getAllMods()
                }; isLoading = true }

                RWTextButton(
                    readI18n("mod.disableAll"),
                    modifier = Modifier.padding(5.dp)
                ) { disableAll = true }

                RWTextButton(
                    readI18n("mod.apply"),
                    modifier = Modifier.padding(5.dp),
                ) { loadingAction = { modManager.modSaveChange(); onExit() }; isLoading = true }
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
                        modifier = Modifier.fillMaxWidth(.5f),
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    ) {
                        filter = it
                    }
                }


                remember(filter, updated) {
                    if (filter.isNotBlank() || updated) mods =
                        modManager.getAllMods().filter { it.name.uppercase().contains(filter.uppercase()) }
                    if (filter.isBlank()) mods = modManager.getAllMods()
                    updated = false
                }

                Spacer(modifier = Modifier.size(20.dp))

                val state = rememberLazyListState()

                LazyColumnScrollbar(
                    listState = state,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        state = state
                    ) {
                        items(
                            count = mods.size,
                            key = { mods[it].id }
                        ) { index ->
                            val mod = mods[index]
                            BorderCard(
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(.6f),
                                modifier = Modifier.animateItem()
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(5.dp)
                            ) {
                                Column {
                                    var checked by remember { mutableStateOf(mod.isEnabled) }
                                    remember(disableAll) {
                                        if (disableAll) {
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
                                        Text(
                                            mod.name,
                                            modifier = Modifier.padding(5.dp),
                                            style = MaterialTheme.typography.headlineLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    //var isNetwork by remember { mutableStateOf(mod.isNetworkMod) }

//                                    if(isNetwork) {
//                                        Text(
//                                            readI18n("mod.networkModInfo"),
//                                            modifier = Modifier.padding(start = 2.dp),
//                                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 12.sp),
//                                            color = Color.Yellow
//                                        )
//
//                                        RWTextButton(readI18n("mod.cancel")) {
//                                            mod.isNetworkMod = false
//                                            isNetwork = false
//                                        }
//                                    }

                                    val expandedStyle = remember {
                                        SpanStyle(
                                            fontWeight = FontWeight.W500,
                                            color = Color(173, 216, 230),
                                            fontStyle = FontStyle.Italic,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    }

                                    ExpandableText(
                                        text = mod.description,
                                        modifier = Modifier.padding(start = 2.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textModifier = Modifier.padding(top = 5.dp),
                                        showMoreStyle = expandedStyle,
                                        showLessStyle = expandedStyle
                                    )

                                    Text(
                                        "(RAM: ${mod.getRamUsed()})",
                                        modifier = Modifier.padding(start = 2.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Green
                                    )

                                    if (mod.errorMessage != null) {
                                        Text(
                                            mod.errorMessage!!,
                                            modifier = Modifier.padding(start = 2.dp),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Red
                                        )
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
}