/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rwpp.AppContext
import io.github.rwpp.appKoin
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.config.Settings
import io.github.rwpp.core.Initialization
import io.github.rwpp.coreVersion
import io.github.rwpp.event.EventPriority
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.events.DisconnectEvent
import io.github.rwpp.game.Player
import io.github.rwpp.i18n.I18nType
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.net.Net
import io.github.rwpp.projectVersion
import io.github.rwpp.rwpp_core.generated.resources.*
import io.github.rwpp.ui.UI.showQuestion
import io.github.rwpp.ui.UI.showWarning
import io.github.rwpp.ui.color.getTeamColor
import io.github.rwpp.widget.MenuButton
import io.github.rwpp.widget.lazyRowDesktopScrollable
import io.github.rwpp.widget.v2.RWIconButton
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

object UI : Initialization, IUserInterface {
    internal var backgroundTransparency by mutableStateOf(appKoin.get<Settings>().backgroundTransparency)
    internal var selectedColorSchemeName by mutableStateOf(appKoin.get<Settings>().selectedTheme ?: "RWPP")
    var question by mutableStateOf<Question?>(null)
        internal set
    var warning by mutableStateOf<Warning?>(null)
        internal set
    var dialogWidget by mutableStateOf<Widget?>(null)
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

    var roomSelectedPlayer by mutableStateOf<Player?>(null)
        internal set
    var receivingNetworkDialogTitle by mutableStateOf("")
    var showNetworkDialog by mutableStateOf(false)
    var UiProvider: UIProvider = UIProvider()

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

    override fun showDialog(widget: Widget) {
        synchronized(UI) {
            dialogWidget = widget
        }
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

    override fun init() {
        GlobalEventChannel.filter(DisconnectEvent::class).subscribeAlways(priority = EventPriority.MONITOR) {
            chatMessages = AnnotatedString("")
        }
    }
}

open class UIProvider {
    val extraMenuList = mutableListOf<Menu>()

    @Composable
    open fun MainMenu(
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

                    items(extraMenuList, key = { it.title }) {
                        MenuButton(
                            it.title,
                            it.iconModel,
                            onClick = it.onClick
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

    class Menu(
        val title: String,
        val iconModel: Any?,
        val onClick: () -> Unit
    )
}

