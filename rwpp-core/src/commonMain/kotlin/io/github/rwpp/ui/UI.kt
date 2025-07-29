/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import io.github.rwpp.appKoin
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.config.Settings
import io.github.rwpp.core.Initialization
import io.github.rwpp.event.EventPriority
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.events.DisconnectEvent
import io.github.rwpp.game.Player
import io.github.rwpp.ui.color.getTeamColor

object UI : Initialization, IUserInterface {
    internal var backgroundTransparency by mutableStateOf(appKoin.get<Settings>().backgroundTransparency)
    internal var selectedColorSchemeName by mutableStateOf(appKoin.get<Settings>().selectedTheme ?: "RWPP")
    var question by mutableStateOf<Question?>(null)
        internal set
    var warning by mutableStateOf<Warning?>(null)
        internal set
    var chatMessages by mutableStateOf(AnnotatedString(""))
        internal set

    var showMissionView by mutableStateOf(false)
    var showMultiplayerView by mutableStateOf(false)
    var showReplayView by mutableStateOf(false)
    var showSettingsView by mutableStateOf(false)
    var showModsView by mutableStateOf(false)
    var showRoomView by mutableStateOf(false)
    var showExtensionView by mutableStateOf(false)
    var showContributorList by mutableStateOf(false)
    var showResourceBrowser by mutableStateOf(false)

    var receivingNetworkDialogTitle by mutableStateOf("")
    var showNetworkDialog by mutableStateOf(false)

    private val relayRegex = Regex("""R\d+""")

    override fun showWarning(reason: String, isKicked: Boolean) {
        synchronized(UI) {
            warning = Warning(reason, isKicked)
        }
    }

    override fun showQuestion(title: String, message: String, callback: (String?) -> Unit) {
        synchronized(UI) {
            question = Question(title, message, callback)
        }
    }

    override fun showRequest(title: String, message: String, callback: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun showDialog(widget: Widget) {
        TODO("Not yet implemented")
    }

    fun onReceiveChatMessage(sender: String,  message: String, color: Int) {
        val configIO = appKoin.get<ConfigIO>()
        synchronized(UI) {
            chatMessages =
                buildAnnotatedString {
                    if (sender == "RELAY_CN-ADMIN") {
                        val result = relayRegex.find(message)?.value

                        if (!result.isNullOrBlank()) {
                            configIO.setGameConfig("lastNetworkIP", result)
                        }
                    }

                    if (sender.isNotBlank()) {
                        withStyle(
                            style = SpanStyle(
                                color = Player.getTeamColor(color),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("$sender: ")
                        }
                    }

                    withStyle(style = SpanStyle(color = Color.White)) {
                        append(message)
                    }

                    append("\n")
                } + chatMessages
        }

    }

    /**
     * @see [showQuestion]
     */
    class Question(val title: String, val message: String, val callback: (String?) -> Unit)

    /**
     * @see [showWarning]
     */
    class Warning(val reason: String, val isKicked: Boolean = false)

    class Request(val title: String, val message: String, val callback: (Boolean) -> Unit)

    class Dialog(val widget: Widget)

    override fun init() {
        GlobalEventChannel.filter(DisconnectEvent::class).subscribeAlways(priority = EventPriority.MONITOR) {
            chatMessages = AnnotatedString("")
        }
    }
}