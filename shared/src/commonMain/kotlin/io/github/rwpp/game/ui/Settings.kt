/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalController
import io.github.rwpp.config.MultiplayerPreferences
import io.github.rwpp.config.instance
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.platform.Platform
import io.github.rwpp.ui.*
import kotlin.math.roundToInt

@Composable
fun SettingsView(onExit: () -> Unit) {
    BackHandler(true, onExit)

    val current = LocalController.current
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    current.saveConfig()
                    onExit()
                },
                shape = CircleShape,
                modifier = Modifier.padding(5.dp),
                containerColor = Color(151, 188, 98),
            ) {
                Icon(Icons.Default.Done, null)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        BorderCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Column {
                ExitButton(onExit)

                LazyColumn {
                    item {
                        SettingsGroup("graphics") {
                            SettingsSwitchComp("showUnitWaypoints")
                            SettingsSwitchComp("showHp", "alwayUnitHealth") // I don't why they are different
                            SettingsSwitchComp("showUnitIcons", "unitIcons")
                            SettingsSwitchComp("renderVsync")
                            SettingsSwitchComp("renderClouds")
                            SettingsSwitchComp("shaderEffects")
                            SettingsSwitchComp("enableMouseCapture")
                            SettingsSwitchComp("quickRally")
                            SettingsSwitchComp("doubleClickToAttackMove")
                        }
                    }

                    item {
                        SettingsGroup("gameplay") {
                            SettingsSwitchComp("showSelectedUnitsList")
                            SettingsSwitchComp("useMinimapAllyColors")
                            SettingsSwitchComp("showWarLogOnScreen")
                            SettingsSwitchComp("smartSelection_v2", "smartSelection") //v2 ???
                            SettingsSwitchComp("forceEnglish")
                            var teamUnitCapSinglePlayer by remember { mutableStateOf(current.getConfig<Int?>("teamUnitCapSinglePlayer")) }
                            RWSingleOutlinedTextField("teamUnitCapSinglePlayer", teamUnitCapSinglePlayer?.toString() ?: "", modifier = Modifier.fillMaxWidth(), lengthLimitCount = 6, typeInNumberOnly = true, typeInOnlyInteger = true) {
                                teamUnitCapSinglePlayer = it.toIntOrNull()
                                current.setConfig("teamUnitCapSinglePlayer", teamUnitCapSinglePlayer ?: 100)
                            }
                            var teamUnitCapHostedGame by remember { mutableStateOf(current.getConfig<Int?>("teamUnitCapHostedGame")) }
                            RWSingleOutlinedTextField("teamUnitCapHostedGame", teamUnitCapHostedGame?.toString() ?: "", modifier = Modifier.fillMaxWidth(), lengthLimitCount = 6, typeInNumberOnly = true, typeInOnlyInteger = true) {
                                teamUnitCapHostedGame = it.toIntOrNull()
                                current.setConfig("teamUnitCapHostedGame", teamUnitCapHostedGame ?: 100)
                            }
                        }
                    }

                    item {
                        SettingsGroup("audio") {
                            SettingsSlider("masterVolume", 0f..1f)
                            SettingsSlider("gameVolume", 0f..1f)
                            SettingsSlider("interfaceVolume", 0f..1f)
                            SettingsSlider("musicVolume", 0f..1f)
                        }
                    }

                    item {
                        SettingsGroup("developer") {
                            SettingsSwitchComp("showFps")
                            SettingsSwitchComp("Show Welcome Message",
                                defaultValue = MultiplayerPreferences.instance.showWelcomeMessage ?: false
                            ) { MultiplayerPreferences.instance.showWelcomeMessage = it }
                        }
                    }

                    item {
                        SettingsGroup("networking") {
                            var port by remember { mutableStateOf(current.getConfig<Int?>("networkPort")) }
                            RWSingleOutlinedTextField("port", port?.toString() ?: "", modifier = Modifier.fillMaxWidth(), lengthLimitCount = 5, typeInNumberOnly = true) {
                                port = it.toIntOrNull()
                                current.setConfig("networkPort", port ?: 5123)
                            }
                            SettingsSwitchComp("udpInMultiplayer")
                            SettingsSwitchComp("showChatAndPingShortcuts")
                            SettingsSwitchComp("showMapPingsOnBattlefield")
                            SettingsSwitchComp("showMapPingsOnMinimap")
                            SettingsSwitchComp("showPlayerChatInGame")
                        }
                    }

                    if(Platform.isAndroid()) {
                        item {
                            SettingsGroup("", "Android") {
                                val context = LocalController.current
                                RWTextButton("Set External Folder") {
                                    context.requestExternalStoragePermission()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    name: String,
    displayName: String? = null,
    content: @Composable ColumnScope.() -> Unit
) = with(LocalController.current) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            displayName ?: i18n("menus.settings.heading.$name"),
            style = MaterialTheme.typography.headlineLarge,
            color = Color(151, 188, 98),
            modifier = Modifier.padding(start = 5.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = Color.DarkGray.copy(.6f),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4),
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchComp(
    name: String,
    labelName: String = name,
    defaultValue: Boolean? = null,
    customConfigSettingAction: ((Boolean) -> Unit)? = null
) = with(LocalController.current) {
    var state by remember { mutableStateOf(defaultValue ?: getConfig(name)) }
    val onClick = customConfigSettingAction?.let{
        {
            state = !state
            customConfigSettingAction(state)
        }
    } ?: {
        state = !state
        setConfig(name, state)
    }
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
    ) {

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if(customConfigSettingAction != null) labelName else i18n("menus.settings.option.$labelName"),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = state,
                    onCheckedChange = { onClick() },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(151, 188, 98)),
                )
            }
            Divider()
        }
    }
}

@Composable
private fun SettingsSlider(
    name: String,
    valueRange: ClosedFloatingPointRange<Float>,
) = with(LocalController.current) {
    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        Text(
            i18n("menus.settings.option.$name"),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(5.dp)
        )
        var value by remember { mutableStateOf(getConfig<Float>(name)) }
        CustomSlider(
            modifier = Modifier.weight(1f).padding(5.dp),
            value = value,
            onValueChange = {
                value = it
                setConfig(name, value)
            },
            valueRange = valueRange,
            showIndicator = false,
            displayValue = { (it * 100).roundToInt() },
            thumb = {
                CustomSliderDefaults.Thumb(
                    thumbValue = "$it%",
                    color = Color.Transparent,
                    size = 40.dp,
                    modifier = Modifier.background(
                        shape = CircleShape,
                        brush = Brush.linearGradient(listOf(Color.Green, Color(151, 188, 98)))
                    )
                )
            },
            track = {
                Box(
                    modifier = Modifier
                        .track()
                        .border(
                            width = 1.dp,
                            color = Color.LightGray.copy(alpha = .4f),
                            shape = CircleShape
                        )
                        .background(Color.White)
                        .padding(3.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .progress(it)
                            .background(brush = Brush.linearGradient(listOf(Color(44, 95, 45), Color(151, 188, 98)))),
                    )
                }
            }
        )
        Spacer(modifier = Modifier.size(20.dp))
    }
}