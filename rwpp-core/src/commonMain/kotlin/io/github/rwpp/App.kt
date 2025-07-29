/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("DuplicatedCode")

package io.github.rwpp

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import io.github.rwpp.coil.ImageableFetcherFactory
import io.github.rwpp.coil.ImageableKeyer
import io.github.rwpp.config.CoreData
import io.github.rwpp.config.Settings
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadcast
import io.github.rwpp.event.events.KeyboardEvent
import io.github.rwpp.event.events.ReloadModEvent
import io.github.rwpp.event.events.ReloadModFinishedEvent
import io.github.rwpp.event.events.ReturnMainMenuEvent
import io.github.rwpp.event.onDispose
import io.github.rwpp.game.Game
import io.github.rwpp.i18n.I18nType
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.net.LatestVersionProfile
import io.github.rwpp.net.Net
import io.github.rwpp.rwpp_core.generated.resources.*
import io.github.rwpp.ui.*
import io.github.rwpp.ui.UI.selectedColorSchemeName
import io.github.rwpp.ui.UI.showContributorList
import io.github.rwpp.ui.UI.showExtensionView
import io.github.rwpp.ui.UI.showMissionView
import io.github.rwpp.ui.UI.showModsView
import io.github.rwpp.ui.UI.showMultiplayerView
import io.github.rwpp.ui.UI.showReplayView
import io.github.rwpp.ui.UI.showResourceBrowser
import io.github.rwpp.ui.UI.showRoomView
import io.github.rwpp.ui.UI.showSettingsView
import io.github.rwpp.widget.*
import io.github.rwpp.widget.v2.LineSpinFadeLoaderIndicator
import io.github.rwpp.widget.v2.RWIconButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

var LocalWindowManager = staticCompositionLocalOf { WindowManager.Large }

