/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("DuplicatedCode")

package io.github.rwpp

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.graphicsLayer
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
import io.github.rwpp.core.UI.selectedColorSchemeName
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
import io.github.rwpp.net.LatestVersionProfile
import io.github.rwpp.net.Net
import io.github.rwpp.platform.loadSvg
import io.github.rwpp.ui.*
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


    var isSinglePlayerGame by remember { mutableStateOf(false) }

    var checkUpdateDialogVisible by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<LatestVersionProfile?>(null) }

    LaunchedEffect(Unit) {
        val now = System.currentTimeMillis()
        coreData.lastPlayTime = now
        // per day
        if ((now - (coreData.lastAutoCheckUpdateTime + 1000 * 60 * 60 * 24) > 0) || coreData.debug) {
            withContext(Dispatchers.IO) {
                if (settings.autoCheckUpdate) {
                    val _profile = net.getLatestVersionProfile()

                    if (_profile != null) {
                        coreData.lastAutoCheckUpdateTime = now

                        if (_profile.version != projectVersion && settings.ignoreVersion != _profile.version || coreData.debug) {
                            profile = _profile
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

    RWPPTheme {

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

                val enableAnimations = settings.enableAnimations

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
                        enter = if(enableAnimations) fadeIn() else EnterTransition.None,
                        exit = if(enableAnimations) fadeOut() else ExitTransition.None,
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
                        showMissionView,
                        enter = if (enableAnimations) fadeIn() + expandIn() else EnterTransition.None,
                        exit = if (enableAnimations) shrinkOut() + fadeOut() else ExitTransition.None,
                    ) {
                        MissionView { showMissionView = false }
                    }
                }

                AnimatedVisibility(
                    showMultiplayerView,
                    enter = if(enableAnimations) fadeIn() + slideInVertically() else EnterTransition.None,
                    exit = if(enableAnimations) fadeOut() + slideOutVertically() else ExitTransition.None,
                ) {
                    MultiplayerView(
                        { showMultiplayerView = false },
                        { showRoomView = true },
                    )
                }

                AnimatedVisibility(
                    showSettingsView,
                    enter = if (enableAnimations) fadeIn() + slideInVertically() else EnterTransition.None,
                    exit = if (enableAnimations) fadeOut() + slideOutVertically() else ExitTransition.None,
                ) {
                    SettingsView(
                        {
                            if (it.version == projectVersion || settings.ignoreVersion == it.version) return@SettingsView
                            profile = it
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
                    showModsView,
                    enter = if (enableAnimations) fadeIn() + expandIn() else EnterTransition.None,
                    exit = if (enableAnimations) shrinkOut() + fadeOut() else ExitTransition.None,
                ) {
                    ModsView { showModsView = false }
                }

                AnimatedVisibility(
                    showExtensionView,
                    enter = if (enableAnimations) fadeIn() + expandIn() else EnterTransition.None,
                    exit = if (enableAnimations) shrinkOut() + fadeOut() else ExitTransition.None,
                ) {
                    ExtensionView {
                        showExtensionView = false
                    }
                }

                AnimatedVisibility(
                    showReplayView,
                    enter = if (enableAnimations) fadeIn() + expandIn() else EnterTransition.None,
                    exit = if (enableAnimations) shrinkOut() + fadeOut() else ExitTransition.None,
                ) {
                    ReplaysViewDialog {
                        showReplayView = false
                    }
                }

                AnimatedVisibility(
                    showRoomView,
                    enter = if (enableAnimations) fadeIn() + expandIn() else EnterTransition.None,
                    exit = if (enableAnimations) shrinkOut() + fadeOut() else ExitTransition.None,
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
                                Icon(
                                    Icons.Default.Warning,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(50.dp).offset(5.dp, 5.dp).blur(2.dp)
                                )
                                Icon(
                                    Icons.Default.Warning,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                            HorizontalDivider(Modifier.weight(1f), thickness = 2.dp, color = Color.DarkGray)
                        }

                        Text(
                            readI18n("settings.newVersion", I18nType.RWPP, (if (profile!!.prerelease) "Prerelease " else "") + profile!!.version),
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(5.dp),
                            style = MaterialTheme.typography.headlineMedium
                        )

                        val annotatedString = buildAnnotatedString {
                            if (!profile!!.prerelease) {
                                withLink(
                                    link = LinkAnnotation
                                        .Clickable(
                                            tag = "github",
                                            linkInteractionListener = { net.openUriInBrowser("https://github.com/Minxyzgo/RWPP/releases") },
                                            styles = TextLinkStyles(
                                                style = SpanStyle(
                                                    color = Color(0, 0, 238),
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            )
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
                                            styles = TextLinkStyles(
                                                style = SpanStyle(
                                                    color = Color(0, 0, 238),
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            )
                                        )
                                ) {
                                    append(readI18n("settings.goToDownload", I18nType.RWPP, "123pan"))
                                }
                            } else {
                                append(readI18n("settings.prereleaseNote\n"))

                                withLink(
                                    link = LinkAnnotation
                                        .Clickable(
                                            tag = "afdian",
                                            linkInteractionListener = { net.openUriInBrowser("https://afdian.com/a/minxyzgo") },
                                            styles = TextLinkStyles(
                                                style = SpanStyle(
                                                    color = Color(0, 0, 238),
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            )
                                        )
                                ) {
                                    append(readI18n("settings.goToDownload", I18nType.RWPP, "爱发电"))
                                }
                            }
                        }

                        Text(
                            annotatedString,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(2.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        RWTextButton(
                            readI18n("settings.ignoreVersion"),
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(5.dp)
                        ) {
                            settings.ignoreVersion = profile!!.version
                            dismiss()
                        }

                        BorderCard(backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)) {
                            Markdown(
                                profile!!.body,
                                modifier = Modifier.padding(5.dp),
                                colors = markdownColor(),
                                typography = markdownTypography()
                            )
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

                        UI.question = null
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
            val state = rememberLazyListState()
            LazyRow(
                state = state,
                modifier = Modifier.fillMaxWidth().lazyRowDesktopScrollable(state),
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
            RWIconButton(Icons.Filled.Favorite, modifier = Modifier.padding(10.dp), tint = Color.Red, onClick = contributor)

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