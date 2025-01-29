/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("DuplicatedCode")

package io.github.rwpp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import io.github.rwpp.config.CoreData
import io.github.rwpp.config.Settings
import io.github.rwpp.core.UI
import io.github.rwpp.core.UI.showRoomView
import io.github.rwpp.core.UI.showMultiplayerView
import io.github.rwpp.core.UI.showMissionView
import io.github.rwpp.core.UI.showReplayView
import io.github.rwpp.core.UI.showSettingsView
import io.github.rwpp.core.UI.showModsView
import io.github.rwpp.core.UI.showExtensionView
import io.github.rwpp.core.UI.showContributorList
import io.github.rwpp.game.Game
import io.github.rwpp.game.ui.ContributorList
import io.github.rwpp.game.ui.ExtensionView
import io.github.rwpp.game.ui.MissionView
import io.github.rwpp.game.ui.ModsView
import io.github.rwpp.game.ui.MultiplayerRoomView
import io.github.rwpp.game.ui.MultiplayerView
import io.github.rwpp.game.ui.ReplaysViewDialog
import io.github.rwpp.game.ui.SettingsView
import io.github.rwpp.i18n.I18nType
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.net.Net
import io.github.rwpp.platform.loadSvg
import io.github.rwpp.ui.AnimatedAlertDialog
import io.github.rwpp.ui.BorderCard
import io.github.rwpp.ui.ConstraintWindowManager
import io.github.rwpp.ui.ExitButton
import io.github.rwpp.ui.GeneralProportion
import io.github.rwpp.ui.JostFonts
import io.github.rwpp.ui.LargeDividingLine
import io.github.rwpp.ui.MenuButton
import io.github.rwpp.ui.RWSelectionColors
import io.github.rwpp.ui.RWSingleOutlinedTextField
import io.github.rwpp.ui.RWTextButton
import io.github.rwpp.ui.ValoraxFont
import io.github.rwpp.ui.WindowManager
import io.github.rwpp.ui.autoClearFocus
import io.github.rwpp.ui.themes
import io.github.rwpp.ui.v2.RWIconButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

var LocalWindowManager = staticCompositionLocalOf { WindowManager.Large }

