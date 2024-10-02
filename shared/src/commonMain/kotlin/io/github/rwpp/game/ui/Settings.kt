/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import io.github.rwpp.app.PermissionHelper
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.config.Settings
import io.github.rwpp.game.Game
import io.github.rwpp.i18n.I18nType
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.net.Net
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.platform.Platform
import io.github.rwpp.ui.*
import io.github.rwpp.ui.v2.ExpandedCard
import io.github.rwpp.ui.v2.LazyColumnScrollbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun SettingsView(
    onCheckUpdate: (String) -> Unit,
    onExit: () -> Unit
) {
    BackHandler(true, onExit)

    val configIO = koinInject<ConfigIO>()
    koinInject<Game>()
    val settings = koinInject<Settings>()

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    configIO.saveAllConfig()
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
        ExpandedCard {
            ExitButton(onExit)

//            var selectedItem by remember { mutableIntStateOf(0) }
//            val items = listOf("graphics", "gameplay")
//
//            NavigationBar(
//                modifier = Modifier.fillMaxWidth(),
//                containerColor = Color(27, 18, 18),
//            ) {
//                items.forEachIndexed { index, s ->
//                    NavigationBarItem(
//                        icon = {},
//                        label = { Text(readI18n("menus.settings.heading.$s", I18nType.RW), style = MaterialTheme.typography.bodyLarge) },
//                        selected = selectedItem == index,
//                        onClick = { selectedItem = index },
//                    )
//                }
//            }

            val state = rememberLazyListState()

            LazyColumnScrollbar(listState = state) {
                LazyColumn(state = state) {
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

                            if (Platform.isDesktop()) {
                                SettingsSwitchComp(
                                    readI18n("menus.settings.option.immersiveFullScreen", I18nType.RW),
                                    defaultValue = settings.isFullscreen,
                                    customConfigSettingAction = {
                                        settings.isFullscreen = it
                                    }
                                )
                            }
                        }
                    }

                    item {
                        SettingsGroup("gameplay") {
                            SettingsSwitchComp("showSelectedUnitsList")
                            SettingsSwitchComp("useMinimapAllyColors")
                            SettingsSwitchComp("showWarLogOnScreen")
                            SettingsSwitchComp("smartSelection_v2", "smartSelection") //v2 ???
                            SettingsSwitchComp("forceEnglish")
                            var teamUnitCapSinglePlayer by remember { mutableStateOf(configIO.getGameConfig<Int?>("teamUnitCapSinglePlayer")) }
                            RWSingleOutlinedTextField(
                                "teamUnitCapSinglePlayer",
                                teamUnitCapSinglePlayer?.toString() ?: "",
                                modifier = Modifier.fillMaxWidth(),
                                lengthLimitCount = 6,
                                typeInNumberOnly = true,
                                typeInOnlyInteger = true
                            ) {
                                teamUnitCapSinglePlayer = it.toIntOrNull()
                                configIO.setGameConfig("teamUnitCapSinglePlayer", teamUnitCapSinglePlayer ?: 100)
                            }
                            var teamUnitCapHostedGame by remember { mutableStateOf(configIO.getGameConfig<Int?>("teamUnitCapHostedGame")) }
                            RWSingleOutlinedTextField(
                                "teamUnitCapHostedGame",
                                teamUnitCapHostedGame?.toString() ?: "",
                                modifier = Modifier.fillMaxWidth(),
                                lengthLimitCount = 6,
                                typeInNumberOnly = true,
                                typeInOnlyInteger = true
                            ) {
                                teamUnitCapHostedGame = it.toIntOrNull()
                                configIO.setGameConfig("teamUnitCapHostedGame", teamUnitCapHostedGame ?: 100)
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
                            SettingsSwitchComp(
                                "Show Welcome Message",
                                defaultValue = settings.showWelcomeMessage ?: false
                            ) { settings.showWelcomeMessage = it }
                        }
                    }

                    item {
                        SettingsGroup("networking") {
                            var port by remember { mutableStateOf(configIO.getGameConfig<Int?>("networkPort")) }
                            RWSingleOutlinedTextField(
                                "port",
                                port?.toString() ?: "",
                                modifier = Modifier.fillMaxWidth(),
                                lengthLimitCount = 5,
                                typeInNumberOnly = true
                            ) {
                                port = it.toIntOrNull()
                                configIO.setGameConfig("networkPort", port ?: 5123)
                            }
                            SettingsSwitchComp("udpInMultiplayer")
                            SettingsSwitchComp("showChatAndPingShortcuts")
                            SettingsSwitchComp("showMapPingsOnBattlefield")
                            SettingsSwitchComp("showMapPingsOnMinimap")
                            SettingsSwitchComp("showPlayerChatInGame")
                        }
                    }

                    item {
                        val net = koinInject<Net>()
                        var checking by remember { mutableStateOf(false) }
                        val scope = rememberCoroutineScope()
                        
                        SettingsGroup("", readI18n("settings.client")) {
                            Row {
                                RWTextButton(readI18n("settings.checkUpdate"), modifier = Modifier.padding(5.dp)) {
                                    checking = true
                                    scope.launch(Dispatchers.IO) {
                                        net.getLatestVersion()?.let(onCheckUpdate)
                                        checking = false
                                    }
                                }

                                if (checking) CircularProgressIndicator(color = Color(199, 234, 70))
                            }

                            SettingsSwitchComp(
                                "",
                                readI18n("settings.autoCheckUpdate"),
                                settings.autoCheckUpdate
                            ) {
                                settings.autoCheckUpdate = it
                            }
                        }
                    }

                    if(Platform.isAndroid()) {
                        item {
                            SettingsGroup("", "Android") {
                                val permissionHelper = koinInject<PermissionHelper>()
                                RWTextButton(readI18n("settings.setExternalFolder")) {
                                    permissionHelper.requestExternalStoragePermission()
                                }

                                RWTextButton(readI18n("settings.manageAllFiles")) {
                                    permissionHelper.requestManageFilePermission()
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
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            displayName ?: readI18n("menus.settings.heading.$name", I18nType.RW),
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
) {
    val configIO = koinInject<ConfigIO>()

    var state by remember { mutableStateOf(defaultValue ?: configIO.getGameConfig(name)) }
    val onClick = customConfigSettingAction?.let{
        {
            state = !state
            customConfigSettingAction(state)
        }
    } ?: {
        state = !state
        configIO.setGameConfig(name, state)
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
                        text = if(customConfigSettingAction != null) labelName else readI18n("menus.settings.option.$labelName", I18nType.RW),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = state,
                    modifier = Modifier.padding(end = 15.dp),
                    onCheckedChange = { onClick() },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(151, 188, 98), checkedThumbColor = Color.White),
                )
            }
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSlider(
    name: String,
    valueRange: ClosedFloatingPointRange<Float>,
) {
    val configIO = koinInject<ConfigIO>()

    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        Text(
            readI18n("menus.settings.option.$name", I18nType.RW),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(5.dp)
        )
        var value by remember { mutableStateOf(configIO.getGameConfig<Float>(name)) }
        CustomSlider(
            modifier = Modifier.weight(1f).padding(5.dp),
            value = value,
            onValueChange = {
                value = it
                configIO.setGameConfig(name, value)
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
                    ),
                    content = {
                        Text(
                            text = "$it%",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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