/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import io.github.rwpp.app.PermissionHelper
import io.github.rwpp.game.Game
import io.github.rwpp.game.map.GameMap
import io.github.rwpp.game.map.MapType
import io.github.rwpp.rwpp_core.generated.resources.Res
import io.github.rwpp.rwpp_core.generated.resources.error_missingmap
import io.github.rwpp.ui.*
import io.github.rwpp.ui.v2.RWIconButton
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Composable
fun MapViewDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    lastSelectedIndex: Int = 0,
    lastSelectedMapType: MapType = MapType.SkirmishMap,
    onSelectedMap: (Int, GameMap) -> Unit
) = AnimatedAlertDialog(
    visible = visible, onDismissRequest = onDismissRequest
) { d ->
    BorderCard(
        modifier = Modifier
           // .fillMaxSize(0.95f)
            .padding(10.dp)
            .autoClearFocus()
    ) {
        ExitButton(d)

        val game = koinInject<Game>()
        var filter by remember { mutableStateOf("") }
        val room = koinInject<Game>().gameRoom

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("MapView", style = MaterialTheme.typography.headlineLarge.run { copy(fontSize = this.fontSize * scaleFitFloat()) })
        }

        var selectedIndex0 by remember { mutableStateOf(lastSelectedMapType.ordinal) }
        var maps by remember { mutableStateOf(listOf<GameMap>()) }
        var mapType = remember { MapType.entries[selectedIndex0] }

        val permissionHelper = koinInject<PermissionHelper>()
        remember(mapType) {
            if (mapType != MapType.SkirmishMap) permissionHelper.requestExternalStoragePermission()
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 5.dp)

        ) {
            LargeDropdownMenu(
                modifier = Modifier.wrapContentSize().padding(5.dp),
                label = "Map Type",
                items = if (room.isHost) MapType.entries else listOf(MapType.SkirmishMap),
                selectedIndex = selectedIndex0,
                onItemSelected = { index, _ -> selectedIndex0 = index }
            )

            RWSingleOutlinedTextField(
                "Filter",
                filter,
                modifier = Modifier.fillMaxWidth(.4f).padding(5.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) }
            ) {
                filter = it
            }

            RWIconButton(Icons.Default.Refresh, modifier = Modifier.offset(y = 10.dp).padding(5.dp), size = 50.dp) {
                game.getAllMaps(true)
                maps = game.getAllMapsByMapType(mapType).filter { it.displayName().contains(filter, true) }
            }
        }

        LargeDividingLine { 0.dp }

        with(game) {
            remember(selectedIndex0, filter) {
                mapType = MapType.entries[selectedIndex0]
                maps = getAllMapsByMapType(mapType).filter { it.displayName().contains(filter, true) }
            }

            val state = rememberLazyListState()
            val state1 = rememberLazyGridState()

            LaunchedEffect(Unit) {
                state1.scrollToItem(lastSelectedIndex)
            }

            LazyVerticalGrid(
                state = state1,
                columns = GridCells.Fixed(5),
            ) {
                items(
                    count = maps.size,
                    key = { maps[it].id }
                ) {
                    val map = maps[it]
                    MapItem(it, state, map.displayName(), map.image, mapType != MapType.SavedGame) { onSelectedMap(it, map); d() }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.MapItem(
    index: Int,
    state: LazyListState,
    name: String,
    image: Painter?,
    showImage: Boolean = true,
    onClick: () -> Unit,
) {
    BorderCard(
        modifier = Modifier.animateItem()
            .padding(10.dp)
            .sizeIn(maxHeight = 200.dp * scaleFitFloat(), maxWidth = 200.dp * scaleFitFloat())
            .clickable { onClick() },
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(.7f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(showImage) Image(
                modifier = Modifier.padding(5.dp).weight(1f),
                painter = image ?: painterResource(Res.drawable.error_missingmap),
                contentDescription = null
            )
            Text(
                name,
                modifier = Modifier.padding(5.dp),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}