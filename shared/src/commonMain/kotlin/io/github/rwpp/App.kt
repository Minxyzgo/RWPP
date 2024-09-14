/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("DuplicatedCode")

package io.github.rwpp

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.KickedEvent
import io.github.rwpp.event.events.QuestionDialogEvent
import io.github.rwpp.event.events.QuestionReplyEvent
import io.github.rwpp.event.onDispose
import io.github.rwpp.game.Game
import io.github.rwpp.game.ui.*
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.net.Net
import io.github.rwpp.platform.loadSvg
import io.github.rwpp.ui.*
import io.github.rwpp.ui.v2.bounceClick
import org.koin.compose.koinInject

var LocalWindowManager = staticCompositionLocalOf { WindowManager.Large }

@Composable
fun App(sizeModifier: Modifier = Modifier.fillMaxSize()) {

    val jostFonts = JostFonts()
    val valoraxFont = ValoraxFont()

    val typography = Typography(
        displayLarge = TextStyle(
            color = Color.White,
            fontFamily = valoraxFont,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp
        ),
        headlineLarge = TextStyle(
            color = Color.White,
            fontFamily = jostFonts,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp
        ),
        headlineMedium = TextStyle(
            color = Color.White,
            fontFamily = jostFonts,
            fontWeight = FontWeight.Normal,
            fontSize = 19.sp
        ),
        bodyLarge = TextStyle(
            color = Color.White,
            fontFamily = jostFonts,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        ),
        bodyMedium = TextStyle(
            color = Color.White,
            fontFamily = jostFonts,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp
        )
    )

    var isSandboxGame by remember { mutableStateOf(false) }
    var showSinglePlayerView by remember { mutableStateOf(false) }
    var showMultiplayerView by remember { mutableStateOf(false) }
    var showReplayView by remember { mutableStateOf(false) }
    var showSettingsView by remember { mutableStateOf(false) }
    var showModsView by remember { mutableStateOf(false) }
    var showRoomView by remember { mutableStateOf(false) }
    var showResourceView by remember { mutableStateOf(false) }
    var showContributorList by remember { mutableStateOf(false) }

    val showMainMenu = !(showMultiplayerView
            || showSinglePlayerView
            || showSettingsView
            || showModsView
            || showRoomView
            || showResourceView
            || showReplayView
            || showContributorList)


    val game = koinInject<Game>()

    MaterialTheme(
        typography = typography,
        colorScheme = lightColorScheme(
            surface = Color(27, 18, 18),
            onSurface = Color.White,
            primary = Color.Black
        )
    ) {

        BoxWithConstraints(
            modifier = Modifier
                .then(sizeModifier)
                //.background(brush),
        ) {
            CompositionLocalProvider(
                LocalTextSelectionColors provides RWSelectionColors,
                LocalWindowManager provides ConstraintWindowManager(maxWidth, maxHeight)
            ) {

                Scaffold(
                    containerColor = Color.Transparent,
                    floatingActionButton = {
                        if(game.isGameCouldContinue() && (showMainMenu || showSinglePlayerView)) {
                            FloatingActionButton(
                                onClick = { game.continueGame() },
                                shape = CircleShape,
                                modifier = Modifier.padding(5.dp),
                                containerColor = Color(151, 188, 98),
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                            }
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) {
                    AnimatedVisibility(
                        showMainMenu
                    ) {
                        MainMenu(
                            multiplayer = {
                                isSandboxGame = false
                                showMultiplayerView = true
                            },
                            mission = {
                                showSinglePlayerView = true
                            },
                            settings = {
                                showSettingsView = true
                            },
                            mods = {
                                showModsView = true
                            },
                            sandbox = {
                                isSandboxGame = true
                                showRoomView = true
                                game.hostNewSandbox()
                            },
                            resource = {
                                showResourceView = true
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
                        showSinglePlayerView
                    ) {
                        MissionView { showSinglePlayerView = false }
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
                    showSettingsView
                ) {
                    SettingsView { showSettingsView = false }
                }

                AnimatedVisibility(
                    showModsView
                ) {
                    ModsView { showModsView = false }
                }

                AnimatedVisibility(
                    showResourceView
                ) {
                    ResourceView { showResourceView = false }
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
                    MultiplayerRoomView(isSandboxGame) {
                        if(!isSandboxGame) {
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

                var kickedDialogVisible by remember { mutableStateOf(false) }
                var kickedReason by remember { mutableStateOf("") }
                GlobalEventChannel.filter(KickedEvent::class).onDispose {
                    subscribeAlways {
                        showRoomView = false
                        showMultiplayerView = true
                        kickedDialogVisible = true
                        kickedReason = it.reason
                    }
                }

                AnimatedAlertDialog(
                    kickedDialogVisible,
                    onDismissRequest = { kickedDialogVisible = false }) { dismiss ->
                    BorderCard(
                       modifier = Modifier.size(500.dp),
                    ) {
                        ExitButton(dismiss)

                        Row(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                            HorizontalDivider(Modifier.weight(1f), thickness = 2.dp, color = Color.DarkGray)
                            Box {
                                Icon(Icons.Default.Warning, null, tint = Color(151, 188, 98), modifier = Modifier.size(50.dp).offset(5.dp, 5.dp).blur(2.dp))
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFb6d7a8), modifier = Modifier.size(50.dp))
                            }
                            HorizontalDivider(Modifier.weight(1f), thickness = 2.dp, color = Color.DarkGray)
                        }



                        LargeDividingLine { 5.dp }

                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                kickedReason,
                                modifier = Modifier.padding(5.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                var questionDialogVisible by remember { mutableStateOf(false) }
                var questionEvent by remember { mutableStateOf(QuestionDialogEvent("", "")) }
                GlobalEventChannel.filter(QuestionDialogEvent::class).onDispose {
                    subscribeAlways {
                        questionDialogVisible = true
                        questionEvent = it
                    }
                }


                AnimatedAlertDialog(questionDialogVisible,
                    onDismissRequest = {
                        questionDialogVisible = false
                        QuestionReplyEvent("", true).broadCastIn()
                        if(showRoomView) {
                            showRoomView = false
                            showMultiplayerView = true
                        }
                    }
                ) { dismiss ->
                    BorderCard(
                        modifier = Modifier.fillMaxWidth(if (LocalWindowManager.current == WindowManager.Small) 0.9f else 0.75f),
                    ) {

                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(if (LocalWindowManager.current == WindowManager.Small) 75.dp else 150.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xE9EE8888),
                                        Color(0xFFE4BD79)))
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                ExitButton(dismiss)
                                Row(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, null, modifier = Modifier.size(25.dp).padding(5.dp))
                                    Text(
                                        questionEvent.title,
                                        modifier = Modifier.padding(5.dp),
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        LargeDividingLine { 5.dp }
                        Column(
                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                questionEvent.message,
                                modifier = Modifier.padding(5.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            var message by remember { mutableStateOf("") }
                            RWSingleOutlinedTextField(
                                label = "Reply",
                                value = message,
                                modifier = Modifier.fillMaxWidth().padding(10.dp)
                                    .onKeyEvent {
                                        if(it.key == androidx.compose.ui.input.key.Key.Enter && message.isNotEmpty()) {
                                            QuestionReplyEvent(message, false).broadCastIn()
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
                                            QuestionReplyEvent(message, false).broadCastIn()
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
    settings: () -> Unit,
    mods: () -> Unit,
    sandbox: () -> Unit,
    resource: () -> Unit,
    replay: () -> Unit,
    contributor: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BorderCard(modifier = Modifier.verticalScroll(rememberScrollState()).width(IntrinsicSize.Max)) {
            Text(
                "RWPP",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = TextStyle(
                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                    brush = Brush.linearGradient(listOf(Color(44, 95, 45), Color(151, 188, 98))),
                    fontSize = 100.sp
                )
            )

            Text(
                projectVersion,
                modifier = Modifier.padding(top = 1.dp, bottom = 5.dp).align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.Center) {
                MenuButton(
                    readI18n("menu.mission"),
                    loadSvg("destruction"),
                    onClick = mission
                )

                MenuButton(
                    readI18n("menu.multiplayer"),
                    loadSvg("group"),
                    onClick = multiplayer
                )

                MenuButton(
                    readI18n("menu.mods"),
                    loadSvg("dns"),
                    onClick = mods
                )

                MenuButton(
                    readI18n("menu.sandbox"),
                    loadSvg("edit_square"),
                    onClick = sandbox
                )
            }

            Row(horizontalArrangement = Arrangement.Center) {
                MenuButton(
                    readI18n("menu.settings"),
                    Icons.Default.Settings,
                    onClick = settings
                )

                MenuButton(
                    readI18n("menu.resource"),
                    loadSvg("stacks"),
                    onClick = resource
                )

                MenuButton(
                    readI18n("menu.replay"),
                    Icons.Default.PlayArrow,
                    onClick = replay
                )

                with(koinInject<AppContext>()) {
                    MenuButton(
                        readI18n("menu.exit"),
                        loadSvg("exit"),
                    ) { exit() }
                }
            }


            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Card(
                    border = BorderStroke(3.dp, Color.DarkGray),
                    colors = CardDefaults.cardColors(containerColor = Color(27, 18, 18)),
                    shape = RoundedCornerShape(5.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    modifier = Modifier.bounceClick { contributor() }.padding(10.dp),
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        null,
                        modifier = Modifier.size(50.dp).align(Alignment.CenterHorizontally).padding(10.dp)
                    )
                }

                val net = koinInject<Net>()

                Card(
                    border = BorderStroke(3.dp, Color.DarkGray),
                    colors = CardDefaults.cardColors(containerColor = Color(27, 18, 18)),
                    shape = RoundedCornerShape(5.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    modifier = Modifier.bounceClick { net.openUriInBrowser("https://github.com/Minxyzgo/RWPP") }
                        .padding(10.dp),
                ) {
                    Icon(
                        painter = loadSvg("octocat"),
                        null,
                        modifier = Modifier.size(50.dp).align(Alignment.CenterHorizontally).padding(7.dp)
                    )
                }
            }
        }
    }
}