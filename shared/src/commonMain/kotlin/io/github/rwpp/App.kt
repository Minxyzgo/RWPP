package io.github.rwpp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
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
import io.github.rwpp.ui.*
import org.jetbrains.compose.resources.*


var LocalController = staticCompositionLocalOf<ContextController> { null!! }
@Preview
@Composable
internal fun preview() {
    App()
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App(sizeModifier: Modifier = Modifier.fillMaxSize()) {
    val image = resource("metal.png").rememberImageBitmap()

    val brush = remember(image) {
        ShaderBrush(ImageShader(image.orEmpty(), TileMode.Repeated, TileMode.Repeated))
    }

    val deliciousFont = FontFamily(
        Font("font/Delicious-Bold.otf", FontWeight.Bold),
        Font("font/Delicious-BoldItalic.otf", FontWeight.Bold, FontStyle.Italic),
        Font("font/Delicious-Italic.otf", FontWeight.Normal, FontStyle.Italic)
    )

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
        Box(
            modifier = Modifier
                .then(sizeModifier)
                .background(brush),
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
                MultiplayerRoomView {
                    showRoomView = false
                    context.cancelJoinServer()
                    context.gameRoom.disconnect()
                    showMultiplayerView = true
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

            AnimatedAlertDialog(kickedDialogVisible, onDismissRequest = { kickedDialogVisible = false }) { modifier, dismiss ->
                BorderCard(
                    modifier = Modifier.fillMaxSize(0.3f).then(modifier),
                    backgroundColor = Color.Gray
                ) {
                    ExitButton(dismiss)
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(50.dp))
                        Text(kickedReason, modifier = Modifier.padding(5.dp), color = Color.Red, style = MaterialTheme.typography.headlineLarge)
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
                    modifier = Modifier.fillMaxSize(0.5f).then(modifier),
                    backgroundColor = Color.Gray
                ) {
                    ExitButton(dismiss)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(25.dp).padding(5.dp))
                        Text(questionEvent.title, modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.headlineLarge, color = Color(151, 188, 98))
                    }
                    LargeDividingLine { 5.dp }
                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(questionEvent.message, modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = Color.White)
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

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MainMenu(
    multiplayer: () -> Unit,
    mission: () -> Unit,
    settings: () -> Unit,
    mods: () -> Unit
) {
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
                        "Update news - v1.15",
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

                        Heading(1, "New units, shaders, performance and features.")
                        CodeBlock(
                            """
                                        Added Heavy anti-air mech, Spy Drone units. Shader effects (enable in settings). 
                                        Over 100% faster rendering (in tests with high numbers of units). 
                                        New modding features such as streaming resources, 3d voxel units, decals, easier logic, etc.
                                    """.trimIndent()
                        )
                        Heading(1, "New units, maps and features.")
                        CodeBlock(
                            """
                                        Large Modular Spider starting unit. Lots of new units. Better performance. 
                                        Tons of new modding features. 
                                        iOS version out very soon (with android and PC crossplay).
                                    """.trimIndent()
                        )
                        Heading(1, "Faster, new modding features, handles more mods.")
                        CodeBlock(
                            """
                                        Reduced memory use loading larger maps, 2x-3x pathfinding performance, 
                                        lots of new mods features including creating new resource types.
                                    """.trimIndent()
                        )
                        Heading(1, "New Units & Maps! Much more Modding! Faster!")
                        CodeBlock(
                            """
                                        8 new units, 3 maps and 1 mission. 
                                        Optimisations to handle 10,000+ units and large maps. 
                                        Smarter AI. 
                                        Huge set of modding features added. 
                                        Reduced memory usage. Patrol/Guard orders.
                                    """.trimIndent()
                        )
                        Heading(1, "Workshop support added for new Maps & Units!")
                        CodeBlock(
                            """
                                        Also new units, 
                                        queue lines of buildings, 
                                        performance boost, fixes, 
                                        and many more graphical effects.
                                    """.trimIndent()
                        )
                        Heading(1, "Multiplayer Replays! More Mechs!")
                        CodeBlock(
                            """
                                        Watch back your multiplayer games. 
                                        More mechs added. New mission - spider battle. 
                                        Lots of optimisations.
                                    """.trimIndent()
                        )
                        Heading(1, "Shared control, Mech units, Lots more!")
                        CodeBlock(
                            """
                                        Shared control option. 
                                        Mech factory added with a set of initial mechs. 
                                        AI and path finding fixes. 
                                        New placement system.
                                        Better mod support. Lots more.
                                    """.trimIndent()
                        )
                        Heading(1, "Sandbox editor has been added")
                        CodeBlock(
                            """
                                        Saves from the sandbox can be shared and played in singleplayer or multiplayer.
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
                item { MenuButton("Mission", mission) }
                item { MenuButton("Sandbox Editor") {} }
                item { MenuButton("Multiplayer", multiplayer) }
                item { MenuButton("Mods", mods) }
                item { MenuButton("Settings", settings) }
                item {
                    with(LocalController.current) {
                        MenuButton("Exit") { exit() }
                    }
                }
                item { Spacer(Modifier.size(50.dp)) }
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(10.dp),
                            border = BorderStroke(2.dp, Color.DarkGray),
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Gray)
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(40.dp),
                                painter = painterResource("GitHub-Mark.png"),
                                contentDescription = null
                            )
                        }

                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(10.dp),
                            border = BorderStroke(2.dp, Color.DarkGray),
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Gray)
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(40.dp),
                                painter = painterResource("qq-icon.png"),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}