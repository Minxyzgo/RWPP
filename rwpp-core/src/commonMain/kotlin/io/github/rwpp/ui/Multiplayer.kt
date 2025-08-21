/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("DuplicatedCode")

package io.github.rwpp.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.rwpp.AppContext
import io.github.rwpp.config.*
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.CloseUIPanelEvent
import io.github.rwpp.event.events.JoinGameEvent
import io.github.rwpp.game.Game
import io.github.rwpp.game.data.RoomOption
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.gameVersion
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.io.SizeUtils
import io.github.rwpp.logger
import io.github.rwpp.maxModSize
import io.github.rwpp.net.Net
import io.github.rwpp.net.RoomDescription
import io.github.rwpp.net.sorted
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.platform.readPainterByBytes
import io.github.rwpp.rwpp_core.generated.resources.*
import io.github.rwpp.widget.*
import io.github.rwpp.widget.v2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.roundToInt

@Suppress("UnusedMaterial3ScaffoldPaddingParameter", "RememberReturnType")
@Composable
fun MultiplayerView(
    onExit: () -> Unit,
    onOpenRoomView: () -> Unit,
) {
    BackHandler(UI.showMultiplayerView, onExit)
    DisposableEffect(Unit) {
        onDispose {
            CloseUIPanelEvent("multiplayer").broadcastIn()
        }
    }

    val instance = koinInject<MultiplayerPreferences>()
    val settings = koinInject<Settings>()
    val configIO = koinInject<ConfigIO>()
    val game = koinInject<Game>()
    val net = koinInject<Net>()
    val blacklistsInstance = koinInject<Blacklists>()

    val refresh = remember { Channel<Unit>(1) }
    var isRefreshing by remember { mutableStateOf(false) }


    var updateServerConfig by remember { mutableStateOf(false) }
    val allServerData = remember {
        SnapshotStateList<ServerData>().apply {
            addAll(
                instance.allServerConfig.map { ServerData(it) }
            )
        }
    }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        allServerData.add(to.index - 1, allServerData.removeAt(from.index - 1))
        instance.allServerConfig.add(to.index - 1, instance.allServerConfig.removeAt(from.index - 1))
    }

    var currentViewList by remember { mutableStateOf<List<RoomDescription>>(listOf()) }
    var throwable by remember { mutableStateOf<Throwable?>(null) }

    var userName by remember {
        val lastName = configIO.getGameConfig<String?>("lastNetworkPlayerName")
        mutableStateOf((lastName ?: "RWPP${(0..999).random()}").also { game.setUserName(it) })
    }

    val blacklists = remember { mutableStateListOf<Blacklist>().apply { addAll(blacklistsInstance.blacklists) } }


    var enableModFilter by remember { mutableStateOf(false) }
    var mapNameFilter by remember { mutableStateOf(instance.mapNameFilter) }
    var creatorNameFilter by remember { mutableStateOf(instance.creatorNameFilter) }
    var playerLimitRange by remember { mutableStateOf(instance.playerLimitRangeFrom..instance.playerLimitRangeTo) }
    var joinServerAddress by rememberSaveable { mutableStateOf(instance.joinServerAddress) }
    val showWelcomeMessage by remember { mutableStateOf(settings.showWelcomeMessage) }
    var battleroom by remember { mutableStateOf(instance.battleroom) }

    var serverAddress by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }

    var isShowingRoomList by remember { mutableStateOf(false) }
    var selectedServerConfig by remember {
        val config = instance.allServerConfig.firstOrNull { it.useAsDefaultList }
        if (config != null) isShowingRoomList = true
        mutableStateOf(config)
    }

    var selectedDefaultRoomList by remember { mutableStateOf(allServerData.firstOrNull { it.config.useAsDefaultList }) }
    var showServerInfoConfig by remember { mutableStateOf(false) }

    var selectedRoomDescription by remember { mutableStateOf<RoomDescription?>(null) }
    var showJoinRequestDialog by remember { mutableStateOf(false) }
    var showWelcomeMessageAdmittingDialog by remember { mutableStateOf(showWelcomeMessage == null) }
    val scope = rememberCoroutineScope()

    remember(blacklists.size) {
        blacklistsInstance.blacklists = blacklists.toMutableList()
    }
    remember(mapNameFilter) { instance.mapNameFilter = mapNameFilter }
    remember(creatorNameFilter) { instance.creatorNameFilter = creatorNameFilter }
    remember(joinServerAddress) { instance.joinServerAddress = joinServerAddress }
    remember(playerLimitRange) {
        instance.playerLimitRangeFrom = playerLimitRange.first
        instance.playerLimitRangeTo = playerLimitRange.last
    }

    DisposableEffect(Unit) {
        onDispose {
            game.setUserName(userName)
        }
    }

    WelcomeMessageAdmittingDialog(
        showWelcomeMessageAdmittingDialog
    ) {
        showWelcomeMessageAdmittingDialog = false
    }

    JoinServerRequestDialog(showJoinRequestDialog, { showJoinRequestDialog = false },
       selectedRoomDescription, blacklists
    ) { dismiss ->
        serverAddress = selectedRoomDescription!!.addressProvider()
        isConnecting = true
        dismiss()
    }

    LoadingView(isConnecting, onLoaded = { game.cancelJoinServer(); isConnecting = false }, cancellable = true) {
        if(serverAddress.isBlank()) {
            message("That server no longer exists")
            return@LoadingView false
        }

        message("connecting...")

        game.setUserName(userName)
        configIO.setGameConfig("lastNetworkIP", serverAddress)

        val result = game.directJoinServer(serverAddress, selectedRoomDescription?.uuid2, this)
        selectedRoomDescription = null
        if(result.isSuccess) {
            onExit()
            onOpenRoomView()
            JoinGameEvent(serverAddress).broadcastIn()
            true
        } else {
            message(result.exceptionOrNull()!!.message!!)
            false
        }
    }

    @Composable
    fun HostGameDialog(
        visible: Boolean,
        onDismissRequest: () -> Unit,
        onHost: () -> Unit,
    ) = AnimatedAlertDialog(
        visible, onDismissRequest = onDismissRequest
    ) { dismiss ->
        BorderCard(
            modifier = Modifier
                .width(500.dp)
                .padding(10.dp)
                .autoClearFocus(),
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                ),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Host Game", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            LargeDividingLine { 0.dp }

            val modManager = koinInject<ModManager>()
            var enableMods by remember { mutableStateOf(false) }
            var hostByRCN by remember { mutableStateOf(false) }
            var transferMod by remember { mutableStateOf(false) }
            val modSize by remember {
                mutableStateOf(
                    modManager.getAllMods()
                        .filter { it.isEnabled }
                        .sumOf { it.getSize() }
                )
            }


            remember(enableMods, hostByRCN) {
                if(!enableMods || hostByRCN) transferMod = false
            }

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RWCheckbox(enableMods, onCheckedChange = { enableMods = !enableMods }, modifier = Modifier.padding(5.dp))
                    Text(readI18n("multiplayer.enableMods"), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 5.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RWCheckbox(hostByRCN, onCheckedChange = { hostByRCN = !hostByRCN }, modifier = Modifier.padding(5.dp))
                    Text(readI18n("multiplayer.hostByRCN"), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 5.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RWCheckbox(transferMod,
                        onCheckedChange = { transferMod = !transferMod },
                        modifier = Modifier.padding(5.dp),
                        enabled = enableMods && modSize <= maxModSize && !hostByRCN
                    )
                    Text("${readI18n("multiplayer.transferMod")} ${if(modSize > maxModSize) "(Disabled for total mods size: ${SizeUtils.byteToMB(modSize)}MB > ${SizeUtils.byteToMB(maxModSize)}MB)" else ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }


                var password by remember { mutableStateOf("") }
                RWSingleOutlinedTextField(
                    readI18n("multiplayer.password"),
                    password,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 2.dp),
                    enabled = !hostByRCN,
                ) { password = it }

                var port by remember { mutableStateOf(configIO.getGameConfig<Int?>("networkPort")) }
                RWSingleOutlinedTextField(
                    readI18n("multiplayer.port"),
                    port?.toString() ?: "",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
                    lengthLimitCount = 5,
                    typeInNumberOnly = true,
                    enabled = !hostByRCN,
                ) {
                    port = it.toIntOrNull()
                    configIO.setGameConfig("networkPort", port ?: 5123)
                }

                val rcnAddress = "36.151.65.203:5123"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    RWTextButton(readI18n("multiplayer.hostPrivate"), modifier = Modifier.padding(5.dp)) {
                        dismiss()
                        game.gameRoom.option = RoomOption(transferMod, modSize.toInt())
                        if (hostByRCN) {
                            game.onQuestionCallback(if (enableMods) "smod" else "snew")
                            serverAddress = rcnAddress
                            isConnecting = true
                        } else {
                            onHost()
                            game.hostStartWithPasswordAndMods(
                                false, password.ifBlank { null }, enableMods,
                            )
                        }
                    }
                    RWTextButton(readI18n("multiplayer.hostPublic"), modifier = Modifier.padding(5.dp)) {
                        dismiss()
                        game.gameRoom.option = RoomOption(transferMod, modSize.toInt())
                        if (hostByRCN) {
                            game.onQuestionCallback(if (enableMods) "smodup" else "snewup")
                            serverAddress = rcnAddress
                            isConnecting = true
                        } else {
                            onHost()
                            game.hostStartWithPasswordAndMods(
                                true, password.ifBlank { null }, enableMods,
                            )
                        }
                    }
                }
            }


        }
    }

    fun LazyListScope.RoomListAnimated(
        descriptions: List<RoomDescription>
    ) {

        val statusWeight = .3f
        val creatorNameWeight = .3F
        val countWeight = .1f
        val mapWeight = .6f
        val versionWeight = .2f
        val openWeight = .1f

        items(
            count = descriptions.size,
            key = { descriptions[it].uuid }
        ) { index ->
            val desc = descriptions[index]
            Row(
                modifier = Modifier
                    .then(if (koinInject<Settings>().enableAnimations)
                        Modifier.animateItem()
                    else Modifier)
                    .height(IntrinsicSize.Max)
                    .padding(5.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), CircleShape)
                    .fillMaxWidth()
                    .clickable {
                        selectedRoomDescription = desc
                        showJoinRequestDialog = true
                    }
            ) {
                val color: Color =
                    if (desc.gameVersion != gameVersion) {
                        Color.Gray
                    } else if (desc.isUpperCase) {
                        Color.White
                    } else if (desc.isLocal) {
                        Color(255, 127, 80)
                    } else if (desc.isOpen) {
                        Color(200, 200, 200)
                    } else {
                        Color.Gray
                    }
                val open: String =
                    if (desc.requiredPassword) {
                        "P"
                    } else if (desc.isLocal) {
                        "L"
                    } else if (desc.isOpen) {
                        "Y"
                    } else {
                        "N"
                    }

                TableCell(
                    desc.status,
                    statusWeight,
                    color = color,
                    modifier = Modifier.fillMaxHeight(),
                    drawStroke = false
                )
                TableCell(
                    desc.creator,
                    creatorNameWeight,
                    modifier = Modifier.fillMaxHeight(),
                    color = color
                )
                TableCell(
                    (desc.playerCurrentCount ?: "").toString() + "/" + desc.playerMaxCount.toString(),
                    countWeight,
                    modifier = Modifier.fillMaxHeight(),
                    color = color
                )
                TableCell(
                    desc.mapName.removeSuffix(".tmx"),
                    mapWeight,
                    modifier = Modifier.fillMaxHeight(),
                    color = color
                )
                TableCell(desc.version, versionWeight, modifier = Modifier.fillMaxHeight(), color = color)

                TableCell(
                    open,
                    openWeight,
                    drawStroke = false,
                    modifier = Modifier.fillMaxHeight(),
                    color = color
                )
            }
        }
    }

    @Composable
    fun AnimatedServerConfigInfo(
        visible: Boolean,
        serverConfig: ServerConfig?,
        onDismissRequest: () -> Unit,
    ) {
        AnimatedAlertDialog(
            visible,
            onDismissRequest = onDismissRequest
        ) { dismiss ->
            BorderCard(
                modifier = Modifier
                    .width(500.dp)
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
                    .autoClearFocus()
            ) {
                Box {
                    ExitButton(dismiss)
                    var type by remember { mutableStateOf(serverConfig?.type ?: ServerType.Server) }
                    var url by remember { mutableStateOf(serverConfig?.ip ?: "") }
                    var name by remember { mutableStateOf(serverConfig?.name ?: "") }

                    Column(modifier = Modifier.padding(10.dp)) {
                        if (serverConfig == null) {
                            LargeDropdownMenu(
                                modifier = Modifier.padding(20.dp),
                                label = "Server Type",
                                items = ServerType.entries,
                                selectedIndex = type.ordinal,
                                onItemSelected = { _, t -> type = t }
                            )
                        }

                        RWSingleOutlinedTextField(
                            "Name",
                            name,
                            modifier = Modifier.fillMaxWidth().padding(10.dp)
                        ) { name = it }

                        RWSingleOutlinedTextField(
                            "Url/Ip",
                            url,
                            modifier = Modifier.fillMaxWidth().padding(10.dp)
                        ) { url = it }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            TextButton(
                                onClick =
                                {
                                    if (serverConfig != null) {
                                        serverConfig.ip = url
                                        serverConfig.name = name
                                    } else {
                                        val config = ServerConfig(url, name, type)
                                        instance.allServerConfig.add(config)
                                        allServerData.add(
                                            ServerData(
                                                config
                                            )
                                        )
                                    }

                                    updateServerConfig = !updateServerConfig
                                    onDismissRequest()
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd),
                            ) { Text("Apply", style = MaterialTheme.typography.bodyLarge) }
                        }
                    }
                }
            }
        }
    }

    fun LazyListScope.ServerList(
        serverDataList: List<ServerData>
    ) {
        items(
            serverDataList,
            key = { it.hashCode() }
        ) { serverData ->
            //val serverData = remember { serverDataList[index] }

            ReorderableItem(reorderableLazyListState, serverData.hashCode()) {

                val interactionSource = remember { MutableInteractionSource() }

                Card(
                    onClick = {
                        if (serverData.config.type == ServerType.Server) {
                            val ip = serverData.config.ip
                            serverAddress = ip
                            configIO.setGameConfig("lastNetworkIP", ip)
                            isConnecting = true
                        } else if (serverData.config.type == ServerType.RoomList) {
                            selectedServerConfig = serverData.config
                            isShowingRoomList = true
                            currentViewList = listOf()
                            refresh.trySend(Unit)
                        }
                    },
                    modifier = Modifier.then(if (koinInject<Settings>().enableAnimations)
                        Modifier.animateItem()
                    else Modifier)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(5.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(0.9f)),
                    elevation =  CardDefaults.cardElevation(defaultElevation = 10.dp),
                    border = BorderStroke(2.dp,  MaterialTheme.colorScheme.surfaceContainer),
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box {
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.TopEnd),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (serverData.config.editable) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.padding(5.dp).clickable {
                                    instance.allServerConfig.remove(serverData.config)
                                    allServerData.remove(serverData)
                                })

                                Icon(
                                    Icons.Default.Info,
                                    null,
                                    modifier = Modifier.padding(5.dp).clickable {
                                        selectedServerConfig = serverData.config
                                        showServerInfoConfig = true
                                    }
                                )
                            }

                            IconButton(
                                modifier = Modifier.padding(5.dp, 5.dp, 20.dp, 5.dp).size(30.dp).draggableHandle(
                                    onDragStopped = {
                                        updateServerConfig = !updateServerConfig
                                    },
//                                    onDragStarted = {
//                                        ViewCompat.performHapticFeedback(
//                                            view,
//                                            HapticFeedbackConstantsCompat.GESTURE_START
//                                        )
//                                    },
//                                    onDragStopped = {
//                                        ViewCompat.performHapticFeedback(
//                                            view,
//                                            HapticFeedbackConstantsCompat.GESTURE_END
//                                        )
//                                    },
                                    interactionSource = interactionSource,
                                ),
                                onClick = {},
                            ) {
                                Icon(painter = painterResource(Res.drawable.drag_30), contentDescription = "Reorder")
                            }
                        }

                        Row(modifier = Modifier.align(Alignment.TopStart)) {
                            val iconPainter = remember {
                                runCatching {
                                    serverData.infoPacket?.iconBytes?.let { readPainterByBytes(it) }
                                }.getOrNull()
                            }

                            val checked = remember(selectedDefaultRoomList) {
                                selectedDefaultRoomList == serverData
                            }

                            if (serverData.config.type == ServerType.Server) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        iconPainter ?: painterResource(Res.drawable.error_missingmap),
                                        null,
                                        modifier = Modifier.size(120.dp).padding(5.dp)
                                    )
                                    if (serverData.isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                                }
                            }

                            val info = remember(serverData.infoPacket) { serverData.infoPacket }
                            val playerCount = remember(info) { info?.run { "$currentPlayer / $maxPlayerSize" } ?: "" }
                            val version = remember(info) {
                                info?.run { version } ?: ""
                            }
                            val description = remember(info) {
                                info?.run { description } ?: ""
                            }
                            val mapName = remember(info) {
                                info?.run { mapName } ?: ""
                            }