@Suppress("UnusedBoxWithConstraintsScope", "UnusedMaterial3ScaffoldPaddingParameter")
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

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                add(ImageableFetcherFactory())
                add(ImageableKeyer())
            }
            .build()
    }

    val showMainMenu = !(showMultiplayerView
            || showMissionView
            || showSettingsView
            || showModsView
            || showRoomView
            || showExtensionView
            || showReplayView
            || showContributorList
            || showResourceBrowser)

    val game = koinInject<Game>()

    val globalFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }

    RWPPTheme {
        BoxWithConstraints(
            modifier = Modifier
                .then(sizeModifier)
                .focusRequester(globalFocusRequester)
                .clickable(
                    interactionSource,
                    null
                ) {
                    globalFocusRequester.requestFocus()
                    keyboardController?.hide()
                }.onKeyEvent {
                    runBlocking {
                        if (it.type == KeyEventType.KeyDown) {
                            KeyboardEvent(it.key.keyCode.toInt()).broadcast().isIntercepted
                        } else false
                    }
                }
        ) {
            CompositionLocalProvider(
                LocalTextSelectionColors provides RWSelectionColors,
                LocalWindowManager provides ConstraintWindowManager(maxWidth, maxHeight)
            ) {

                val enableAnimations = settings.enableAnimations
                val state = rememberLazyListState()
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
                            state,
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
                            },
                            resourceBrowser = {
                                showResourceBrowser = true
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
                    showResourceBrowser,
                    enter = if (enableAnimations) fadeIn() + expandIn() else EnterTransition.None,
                    exit = if (enableAnimations) shrinkOut() + fadeOut() else ExitTransition.None,
                ) {
                    ResourceBrowser { showResourceBrowser = false }
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
                        Row(modifier = Modifier.fillMaxWidth().padding(5.dp), horizontalArrangement = Arrangement.Center) {
                            Box {
                                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                            }
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
                                        if(it.key == Key.Enter && message.isNotEmpty()) {
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

                var reloadingModViewVisible by remember { mutableStateOf(false) }
                GlobalEventChannel.filter(ReloadModEvent::class).onDispose {
                    subscribeAlways {
                        reloadingModViewVisible = true
                    }
                }

                GlobalEventChannel.filter(ReloadModFinishedEvent::class).onDispose {
                    subscribeAlways {
                        reloadingModViewVisible = false
                    }
                }

                LoadingView(reloadingModViewVisible, onLoaded = {}) { null }

                AnimatedAlertDialog(
                    UI.showNetworkDialog,
                    {
                        UI.showNetworkDialog = false
                        UI.receivingNetworkDialogTitle = ""
                        val game = appKoin.get<Game>()
                        game.gameRoom.disconnect("refuse to receive network data.")
                    }, enableDismiss = true
                ) { dismiss ->
                    BorderCard(modifier = Modifier.size(500.dp)) {
                        Spacer(Modifier.weight(1f))
                        LineSpinFadeLoaderIndicator(MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(
                            UI.receivingNetworkDialogTitle,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp).offset(y = 50.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenu(
    state: LazyListState,
    multiplayer: () -> Unit,
    mission: () -> Unit,
    skirmish: () -> Unit,
    settings: () -> Unit,
    mods: () -> Unit,
    sandbox: () -> Unit,
    extension: () -> Unit,
    replay: () -> Unit,
    contributor: () -> Unit,
    resourceBrowser: () -> Unit
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
                state = state,
                modifier = Modifier.fillMaxWidth().lazyRowDesktopScrollable(state),
                horizontalArrangement = Arrangement.Center
            ) {
                item {
                    MenuButton(
                        readI18n("menu.mission"),
                        painterResource(Res.drawable.destruction_30),
                        onClick = mission
                    )
                }

                item {
                    MenuButton(
                        readI18n("menus.singlePlayer.skirmish", I18nType.RW),
                        painterResource(Res.drawable.swords_30),
                        onClick = skirmish
                    )
                }

                item {
                    MenuButton(
                        readI18n("menu.multiplayer"),
                        painterResource(Res.drawable.group_30),
                        onClick = multiplayer
                    )
                }

                item {
                    MenuButton(
                        readI18n("menu.mods"),
                        painterResource(Res.drawable.dns_30),
                        onClick = mods
                    )
                }

                item {
                    MenuButton(
                        readI18n("menu.sandbox"),
                        painterResource(Res.drawable.edit_square_30),
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
                        readI18n("browser.resourceBrowser"),
                        painterResource(Res.drawable.public_30),
                        onClick = resourceBrowser
                    )
                }

                item {
                    MenuButton(
                        readI18n("menu.extension"),
                        painterResource(Res.drawable.extension_30),
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
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            RWIconButton(Icons.Filled.Favorite, modifier = Modifier.padding(10.dp), tint = Color.Red, onClick = contributor)

            val net = koinInject<Net>()

            RWIconButton(painterResource(Res.drawable.qq), modifier = Modifier.padding(10.dp)) {
                net.openUriInBrowser("https://qun.qq.com/universal-share/share?ac=1&authKey=QmG6huGEuUos23WJ0WBwh2sUXiP8%2FsLbsX375KEw9HQzdqT2HK2yEY1WS1Me87%2Bw&busi_data=eyJncm91cENvZGUiOiI5Mjc1OTc0OTUiLCJ0b2tlbiI6ImNaZ2dRYXNLd1d3Q0dhS1p0aG9pcVBsOTYxTEJNb0Z4ZDdXT3lmdTljazB2ZEhVQXd5S1dNa0lYVCtwdDZGYXoiLCJ1aW4iOiIxMjI1MzI3ODY2In0%3D&data=ys1-t0OmBONJktYt21HLtehR3nE23CFtG-YUNRRq7Q7aAMmkd-K_EupcjeKeapL9Aob7bXEpuXIp74FsCcUStg&svctype=4&tempid=h5_group_info")
            }

            RWIconButton(painterResource(Res.drawable.library_30), modifier = Modifier.padding(10.dp)) {
                net.openUriInBrowser("https://rwpp.netlify.app/")
            }

            RWIconButton(painterResource(Res.drawable.octocat_30), modifier = Modifier.padding(10.dp)) {
                net.openUriInBrowser("https://github.com/Minxyzgo/RWPP")
            }

            with(koinInject<AppContext>()) {
                RWIconButton(painterResource(Res.drawable.exit_30), modifier = Modifier.padding(10.dp)) {
                    exit()
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}