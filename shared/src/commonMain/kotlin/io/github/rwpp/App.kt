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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import io.github.rwpp.config.CoreData
import io.github.rwpp.config.Settings
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.KickedEvent
import io.github.rwpp.event.events.QuestionDialogEvent
import io.github.rwpp.event.events.QuestionReplyEvent
import io.github.rwpp.event.onDispose
import io.github.rwpp.game.Game
import io.github.rwpp.game.ui.*
import io.github.rwpp.i18n.I18nType
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.net.Net
import io.github.rwpp.platform.loadSvg
import io.github.rwpp.ui.*
import io.github.rwpp.ui.v2.RWIconButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.util.Date

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
        headlineSmall = TextStyle(
            color = Color.White,
            fontFamily = jostFonts,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp
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

    var isSinglePlayerGame by remember { mutableStateOf(false) }
    var showMissionView by remember { mutableStateOf(false) }
    var showMultiplayerView by remember { mutableStateOf(false) }
    var showReplayView by remember { mutableStateOf(false) }
    var showSettingsView by remember { mutableStateOf(false) }
    var showModsView by remember { mutableStateOf(false) }
    var showRoomView by remember { mutableStateOf(false) }
    var showResourceView by remember { mutableStateOf(false) }
    var showContributorList by remember { mutableStateOf(false) }

    var checkUpdateDialogVisible by remember { mutableStateOf(false) }
    var latestVersion by remember { mutableStateOf<String?>(null) }
    var latestVersionBody by remember { mutableStateOf<String>("null") }

    val coreData = koinInject<CoreData>()
    val settings = koinInject<Settings>()
    val net = koinInject<Net>()


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
            || showResourceView
            || showReplayView
            || showContributorList)


    val game = koinInject<Game>()

    MaterialTheme(
        typography = typography,
        colorScheme = darkColorScheme(
            surface = Color(27, 18, 18),
            onSurface = Color.White,
            primary = Color.Black
        )
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
                                containerColor = Color(151, 188, 98),
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
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
                    SettingsView({
                        if (it.version == projectVersion || settings.ignoreVersion == it.version) return@SettingsView
                        latestVersion = it.version
                        checkUpdateDialogVisible = true
                    }) { showSettingsView = false }
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
                       modifier = Modifier.size(500.dp).autoClearFocus(),
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

                AnimatedAlertDialog(
                    checkUpdateDialogVisible,
                    onDismissRequest = { checkUpdateDialogVisible = false }
                ) { dismiss ->
                    BorderCard(modifier = Modifier.fillMaxWidth(GeneralProportion()).verticalScroll(rememberScrollState())) {

                        Row(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                            HorizontalDivider(Modifier.weight(1f), thickness = 2.dp, color = Color.DarkGray)
                            Box {
                                Icon(Icons.Default.Warning, null, tint = Color(151, 188, 98), modifier = Modifier.size(50.dp).offset(5.dp, 5.dp).blur(2.dp))
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFb6d7a8), modifier = Modifier.size(50.dp))
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

                        BorderCard(backgroundColor = Color.DarkGray.copy(alpha = 0.8f)) {
                            Markdown(latestVersionBody, modifier = Modifier.padding(5.dp), colors = DefaultMarkdownColors(
                                text = MaterialTheme.colorScheme.onBackground,
                                codeText = MaterialTheme.colorScheme.onBackground,
                                inlineCodeText= MaterialTheme.colorScheme.onBackground,
                                linkText = MaterialTheme.colorScheme.onBackground,
                                codeBackground = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                inlineCodeBackground = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                dividerColor = MaterialTheme.colorScheme.outlineVariant,
                            ), typography = DefaultMarkdownTypography(
                                h1 = MaterialTheme.typography.displayLarge,
                                h2 = MaterialTheme.typography.displayMedium,
                                h3 = MaterialTheme.typography.displaySmall,
                                h4 = MaterialTheme.typography.headlineMedium,
                                h5 = MaterialTheme.typography.headlineSmall,
                                h6= MaterialTheme.typography.titleLarge,
                                text = MaterialTheme.typography.bodyLarge,
                                code = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                inlineCode = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                                quote = MaterialTheme.typography.bodyMedium.plus(SpanStyle(fontStyle = FontStyle.Italic)),
                                paragraph = MaterialTheme.typography.bodyLarge,
                                ordered = MaterialTheme.typography.bodyLarge,
                                bullet = MaterialTheme.typography.bodyLarge,
                                list = MaterialTheme.typography.bodyLarge,
                                link = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ))
                        }
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
                ) { _ ->
                    BorderCard(
                        modifier = Modifier.fillMaxWidth(if (LocalWindowManager.current == WindowManager.Small) 0.9f else 0.75f).autoClearFocus(),
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
    skirmish: () -> Unit,
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

        Spacer(modifier = Modifier.weight(1f))

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
            "$projectVersion (core $coreVersion)",
            modifier = Modifier.padding(top = 1.dp, bottom = 5.dp).align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(53, 57, 53).copy(0.7f)),
            border = BorderStroke(4.dp, Color.DarkGray),
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
                        readI18n("menu.resource"),
                        loadSvg("stacks"),
                        onClick = resource
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