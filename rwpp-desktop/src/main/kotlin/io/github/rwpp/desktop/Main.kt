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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.rwpp.*
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.config.ConfigModule
import io.github.rwpp.config.Settings
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.GameLoadedEvent
import io.github.rwpp.event.events.QuitGameEvent
import io.github.rwpp.event.onDispose
import io.github.rwpp.game.Game
import io.github.rwpp.game.comp.CompModule
import io.github.rwpp.game.sendChatMessageOrCommand
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.inject.GameLibraries
import io.github.rwpp.inject.runtime.Builder
import io.github.rwpp.scripts.Render
import io.github.rwpp.ui.*
import io.github.rwpp.ui.UI.chatMessages
import io.github.rwpp.widget.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import org.slf4j.LoggerFactory
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import kotlin.system.exitProcess


typealias ColorCompose = androidx.compose.ui.graphics.Color

var native: Boolean = false
var isSendingTeamChat by mutableStateOf(false)
lateinit var mainJFrame: JFrame
lateinit var gameCanvas: Canvas
lateinit var displaySize: Dimension
lateinit var sendMessageDialog: Dialog
lateinit var rwppVisibleSetter: (Boolean) -> Unit
lateinit var focusRequester: FocusRequester
var inGameWidget: Widget? = null
lateinit var inGameWidgetDialog: Dialog
var requireReloadingLib = false

//val cacheModSize = AtomicInteger(0)