@Composable
fun App(
    sizeModifier: Modifier = Modifier.fillMaxSize(),
    isPremium: Boolean = false,
    onChangeBackgroundImage: (String) -> Unit,
) {
    val coreData = koinInject<CoreData>()
    val settings = koinInject<Settings>()
    val net = koinInject<Net>()

    val jostFonts = JostFonts()
    val valoraxFont = ValoraxFont()

    var selectedColorSchemeName by remember { mutableStateOf(settings.selectedTheme ?: "RWPP") }
    val selectedColorScheme = remember(selectedColorSchemeName) { if (isPremium) themes[selectedColorSchemeName]!! else themes["RWPP"]!! }

    val typography = Typography(
        displayLarge = TextStyle(
            color = selectedColorScheme.onSurface,
            fontFamily = valoraxFont,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp
        ),
        headlineLarge = TextStyle(
            color = selectedColorScheme.onSurface,
            fontFamily = jostFonts,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp
        ),
        headlineMedium = TextStyle(
            color = selectedColorScheme.onSurface,
            fontFamily = jostFonts,
            fontWeight = FontWeight.Normal,
            fontSize = 19.sp
        ),
        headlineSmall = TextStyle(
            color = selectedColorScheme.onSurface,
            fontFamily = jostFonts,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp
        ),
        bodyLarge = TextStyle(
            color = selectedColorScheme.onSurface,
            fontFamily = jostFonts,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        ),
        bodyMedium = TextStyle(
            color = selectedColorScheme.onSurface,
            fontFamily = jostFonts,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp
        )
    )

    var isSinglePlayerGame by remember { mutableStateOf(false) }

    var checkUpdateDialogVisible by remember { mutableStateOf(false) }
    var latestVersion by remember { mutableStateOf<String?>(null) }
    var latestVersionBody by remember { mutableStateOf("null") }


    LaunchedEffect(Unit) {
        val now = System.currentTimeMillis()
        coreData.lastPlayTime = now
        // per day
        if ((now - (coreData.lastAutoCheckUpdateTime + 1000 * 60 * 60 * 24) > 0) || coreData.debug) {
            withContext(Dispatchers.IO) {
                if (settings.autoCheckUpdate) {
                    val profile = net.getLatestVersionProfile()

                    if (profile != null) {
                        coreData.lastAutoCheckUpdateTime = now
                        latestVersion = profile.version
                        latestVersionBody = profile.body
                        if (latestVersion != projectVersion && settings.ignoreVersion != latestVersion || coreData.debug) {
                            checkUpdateDialogVisible = true
                        }
                    }
                }
            }
        }
    }

    val showMainMenu = !(showMultiplayerView
            || showMissionView
            || showSettingsView
            || showModsView
            || showRoomView
            || showExtensionView
            || showReplayView
            || showContributorList)


    val game = koinInject<Game>()

    MaterialTheme(
        typography = typography,
        colorScheme = selectedColorScheme
    ) {

        BoxWithConstraints(
            modifier = Modifier
                .then(sizeModifier)
                .autoClearFocus()
                //.background(brush),
        ) {
            CompositionLocalProvider(
                LocalTextSelectionColors provides RWSelectionColors,
                LocalWindowManager provides ConstraintWindowManager(maxWidth, maxHeight)
            ) {

                Scaffold(
                    containerColor = Color.Transparent,
                    floatingActionButton = {
                        if(game.isGameCouldContinue() && (showMainMenu || showMissionView)) {
                            FloatingActionButton(
                                onClick = { game.continueGame() },
                                shape = CircleShape,
                                modifier = Modifier.padding(5.dp),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.surfaceTint)
                            }
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) {
                    AnimatedVisibility(
                        showMainMenu,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        MainMenu(
                            multiplayer = {
                                isSinglePlayerGame = false
                                showMultiplayerView = true
                            },
                            mission = {
                                showMissionView = true
                            },
                            skirmish = {
                                showRoomView = true
                                isSinglePlayerGame = true
                                game.hostNewSinglePlayer(false)
                            },
                            settings = {
                                showSettingsView = true
                            },
                            mods = {
                                showModsView = true
                            },
                            sandbox = {
                                isSinglePlayerGame = true
                                showRoomView = true
                                game.hostNewSinglePlayer(sandbox = true)
                            },
                            extension = {
                                showExtensionView = true
                            },
                            replay = {
                                showReplayView = true
                            },
                            contributor = {
                                showContributorList = true
                            }
                        )
                    }

                    AnimatedVisibility(
                        showMissionView
                    ) {
                        MissionView { showMissionView = false }
                    }
                }

                AnimatedVisibility(
                    showMultiplayerView,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    MultiplayerView(
                        { showMultiplayerView = false },
                        { showRoomView = true },
                    )
                }

                AnimatedVisibility(
                    showSettingsView,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    SettingsView(
                        {
                            if (it.version == projectVersion || settings.ignoreVersion == it.version) return@SettingsView
                            latestVersion = it.version
                            latestVersionBody = it.body
                            checkUpdateDialogVisible = true
                        },
                        selectedColorSchemeName,
                        { theme ->
                            settings.selectedTheme = theme
                            selectedColorSchemeName = theme
                        },
                        onChangeBackgroundImage
                    ) { showSettingsView = false }
                }

                AnimatedVisibility(
                    showModsView
                ) {
                    ModsView { showModsView = false }
                }

                AnimatedVisibility(
                    showExtensionView
                ) {
                    ExtensionView {
                        showExtensionView = false
                    }
                }

                AnimatedVisibility(
                    showReplayView
                ) {
                    ReplaysViewDialog {
                        showReplayView = false
                    }
                }

                AnimatedVisibility(
                    showRoomView
                ) {
                    MultiplayerRoomView(isSinglePlayerGame) {
                        if(!isSinglePlayerGame) {
                            game.cancelJoinServer()
                            showMultiplayerView = true
                        }

                        game.onBanUnits(listOf())
                        game.gameRoom.disconnect()

                        showRoomView = false
                    }
                }

                AnimatedVisibility(
                    showContributorList,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    ContributorList {
                        showContributorList = false
                    }
                }

                var warningDialogVisible by remember { mutableStateOf(false) }

                remember(UI.warning) {
                    if (UI.warning != null) {
                        warningDialogVisible = true
                        if (UI.warning?.isKicked == true) {
                            showRoomView = false
                            showMultiplayerView = true
                        }
                    }
                }

                AnimatedAlertDialog(
                    warningDialogVisible,
                    onDismissRequest = { warningDialogVisible = false }) { dismiss ->
                    BorderCard(
                       modifier = Modifier.size(500.dp).autoClearFocus(),
                    ) {
                        ExitButton(dismiss)

                        Row(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                            HorizontalDivider(Modifier.weight(1f), thickness = 2.dp, color = Color.DarkGray)
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
                                UI.warning?.reason ?: "",
                                modifier = Modifier.padding(5.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                AnimatedAlertDialog(
                    checkUpdateDialogVisible,
                    onDismissRequest = { checkUpdateDialogVisible = false }
                ) { dismiss ->
                    BorderCard(modifier = Modifier.fillMaxWidth(GeneralProportion()).verticalScroll(rememberScrollState())) {

                        Row(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                            HorizontalDivider(Modifier.weight(1f), thickness = 2.dp, color = Color.DarkGray)
                            Box {
                                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(50.dp).offset(5.dp, 5.dp).blur(2.dp))
                                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                            }
                            HorizontalDivider(Modifier.weight(1f), thickness = 2.dp, color = Color.DarkGray)
                        }

                        Text(readI18n("settings.newVersion", I18nType.RWPP, latestVersion!!),
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(5.dp),
                            style = MaterialTheme.typography.headlineMedium
                        )

                        val annotatedString = buildAnnotatedString {
                            withLink(
                                link = LinkAnnotation
                                    .Clickable(
                                        tag = "github",
                                        linkInteractionListener = { net.openUriInBrowser("https://github.com/Minxyzgo/RWPP/releases") },
                                        styles = TextLinkStyles(style = SpanStyle(color = Color(0, 0, 238), textDecoration = TextDecoration.Underline))
                                    )
                            ) {
                                append(readI18n("settings.goToDownload", I18nType.RWPP, "github"))
                            }

                            append("\n")

                            withLink(
                                link = LinkAnnotation
                                    .Clickable(
                                        tag = "123pan",
                                        linkInteractionListener = { net.openUriInBrowser("https://www.123684.com/s/6Rijjv-79Fi?提取码:VtDG") },
                                        styles = TextLinkStyles(style = SpanStyle(color = Color(0, 0, 238), textDecoration = TextDecoration.Underline))
                                    )
                            ) {
                                append(readI18n("settings.goToDownload", I18nType.RWPP, "123pan"))
                            }
                        }


                        Text(annotatedString,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(2.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        RWTextButton(
                            readI18n("settings.ignoreVersion"),
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(5.dp)
                        ) {
                            settings.ignoreVersion = latestVersion
                            dismiss()
                        }

                        BorderCard(backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)) {
                            Markdown(latestVersionBody, modifier = Modifier.padding(5.dp), colors = markdownColor(), typography = markdownTypography())
                        }
                    }
                }

                var questionDialogVisible by remember { mutableStateOf(false) }
                remember(UI.question) {
                    questionDialogVisible = UI.question != null
                }


                AnimatedAlertDialog(questionDialogVisible,
                    onDismissRequest = {
                        questionDialogVisible = false
                        UI.question?.callback?.invoke(null)
                        if(showRoomView) {
                            showRoomView = false
                            showMultiplayerView = true
                        }
                    }
                ) { _ ->
                    BorderCard(
                        modifier = Modifier.fillMaxWidth(if (LocalWindowManager.current == WindowManager.Small) 0.9f else 0.75f).autoClearFocus(),
                    ) {

                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xE9EE8888),
                                        Color(0xFFE4BD79)))
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, null, modifier = Modifier.size(25.dp).padding(5.dp))
                                    Text(
                                        UI.question?.title ?: "",
                                        modifier = Modifier.padding(5.dp),
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        LargeDividingLine { 0.dp }
                        Column(
                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                UI.question?.message ?: "",
                                modifier = Modifier.padding(5.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            var message by remember { mutableStateOf("") }
                            RWSingleOutlinedTextField(
                                label = "Reply",
                                value = message,
                                modifier = Modifier.fillMaxWidth().padding(10.dp)
                                    .onKeyEvent {
                                        if(it.key == androidx.compose.ui.input.key.Key.Enter && message.isNotEmpty()) {
                                            UI.question?.callback?.invoke(message)
                                            message = ""
                                            questionDialogVisible = false
                                            true
                                        } else false
                                    },
                                trailingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        null,
                                        modifier = Modifier.clickable {
                                            UI.question?.callback?.invoke(message)
                                            message = ""
                                            questionDialogVisible = false
                                        }
                                    )
                                },
                                onValueChange =
                                {
                                    message = it
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenu(
    multiplayer: () -> Unit,
    mission: () -> Unit,
    skirmish: () -> Unit,
    settings: () -> Unit,
    mods: () -> Unit,
    sandbox: () -> Unit,
    extension: () -> Unit,
    replay: () -> Unit,
    contributor: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.weight(1f))

        Text(
            "RWPP",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = TextStyle(
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)),
                fontSize = 100.sp
            )
        )

        Text(
            "$projectVersion (core $coreVersion)",
            modifier = Modifier.padding(top = 1.dp, bottom = 5.dp).align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background.copy((UI.backgroundTransparency + 0.1f).coerceAtMost(1f))),
            border = BorderStroke(4.dp, MaterialTheme.colorScheme.surfaceContainer),
            shape = RectangleShape
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                item {
                    MenuButton(
                        readI18n("menu.mission"),
                        loadSvg("destruction"),
                        onClick = mission
                    )
                }

                item {
                    MenuButton(
                        readI18n("menus.singlePlayer.skirmish", I18nType.RW),
                        loadSvg("swords"),
                        onClick = skirmish
                    )
                }

                item {
                    MenuButton(
                        readI18n("menu.multiplayer"),
                        loadSvg("group"),
                        onClick = multiplayer
                    )
                }

                item {
                    MenuButton(
                        readI18n("menu.mods"),
                        loadSvg("dns"),
                        onClick = mods
                    )
                }

                item {
                    MenuButton(
                        readI18n("menu.sandbox"),
                        loadSvg("edit_square"),
                        onClick = sandbox
                    )
                }

                item {
                    MenuButton(
                        readI18n("menu.settings"),
                        Icons.Default.Settings,
                        onClick = settings
                    )
                }

                item {
                    MenuButton(
                        readI18n("menu.extension"),
                        loadSvg("extension"),
                        onClick = extension
                    )
                }


                item {
                    MenuButton(
                        readI18n("menu.replay"),
                        Icons.Default.PlayArrow,
                        onClick = replay
                    )
                }

                item {
                    with(koinInject<AppContext>()) {
                        MenuButton(
                            readI18n("menu.exit"),
                            loadSvg("exit"),
                        ) { exit() }
                    }
                }
            }
        }




        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            RWIconButton(Icons.Filled.Favorite, modifier = Modifier.padding(10.dp), onClick = contributor)

            val net = koinInject<Net>()

            RWIconButton(loadSvg("library"), modifier = Modifier.padding(10.dp)) {
                net.openUriInBrowser("https://rwpp.netlify.app/")
            }

            RWIconButton(loadSvg("octocat"), modifier = Modifier.padding(10.dp)) {
                net.openUriInBrowser("https://github.com/Minxyzgo/RWPP")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}