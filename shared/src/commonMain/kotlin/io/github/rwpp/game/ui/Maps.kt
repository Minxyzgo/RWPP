/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalController
import io.github.rwpp.game.map.GameMap
import io.github.rwpp.game.map.MapType
import io.github.rwpp.ui.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@Composable
fun MapViewDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    lastSelectedIndex: Int = 0,
    lastSelectedMapType: MapType = MapType.SkirmishMap,
    onSelectedMap: (Int, GameMap) -> Unit
) = AnimatedAlertDialog(
    visible = visible, onDismissRequest = onDismissRequest
) { m, d ->
    BorderCard(
        backgroundColor = Color.Gray,
        modifier = Modifier
            .fillMaxSize(0.95f)
            .padding(10.dp)
            .then(m)
    ) {
        ExitButton(d)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("MapView", style = MaterialTheme.typography.displayLarge.run { copy(fontSize = this.fontSize * scaleFitFloat()) })
        }

        var selectedIndex0 by remember { mutableStateOf(lastSelectedMapType.ordinal) }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 5.dp)
                .scaleFit()
        ) {
            LargeDropdownMenu(
                modifier = Modifier.wrapContentSize().padding(5.dp),
                label = "Map Type",
                items = MapType.entries,
                selectedIndex = selectedIndex0,
                onItemSelected = { index, _ -> selectedIndex0 = index }
            )
        }

        LargeDividingLine { 5.dp }

        with(LocalController.current) {
            var maps by remember { mutableStateOf(listOf<GameMap>()) }
            var mapType = remember { MapType.entries[selectedIndex0] }
            remember(selectedIndex0) {
                mapType = MapType.entries[selectedIndex0]
                maps = getAllMapsByMapType(mapType)
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

@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.MapItem(
    index: Int,
    state: LazyListState,
    name: String,
    image: Painter?,
    showImage: Boolean = true,
    onClick: () -> Unit,
) {
    val (_, easing) = state.calculateDelayAndEasing(index, 5)
    val animation = tween<Float>(durationMillis = 500, delayMillis = 0, easing = easing)
    val args = ScaleAndAlphaArgs(fromScale = 2f, toScale = 1f, fromAlpha = 0f, toAlpha = 1f)
    val (scale, alpha) = scaleAndAlpha(args = args, animation = animation)

    BorderCard(
        modifier = Modifier
            .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
            .animateItemPlacement()
            .padding(10.dp)
            .sizeIn(maxHeight = 200.dp * scaleFitFloat(), maxWidth = 200.dp * scaleFitFloat())
            .clickable { onClick() },
        backgroundColor = Color.DarkGray.copy(.7f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(showImage) Image(
                modifier = Modifier.padding(5.dp).weight(1f),
                painter = image ?: painterResource("error_missingmap.png"),
                contentDescription = null
            )
            Text(
                name,
                modifier = Modifier.padding(5.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}