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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalController
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
fun ResourceView(
    onExit: () -> Unit
) {
    BackHandler(true, onExit)

    val context = LocalController.current
    var selectedResource by remember { mutableStateOf(context.getUsingResource()) }
    var showResultView by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    LoadingView(isLoading, onLoaded = {
        isLoading = false
        showResultView = true
    }) {
        message("Loading")
        result = kotlin.runCatching {
            withContext(Dispatchers.IO) {
                context.enableResource(selectedResource)
            }
        }.exceptionOrNull()?.stackTraceToString() ?: "Loading successfully. You should restart RWPP to enable changes."
        true
    }
    AnimatedAlertDialog(
        showResultView,
        onDismissRequest = { }) { modifier, dismiss ->
        BorderCard(
            modifier = Modifier.fillMaxSize(GeneralProportion()).then(modifier),
            backgroundColor = Color.Gray
        ) {

            Row(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                Divider(Modifier.weight(1f), thickness = 2.dp, color = Color.DarkGray)
                Box {
                    Icon(Icons.Default.Warning, null, tint = Color(151, 188, 98), modifier = Modifier.size(50.dp).offset(5.dp, 5.dp).blur(2.dp))
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFb6d7a8), modifier = Modifier.size(50.dp))
                }
                Divider(Modifier.weight(1f), thickness = 2.dp, color = Color.DarkGray)
            }

            LargeDividingLine { 5.dp }

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    result,
                    modifier = Modifier.padding(5.dp),
                    color = Color.Black,
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    RWTextButton(
                        "Restart",
                        modifier = Modifier.padding(5.dp)
                    ) { context.exit() }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.Center) {
                RWTextButton(
                    "Apply",
                    modifier = Modifier.padding(5.dp),
                ) {
                    isLoading = true
                }
            }
        }
    ) {
        BorderCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            ExitButton(onExit)

            val state = rememberLazyListState()
            val resources = context.getAllResources()

            LazyColumnWithScrollbar(
                state = state,
                data = resources,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    key = { resources[it].id },
                    count = resources.size
                ) { index ->
                    val resource = resources[index]
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
                            var checked by remember(selectedResource) {
                                mutableStateOf(selectedResource == resource)
                            }

                            Row {
                                Image(
                                    resource.iconPainter ?: painterResource("error_missingmap.png"),
                                    null,
                                    modifier = Modifier.size(120.dp).padding(5.dp)
                                )
                                RWCheckbox(checked, onCheckedChange = {
                                    checked = !checked
                                    selectedResource = if(checked) resource else null
                                }, modifier = Modifier.padding(5.dp))
                                Text(
                                    resource.config.name,
                                    modifier = Modifier.padding(5.dp),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color(151, 188, 98)
                                )
                            }

                            val expandedStyle = remember {
                                SpanStyle(
                                    fontWeight = FontWeight.W500,
                                    color = Color(173, 216, 230),
                                    fontStyle = FontStyle.Italic,
                                    textDecoration = TextDecoration.Underline
                                )
                            }

                            SelectionContainer {
                                ExpandableText(
                                    text = resource.config.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textModifier = Modifier.padding(top = 5.dp),
                                    showMoreStyle = expandedStyle,
                                    showLessStyle = expandedStyle
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