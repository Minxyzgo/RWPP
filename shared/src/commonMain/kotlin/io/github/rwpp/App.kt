/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.ui.*
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.KickedEvent
import io.github.rwpp.event.events.QuestionDialogEvent
import io.github.rwpp.event.events.QuestionReplyEvent
import io.github.rwpp.event.onDispose
import io.github.rwpp.game.ui.*
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.platform.deliciousFonts
import io.github.rwpp.ui.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

var LocalController = staticCompositionLocalOf<ContextController> { null!! }
var LocalWindowManager = staticCompositionLocalOf { WindowManager.Large }

val welcomeMessage
    get() = """
            这是一个使用[RWPP]所开始的房间
            [RWPP]是在github上开源的多平台RW启动器, 支持多种拓展功能
            开源地址请访问 https://github.com/Minxyzgo/RWPP 
            bug反馈与交流加入群: 150450999
            当前版本: 1.0.5-alpha (不稳定)
            Copyright 2023 RWPP contributors
        """.trimIndent()

@Composable
fun App(sizeModifier: Modifier = Modifier.fillMaxSize()) {
//

    val deliciousFont = deliciousFonts()

    val typography = Typography(
        displayLarge = TextStyle(
            color = Color.Black,
            fontFamily = deliciousFont,
            fontWeight = FontWeight.Bold,
            fontSize = 35.sp
        ),
        headlineLarge = TextStyle(
            color = Color.Black,
            fontFamily = deliciousFont,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp
        ),
        bodyLarge = TextStyle(
            color = Color.Black,
            fontFamily = deliciousFont,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    )

    var isLoading by remember { mutableStateOf(true) }
    var isSandboxGame by remember { mutableStateOf(false) }
    var showSinglePlayerView by remember { mutableStateOf(false) }
    var showMultiplayerView by remember { mutableStateOf(false) }
    var showSettingsView by remember { mutableStateOf(false) }
    var showModsView by remember { mutableStateOf(false) }
    var showRoomView by remember { mutableStateOf(false) }

    val context = LocalController.current

    MaterialTheme(
        typography = typography,
        colorScheme = lightColorScheme()
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
                LoadingView(isLoading, onLoaded = { isLoading = false }) {
                    context.load(this)
                    true
                }

                AnimatedVisibility(
                    !(showMultiplayerView || showSinglePlayerView || isLoading || showSettingsView || showModsView || showRoomView)
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
                            context.hostNewSandbox()
                        }
                    )
                }

                AnimatedVisibility(
                    showSinglePlayerView
                ) {
                    MissionView { showSinglePlayerView = false }
                }

                AnimatedVisibility(
                    showMultiplayerView
                ) {
                    MultiplayerView({ showMultiplayerView = false }) { showRoomView = true }
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
                    showRoomView
                ) {
                    MultiplayerRoomView(isSandboxGame) {
                        if(!isSandboxGame) {
                            context.cancelJoinServer()
                            showMultiplayerView = true
                        }

                        context.onBanUnits(listOf())
                        context.gameRoom.disconnect()

                        showRoomView = false
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
                    onDismissRequest = { kickedDialogVisible = false }) { modifier, dismiss ->
                    BorderCard(
                        modifier = Modifier.fillMaxSize(GeneralProportion()).then(modifier),
                        backgroundColor = Color.Gray
                    ) {
                        ExitButton(dismiss)

                        Row(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                            Divider(Modifier.weight(1f), thickness = 2.dp, color = Color.DarkGray)
                            Box {
                                Icon(Icons.Default.Warning, null, tint = Color(151, 188, 98), modifier = Modifier.size(50.dp).offset(5.dp, 5.dp).blur(2.dp))
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFb6d7a8), modifier = Modifier.size(50.dp))
                            }
                            Divider(Modifier.weight(1f), thickness = 2.dp, color = Color.DarkGray)
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
                                color = Color.Black,
                                style = MaterialTheme.typography.headlineLarge
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
                ) { modifier, dismiss ->
                    BorderCard(
                        modifier = Modifier.fillMaxSize(GeneralProportion()).then(modifier),
                        backgroundColor = Color.Gray
                    ) {
                        ExitButton(dismiss)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(25.dp).padding(5.dp))
                            Text(
                                questionEvent.title,
                                modifier = Modifier.padding(5.dp),
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color(151, 188, 98)
                            )
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
                                        Icons.Default.ArrowForward,
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

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MainMenu(
    multiplayer: () -> Unit,
    mission: () -> Unit,
    settings: () -> Unit,
    mods: () -> Unit,
    sandbox: () -> Unit
) {
    MaterialTheme.colorScheme
    val deliciousFont = MaterialTheme.typography.headlineLarge.fontFamily!!
    Row(modifier = Modifier.fillMaxSize()) {
        BorderCard(
            modifier = Modifier
                .weight(1f)
                .padding(10.dp)
        ) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                item {
                    Text(
                        "RWPP Update news - 1.0.4-alpha",
                        fontFamily = deliciousFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                }
                item {
                    RichText(
                        modifier = Modifier
                            .wrapContentSize(),
                        style = RichTextStyle(
                            headingStyle = { level, _ ->
                                when (level) {
                                    0 -> TextStyle(
                                        fontFamily = deliciousFont,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    1 -> TextStyle(
                                        fontFamily = deliciousFont,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    2 -> TextStyle(
                                        fontFamily = deliciousFont,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    3 -> TextStyle(
                                        fontFamily = deliciousFont,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Italic
                                    )
                                    4 -> TextStyle(
                                        fontFamily = deliciousFont,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    5 -> TextStyle(
                                        fontFamily = deliciousFont,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    else -> TextStyle(fontFamily = deliciousFont)
                                }
                            }
                        )
                    ) {
                        LargeDividingLine {
                            with(LocalDensity.current) {
                                currentRichTextStyle.resolveDefaults().paragraphSpacing!!.toDp()
                            }
                        }

                        Heading(1, "Contact")
                        CodeBlock(
                           """
                               QQ Group: 150450999
                               Github: https://github.com/Minxyzgo/RWPP
                           """.trimIndent()
                        )
                        Heading(1, "Changelogs")
                        CodeBlock("""
                            1.0.5-alpha: Ban units, Game layer
                            1.0.4-alpha: Add i18n support. Player options
                            1.0.3-alpha: Improve menu and resize the layout on small screen device.
                        """.trimIndent())
                        Heading(1, "Notes")
                        CodeBlock(
                            readI18n("menu.notes")
                        )
                        Heading(1, "Features")
                        CodeBlock(
                            """
                                The desktop version of RWPP will make it easier for you to enter Chinese (also included other languages requiring IME)
                                More options for host.
                                Mod Menu Improvement & Server list Filter
                            """.trimIndent()
                        )
                    }
                }
            }
        }

        BorderCard(
            modifier = Modifier
                .weight(0.6f)
                .padding(10.dp)
        ) {

            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item { Image(painter = painterResource("title.png"), "title", modifier = Modifier.padding(15.dp)) }
                item { MenuButton(readI18n("menu.mission"), onClick = mission) }
                item { MenuButton(readI18n("menu.sandbox"), onClick = sandbox) }
                item { MenuButton(readI18n("menu.multiplayer"), onClick = multiplayer) }
                item { MenuButton(readI18n("menu.mods"), onClick = mods) }
                item { MenuButton(readI18n("menu.settings"), onClick = settings) }
                item {
                    with(LocalController.current) {
                        MenuButton(readI18n("menu.exit")) { exit() }
                    }
                }
                item { Spacer(Modifier.size(50.dp)) }
//                item {
//                    Row(
//                        horizontalArrangement = Arrangement.Center,
//                    ) {
//                        OutlinedButton(
//                            onClick = { },
//                            modifier = Modifier
//                                .wrapContentSize()
//                                .padding(10.dp),
//                            border = BorderStroke(2.dp, Color.DarkGray),
//                            shape = CircleShape,
//                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Gray)
//                        ) {
//                            Image(
//                                modifier = Modifier
//                                    .size(40.dp),
//                                painter = painterResource("GitHub-Mark.png"),
//                                contentDescription = null
//                            )
//                        }
//
//                        OutlinedButton(
//                            onClick = { },
//                            modifier = Modifier
//                                .wrapContentSize()
//                                .padding(10.dp),
//                            border = BorderStroke(2.dp, Color.DarkGray),
//                            shape = CircleShape,
//                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Gray)
//                        ) {
//                            Image(
//                                modifier = Modifier
//                                    .size(40.dp),
//                                painter = painterResource("qq-icon.png"),
//                                contentDescription = null
//                            )
//                        }
//                    }
//                }
            }
        }
    }
}