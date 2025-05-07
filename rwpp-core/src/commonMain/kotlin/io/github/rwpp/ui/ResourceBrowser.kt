/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.mapDir
import io.github.rwpp.modDir
import io.github.rwpp.net.Net
import io.github.rwpp.net.NetResourceInfo
import io.github.rwpp.net.ResourceType
import io.github.rwpp.rwpp_core.generated.resources.Res
import io.github.rwpp.rwpp_core.generated.resources.download
import io.github.rwpp.rwpp_core.generated.resources.error_missingmap
import io.github.rwpp.rwpp_core.generated.resources.replay_30
import io.github.rwpp.widget.*
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import java.io.File

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ResourceBrowser(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    var downloadingMod by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    AnimatedAlertDialog(visible, onDismiss) {
        var isLoading by remember { mutableStateOf(false) }
        val net = koinInject<Net>()
        var page by remember { mutableStateOf(1) }
        var selectedProtocolIndex by remember { mutableStateOf(0) }
        var selectedTypeIndex by remember { mutableStateOf(0) }
        var keyword by remember { mutableStateOf("") }
        val allInfo = remember { SnapshotStateList<NetResourceInfo>() }

        Scaffold(
            modifier = Modifier.fillMaxSize(0.8f),
            containerColor = Color.Transparent,
            bottomBar = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        RWTextButton(
                            label = readI18n("mod.loadMore"),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.replay_30),
                                    null,
                                    modifier = Modifier.size(30.dp)
                                )
                            },
                            modifier = Modifier.padding(10.dp)
                        ) {
                            page += 1
                        }
                    }
                }
            }
        ) {
            BorderCard(Modifier.fillMaxSize().autoClearFocus()) {
                Box {
                    Column {
                        LaunchedEffect(keyword, page, selectedTypeIndex) {
                            isLoading = true
                            net.searchBBS(
                                net.bbsProtocols[selectedProtocolIndex],
                                page,
                                keyword,
                                if (selectedTypeIndex == 0)
                                    ResourceType.Mod
                                else ResourceType.Map,
                            ) { result ->
                                result.getOrNull()?.let { allInfo.addAll(it) }
                                isLoading = false
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val list = remember {
                                listOf(readI18n(("common.mod")), readI18n("common.map"))
                            }
                            LargeDropdownMenu(
                                modifier = Modifier.weight(.3f).padding(5.dp),
                                label = readI18n("mod.resourceType"),
                                items = list,
                                selectedIndex = selectedTypeIndex,
                                onItemSelected = { index, _ ->
                                    allInfo.clear()
                                    selectedTypeIndex = index
                                }
                            )

                            LargeDropdownMenu(
                                modifier = Modifier.weight(.3f).padding(5.dp),
                                label = "Api",
                                items = net.bbsProtocols,
                                selectedIndex = selectedProtocolIndex,
                                selectedItemToString = { it.name },
                                onItemSelected = { index, _ -> selectedProtocolIndex = index }
                            )

                            var search by remember { mutableStateOf("") }
                            RWSingleOutlinedTextField(
                                "Search",
                                search,
                                modifier = Modifier.weight(.3f).padding(5.dp),
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                trailingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        null,
                                        modifier = Modifier.clickable {
                                            keyword = search
                                            page = 1
                                            allInfo.clear()
                                        })
                                },
                            ) {
                                search = it
                            }
                        }

                        HorizontalDivider(thickness = 3.dp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 5.dp),
                            color = MaterialTheme.colorScheme.primary
                        )

                        @Composable
                        fun ResourceInfoCard(resourceInfo: NetResourceInfo) {
                            BorderCard(
                                modifier = Modifier.width(300.dp).padding(10.dp),
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainer
                            ) {
                                val image = remember { resourceInfo.imagePainter }
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Card(
                                        Modifier.padding(5.dp),
                                        shape = RectangleShape,
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(3.dp, MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Image(
                                            image ?: painterResource(Res.drawable.error_missingmap),
                                            null,
                                            modifier = Modifier.size(100.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.height(IntrinsicSize.Max).weight(1f)) {
                                        Text(
                                            resourceInfo.title,
                                            modifier = Modifier.padding(5.dp).align(Alignment.Start),
                                            style = MaterialTheme.typography.bodyLarge,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )

                                        if (resourceInfo.version != null) {
                                            Text(
                                                resourceInfo.version,
                                                modifier = Modifier.padding(start = 2.dp).widthIn(10.dp, 150.dp),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.DarkGray
                                            )
                                        }

                                        if (resourceInfo.author != null) {
                                            Text(
                                                resourceInfo.author,
                                                modifier = Modifier.padding(start = 2.dp).widthIn(10.dp, 150.dp),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }

                                    VerticalDivider(
                                        modifier = Modifier
                                            .height(100.dp)
                                            .padding(2.dp)
                                            .align(Alignment.CenterVertically),
                                        thickness = 4.dp,
                                    )

                                    Column(Modifier.align(Alignment.CenterVertically)) {
                                        if (resourceInfo.bbsUrl != null) {
                                            IconButton(onClick = {
                                                resourceInfo.bbsUrl.let { net.openUriInBrowser(it) }
                                            }, modifier = Modifier.size(45.dp)) {
                                                Icon(
                                                    Icons.Default.Home,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.surfaceTint
                                                )
                                            }
                                        }

                                        if (resourceInfo.downloadUrl != null) {
                                            IconButton(onClick = {
                                                resourceInfo.downloadUrl.let {
                                                    downloadingMod = true
                                                    net.downloadFile(
                                                        it,
                                                        File(
                                                            if (selectedTypeIndex == 0)
                                                                "$modDir/${resourceInfo.title}.rwmod"
                                                            else "$mapDir/${resourceInfo.title}.tmx"
                                                        )
                                                    ) { p -> progress = p }
                                                }
                                            }, modifier = Modifier.size(45.dp)) {
                                                Icon(
                                                    painter = painterResource(Res.drawable.download),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.surfaceTint
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        LazyVerticalGrid(GridCells.Adaptive(320.dp), modifier = Modifier.fillMaxWidth()) {
                            items(allInfo, key = { it.id }) { info ->
                                ResourceInfoCard(info)
                            }

                            item(span = {
                                // LazyGridItemSpanScope:
                                // maxLineSpan
                                GridItemSpan(maxLineSpan)
                            }) {
                                Spacer(Modifier.height(50.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    val downloaded by remember {
        derivedStateOf {
            progress >= 1
        }
    }

    val failed by remember {
        derivedStateOf {
            progress < 0
        }
    }

    AnimatedAlertDialog(downloadingMod,
        {
            downloadingMod = false
            progress = 0f
        }, enableDismiss = downloaded
    ) { dismiss ->
        BorderCard(modifier = Modifier.size(200.dp)) {
            Spacer(Modifier.weight(1f))
            Text(
                if (downloaded) {
                     "Done"
                } else if (failed)
                    "Failed"
                else "Downloading...",
                modifier = Modifier.padding(start = 2.dp).align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            LinearProgressIndicator(
                progress = { progress },
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(5.dp)
            )
            Spacer(Modifier.weight(1f))
        }
    }
}