fun main(array: Array<String>) {
    if (array.contains("-localgl") && File("opengl32.dll").exists()) { // for only debug
        System.loadLibrary("opengl32")
    }

    logger = LoggerFactory.getLogger(packageName)
    native = array.contains("-native")

    if (native) {
        // 指定输出文件路径
        val outFilePath = "rwpp-log.txt"

        try {
            // 创建文件输出流
            val fileOut = FileOutputStream(outFilePath)

            // 创建PrintStream实例
            val printOut = PrintStream(fileOut)

            // 重定向标准输出和标准错误输出
            System.setOut(printOut)
            System.setErr(printOut)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    koinInit = true
    appKoin = startKoin {
        logger(org.koin.core.logger.PrintLogger(org.koin.core.logger.Level.ERROR))
        modules(ConfigModule().module, DesktopModule().module, CompModule().module)
    }.koin

    appKoin.get<ConfigIO>().readAllConfig()
    Builder.outputDir = generatedLibDir
    Builder.logger = defaultBuildLogger
    requireReloadingLib = Builder.prepareReloadingLib()

    if (!requireReloadingLib) {
        val app = appKoin.get<AppContext>()
        app.init()
    }

    Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
    File("mods/maps/")
        .walk()
        .filter { it.name.startsWith("generated_") }
        .forEach {
            it.delete()
        }

    val settings = appKoin.get<Settings>()
    if (settings.renderingBackend != "Default") {
        System.setProperty("skiko.renderApi", settings.renderingBackend.uppercase())
    }

    displaySize =
        GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .defaultScreenDevice
            .displayMode
            .run { Dimension(width, height) }

    swingApplication()
}

fun swingApplication() = SwingUtilities.invokeLater {
    val panel = ComposePanel()
    panel.isVisible = true
    panel.size = displaySize.size
    panel.isOpaque = false
    panel.isFocusable = true
    rwppVisibleSetter = { panel.isVisible = it }
    panel.setContent {
        var isLoading by remember { mutableStateOf(true) }
        var message by remember { mutableStateOf("loading...") }
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                if (requireReloadingLib) {
                    runCatching {
                        Builder.init(GameLibraries.`game-lib`, File(System.getProperty("user.dir"), "game-lib.jar"))

                        Builder.logger?.info("Apply config successfully. Now you can restart game to take effect. (已成功应用配置，请重启游戏以生效。)")
                        if (native) {
                            val processBuilder = ProcessBuilder(System.getProperty("user.dir") + "/RWPP.exe")
                            processBuilder.start()
                            exitProcess(0)
                        }
                    }.onFailure {
                        Builder.logger?.error(it.stackTraceToString())
                    }
                } else {
                    val game = appKoin.get<Game>()
                    game.load { message = it }

                    GameLoadedEvent().broadcastIn()

                    isLoading = false
                }
            }
        }

        if (requireReloadingLib) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
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
                RWPPTheme(true) {
                    InjectConsole()
                }
            }
        } else {
            val settings = koinInject<Settings>()
            val isPremium = true
            var backgroundImagePath by remember { mutableStateOf(settings.backgroundImagePath ?: "") }
            val painter = remember(backgroundImagePath) {
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
    }

    val window = JFrame()
    val frame = JFrame("退出RWPP")
    frame.setSize(300, 200)
    if (requireReloadingLib) {
        window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    } else {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE)
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE)
    }
    window.background = java.awt.Color.BLACK
    window.extendedState = JFrame.MAXIMIZED_BOTH
    if(!requireReloadingLib && appKoin.get<Settings>().isFullscreen) {
        window.isUndecorated = true
    } else {
        window.minimumSize = Dimension(800, 600)
        window.isResizable = true
    }
    window.title = "Rusted Warfare Plus Plus"
    window.iconImage = ImageIO.read(ClassLoader.getSystemResource("composeResources/io.github.rwpp.rwpp_core.generated.resources/drawable/logo.png"))
    if (!requireReloadingLib) {
        window.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                val result = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure to exit RWPP? (确定要退出RWPP吗？)",
                    "提示",
                    JOptionPane.YES_NO_OPTION
                )
                if (result == JOptionPane.YES_OPTION) {
                    appKoin.get<AppContext>().exit()
                }
            }
        })
    }

    val canvas = Canvas()
    gameCanvas = canvas
    canvas.size = displaySize.size
    canvas.isVisible = false
    canvas.background = java.awt.Color.BLACK
    canvas.isFocusable = true

    window.layout = BorderLayout()
    window.add(canvas, BorderLayout.CENTER)
    window.add(panel, BorderLayout.CENTER)


    window.isVisible = true
    panel.requestFocus()

    focusRequester = FocusRequester()
    val panel2 = ComposePanel()
    panel2.isOpaque = false
    panel2.isFocusable = true
    panel2.size = Dimension(550, 540)
    panel2.setContent {
        val game = koinInject<Game>()
        RWPPTheme {
            BorderCard(
                modifier = Modifier
                    .fillMaxSize()
                    .onKeyEvent {
                        if (it.key == Key.Escape && it.type == KeyDown) {
                            sendMessageDialog.isVisible = false
                        }
                        true
                    },

                backgroundColor = Color(53, 57, 53),
                shape = RectangleShape
            ) {
                var chatMessage by remember { mutableStateOf("") }
                var allChatMessages by remember(chatMessages) {
                    mutableStateOf(TextFieldValue(chatMessages))
                }

                Box {
                    fun onExit() {
                        sendMessageDialog.isVisible = false
                        isSendingTeamChat = false
                    }

                    fun onSendMessage() {
                        if (isSendingTeamChat) {
                            game.gameRoom.sendChatMessage("-t $chatMessage")
                        } else {
                            game.gameRoom.sendChatMessageOrCommand(chatMessage)
                        }

                        chatMessage = ""
                        onExit()
                    }

                    ExitButton {
                        onExit()
                    }

                    GlobalEventChannel.filter(QuitGameEvent::class).onDispose {
                        subscribeAlways { onExit() }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(30.dp))
                        TextField(
                            value = allChatMessages,
                            onValueChange = { allChatMessages = it },
                            readOnly = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            colors = RWTextFieldColors,
                            maxLines = 100
                        )
                        RWSingleOutlinedTextField(
                            label = if (isSendingTeamChat) readI18n("ingame.sendTeamMessage") else readI18n("ingame.sendMessage"),
                            value = chatMessage,
                            focusRequester = focusRequester,
                            modifier = Modifier.fillMaxWidth().padding(10.dp)
                                .onKeyEvent {
                                    if ((it.key == Key.Enter || it.key == Key.NumPadEnter) &&  chatMessage.isNotEmpty()) {
                                        onSendMessage()
                                    }

                                    if (it.key == Key.Escape && it.type == KeyDown) {
                                        sendMessageDialog.isVisible = false
                                    }

                                    true
                                },
                            trailingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    null,
                                    modifier = Modifier.clickable {
                                        onSendMessage()
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
                                onExit()
                            }

                            RWTextButton(
                                readI18n("ingame.sendTeamMessage"),
                                modifier = Modifier.padding(5.dp)
                            ) {
                                game.gameRoom.sendChatMessage("-t $chatMessage")
                                chatMessage = ""
                                onExit()
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
    sendMessageDialog.size = Dimension(550, 540)
    sendMessageDialog.add(panel2)
    mainJFrame = window
    canvas.createBufferStrategy(2)

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

            resetSendDialogLocation()
        }

        override fun componentMoved(e: ComponentEvent) {
            resetSendDialogLocation()
        }
    })

    onInitInGameWidgetDialog()
}

fun onInitInGameWidgetDialog() = SwingUtilities.invokeLater {
    val panel = ComposePanel()
    panel.isOpaque = false
    panel.isFocusable = true
   // panel.size = Dimension(550, 540)
    panel.setContent {
        RWPPTheme {
            BorderCard(
                modifier = Modifier
                  //  .wrapContentSize()
                    .onKeyEvent {
                        if (it.key == Key.Escape && it.type == KeyDown) {
                            inGameWidgetDialog.isVisible = false
                            true
                        } else {
                            false
                        }
                    }.onGloballyPositioned { coordinates ->
                        SwingUtilities.invokeLater {
                            val scale = getDPIScale()
                            inGameWidgetDialog.preferredSize = Dimension(
                                (coordinates.size.width / scale).toInt(),
                                (coordinates.size.height / scale).toInt()
                            )
                            inGameWidgetDialog.pack()
                            resetInGameWidgetDialogLocation()
                        }
                    },
                backgroundColor = Color(53, 57, 53),
                shape = RectangleShape
            ) {
                inGameWidget?.Render()
            }
        }
    }

    inGameWidgetDialog = Dialog(mainJFrame).apply {
        isUndecorated = true
        isFocusable = true
        isVisible = false
        isAlwaysOnTop = true
        add(panel)
    }

    mainJFrame.addComponentListener(object : java.awt.event.ComponentAdapter() {
        override fun componentMoved(e: java.awt.event.ComponentEvent) {
            resetInGameWidgetDialogLocation()
        }

        override fun componentResized(e: java.awt.event.ComponentEvent) {
            resetInGameWidgetDialogLocation()
        }
    })
}

fun resetInGameWidgetDialogLocation() {
    inGameWidgetDialog.setLocation(
        mainJFrame.x + mainJFrame.width / 2 - inGameWidgetDialog.width / 2,
        mainJFrame.y + mainJFrame.height / 2 - inGameWidgetDialog.height / 2
    )
}

fun showSendMessageDialog() {
    sendMessageDialog.isVisible = true
    sendMessageDialog.requestFocus()
    focusRequester.requestFocus()
    resetSendDialogLocation()
}

private fun resetSendDialogLocation() {
    val window = mainJFrame
    sendMessageDialog.setLocation(window.x + window.width / 2 - sendMessageDialog.width / 2, window.y + window.height / 2 - sendMessageDialog.height / 2)
}

fun getDPIScale(): Double {
    // 获取原生系统缩放比例（需考虑多显示器场景）
    val env = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val device = env.defaultScreenDevice
    val config = device.defaultConfiguration

    // 获取系统推荐缩放倍数（Windows 的 % 缩放比例）
    val transform = config.defaultTransform
    return transform.scaleX // 通常 X/Y 缩放一致
}
