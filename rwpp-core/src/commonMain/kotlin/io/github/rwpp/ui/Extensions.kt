/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */
@file:Suppress("DuplicatedCode")

package io.github.rwpp.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.rwpp.AppContext
import io.github.rwpp.app.PermissionHelper
import io.github.rwpp.appKoin
import io.github.rwpp.config.EnabledExtensions
import io.github.rwpp.config.Settings
import io.github.rwpp.core.UI
import io.github.rwpp.event.broadcast
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.CloseUIPanelEvent
import io.github.rwpp.external.Extension
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.rwpp_core.generated.resources.Res
import io.github.rwpp.rwpp_core.generated.resources.error_missingmap
import io.github.rwpp.scripts.Render
import io.github.rwpp.widget.*
import io.github.rwpp.widget.v2.LazyColumnScrollbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ExtensionView(
    onExit: () -> Unit
) {
    DisposableEffect(Unit) {
        onDispose {
            CloseUIPanelEvent("extension").broadcastIn()
        }
    }
    BackHandler(true, onExit)
    val appContext = koinInject<AppContext>()
    val externalHandler = koinInject<ExternalHandler>()
    var updateExtensions by remember { mutableStateOf(false) }

    val extensions = remember(updateExtensions) {
        externalHandler.getAllExtensions().onFailure {
            UI.showWarning(it.message ?: "Unexpected error")
        }.getOrDefault(listOf()).filter { !it.config.hasResource }
    }

    val resources = remember(updateExtensions) {
        externalHandler.getAllExtensions().onFailure {
            UI.showWarning(it.message ?: "Unexpected error")
        }.getOrDefault(listOf()).filter { it.config.hasResource }
    }

    val permissionHelper = koinInject<PermissionHelper>()
    LaunchedEffect(Unit) {
        permissionHelper.requestManageFilePermission()
    }

    var showExtensionSetting by remember { mutableStateOf(false) }
    var selectedExtension by remember { mutableStateOf<Extension?>(null) }
    AnimatedAlertDialog(
        showExtensionSetting,
        onDismissRequest = { showExtensionSetting = false }) { _ ->
        BorderCard(
            modifier = Modifier.size(500.dp),
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(selectedExtension!!.config.displayName, modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                LargeDividingLine { 0.dp }
                for (widget in selectedExtension!!.settingPanel) {
                    widget.Render()
                }
            }
        }
    }

    var selectedResource by remember { mutableStateOf(externalHandler.getUsingResource()) }
    val defaultResource = remember { selectedResource }

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
            if (defaultResource != selectedResource) {
                withContext(Dispatchers.IO) {
                    externalHandler.enableResource(selectedResource)
                }
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
                    readI18n("mod.update"),
                    modifier = Modifier.padding(5.dp),
                )  { updateExtensions = !updateExtensions }

                RWTextButton(
                    readI18n("mod.apply"),
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
            Box {
                ExitButton(onExit)
                val state = rememberLazyListState()

                LazyColumnScrollbar(
                    listState = state,
                    modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        state = state
                    ) {
                        item {
                            Column {
                                Text(
                                    readI18n("extension.extension"),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 5.dp)
                                )

                                HorizontalDivider(thickness = 3.dp,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 5.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        items(
                            key = { extensions[it].config.id },
                            count = extensions.size
                        ) { index ->
                            val extension = extensions[index]
                            ExtensionCard(
                                extension,
                                onClickSettings = {
                                    selectedExtension = extension
                                    showExtensionSetting = true
                                }
                            )
                        }

                        item {
                            Column {
                                Text(
                                    readI18n("extension.resource"),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 5.dp)
                                )

                                HorizontalDivider(thickness = 3.dp,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 5.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        items(
                            key = { resources[it].config.id },
                            count = resources.size
                        ) { index ->
                            val resource = resources[index]
                            ExtensionCard(
                                resource,
                                onClickSettings = {
                                    // Resource cannot be enabled.
                                },
                                selectedResource,
                                onCheckedChange = {
                                    selectedResource = resource
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.ExtensionCard(
    extension: Extension,
    onClickSettings: () -> Unit,
    selectedResource: Extension? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    BorderCard(
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(.6f),
        modifier = Modifier.then(if (koinInject<Settings>().enableAnimations)
            Modifier.animateItem()
        else Modifier)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp)
    ) {
        if (extension.settingPanel.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    Icons.Default.Settings,
                    null,
                    modifier = Modifier.padding(5.dp, 5.dp, 20.dp, 5.dp).clickable {
                        onClickSettings()
                    }, tint = MaterialTheme.colorScheme.surfaceTint
                )
            }
        }

        Column {
            var checked by remember(selectedResource) {
                mutableStateOf(
                    if (extension.config.hasResource)
                        selectedResource == extension
                    else extension.isEnabled)
            }

            Row {
                Card(
                    Modifier.padding(5.dp),
                    shape = RectangleShape,
                    border = BorderStroke(3.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Image(
                        extension.iconPainter ?: painterResource(Res.drawable.error_missingmap),
                        null,
                        modifier = Modifier.size(120.dp)
                    )
                }

                Column(modifier = Modifier.padding(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RWCheckbox(checked, onCheckedChange = {
                            checked = !checked

                            if (!extension.config.hasResource) {
                                extension.isEnabled = checked
                            }

                            onCheckedChange?.invoke(checked)
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