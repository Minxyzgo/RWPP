/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import io.github.rwpp.App
import io.github.rwpp.AppContext
import io.github.rwpp.appKoin
import io.github.rwpp.config.ConfigModule
import io.github.rwpp.config.Settings
import io.github.rwpp.game.Game
import io.github.rwpp.game.team.TeamModeModule
import io.github.rwpp.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import java.awt.*
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants


typealias ColorCompose = androidx.compose.ui.graphics.Color

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

    displaySize =
        GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .defaultScreenDevice
            .displayMode
            .run { Dimension(width, height) }


//    if (array.contains("-native")) {
//        println("start native")
//
//        val classLoader = Thread.currentThread().contextClassLoader
//
//        val ucp = classLoader::class.java.getDeclaredField("ucp").apply {
//            isAccessible = true
//        }.get(classLoader)
//
//        val addURL: Method = ucp::class.java.getDeclaredMethod("addURL", URL::class.java).apply {
//            isAccessible = true
//        }
//
//        val allLibFiles = File(System.getProperty("user.dir") + "/libs").walk().filter { it.extension == "jar" }
//
//        allLibFiles.forEach {
//            addURL.invoke(ucp, it.toURI().toURL())
//        }
//    }

//    Thread.setDefaultUncaughtExceptionHandler { _, e ->
//        JOptionPane.showMessageDialog(
//            JFrame(), e.stackTraceToString(), "Error",
//            JOptionPane.ERROR_MESSAGE
//        )
//
//        exitProcess(0)
//    }

    swingApplication()
}

fun swingApplication() = SwingUtilities.invokeLater {


    val window = JFrame()

    appKoin = startKoin {
        logger(org.koin.core.logger.PrintLogger(org.koin.core.logger.Level.ERROR))
        modules(ConfigModule().module, DesktopModule().module, TeamModeModule().module)
    }.koin

    val app = appKoin.get<AppContext>()

    app.init()

    Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
//    File("mods/units")
//        .walk()
//        .forEach {
//            if (it.name.contains(".network")) {
//                it.delete()
//            } else if (it.name.endsWith(".netbak")) {
//                it.renameTo(File(it.absolutePath.removeSuffix(".netbak")))
//            }
//        }

    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val panel = ComposePanel()
    panel.isVisible = true
    panel.size = displaySize.size
    panel.isOpaque = false
    panel.isFocusable = true
    rwppVisibleSetter = { panel.isVisible = it }

    window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    window.title = "Rusted Warfare Plus Plus"
    window.iconImage = ImageIO.read(ClassLoader.getSystemResource("drawable/logo.png"))
    //window.extendedState = JFrame.MAXIMIZED_BOTH

    val canvas = Canvas()

    gameCanvas = canvas
    canvas.size = displaySize.size
    canvas.isVisible = false
    canvas.background = Color.BLACK
    canvas.isFocusable = true

    window.layout = BorderLayout()
    window.add(canvas, BorderLayout.CENTER)
    window.add(panel, BorderLayout.CENTER)

    panel.setContent {

        var isLoading by remember { mutableStateOf(true) }
        var message by remember { mutableStateOf("loading...") }

        val game = koinInject<Game>()
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                game.load(
                    LoadingContext { message = it }
                )

                isLoading = false
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().background(
                brush = Brush.verticalGradient(
                    listOf(
                        ColorCompose.Black,
                        androidx.compose.ui.graphics.Color(52, 52, 52),
                        androidx.compose.ui.graphics.Color(2, 48, 32),
                        androidx.compose.ui.graphics.Color(52, 52, 52),
                        ColorCompose.Black
                    )
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) MenuLoadingView(message) else App()
        }


    }


    window.background = Color.BLACK
    window.extendedState = JFrame.MAXIMIZED_BOTH

    if (appKoin.get<Settings>().isFullscreen) {
        window.isUndecorated = true
    } else {
        window.minimumSize = Dimension(800, 600)
        window.isResizable = true
    }

    window.isVisible = true

//    window.addComponentListener(object : ComponentListener {
//        override fun componentResized(p0: ComponentEvent) {
//            println(p0.component.size)
//        }
//
//        override fun componentMoved(p0: ComponentEvent?) {
//        }
//
//        override fun componentShown(p0: ComponentEvent) {
//            val size = p0.component.size
//            canvas.size = size
//            canvas.doLayout()
//            val mode = canvas.graphicsConfiguration.device.displayMode
//            println("graphics bounds: ${canvas.graphicsConfiguration.bounds.size}")
//            println("bounds size: " + canvas.bounds.size)
//            println("mode: ${mode.width} ${mode.height}")
//            println("screen size toolkit: " + canvas.toolkit.screenSize)
//        }
//
//        override fun componentHidden(p0: ComponentEvent?) {
//        }
//
//    })

    panel.requestFocus()

    val panel2 = ComposePanel()

    panel2.isOpaque = false
    panel2.isFocusable = true
    panel2.size = Dimension(550, 180)

    panel2.setContent {
        val game = koinInject<Game>()
        BorderCard(
            modifier = Modifier
            .height(180.dp)
            .width(550.dp).onKeyEvent {
                if (it.key == Key.Escape && it.type == KeyDown) {
                    sendMessageDialog.isVisible = false
                }
                true
            },
            shape = RectangleShape
        ) {
            var chatMessage by remember { mutableStateOf("") }
            ExitButton {
                sendMessageDialog.isVisible = false
            }
            RWSingleOutlinedTextField(
                label = "Send Message",
                value = chatMessage,
                requestFocus = true,
                modifier = Modifier.fillMaxWidth().padding(10.dp)
                    .onKeyEvent {
                        if(it.key == Key.Enter && chatMessage.isNotEmpty()) {
                            game.gameRoom.sendChatMessage(chatMessage)
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
                            game.gameRoom.sendChatMessage(chatMessage)
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                RWTextButton("Send Message") {
                    game.gameRoom.sendChatMessage(chatMessage)
                    chatMessage = ""
                    sendMessageDialog.isVisible = false
                }

                RWTextButton("Send Team Message") {
                    game.gameRoom.sendChatMessage("-t $chatMessage")
                    chatMessage = ""
                    sendMessageDialog.isVisible = false
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
    val window = mainJFrame
    sendMessageDialog.setLocation(window.x + window.width / 2 - sendMessageDialog.width / 2, window.y + window.height / 2 - sendMessageDialog.height / 2)
}

