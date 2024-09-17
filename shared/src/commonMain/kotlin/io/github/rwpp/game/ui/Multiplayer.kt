/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:Suppress("DuplicatedCode")

package io.github.rwpp.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.config.*
import io.github.rwpp.game.Game
import io.github.rwpp.game.data.RoomOption
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.gameVersion
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.maxModSize
import io.github.rwpp.net.Net
import io.github.rwpp.net.RoomDescription
import io.github.rwpp.net.ServerStatus
import io.github.rwpp.net.sorted
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.platform.loadSvg
import io.github.rwpp.platform.readPainterByBytes
import io.github.rwpp.shared.generated.resources.Res
import io.github.rwpp.shared.generated.resources.error_missingmap
import io.github.rwpp.ui.*
import io.github.rwpp.ui.v2.ExpandedCard
import io.github.rwpp.ui.v2.LazyColumnScrollbar
import io.github.rwpp.ui.v2.RWIconButton
import io.github.rwpp.ui.v2.bounceClick
import io.github.rwpp.utils.io.SizeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MultiplayerView(
    onExit: () -> Unit,
    onOpenRoomView: () -> Unit,
) {
    BackHandler(true, onExit)

    val instance = koinInject<MultiplayerPreferences>()
    val settings = koinInject<Settings>()
    val configIO = koinInject<ConfigIO>()
    val game = koinInject<Game>()
    val modManager = koinInject<ModManager>()
    val net = koinInject<Net>()
    val blacklistsInstance = koinInject<Blacklists>()

    val refresh = remember { Channel<Unit>(1) }
    var isRefreshing by remember { mutableStateOf(false) }
    val allServerData = remember {
        SnapshotStateList<ServerData>().apply {
            addAll(
                instance.allServerConfig.map { ServerData(it) }
            )
        }
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
                .size(500.dp)
                //.fillMaxSize(LargeProportion())
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(if (LocalWindowManager.current == WindowManager.Small) 75.dp else 150.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(44, 95, 45), Color(151, 188, 98)))
                ),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Host Game", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    }
                }
            }

            LargeDividingLine { 10.dp }
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


            remember(enableMods) {
                if(!enableMods) transferMod = false
            }

            Row {
                RWCheckbox(enableMods, onCheckedChange = { enableMods = !enableMods }, modifier = Modifier.padding(5.dp))
                Text(readI18n("multiplayer.enableMods"), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 15.dp))
            }

            Row {
                RWCheckbox(hostByRCN, onCheckedChange = { hostByRCN = !hostByRCN }, modifier = Modifier.padding(5.dp))
                Text(readI18n("multiplayer.hostByRCN"), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 15.dp))
            }

            Row {
                RWCheckbox(transferMod,
                    onCheckedChange = { transferMod = !transferMod },
                    modifier = Modifier.padding(5.dp),
                    enabled = enableMods && modSize <= maxModSize
                )
                Text("${readI18n("multiplayer.transferMod")} ${if(modSize > maxModSize) "(Disabled for total mods size: ${SizeUtils.byteToMB(modSize)}MB > ${SizeUtils.byteToMB(maxModSize)}MB)" else ""}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 15.dp),
                    color = if(enableMods && modSize <= maxModSize) Color.White else Color.Gray
                )
            }

            var password by remember { mutableStateOf("") }
            RWSingleOutlinedTextField("password", password, modifier = Modifier.fillMaxWidth().padding(5.dp)) { password = it }

            Spacer(modifier = Modifier.weight(1f))


            val rcnAddress = "43.248.96.172:5123"
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                RWTextButton("Host Private", modifier = Modifier.padding(5.dp)) {
                    dismiss()
                    game.gameRoom.option = RoomOption(transferMod)
                    if(hostByRCN) {
                        game.onQuestionCallback(if(enableMods) "smod" else "snew")
                        serverAddress = rcnAddress
                        isConnecting = true
                    } else {
                        game.hostStartWithPasswordAndMods(
                            false, password.ifBlank { null }, enableMods,
                        )
                        onHost()
                    }
                }
                RWTextButton("Host Public", modifier = Modifier.padding(5.dp)) {
                    dismiss()
                    game.gameRoom.option = RoomOption(transferMod)
                    if(hostByRCN) {
                        game.onQuestionCallback(if(enableMods) "smodup" else "snewup")
                        serverAddress = rcnAddress
                        isConnecting = true
                    } else {
                        game.hostStartWithPasswordAndMods(
                            true, password.ifBlank { null }, enableMods,
                        )
                        onHost()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    fun LazyListScope.RoomListAnimated(
        descriptions: List<RoomDescription>
    ) {

        val statusWeight = .3f
        val creatorNameWeight = .3F
        val countWeight = .1f
        val mapWeight = .6f
        val versionWeight = .2f
        val openWeight = .1f


//                Row(
//                    modifier = Modifier
//                        .padding(5.dp)
//                        .border(BorderStroke(2.dp, Color(101, 147, 74)), CircleShape)
//                        .fillMaxWidth()
//                ) {
//                    TableCell("Status", statusWeight, drawStroke = false)
//                    TableCell("Creator Name", creatorNameWeight)
//                    TableCell("Count", countWeight)
//                    TableCell("GameMap", mapWeight)
//                    TableCell("Version", versionWeight)
//                    TableCell("Open", openWeight, drawStroke = false)
//                }

        items(
            count = descriptions.size,
            key = { descriptions[it].uuid }
        ) { index ->
            val desc = descriptions[index]
            Row(
                modifier = Modifier
                    .animateItemPlacement()
                    .height(IntrinsicSize.Max)
                    .padding(5.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, Color(160, 191, 124)), CircleShape)
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
                  //  .fillMaxSize(0.8f)
                    .size(500.dp)
                    .padding(10.dp)
            ) {
                ExitButton(dismiss)
                var type by remember { mutableStateOf(serverConfig?.type ?: ServerType.Server) }
                var url by remember { mutableStateOf(serverConfig?.ip ?: "") }
                var name by remember { mutableStateOf(serverConfig?.name ?: "") }

                Column(modifier = Modifier.fillMaxSize()) {
                    LargeDropdownMenu(
                        modifier = Modifier.padding(20.dp),
                        label = "Server Type",
                        items = ServerType.entries,
                        selectedIndex = type.ordinal,
                        onItemSelected = { _, t -> type = t }
                    )

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

                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                    ) {
                        TextButton(
                            onClick =
                            {
                                if(serverConfig != null) {
                                    serverConfig.ip = url
                                    serverConfig.name = name
                                    serverConfig.type = type
                                } else {
                                    val config = ServerConfig(url, name, type)
                                    instance.allServerConfig.add(config)
                                    allServerData.add(
                                        ServerData(
                                            config
                                        )
                                    )
                                }

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

    @OptIn(ExperimentalFoundationApi::class)
    fun LazyListScope.ServerList(
        serverDataList: List<ServerData>
    ) {
        items(
            count = serverDataList.size
        ) { index ->
            var selectedDefaultRoomList by remember { mutableStateOf(allServerData.firstOrNull { it.config.useAsDefaultList }) }

            val serverData = serverDataList[index]
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
                        refresh.trySend(Unit)
                    }
                },
                modifier = Modifier.animateItemPlacement()
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp),
                colors = CardDefaults.cardColors(containerColor = Color(27, 18, 18).copy(0.9f)),
                elevation =  CardDefaults.cardElevation(defaultElevation = 10.dp),
                border = BorderStroke(2.dp, Color.DarkGray),
                shape = RoundedCornerShape(20.dp)
            ) {
                //do not edit official room list
                if (index != 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.padding(5.dp).clickable {
                            instance.allServerConfig.remove(serverData.config)
                            allServerData.remove(serverData)
                        })

                        Icon(
                            Icons.Default.Info,
                            null,
                            modifier = Modifier.padding(5.dp, 5.dp, 20.dp, 5.dp).clickable {
                                selectedServerConfig = serverData.config
                                showServerInfoConfig = true
                            }
                        )
                    }
                }

                Row {
                    val iconPainter = remember {
                        runCatching {
                            serverData.infoPacket?.iconBytes?.let { readPainterByBytes(it) }
                        }.getOrNull()
                    }

                    var checked by remember(selectedDefaultRoomList) {
                        mutableStateOf(selectedDefaultRoomList == serverData)
                    }

                    if (serverData.config.type == ServerType.Server) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                iconPainter ?: painterResource(Res.drawable.error_missingmap),
                                null,
                                modifier = Modifier.size(120.dp).padding(5.dp)
                            )
                            if (serverData.isLoading) CircularProgressIndicator(color = Color(199, 234, 70))
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
                    val ping = remember(info) {
                        info?.run { ping.toString() } ?: ""
                    }
                    val mods = remember(info) {
                        info?.run { mods } ?: ""
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            buildAnnotatedString {
                                append(info?.name ?: serverData.config.name)
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
                                    appendLine("ping: ${ping}ms")
                                    if (mods.isNotBlank()) appendLine("enabled mods: $mods")
                                },
                                modifier = Modifier.padding(3.dp),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        } else if (serverData.config.type == ServerType.RoomList) {
                            Row {
                                RWCheckbox(
                                    checked, {
                                        checked = it
                                        selectedDefaultRoomList = if (it) serverData else null
                                        allServerData.forEach { d -> d.config.useAsDefaultList = false }
                                        if (checked) serverData.config.useAsDefaultList = true
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
                    .fillMaxSize(LargeProportion())
                    .padding(10.dp)
                    .autoClearFocus(),
            ) {
                ExitButton(dismiss)
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Filter", style = MaterialTheme.typography.headlineMedium)
                    }

                    LargeDividingLine { 5.dp }

                    var showBlacklistDialog by remember {
                        mutableStateOf(false)
                    }

                    LargeOutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        label = {  },
                        value = "Blacklist"
                    ) {
                        showBlacklistDialog = true
                    }

                    BlacklistTargetDialog(showBlacklistDialog) { showBlacklistDialog = false }

                    OutlinedTextField(
                        label = {
                            Text(
                                "GameMap Name Filter",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineMedium,
                        colors = RWOutlinedTextColors,
                        value = mapNameFilter,
                        enabled = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(5.dp),
                        onValueChange =
                        {
                            mapNameFilter = it
                        },
                    )

                    OutlinedTextField(
                        label = {
                            Text(
                                "Creator Name Filter",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineMedium,
                        colors = RWOutlinedTextColors,
                        value = creatorNameFilter,
                        enabled = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(5.dp, 0.dp, 5.dp, 5.dp),
                        onValueChange =
                        {
                            creatorNameFilter = it
                        },
                    )

                    var range by remember { mutableStateOf(playerLimitRange) }
                    Column(modifier = Modifier.wrapContentSize()) {
                        Text(
                            "${readI18n("multiplayer.playerLimit")} : $range",
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 5.dp, 0.dp, 5.dp)
                        )
                        RangeSlider(
                            valueRange = 0f..100f,
                            modifier = Modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, 5.dp),
                            steps = 101,
                            value = range.first.toFloat()..range.last.toFloat(),
                            colors = RWSliderColors,
                            onValueChange = { range = it.start.roundToInt()..it.endInclusive.roundToInt() },
                            onValueChangeFinished = { playerLimitRange = range }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(5.dp)
                    ) {
                        Text(
                            readI18n("multiplayer.enableMods"), style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .padding(5.dp)
                                .weight(1f)
                        )
                        Switch(
                            checked = enableModFilter,
                            onCheckedChange = { enableModFilter = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(151, 188, 98), checkedThumbColor = Color.White),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(5.dp)
                    ) {
                        Text(
                            readI18n("multiplayer.battleroom"), style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .padding(5.dp)
                                .weight(1f)
                        )
                        Switch(
                            checked = battleroom,
                            onCheckedChange = { battleroom = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(151, 188, 98), checkedThumbColor = Color.White),
                        )
                    }

                    LargeOutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(readI18n("multiplayer.reset")) },
                        value = ""
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
                        currentViewList = getRoomListFromSourceUrl(
                            roomList.ip.split(";")
                        )
                    } else {
                        for(s in allServerData) {
                            if (s.config.type == ServerType.Server) {
                                launch(Dispatchers.IO) {
                                    s.isLoading = true
                                    s.infoPacket = runCatching {
                                        s.config.getServerInfo()
                                    }.onFailure {
                                        it.printStackTrace()
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
        onExit(); onOpenRoomView(); game.setUserName(userName)
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
                        leadingIcon = { Icon(loadSvg("replay"), null, modifier = Modifier.size(30.dp)) },
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
                            containerColor = Color(151, 188, 98),
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    } else {
                        FloatingActionButton(
                            onClick = { selectedServerConfig = null; showServerInfoConfig = true },
                            shape = CircleShape,
                            modifier = Modifier.padding(5.dp),
                            containerColor = Color(151, 188, 98),
                        ) { Icon(Icons.Default.Add, null) }
                    }

                    FloatingActionButton(
                        onClick = { if(!isRefreshing) scope.launch { refresh.trySend(Unit) } },
                        shape = CircleShape,
                        modifier = Modifier.padding(5.dp),
                        containerColor = Color(151, 188, 98),
                    ) {
                        if(isRefreshing) {
                            CircularProgressIndicator(color = Color(199, 234, 70))
                        } else {
                            Icon(Icons.Default.Refresh, null)
                        }
                    }
                }
            }
        },
    ) {
        ExpandedCard {
            ExitButton(onExit)

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
                        return@filter room.playerMaxCount in playerLimitRange || room.playerMaxCount > 100
                    }

                    true
                }.sorted
            }

            val realServerData = remember(
                allServerData.size, enableModFilter, playerLimitRange, mapNameFilter, creatorNameFilter, battleroom
            ) {
                allServerData.filter { data ->
                    val info = data.infoPacket

                    if (battleroom && info?.status == ServerStatus.InGame) {
                        return@filter false
                    }

                    if (enableModFilter) {
                        if (info?.mods.isNullOrBlank()) {
                            return@filter false
                        }
                    }

                    if (mapNameFilter.isNotBlank()) {
                        return@filter info?.mapName?.contains(mapNameFilter, true) == true
                    }

                    if (creatorNameFilter.isNotBlank()) {
                        return@filter (info?.name ?: data.config.name).contains(creatorNameFilter, true)
                    }

                    if (info?.maxPlayerSize != null) {
                        return@filter info.maxPlayerSize in playerLimitRange || info.maxPlayerSize > 100
                    }

                    true
                }
            }

            AnimatedServerConfigInfo(
                showServerInfoConfig,
                selectedServerConfig,
            ) { showServerInfoConfig = false }

            CompositionLocalProvider(
                LocalContentColor provides Color.White
            ) {
                val state = rememberLazyListState()
                LazyColumnScrollbar(
                    listState = state,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = state
                    ) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
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

                                RWIconButton(loadSvg("tune"), modifier = Modifier.offset(y = 25.dp), size = 50.dp) { filterSurfaceDialogVisible = true }
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
                                ServerList(realServerData)
                            }
                        }
                    }
                }
            }
        }
    }
}




