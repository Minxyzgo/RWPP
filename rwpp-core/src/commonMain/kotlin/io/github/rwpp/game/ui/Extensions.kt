/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */
@file:Suppress("DuplicatedCode")

package io.github.rwpp.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.rwpp.AppContext
import io.github.rwpp.app.PermissionHelper
import io.github.rwpp.appKoin
import io.github.rwpp.config.EnabledExtensions
import io.github.rwpp.core.UI
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.rwpp_core.generated.resources.Res
import io.github.rwpp.rwpp_core.generated.resources.error_missingmap
import io.github.rwpp.ui.*
import io.github.rwpp.ui.v2.LazyColumnScrollbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Composable
fun ExtensionView(
    onExit: () -> Unit
) {
    BackHandler(true, onExit)
    val appContext = koinInject<AppContext>()
    val externalHandler = koinInject<ExternalHandler>()
    val extensions = externalHandler.getAllExtensions().onFailure {
        UI.showWarning(it.message ?: "Unexpected error")
    }.getOrDefault(listOf())

    val permissionHelper = koinInject<PermissionHelper>()
    LaunchedEffect(Unit) {
        permissionHelper.requestManageFilePermission()
    }

    var showResultView by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    LoadingView(isLoading, onLoaded = {
        isLoading = false
        showResultView = true
    }) {
        message("Loading")
        result = kotlin.runCatching {
            appKoin.get<EnabledExtensions>().values = extensions.filter { it.isEnabled }.map { it.config.id }
            withContext(Dispatchers.IO) {
                extensions.forEach(externalHandler::enableResource)
            }
        }.exceptionOrNull()?.stackTraceToString() ?: "Loading successfully. You should restart RWPP to enable changes."
        true
    }
    AnimatedAlertDialog(
        showResultView,
        onDismissRequest = { }) { _ ->
        BorderCard(
            modifier = Modifier.size(500.dp),
        ) {

            Row(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                HorizontalDivider(Modifier.weight(1f), thickness = 2.dp, color = MaterialTheme.colorScheme.surfaceContainer)
                Box {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(50.dp).offset(5.dp, 5.dp).blur(2.dp))
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                }
                HorizontalDivider(Modifier.weight(1f), thickness = 2.dp, color = MaterialTheme.colorScheme.surfaceContainer)
            }

            LargeDividingLine { 0.dp }

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    result,
                    modifier = Modifier.padding(5.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    RWTextButton(
                        "Restart",
                        modifier = Modifier.padding(5.dp)
                    ) { appContext.exit() }
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

            LazyColumnScrollbar(
                listState = state,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = state
                ) {
                    items(
                        key = { extensions[it].config.id },
                        count = extensions.size
                    ) { index ->
                        val extension = extensions[index]
                        BorderCard(
                            backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(.6f),
                            modifier = Modifier.animateItem()
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(5.dp)
                        ) {
                            Column {
                                var checked by remember {
                                    mutableStateOf(extension.isEnabled)
                                }

                                Row {
                                    Image(
                                        extension.iconPainter ?: painterResource(Res.drawable.error_missingmap),
                                        null,
                                        modifier = Modifier.size(120.dp).padding(5.dp)
                                    )
                                    Column(modifier = Modifier.padding(5.dp)) {
                                        Row {
                                            RWCheckbox(checked, onCheckedChange = {
                                                checked = !checked
                                                extension.isEnabled = checked
                                            }, modifier = Modifier.padding(5.dp))
                                            Text(
                                                extension.config.displayName,
                                                modifier = Modifier.padding(5.dp),
                                                style = MaterialTheme.typography.headlineLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Text(
                                            "Author: ${extension.config.author}",
                                            modifier = Modifier.padding(2.dp),
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Text(
                                            "Version: ${extension.config.version}",
                                            modifier = Modifier.padding(top = 0.dp, bottom = 2.dp),
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
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
                                        text = extension.config.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textModifier = Modifier.padding(top = 5.dp, start = 5.dp),
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
}