//                    val ping = remember(info) {
//                        info?.run { ping.toString() } ?: ""
//                    }
                            val mods = remember(info) {
                                info?.run { mods } ?: ""
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                val configName = remember(updateServerConfig) { serverData.config.name }

                                Text(
                                    buildAnnotatedString {
                                        append(info?.name ?: configName)
                                        withStyle(
                                            SpanStyle(
                                                color = Color.DarkGray,
                                                fontStyle = FontStyle.Italic
                                            )
                                        ) {
                                            if (version.isNotEmpty()) append("  Version: $version")
                                        }
                                    },
                                    modifier = Modifier.padding(3.dp),
                                    style = MaterialTheme.typography.headlineMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (serverData.config.type == ServerType.Server) {
                                    Text(
                                        buildString {
                                            appendLine("player: $playerCount")
                                            appendLine("playing map: $mapName")
                                            appendLine(description)
                                            //   appendLine("ping: ${ping}ms")
                                            if (mods.isNotBlank()) appendLine("enabled mods: $mods")
                                        },
                                        modifier = Modifier.padding(3.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                } else if (serverData.config.type == ServerType.RoomList) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RWCheckbox(
                                            checked, {
                                                if (it) {
                                                    instance.allServerConfig.forEach { c -> c.useAsDefaultList = false }
                                                    serverData.config.useAsDefaultList = true
                                                    selectedDefaultRoomList = serverData
                                                } else {
                                                    selectedDefaultRoomList = null
                                                    serverData.config.useAsDefaultList = false
                                                }
                                            }
                                        )

                                        Text(readI18n("multiplayer.useAsDefaultList"))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }
        }
    }


    fun resetFilter() {
        enableModFilter = false
        playerLimitRange = 0..100
        mapNameFilter = ""
        creatorNameFilter = ""
    }

    @Composable
    fun BlacklistTargetDialog(
        visible: Boolean,
        onDismissRequest: () -> Unit,
    ) {
        var showBlacklistInfo by remember { mutableStateOf(false) }
        var infoSelectedIndex by remember { mutableStateOf(0) }
        var addMode by remember { mutableStateOf(false) }

        DisposableEffect(key1 = visible) {
            onDispose {
                if(!visible) {
                    showBlacklistInfo = false
                    addMode = false
                }
            }
        }

        AnimatedAlertDialog(
            visible = visible,
            onDismissRequest = { onDismissRequest() },
        ) { _ ->
            BorderCard(
                modifier = Modifier.fillMaxSize(GeneralProportion()),
            ) {
                AnimatedBlackList(
                    !showBlacklistInfo,
                    blacklists,
                    {
                        blacklists.removeAt(it)
                        infoSelectedIndex = 0
                    },
                    { index  ->
                        infoSelectedIndex = index
                        showBlacklistInfo = true
                    },
                    {
                        addMode = true
                        showBlacklistInfo = true
                    }
                )

                if(addMode) {
                    AnimatedBlacklistInfo(showBlacklistInfo, Blacklist("", ""), {
                        showBlacklistInfo = false
                        addMode = false
                    }) {
                        blacklists.add(it)
                    }
                } else {
                    AnimatedBlacklistInfo(showBlacklistInfo, blacklists.getOrNull(infoSelectedIndex), { showBlacklistInfo = false }) {
                        blacklists[infoSelectedIndex] = it
                    }
                }
            }
        }
    }

    @Composable
    fun JoinServerField() {
        RWSingleOutlinedTextField(
            label = readI18n("multiplayer.joinServer"),
            value = joinServerAddress,
            modifier = Modifier.width(400.dp).padding(10.dp),
            leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(30.dp)) },
            trailingIcon = {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    modifier = Modifier.clickable {
                        if(joinServerAddress.isNotBlank()) {
                            serverAddress = joinServerAddress
                            isConnecting = true
                        }
                    })
            },
            onValueChange =
            {
                joinServerAddress = it
                configIO.setGameConfig("lastNetworkIP", it)
            },
        )
    }

    @Composable
    fun FilterSurfaceDialog(
        visible: Boolean,
        onDismissRequest: () -> Unit
    ) {
        AnimatedAlertDialog(
            visible, onDismissRequest = onDismissRequest
        ) { dismiss ->
            BorderCard(
                modifier = Modifier
                    .fillMaxSize(GeneralProportion())
                    .padding(10.dp)
                    .autoClearFocus(),
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(.6f)
            ) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(readI18n("common.filter"), style = MaterialTheme.typography.headlineMedium)
                    }

                    LargeDividingLine { 0.dp }

                    var showBlacklistDialog by remember {
                        mutableStateOf(false)
                    }

                    BlacklistTargetDialog(showBlacklistDialog) { showBlacklistDialog = false }

                    SettingsTextField(
                        label = readI18n("multiplayer.filter.gameMapNameFilter"),
                        mapNameFilter,
                    ) {
                        mapNameFilter = it
                    }

                    SettingsTextField(
                        label = readI18n("multiplayer.filter.creatorNameFilter"),
                        creatorNameFilter,
                    ) {
                        creatorNameFilter = it
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            var range by remember(playerLimitRange) { mutableStateOf(playerLimitRange) }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    readI18n("multiplayer.filter.playerLimitRange"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            RangeSlider(
                                valueRange = 0f..100f,
                                modifier = Modifier.width(250.dp).padding(5.dp),
                             //   steps = 101,
                                value = range.first.toFloat()..range.last.toFloat(),
                                colors = RWSliderColors,
                                onValueChange = { range = it.start.roundToInt()..it.endInclusive.roundToInt() },
                                onValueChangeFinished = { playerLimitRange = range }
                            )

                            Text(
                                "${range.first} ~ ${range.last}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(50.dp).padding(5.dp)
                            )
                        }

                        HorizontalDivider()
                    }

                    SettingsSwitchComp(
                        "",
                        readI18n("multiplayer.enableMods"),
                        enableModFilter
                    ) {
                        enableModFilter = it
                    }

                    SettingsSwitchComp(
                        "",
                        readI18n("multiplayer.battleroom"),
                        battleroom
                    ) {
                        battleroom = it
                    }

                    RWTextButton(
                        readI18n("multiplayer.filter.blacklist"),
                        modifier = Modifier.padding(5.dp)
                    ) {
                        showBlacklistDialog = true
                    }

                    RWTextButton(
                        readI18n("multiplayer.filter.reset"),
                        modifier = Modifier.padding(5.dp)
                    ) {
                        resetFilter()
                    }
                }
            }
        }
    }

    with(net) {
        LaunchedEffect(Unit) {
            refresh.send(Unit)
            for(u in refresh) {
                if (isRefreshing) continue
                throwable = null
                isRefreshing = true
                try {
                    val roomList = selectedServerConfig
                    if (isShowingRoomList && roomList != null) {
                        if (roomList.customRoomListProvider != null) {
                            currentViewList = getRoomListFromSourceUrl(
                                roomList.ip.split(";")
                            )
                        } else {
                            currentViewList = roomList.customRoomListProvider!!()
                        }
                    } else {
                        for(s in allServerData) {
                            if (s.config.type == ServerType.Server) {
                                launch(Dispatchers.IO) {
                                    s.isLoading = true
                                    s.infoPacket = runCatching {
                                        s.config.getServerInfo()
                                    }.onFailure {
                                        logger.error(it.stackTraceToString())
                                    }.getOrNull()
                                    s.isLoading = false
                                }
                            }
                        }
                    }

                } catch(e: Throwable) {
                    throwable = e
                }

                isRefreshing = false
            }
        }
    }

    var hostDialogVisible by remember { mutableStateOf(false) }
    var filterSurfaceDialogVisible by remember { mutableStateOf(false) }

    FilterSurfaceDialog(filterSurfaceDialogVisible) { filterSurfaceDialogVisible = false }
    HostGameDialog(hostDialogVisible, { hostDialogVisible = false }) {
        onExit()
        onOpenRoomView()
        game.setUserName(userName)
    }


    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(10.dp)) {

                Row(modifier = Modifier.align(Alignment.Center)) {
                    RWTextButton(
                        readI18n("multiplayer.host"),
                        leadingIcon = { Icon(Icons.Default.Build, null, modifier = Modifier.size(30.dp)) },
                        modifier = Modifier.padding(10.dp)
                    ) { hostDialogVisible = true }

                    RWTextButton(
                        label = readI18n("multiplayer.joinLastGame"),
                        leadingIcon = { Icon(painter = painterResource(Res.drawable.replay_30), null, modifier = Modifier.size(30.dp)) },
                        modifier = Modifier.padding(10.dp)
                    ) {
                        val lastIp = configIO.getGameConfig<String?>("lastNetworkIP")
                        if (lastIp != null) {
                            serverAddress = lastIp
                            isConnecting = true
                        }
                    }
                }

                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                    if (isShowingRoomList) {
                        FloatingActionButton(
                            onClick = { isShowingRoomList = false },
                            shape = CircleShape,
                            modifier = Modifier.padding(5.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.surfaceTint) }
                    } else {
                        FloatingActionButton(
                            onClick = { selectedServerConfig = null; showServerInfoConfig = true },
                            shape = CircleShape,
                            modifier = Modifier.padding(5.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ) { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.surfaceTint) }
                    }

                    Box {
                        val requester = remember { FocusRequester() }

                        LaunchedEffect(Unit) {
                            requester.requestFocus()
                        }

                        FloatingActionButton(
                            onClick = { if(!isRefreshing) scope.launch { refresh.trySend(Unit) } },
                            shape = CircleShape,
                            modifier = Modifier.padding(5.dp).focusRequester(requester),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            if(isRefreshing) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                            } else {
                                Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.surfaceTint)
                            }
                        }

                        val appContext = koinInject<AppContext>()

                        if (appContext.isDesktop()) {
                            Card(
                                modifier = Modifier.align(Alignment.BottomCenter).padding(1.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                            ) {
                                Text("space", modifier = Modifier.padding(1.dp), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
    ) {
        ExpandedCard {
            Box {
                ExitButton(onExit)
                Column {
                    Spacer(modifier = Modifier.height(30.dp))
                    val realList = remember(
                        currentViewList,
                        enableModFilter,
                        playerLimitRange,
                        mapNameFilter,
                        creatorNameFilter,
                        blacklists.size,
                        battleroom
                    ) {
                        currentViewList.filter { room ->
                            if (blacklists.any { it.uuid == room.uuid }) return@filter false

                            if (battleroom && room.status == "ingame") return@filter false
                            if (enableModFilter) {
                                if (!room.version.contains("mod", true) && room.mods.isBlank()) {
                                    return@filter false
                                }
                            }

                            if (mapNameFilter.isNotBlank()) {
                                return@filter room.mapName.contains(mapNameFilter, true)
                            }

                            if (creatorNameFilter.isNotBlank()) {
                                return@filter room.creator.contains(creatorNameFilter, true)
                            }

                            if (room.playerMaxCount != null) {
                                return@filter room.playerMaxCount in playerLimitRange || room.playerMaxCount!! > 100
                            }

                            true
                        }.sorted
                    }

                    AnimatedServerConfigInfo(
                        showServerInfoConfig,
                        selectedServerConfig,
                    ) { showServerInfoConfig = false }

                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface
                    ) {
                        LazyColumnScrollbar(
                            listState = lazyListState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = lazyListState
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            label = {
                                                Text(
                                                    readI18n("multiplayer.userName"),
                                                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily
                                                )
                                            },
                                            textStyle = MaterialTheme.typography.headlineLarge,
                                            colors = RWOutlinedTextColors,
                                            value = userName,
                                            enabled = true,
                                            singleLine = true,
                                            leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(30.dp)) },
                                            modifier = Modifier.width(200.dp).padding(10.dp),
                                            onValueChange =
                                            {
                                                userName = it
                                            },
                                        )

                                        JoinServerField()

                                        RWIconButton(painterResource(Res.drawable.tune_30), modifier = Modifier.padding(5.dp), size = 50.dp) { filterSurfaceDialogVisible = true }
                                    }
                                }

                                if (throwable != null && isShowingRoomList) {
                                    item {
                                        SelectionContainer {
                                            Text(throwable?.stackTraceToString() ?: "", color = Color.Red)
                                        }
                                    }
                                } else {
                                    if (isShowingRoomList) {
                                        RoomListAnimated(realList)
                                    } else {
                                        ServerList(/*realServerData*/ allServerData)
                                    }
                                }
                            }
                        }

//                var list by remember { mutableStateOf(List(100) { "Item $it" }) }
//                val lazyListState = rememberLazyListState()
//                val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
//                    list = list.toMutableList().apply {
//                        add(to.index, removeAt(from.index))
//                    }
//                }
//
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize(),
//                    state = lazyListState,
//                    contentPadding = PaddingValues(8.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp),
//                ) {
//                    items(list, key = { it }) { item ->
//                        ReorderableItem(reorderableLazyListState, key = item) {
//                            val interactionSource = remember { MutableInteractionSource() }
//
//                            Card(
//                                onClick = {},
//                                interactionSource = interactionSource,
//                            ) {
//                                Row {
//                                    Text(item, Modifier.padding(horizontal = 8.dp))
//                                    IconButton(
//                                        modifier = Modifier.draggableHandle(
//                                            onDragStarted = {
//
//                                            },
//                                            onDragStopped = {
//
//                                            },
//                                            interactionSource = interactionSource,
//                                        ),
//                                        onClick = {},
//                                    ) {
//                                        Icon(Icons.Rounded.Place, contentDescription = "Reorder")
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }

                    }
                }
            }
        }
    }
}