@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnimatedBlackList(
    visible: Boolean,
    blacklists: SnapshotStateList<Blacklist>,
    onDeleteSource: (Int) -> Unit,
    onTapInfoButton: (Int) -> Unit,
    onTapAddButton: () -> Unit,
) = AnimatedVisibility(visible) {
    CompositionLocalProvider(
        LocalContentColor provides Color.White
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(readI18n("multiplayer.blacklist"), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(15.dp))
            LargeDividingLine { 5.dp }

            LazyColumn(
                modifier = Modifier.selectableGroup().weight(1f),
            ) {
                items(count = blacklists.size) { index ->
                    val blacklist = blacklists[index]
                    Row(modifier = Modifier
                        .wrapContentSize()
                        .animateItemPlacement()
                    ) {
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
                    readI18n("multiplayer.admitting").trimIndent(),
                    modifier = Modifier.padding(5.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
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
            backgroundColor = Color(53, 57, 53)
        ) {

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(if (LocalWindowManager.current == WindowManager.Small) 75.dp else 150.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(44, 95, 45), Color(151, 188, 98)))
                ),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(32.dp).padding(5.dp))
                        Text("Join Server?", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    }
                }
            }

            LargeDividingLine { 5.dp }
            Text("creator: ${roomDescription.creator}", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text("map: ${roomDescription.mapName}", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text("players: ${roomDescription.playerCurrentCount ?: ""}/${roomDescription.playerMaxCount ?: ""}" , modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text("version: ${roomDescription.version}", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text("mods: ${roomDescription.mods}", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(color = Color(27, 18, 18))
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