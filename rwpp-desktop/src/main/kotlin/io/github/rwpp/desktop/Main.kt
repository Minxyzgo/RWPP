/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.rwpp.*
import io.github.rwpp.config.ConfigModule
import io.github.rwpp.config.Settings
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.GameLoadedEvent
import io.github.rwpp.event.events.KeyboardEvent
import io.github.rwpp.event.events.QuitGameEvent
import io.github.rwpp.event.onDispose
import io.github.rwpp.game.Game
import io.github.rwpp.game.comp.CompModule
import io.github.rwpp.game.sendChatMessageOrCommand
import io.github.rwpp.game.team.TeamModeModule
import io.github.rwpp.i18n.parseI18n
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.impl.CommonImplModule
import io.github.rwpp.ui.*
import io.github.rwpp.widget.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import org.slf4j.LoggerFactory
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants


typealias ColorCompose = androidx.compose.ui.graphics.Color

var native: Boolean = false
lateinit var mainJFrame: JFrame
lateinit var gameCanvas: Canvas
lateinit var displaySize: Dimension
lateinit var sendMessageDialog: Dialog
lateinit var rwppVisibleSetter: (Boolean) -> Unit
//val cacheModSize = AtomicInteger(0)

fun main(array: Array<String>) {
    if (File("opengl32.dll").exists()) { // for only debug
        System.loadLibrary("opengl32")
    }

    logger = LoggerFactory.getLogger(packageName)
    displaySize =
        GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .defaultScreenDevice
            .displayMode
            .run { Dimension(width, height) }
    native = array.contains("-native")

    swingApplication()
}

