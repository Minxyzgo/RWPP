/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.rwpp.AppContext
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.config.Settings
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.CloseUIPanelEvent
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.game.Game
import io.github.rwpp.i18n.I18nType
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.net.LatestVersionProfile
import io.github.rwpp.net.Net
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.widget.*
import io.github.rwpp.widget.v2.ExpandedCard
import io.github.rwpp.widget.v2.LazyColumnScrollbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsView(
    onCheckUpdate: (LatestVersionProfile) -> Unit,
    defaultTheme: String,
    onChangeTheme: (String) -> Unit,
    onChangeBackgroundImage: (String) -> Unit,
    onExit: () -> Unit
) {
    BackHandler(true, onExit)
    DisposableEffect(Unit) {
        onDispose {
            CloseUIPanelEvent("settings").broadcastIn()
        }
    }

    val configIO = koinInject<ConfigIO>()
    val appContext = koinInject<AppContext>()
    val settings = koinInject<Settings>()


    var backgroundImagePath by remember { mutableStateOf(settings.backgroundImagePath ?: "") }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    settings.backgroundImagePath = backgroundImagePath
                    configIO.saveAllConfig()
                    onChangeBackgroundImage(backgroundImagePath)
                    onExit()
                },
                shape = CircleShape,
                modifier = Modifier.padding(5.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(Icons.Default.Done, null, tint = MaterialTheme.colorScheme.surfaceTint)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        ExpandedCard(Modifier.autoClearFocus()) {
            Box {
                ExitButton(onExit)
                Column {
                    Spacer(Modifier.height(30.dp))
                    var selectedItem by remember { mutableIntStateOf(0) }
                    val items = listOf(
                        "graphics",
                        "gameplay",
                        "audio",
                        "developer",
                        "networking",
                        "rwpp-client",
                        "rwpp-theme"
                    )

                    NavigationBar(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        containerColor = Color.Transparent,
                    ) {
                        items.forEachIndexed { index, s ->
                            NavigationBarItem(
                                icon = {},
                                label = {
                                    Text(
                                        if (s.startsWith("rwpp-"))
                                            readI18n("settings.${s.removePrefix("rwpp-")}", I18nType.RWPP)
                                        else readI18n("menus.settings.heading.$s", I18nType.RW),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                selected = selectedItem == index,
                                onClick = { selectedItem = index },
                            )

                            if (index != items.lastIndex) {
                                VerticalDivider(modifier = Modifier.padding(2.dp).height(40.dp), thickness = 2.dp)
                            }
                        }
                    }

                    LargeDividingLine { 0.dp }

                    val state = rememberLazyListState()

                    LazyColumnScrollbar(listState = state) {
                        LazyColumn(state = state) {
                            item(selectedItem) {
                                val str = items[selectedItem]

                                when (str) {
                                    "graphics" -> SettingsGroup("graphics") {
                                        SettingsSwitchComp("showUnitWaypoints")
                                        SettingsSwitchComp("showHp", "alwayUnitHealth") // I don't why they are different
                                        SettingsSwitchComp("showUnitIcons", "unitIcons")
                                        SettingsSwitchComp("renderVsync")
                                        SettingsSwitchComp("renderClouds")
                                        SettingsSwitchComp("shaderEffects")
                                        SettingsSwitchComp("enableMouseCapture")
                                        SettingsSwitchComp("quickRally")
                                        SettingsSwitchComp("doubleClickToAttackMove")

                                        if (appContext.isDesktop()) {
                                            SettingsSwitchComp(
                                                readI18n("menus.settings.option.immersiveFullScreen", I18nType.RW),
                                                defaultValue = settings.isFullscreen,
                                                customConfigSettingAction = {
                                                    settings.isFullscreen = it
                                                }
                                            )
                                        }
                                    }

                                    "gameplay" -> {
                                        SettingsGroup("gameplay") {
                                            SettingsSwitchComp("showSelectedUnitsList")
                                            SettingsSwitchComp("useMinimapAllyColors")
                                            SettingsSwitchComp("showWarLogOnScreen")
                                            SettingsSwitchComp("smartSelection_v2", "smartSelection") //v2 ???
                                            SettingsSwitchComp(
                                                "forceEnglish",
                                                labelName = readI18n("menus.settings.option.forceEnglish", I18nType.RW)
                                            ) { value ->
                                                settings.forceEnglish = value
                                                configIO.setGameConfig("forceEnglish", value)
                                            }
                                            SettingsSwitchComp("showUnitGroups", "unitGroupInterface")
                                            if (appContext.isDesktop()) {
                                                SettingsSlider(
                                                    readI18n("settings.maxDisplayUnitGroupCount"),
                                                    settings.maxDisplayUnitGroupCount / 10f,
                                                    { settings.maxDisplayUnitGroupCount = (it * 10).roundToInt() },
                                                    valueFormat = { "${(it * 10).roundToInt()}" },
                                                )


                                                val list = remember { listOf("Default", "Software", "OpenGL") }
                                                var selectedIndex by remember { mutableStateOf(list.indexOf(settings.renderingBackend)) }

                                                SettingsDropDown("renderingBackend", list, selectedIndex) { index, backend ->
                                                    settings.renderingBackend = list[index]
                                                    selectedIndex = index
                                                }
                                            }
                                            SettingsSwitchComp(
                                                "",
                                                readI18n("settings.enhancedReinforceTroops"),
                                                settings.enhancedReinforceTroops
                                            ) {
                                                settings.enhancedReinforceTroops = it
                                            }

                                            if (appContext.isDesktop()) {
                                                SettingsSwitchComp(
                                                    "",
                                                    readI18n("settings.showUnitTargetLine"),
                                                    settings.showUnitTargetLine
                                                ) {
                                                    settings.showUnitTargetLine = it
                                                }

                                                SettingsSwitchComp(
                                                    "",
                                                    readI18n("settings.improvedHealthBar"),
                                                    settings.improvedHealthBar
                                                ) {
                                                    settings.improvedHealthBar = it
                                                }
                                            }
                                            var teamUnitCapSinglePlayer by remember { mutableStateOf(configIO.getGameConfig<Int?>("teamUnitCapSinglePlayer")) }
                                            SettingsTextField(
                                                "teamUnitCapSinglePlayer",
                                                teamUnitCapSinglePlayer?.toString() ?: "",
                                                lengthLimitCount = 6,
                                                typeInNumberOnly = true,
                                                typeInOnlyInteger = true
                                            ) {
                                                teamUnitCapSinglePlayer = it.toIntOrNull()
                                                configIO.setGameConfig("teamUnitCapSinglePlayer", teamUnitCapSinglePlayer ?: 100)
                                            }
                                            var teamUnitCapHostedGame by remember { mutableStateOf(configIO.getGameConfig<Int?>("teamUnitCapHostedGame")) }
                                            SettingsTextField(
                                                "teamUnitCapHostedGame",
                                                teamUnitCapHostedGame?.toString() ?: "",
                                                lengthLimitCount = 6,
                                                typeInNumberOnly = true,
                                                typeInOnlyInteger = true
                                            ) {
                                                teamUnitCapHostedGame = it.toIntOrNull()
                                                configIO.setGameConfig("teamUnitCapHostedGame", teamUnitCapHostedGame ?: 100)
                                            }
                                        }
                                        if (appContext.isDesktop()) {
                                            SettingsGroup("", readI18n("settings.buildings")) {
                                                SettingsSwitchComp(
                                                    "",
                                                    readI18n("settings.showAttackRange"),
                                                    settings.showBuildingAttackRange
                                                ) {
                                                    settings.showBuildingAttackRange = it
                                                }

                                                SettingsSwitchComp(
                                                    "",
                                                    readI18n("settings.showExtraButton"),
                                                    settings.showExtraButton
                                                ) {
                                                    settings.showExtraButton = it
                                                }
                                            }

                                            SettingsGroup("", readI18n("settings.units")) {
                                                val list = listOf("Never", "Land", "Air", "All")
                                                var selectedIndex by remember { mutableStateOf(list.indexOf(settings.showAttackRangeUnit)) }
                                                SettingsDropDown("showAttackRange", list, selectedIndex) { index, type ->
                                                    selectedIndex = index
                                                    settings.showAttackRangeUnit = type
                                                }
                                            }
                                        }
                                    }

                                    "audio" -> SettingsGroup("audio") {
                                        SettingsSliderRW("masterVolume")
                                        SettingsSliderRW("gameVolume")
                                        SettingsSliderRW("interfaceVolume")
                                        SettingsSliderRW("musicVolume")
                                    }

                                    "developer" -> SettingsGroup("developer") {
                                        SettingsSwitchComp("showFps")
                                        SettingsSwitchComp(
                                            "Show Welcome Message",
                                            defaultValue = settings.showWelcomeMessage ?: false
                                        ) { settings.showWelcomeMessage = it }
                                    }

                                    "networking" -> SettingsGroup("networking") {
                                        SettingsSwitchComp("udpInMultiplayer")
                                        SettingsSwitchComp("saveMultiplayerReplays", "saveReplays")
                                        SettingsSwitchComp("showChatAndPingShortcuts")
                                        SettingsSwitchComp("showMapPingsOnBattlefield")
                                        SettingsSwitchComp("showMapPingsOnMinimap")
                                        SettingsSwitchComp("showPlayerChatInGame")
                                    }

                                    "rwpp-client" -> {
                                        val net = koinInject<Net>()
                                        var checking by remember { mutableStateOf(false) }
                                        val scope = rememberCoroutineScope()

                                        SettingsGroup("", readI18n("settings.client")) {
                                            Row {
                                                RWTextButton(readI18n("settings.checkUpdate"), modifier = Modifier.padding(5.dp)) {
                                                    checking = true
                                                    scope.launch(Dispatchers.IO) {
                                                        net.getLatestVersionProfile()?.let(onCheckUpdate)
                                                        checking = false
                                                    }
                                                }

                                                if (checking) CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondaryContainer)
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

                                    "rwpp-theme" -> {
                                        val list = themes.keys.toList()
                                        var selectedIndex by remember { mutableStateOf(list.indexOf(defaultTheme)) }
                                        SettingsGroup("", readI18n("settings.theme")) {
                                            SettingsSwitchComp(
                                                "",
                                                readI18n("settings.enableAnimations"),
                                                settings.enableAnimations
                                            ) {
                                                settings.enableAnimations = it
                                            }

                                            SettingsSwitchComp(
                                                "",
                                                readI18n("settings.boldText"),
                                                settings.boldText
                                            ) {
                                                settings.boldText = it
                                            }

                                            SettingsSwitchComp(
                                                "",
                                                readI18n("settings.changeGameTheme"),
                                                settings.changeGameTheme
                                            ) {
                                                settings.changeGameTheme = it
                                            }

                                            SettingsDropDown("colorScheme", list, selectedIndex,
                                                selectedItemColor = { theme, _ -> themes[theme]!!.primary }
                                            ) { index, theme ->
                                                onChangeTheme(theme)
                                                selectedIndex = index
                                            }

                                            val externalHandler = koinInject<ExternalHandler>()

                                            SettingsTextField(
                                                "setBackgroundImagePath",
                                                backgroundImagePath,
                                                onValueChange = {
                                                    backgroundImagePath = it
                                                },
                                                trailingIcon = {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.List,
                                                        null,
                                                        modifier = Modifier.clickable {
                                                            externalHandler.openFileChooser { backgroundImagePath = it.canonicalPath }
                                                        }
                                                    )
                                                },
                                            )

                                            SettingsSlider(
                                                readI18n("settings.backgroundTransparency"),
                                                settings.backgroundTransparency,
                                                {
                                                    settings.backgroundTransparency = it
                                                    UI.backgroundTransparency = it
                                                }
                                            )
                                        }
                                    }
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
private fun LazyItemScope.SettingsGroup(
    name: String,
    displayName: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp).then(if (koinInject<Settings>().enableAnimations)
        Modifier.animateItem()
    else Modifier)) {
        Text(
            displayName ?: readI18n("menus.settings.heading.$name", I18nType.RW),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 5.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer.copy(.6f),
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
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, checkedThumbColor = MaterialTheme.colorScheme.onSurface),
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    lengthLimitCount: Int = Int.MAX_VALUE,
    typeInOnlyInteger: Boolean = false,
    typeInNumberOnly: Boolean = false,
    enabled: Boolean = true,
    appendedContent: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = readI18n("settings.$label"),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                RWSingleOutlinedTextField(
                    "",
                    value,
                    Modifier.width(300.dp).padding(end = 15.dp),
                    trailingIcon,
                    leadingIcon,
                    lengthLimitCount,
                    typeInOnlyInteger,
                    typeInNumberOnly,
                    enabled,
                    appendedContent,
                    onValueChange = onValueChange
                )

            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun <T> SettingsDropDown(
    name: String,
    items: List<T>,
    selectedIndex: Int = 0,
    selectedItemColor: @Composable (T?, Int) -> Color = { _, _ -> MaterialTheme.colorScheme.onSurface },
    onSelectedItem: (Int, T) -> Unit,
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = readI18n("settings.$name"),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                LargeDropdownMenu(
                    modifier = Modifier.width(300.dp).padding(end = 15.dp),
                    items = items,
                    label = "",
                    selectedIndex = selectedIndex,
                    onItemSelected = onSelectedItem,
                    selectedItemColor = selectedItemColor
                )
            }

            HorizontalDivider()
        }
    }
}

@Composable
private fun SettingsSlider(
    name: String,
    defaultValue: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueFormat: (Float) -> String = { (it * 100).roundToInt().toString() + "%" }
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(16.dp)
                )
            }

            var value by remember { mutableStateOf(defaultValue) }

            remember(value) {
                onValueChange(value)
            }

            Spacer(modifier = Modifier.weight(1f))

            Slider(
                value = value,
                valueRange = valueRange,
                modifier = Modifier.width(250.dp).padding(end = 5.dp),
                onValueChange = { value = it },
                colors = RWSliderColors
            )

            Text(valueFormat(value), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(50.dp).padding(top = 6.dp, end = 5.dp))
        }

        HorizontalDivider()
    }

}

@Composable
private fun SettingsSliderRW(
    name: String,
) {
    val configIO = koinInject<ConfigIO>()

    SettingsSlider(
        readI18n("menus.settings.option.$name", I18nType.RW),
        configIO.getGameConfig(name),
        { configIO.setGameConfig(name, it) },
        0f..1f,
    )
}