@Composable
private fun AnimatedBlackList(
    visible: Boolean,
    blacklists: SnapshotStateList<Blacklist>,
    onDeleteSource: (Int) -> Unit,
    onTapInfoButton: (Int) -> Unit,
    onTapAddButton: () -> Unit,
) {
    val enableAnimations = koinInject<Settings>().enableAnimations
    AnimatedVisibility(
        visible,
        enter = if (enableAnimations) fadeIn() + expandIn() else EnterTransition.None,
        exit = if (enableAnimations) shrinkOut() + fadeOut() else ExitTransition.None,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(readI18n("multiplayer.blacklist"), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(15.dp))
                LargeDividingLine { 0.dp }

                LazyColumn(
                    modifier = Modifier.selectableGroup().weight(1f),
                ) {
                    items(count = blacklists.size) { index ->
                        val blacklist = blacklists[index]
                        Modifier
                            .wrapContentSize()
                        Row(modifier = Modifier.then(if (koinInject<Settings>().enableAnimations)
                            Modifier.animateItem()
                        else Modifier)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    blacklist.name,
                                    modifier = Modifier.padding(3.dp),
                                    style = MaterialTheme.typography.headlineMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    blacklist.uuid,
                                    modifier = Modifier.padding(3.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    textDecoration = TextDecoration.Underline,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(horizontalArrangement = Arrangement.End) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.padding(5.dp).clickable {
                                    onDeleteSource(index)
                                })

                                Icon(
                                    Icons.Default.Info,
                                    null,
                                    modifier = Modifier.padding(5.dp, 5.dp, 20.dp, 5.dp).clickable { onTapInfoButton(index) },)
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(0.2f).fillMaxWidth()) {
                    IconButton(onClick = onTapAddButton, modifier = Modifier.align(Alignment.BottomEnd)) {
                        Icon(Icons.Default.AddCircle, null)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedBlacklistInfo(
    visible: Boolean,
    blacklist: Blacklist?,
    onDismissRequest: () -> Unit,
    onSourceChanged: (Blacklist) -> Unit
) {
    blacklist ?: return
    AnimatedVisibility(
        visible
    ){
        var name by remember { mutableStateOf(blacklist.name) }
        var url by remember { mutableStateOf(blacklist.uuid) }

        Column(modifier = Modifier.fillMaxSize()) {
            RWSingleOutlinedTextField(
                "Name",
                name,
                modifier = Modifier.fillMaxWidth().padding(10.dp)
            ) { name = it }

            RWSingleOutlinedTextField(
                "UUID",
                url,
                modifier = Modifier.fillMaxWidth().padding(10.dp)
            ) { url = it }

            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
            ) {
                TextButton(
                    onClick =
                    {
                        val cpy = blacklist.copy(name = name, uuid = url)
                        if(blacklist != cpy) onSourceChanged(cpy)
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd),
                ) { Text("Apply", style = MaterialTheme.typography.bodyLarge) }
            }
        }
    }
}

@Composable
private fun WelcomeMessageAdmittingDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
) {
    val settings = koinInject<Settings>()

    AnimatedAlertDialog(
        visible, onDismissRequest = onDismissRequest, enableDismiss = false
    ) { dismiss ->
        BorderCard(
            modifier = Modifier
             //   .fillMaxSize(GeneralProportion())
                .width(IntrinsicSize.Max)
                .padding(5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, null, modifier = Modifier.size(32.dp).padding(5.dp))
                Text(
                    "Admitting",
                    modifier = Modifier.padding(5.dp),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LargeDividingLine { 0.dp }
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    readI18n("multiplayer.admitting").trimIndent(),
                    modifier = Modifier.padding(5.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    RWTextButton("Yes", modifier = Modifier.padding(5.dp), leadingIcon = {
                        Icon(Icons.Default.Done, null, modifier = Modifier.size(30.dp))
                    }) {
                        settings.showWelcomeMessage = true
                        dismiss()
                    }

                    RWTextButton("No", modifier = Modifier.padding(5.dp), leadingIcon = {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(30.dp))
                    }) {
                        settings.showWelcomeMessage = false
                        dismiss()
                    }
                }
            }
        }
    }
}

@Composable
private fun JoinServerRequestDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    roomDescription: RoomDescription?,
    blacklists: SnapshotStateList<Blacklist>,
    onJoin: (dismiss: () -> Unit) -> Unit,
) {
    roomDescription ?: return
    AnimatedAlertDialog(
        visible, onDismissRequest = onDismissRequest
    ) { dismiss ->
        BorderCard(
            modifier = Modifier
              //  .fillMaxSize(GeneralProportion())
                .size(500.dp)
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
        ) {

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                ),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(32.dp).padding(5.dp))
                        Text("Join Server?", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            LargeDividingLine { 0.dp }

            Text("creator: ${roomDescription.creator}", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text("map: ${roomDescription.mapName}", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text("players: ${roomDescription.playerCurrentCount ?: ""}/${roomDescription.playerMaxCount ?: ""}" , modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text("version: ${roomDescription.version}", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text("mods: ${roomDescription.mods}", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            blacklists.add(
                                Blacklist("${roomDescription.creator}: ${roomDescription.mapName}", roomDescription.uuid)
                            )

                            dismiss()
                        }
                        .weight(1f)
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = readI18n("multiplayer.addToBlackList"), style = MaterialTheme.typography.bodyLarge)
                }

                VerticalDivider(modifier = Modifier.padding(2.dp).fillMaxHeight(), thickness = 2.dp)

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onJoin(dismiss)
                        }
                        .weight(1f)
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = readI18n("multiplayer.join"), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}