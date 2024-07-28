/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import io.github.rwpp.App
import io.github.rwpp.ContextController
import io.github.rwpp.LocalController
import io.github.rwpp.desktop.impl.GameContextControllerImpl
import io.github.rwpp.ui.RWSingleOutlinedTextField
import io.github.rwpp.ui.RWTextButton
import io.github.rwpp.ui.v2.Logo
import io.github.rwpp.ui.v2.TitleBrush
import org.jetbrains.compose.resources.*
import java.awt.*
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.WindowConstants
import kotlin.system.exitProcess


lateinit var mainJFrame: JFrame
lateinit var gameCanvas: Canvas
lateinit var displaySize: Dimension
lateinit var sendMessageDialog: Dialog
lateinit var gameContext: ContextController
lateinit var rwppVisibleSetter: (Boolean) -> Unit

fun main(args: Array<String>) {
   // val isSwingApplication = args.contains("-swingApplication")
    if (File("opengl32.dll").exists()) { // for only debug
        System.loadLibrary("opengl32")
    }



    displaySize =
        GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .defaultScreenDevice
            .displayMode
            .run { Dimension(width, height) }



    //if(isSwingApplication) {
    swingApplication()
    // } //else composeApplication()
}

//fun composeApplication() = application {
//    val windowState = rememberWindowState(placement = WindowPlacement.Maximized, position = WindowPosition(Alignment.Center))
//    var gameVisible by remember { mutableStateOf(true) }
//
//    gameCanvas = Canvas()
//
//    gameCanvas.size = Dimension(800, 600)
//    gameCanvas.isVisible = false
//    gameCanvas.background = Color.BLACK
//    gameCanvas.isFocusable = true
//
//
//    rwppVisibleSetter = { gameVisible = it }
//
//    gameContext = GameContextControllerImpl(::exitApplication)
//
//    Window(
//        onCloseRequest = { gameContext.exit() },
//        state = windowState,
//        title = "RWPP",
//        icon = Logo(),
//        resizable = true
//    ) {
//        mainJFrame = window
//
//        val panel = ComposePanel()
//        panel.isVisible = true
//        panel.size = Dimension(600, 200)
//        panel.isOpaque = false
//        panel.isFocusable = true
//        panel.setContent {
//            Column {
//                var chatMessage by remember { mutableStateOf("") }
//                RWSingleOutlinedTextField(
//                    label = "Send Message",
//                    value = chatMessage,
//                    modifier = Modifier.fillMaxWidth().padding(10.dp)
//                        .onKeyEvent {
//                            if(it.key == androidx.compose.ui.input.key.Key.Enter && chatMessage.isNotEmpty()) {
//                                gameContext.gameRoom.sendChatMessage(chatMessage)
//                                chatMessage = ""
//                                sendMessageDialog.isVisible = false
//                                true
//                            } else false
//                        },
//                    trailingIcon = {
//                        Icon(
//                            Icons.Default.ArrowForward,
//                            null,
//                            modifier = Modifier.clickable {
//                                gameContext.gameRoom.sendChatMessage(chatMessage)
//                                chatMessage = ""
//                                sendMessageDialog.isVisible = false
//                            }
//                        )
//                    },
//                    onValueChange =
//                    {
//                        chatMessage = it
//                    },
//                )
//
//                RWTextButton("Send Team") {
//                    gameContext.gameRoom.sendChatMessage("-t $chatMessage")
//                    chatMessage = ""
//                    sendMessageDialog.isVisible = false
//                }
//            }
//
//
//        }
//        sendMessageDialog = Dialog(window)
//        sendMessageDialog.isUndecorated = true
//        sendMessageDialog.isFocusable = true
//        sendMessageDialog.isVisible = false
//        sendMessageDialog.isAlwaysOnTop = true
//        sendMessageDialog.setSize(600, 200)
//        sendMessageDialog.add(panel)
//        window.minimumSize = Dimension(800, 600)
//        CompositionLocalProvider(
//            LocalController provides gameContext
//        ) {
//            val brush = TitleBrush()
//
//            Box(
//                modifier = Modifier.fillMaxSize().background(brush),
//                contentAlignment = Alignment.Center
//            ) {
//                if (!gameVisible) {
//                    App(sizeModifier = Modifier.fillMaxSize().focusable())
//                } else {
//                    SwingPanel(
//                        modifier = Modifier.fillMaxSize(),
//                        factory = { gameCanvas }
//                    )
//                }
//            }
//        }
//    }
//}

fun swingApplication() = SwingUtilities.invokeLater {

    val window = JFrame()
    gameContext = GameContextControllerImpl { exitProcess(0) }

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val panel = ComposePanel()
    panel.isVisible = true
    panel.size = Dimension(800, 600)
    panel.isOpaque = false
    panel.isFocusable = true

    rwppVisibleSetter = { panel.isVisible = it }

    window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    window.title = "RWPP"
    window.iconImage = ImageIO.read(ClassLoader.getSystemResource("drawable/logo.png"))


    val canvas = Canvas()

    gameCanvas = canvas
    canvas.size = displaySize.size
    canvas.isVisible = false
    canvas.background = Color.BLACK
    canvas.isFocusable = true

    window.layout = BorderLayout()
    window.add(canvas, BorderLayout.CENTER)
    window.add(panel, BorderLayout.CENTER)

    val gameContext = GameContextControllerImpl(window::dispose)

    panel.setContent {
        val brush = TitleBrush()
        CompositionLocalProvider(
            LocalController provides gameContext
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(brush),
                contentAlignment = Alignment.Center
            ) {
                App()
            }

        }
    }

    window.background = Color.BLACK
    window.extendedState = JFrame.MAXIMIZED_BOTH
    window.isVisible = true
    window.minimumSize = Dimension(800, 600)
    window.isResizable = true
    window.setLocationRelativeTo(null)
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
    panel2.size = Dimension(600, 200)
    panel2.isOpaque = false
    panel2.isFocusable = true
    panel2.setContent {
        Column {
            var chatMessage by remember { mutableStateOf("") }
            RWSingleOutlinedTextField(
                label = "Send Message",
                value = chatMessage,
                modifier = Modifier.fillMaxWidth().padding(10.dp)
                    .onKeyEvent {
                        if(it.key == androidx.compose.ui.input.key.Key.Enter && chatMessage.isNotEmpty()) {
                            gameContext.gameRoom.sendChatMessage(chatMessage)
                            chatMessage = ""
                            sendMessageDialog.isVisible = false
                            true
                        } else false
                    },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowForward,
                        null,
                        modifier = Modifier.clickable {
                            gameContext.gameRoom.sendChatMessage(chatMessage)
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

            RWTextButton("Send Team") {
                gameContext.gameRoom.sendChatMessage("-t $chatMessage")
                chatMessage = ""
                sendMessageDialog.isVisible = false
            }
        }
    }
    sendMessageDialog = Dialog(window)
    sendMessageDialog.isUndecorated = true
    sendMessageDialog.isFocusable = true
    sendMessageDialog.isVisible = false
    sendMessageDialog.isAlwaysOnTop = true
    sendMessageDialog.setSize(600, 200)
    sendMessageDialog.add(panel2)

    mainJFrame = window
    canvas.createBufferStrategy(2)
}