fun swingApplication() = SwingUtilities.invokeLater {
    val window = JFrame()

    appKoin = startKoin {
        logger(org.koin.core.logger.PrintLogger(org.koin.core.logger.Level.ERROR))
        modules(ConfigModule().module, DesktopModule().module, TeamModeModule().module, CommonImplModule().module, CompModule().module)
    }.koin

    val app = appKoin.get<AppContext>()
    runBlocking { parseI18n() }
    app.init()
    Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
    File("mods/maps/")
        .walk()
        .filter { it.name.startsWith("generated_") }
        .forEach {
            it.delete()
        }

    KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .addKeyEventDispatcher { dispatcher ->
            KeyboardEvent(dispatcher.keyCode).broadcastIn()
            false
        }

    window.addWindowListener(object : WindowListener {
        override fun windowOpened(e: WindowEvent?) {
        }

        override fun windowClosing(e: WindowEvent?) {
        }

        override fun windowClosed(e: WindowEvent?) {
            appKoin.get<AppContext>().exit()
        }
        override fun windowIconified(e: WindowEvent?) {}

        override fun windowDeiconified(e: WindowEvent?) {
        }

        override fun windowActivated(e: WindowEvent?) {
        }

        override fun windowDeactivated(e: WindowEvent?) {
        }
    })

    val panel = ComposePanel()
    panel.isVisible = true
    panel.size = displaySize.size
    panel.isOpaque = false
    panel.isFocusable = true
    rwppVisibleSetter = { panel.isVisible = it }

    window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    window.title = "Rusted Warfare Plus Plus"
    window.iconImage = ImageIO.read(ClassLoader.getSystemResource("composeResources/io.github.rwpp.rwpp_core.generated.resources/drawable/logo.png"))

    val canvas = Canvas()
    gameCanvas = canvas
    canvas.size = displaySize.size
    canvas.isVisible = false
    canvas.background = java.awt.Color.BLACK
    canvas.isFocusable = true

    window.layout = BorderLayout()
    window.add(canvas, BorderLayout.CENTER)
    window.add(panel, BorderLayout.CENTER)

    window.addComponentListener(object : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent) {
            val scale = getDPIScale()

            // 计算逻辑像素尺寸（抵消 HiDPI 缩放）
            val logicalWidth = (window.contentPane.width * scale).toInt()
            val logicalHeight = (window.contentPane.height * scale).toInt()

            // 设置 Canvas 物理像素尺寸
            canvas.setSize(
                logicalWidth,
                logicalHeight
            )

            resetSendMessageDialogLocation()
        }

        override fun componentMoved(e: ComponentEvent) {
            resetSendMessageDialogLocation()
        }
    })

    panel.setContent {

        var isLoading by remember { mutableStateOf(true) }
        var message by remember { mutableStateOf("loading...") }

        val game = koinInject<Game>()
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                game.load(
                    LoadingContext { message = it }
                )

                GameLoadedEvent().broadcastIn()

                isLoading = false
            }
        }
        val settings = koinInject<Settings>()
        val isPremium = true
        var backgroundImagePath by remember { mutableStateOf(settings.backgroundImagePath ?: "") }

        val painter = remember (backgroundImagePath) {
            if (backgroundImagePath.isNotBlank() && isPremium) {
                runCatching { ImageIO.read(File(backgroundImagePath)).toPainter() }.getOrNull()
            } else {
                null
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().composed {
                if (backgroundImagePath.isBlank() || !isPremium || painter == null) {
                    background(
                        brush = Brush.verticalGradient(
                            listOf(
                                ColorCompose.Black,
                                androidx.compose.ui.graphics.Color(52, 52, 52),
                                androidx.compose.ui.graphics.Color(2, 48, 32),
                                androidx.compose.ui.graphics.Color(52, 52, 52),
                                ColorCompose.Black
                            )
                        )
                    )
                } else {
                    this
                }
            },
            contentAlignment = Alignment.Center
        ) {

            if (backgroundImagePath.isNotBlank() && isPremium && painter != null) {
                Image(
                    painter = painter,
                    null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            if (isLoading) MenuLoadingView(message) else App(isPremium = isPremium) { path ->
                backgroundImagePath = path
            }
        }
    }


    window.background = java.awt.Color.BLACK
    window.extendedState = JFrame.MAXIMIZED_BOTH

    if (appKoin.get<Settings>().isFullscreen) {
        window.isUndecorated = true
    } else {
        window.minimumSize = Dimension(800, 600)
        window.isResizable = true
    }

    window.isVisible = true
    panel.requestFocus()

    val panel2 = ComposePanel()
    panel2.isOpaque = false
    panel2.isFocusable = true
    panel2.size = Dimension(550, 180)

    panel2.setContent {
        val game = koinInject<Game>()
        RWPPTheme {
            BorderCard(
                modifier = Modifier
                    .height(180.dp)
                    .width(550.dp).onKeyEvent {
                        if (it.key == Key.Escape && it.type == KeyDown) {
                            sendMessageDialog.isVisible = false
                        }
                        true
                    },

                backgroundColor = Color(53, 57, 53),
                shape = RectangleShape
            ) {
                var chatMessage by remember { mutableStateOf("") }
                Box {
                    ExitButton {
                        sendMessageDialog.isVisible = false
                    }

                    GlobalEventChannel.filter(QuitGameEvent::class).onDispose {
                        subscribeAlways { sendMessageDialog.isVisible = false }
                    }
                    Column {
                        Spacer(modifier = Modifier.height(30.dp))
                        RWSingleOutlinedTextField(
                            label = readI18n("ingame.sendMessage"),
                            value = chatMessage,
                            requestFocus = true,
                            modifier = Modifier.fillMaxWidth().padding(10.dp)
                                .onKeyEvent {
                                    if (it.key == Key.Enter && chatMessage.isNotEmpty()) {
                                        game.gameRoom.sendChatMessageOrCommand(chatMessage)
                                        chatMessage = ""
                                        sendMessageDialog.isVisible = false
                                    }

                                    true
                                },
                            trailingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    null,
                                    modifier = Modifier.clickable {
                                        game.gameRoom.sendChatMessageOrCommand(chatMessage)
                                        chatMessage = ""
                                        sendMessageDialog.isVisible = false
                                    }
                                )
                            },
                            onValueChange =
                            {
                                chatMessage = it
                            },
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            RWTextButton(
                                readI18n("ingame.sendMessage"),
                                modifier = Modifier.padding(5.dp)
                            ) {
                                game.gameRoom.sendChatMessageOrCommand(chatMessage)
                                chatMessage = ""
                                sendMessageDialog.isVisible = false
                            }

                            RWTextButton(
                                readI18n("ingame.sendTeamMessage"),
                                modifier = Modifier.padding(5.dp)
                            ) {
                                game.gameRoom.sendChatMessage("-t $chatMessage")
                                chatMessage = ""
                                sendMessageDialog.isVisible = false
                            }
                        }
                    }
                }
            }
        }
    }

    sendMessageDialog = Dialog(window)
    sendMessageDialog.isUndecorated = true
    sendMessageDialog.isFocusable = true
    sendMessageDialog.isVisible = false
    sendMessageDialog.isAlwaysOnTop = true
    sendMessageDialog.size = Dimension(550, 180)
    sendMessageDialog.add(panel2)

    mainJFrame = window

    canvas.createBufferStrategy(2)
}

fun showSendMessageDialog() {
    sendMessageDialog.isVisible = true
    sendMessageDialog.requestFocus()
    resetSendMessageDialogLocation()
}

private fun resetSendMessageDialogLocation() {
    val window = mainJFrame
    sendMessageDialog.setLocation(window.x + window.width / 2 - sendMessageDialog.width / 2, window.y + window.height / 2 - sendMessageDialog.height / 2)
}

private fun getDPIScale(): Double {
    // 获取原生系统缩放比例（需考虑多显示器场景）
    val env = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val device = env.defaultScreenDevice
    val config = device.defaultConfiguration

    // 获取系统推荐缩放倍数（Windows 的 % 缩放比例）
    val transform = config.defaultTransform
    return transform.scaleX // 通常 X/Y 缩放一致
}
