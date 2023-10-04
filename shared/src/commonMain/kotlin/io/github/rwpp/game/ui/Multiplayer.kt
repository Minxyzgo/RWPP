/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalController
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.config.Blacklist
import io.github.rwpp.config.Blacklists
import io.github.rwpp.config.MultiplayerPreferences
import io.github.rwpp.config.instance
import io.github.rwpp.net.RoomDescription
import io.github.rwpp.net.sorted
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MultiplayerView(
    onExit: () -> Unit,
    onOpenRoomView: () -> Unit
) {
    BackHandler(true, onExit)

    val refresh = remember { Channel<Unit>(1) }
    var isRefreshing by remember { mutableStateOf(false) }
    var currentViewList by remember { mutableStateOf<List<RoomDescription>>(listOf()) }
    var throwable by remember { mutableStateOf<Throwable?>(null) }
    val context = LocalController.current

    var userName by remember {
        val lastName = context.getConfig<String?>("lastNetworkPlayerName")
        mutableStateOf((lastName ?: "RWPP${(0..999).random()}").also { context.setUserName(it) })
    }

    val blacklists = remember { mutableStateListOf<Blacklist>().apply { addAll(Blacklists.blacklists) } }

    val instance = MultiplayerPreferences.instance
    var enableModFilter by remember { mutableStateOf(false) }
    var mapNameFilter by remember { mutableStateOf(instance.mapNameFilter) }
    var creatorNameFilter by remember { mutableStateOf(instance.creatorNameFilter) }
    var playerLimitRange by remember { mutableStateOf(instance.playerLimitRangeFrom..instance.playerLimitRangeTo) }
    var joinServerAddress by rememberSaveable { mutableStateOf(instance.joinServerAddress) }

    var serverAddress by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }

    var selectedRoomDescription by remember { mutableStateOf<RoomDescription?>(null) }
    var showJoinRequestDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    remember(blacklists.size) {
        Blacklists.blacklists = blacklists.toMutableList()
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
            context.setUserName(userName)
        }
    }

    JoinServerRequestDialog(showJoinRequestDialog, { showJoinRequestDialog = false },
       selectedRoomDescription, blacklists
    ) { dismiss ->
        serverAddress = selectedRoomDescription!!.addressProvider()
        isConnecting = true
        dismiss()
    }

    LoadingView(isConnecting, onLoaded = { context.cancelJoinServer(); isConnecting = false }, cancellable = true) {
        if(serverAddress.isBlank()) {
            message("That server no longer exists")
            return@LoadingView false
        }

        context.setUserName(userName)
        context.setConfig("lastNetworkIP", serverAddress)

        scope.launch(Dispatchers.IO) { context.saveConfig() }

        val result = context.directJoinServer(serverAddress, selectedRoomDescription?.uuid2, this)
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
    ) { m, dismiss ->
        BorderCard(
            modifier = Modifier
                .fillMaxSize(if(LocalWindowManager.current == WindowManager.Large) 0.6f else 0.85f)
                .padding(10.dp)
                .then(m)
                .verticalScroll(rememberScrollState()),
            backgroundColor = Color.Gray
        ) {
            Text(
                "Host Game",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp),
                style = MaterialTheme.typography.displayLarge,
                color = Color(151, 188, 98)
            )

            LargeDividingLine { 10.dp }
            var enableMods by remember { mutableStateOf(false) }
            var hostByRCN by remember { mutableStateOf(false) }
            Row {
                RWCheckbox(enableMods, onCheckedChange = { enableMods = !enableMods }, modifier = Modifier.padding(5.dp))
                Text("Enable Mods", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(5.dp))
            }

            Row {
                RWCheckbox(hostByRCN, onCheckedChange = { hostByRCN = !hostByRCN }, modifier = Modifier.padding(5.dp))
                Text("Host By RCN", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(5.dp))
            }

            var password by remember { mutableStateOf("") }
            RWSingleOutlinedTextField("password", password, modifier = Modifier.fillMaxWidth().padding(5.dp)) { password = it }

            Spacer(modifier = Modifier.weight(1f))

            val rcnAddress = "43.248.96.172:5123"
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                RWTextButton("Host Private", modifier = Modifier.padding(5.dp)) {
                    dismiss()
                    if(hostByRCN) {
                        context.onQuestionCallback(if(enableMods) "mod" else "new")
                        serverAddress = rcnAddress
                        isConnecting = true
                    } else {
                        context.hostStartWithPasswordAndMods(
                            false, password.ifBlank { null }, enableMods
                        )
                        onHost()
                    }
                }
                RWTextButton("Host Public", modifier = Modifier.padding(5.dp)) {
                    dismiss()
                    if(hostByRCN) {
                        context.onQuestionCallback(if(enableMods) "modup" else "newup")
                        serverAddress = rcnAddress
                        isConnecting = true
                    } else {
                        context.hostStartWithPasswordAndMods(
                            true, password.ifBlank { null }, enableMods
                        )
                        onHost()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ColumnScope.RoomListAnimatedLazyColumn(descriptions: List<RoomDescription>) {
        BorderCard(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(10.dp),
            backgroundColor = Color.DarkGray.copy(.7f)
        ) {
            val statusWeight = .3f
            val creatorNameWeight = .3F
            val countWeight = .1f
            val mapWeight = .6f
            val versionWeight = .2f
            val openWeight = .1f
            val realDescriptions = remember(
                descriptions, enableModFilter, playerLimitRange, mapNameFilter, creatorNameFilter, blacklists.size
            ) {
                descriptions.filter { room ->
                    if(blacklists.any { it.uuid == room.uuid}) return@filter false
                    if(enableModFilter) {
                        if(!room.version.contains("mod", true) && room.mods.isBlank()) {
                            return@filter false
                        }
                    }

                    if(mapNameFilter.isNotBlank()) {
                        return@filter room.mapName.contains(mapNameFilter, true)
                    }

                    if(creatorNameFilter.isNotBlank()) {
                        return@filter room.mapName.contains(creatorNameFilter, true)
                    }

                    if(room.playerMaxCount != null) {
                        return@filter room.playerMaxCount in playerLimitRange || room.playerMaxCount > 100
                    }

                    true
                }.sorted
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .padding(5.dp)
                        .border(BorderStroke(2.dp, Color(199, 234, 70)), CircleShape)
                        .fillMaxWidth()
                ) {
                    TableCell("status", statusWeight, drawStroke = false)
                    TableCell("Creator Name", creatorNameWeight)
                    TableCell("count", countWeight)
                    TableCell("GameMap", mapWeight)
                    TableCell("version", versionWeight)
                    TableCell("open", openWeight, drawStroke = false)
                }

                val state = rememberLazyListState()

                LazyColumnWithScrollbar(
                    state = state,
                    data = realDescriptions,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(
                        count = realDescriptions.size,
                        key = { realDescriptions[it].uuid }
                    ) { index ->
                        val desc = realDescriptions[index]
                        val (delay, easing) = state.calculateDelayAndEasing(index, 1)
                        val animation = tween<Float>(durationMillis = 500, delayMillis = delay, easing = easing)
                        val args = ScaleAndAlphaArgs(fromScale = 2f, toScale = 1f, fromAlpha = 0f, toAlpha = 1f)
                        val (scale, alpha) = scaleAndAlpha(args = args, animation = animation)
                        Row(
                            modifier = Modifier
                                .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
                                .animateItemPlacement()
                                .padding(5.dp)
                                .border(BorderStroke(2.dp, Color(199, 234, 70)), CircleShape)
                                .fillMaxWidth()
                                .clickable {
                                    selectedRoomDescription = desc
                                    showJoinRequestDialog = true
                                }
                        ) {
                            val color: Color =
                                if(desc.isUpperCase) {
                                    Color.White
                                } else if(desc.gameVersion != LocalController.current.gameVersion) {
                                    Color.Gray
                                } else if(desc.isLocal) {
                                    Color(255, 127, 80)
                                } else if(desc.isOpen) {
                                    Color(200, 200, 200)
                                } else {
                                    Color.Gray
                                }
                            val open: String =
                                if(desc.requiredPassword) {
                                    "P"
                                } else if(desc.isLocal) {
                                    "L"
                                } else if(desc.isOpen) {
                                    "Y"
                                } else {
                                    "N"
                                }


                            TableCell(desc.roomType, statusWeight, color = color, drawStroke = false)
                            TableCell(desc.creator, creatorNameWeight, color = color)
                            TableCell(
                                (desc.playerCurrentCount ?: "").toString() + "/" + desc.playerMaxCount.toString(),
                                countWeight,
                                color = color
                            )
                            TableCell(desc.mapName.removeSuffix(".tmx"), mapWeight, color = color)
                            TableCell(desc.version, versionWeight, color = color)

                            TableCell(open, openWeight, drawStroke = false, color = color)
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
        ) { modifier, _ ->
            BorderCard(
                modifier = Modifier.fillMaxSize(0.5f).then(modifier),
                backgroundColor = Color.Gray
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
    fun ColumnScope.FilterSurface() {
        BorderCard(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            backgroundColor = Color.DarkGray.copy(.7f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Filter", style = MaterialTheme.typography.headlineLarge)
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
                            fontFamily = MaterialTheme.typography.headlineLarge.fontFamily
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineLarge,
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
                            fontFamily = MaterialTheme.typography.headlineLarge.fontFamily
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineLarge,
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
                        "Player Limit : $range",
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
                        "Enabled Mods", style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier
                            .padding(5.dp)
                            .weight(1f)
                    )
                    Switch(
                        checked = enableModFilter,
                        onCheckedChange = { enableModFilter = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(151, 188, 98)),
                    )
                }

                LargeOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reset") },
                    value = ""
                ) {
                    resetFilter()
                }
            }
        }
    }

    with(context) {
        LaunchedEffect(Unit) {
            refresh.send(Unit)
            for(u in refresh) {
                throwable = null
                isRefreshing = true
                try {
                    currentViewList = getRoomListFromSourceUrl(
                        "http://gs1.corrodinggames.com/masterserver/1.4/interface?action=list&game_version=176&game_version_beta=false;http://gs4.corrodinggames.net/masterserver/1.4/interface?action=list&game_version=176&game_version_beta=false".split(";")
                    )
                } catch(e: Throwable) {
                    throwable = e
                }

                isRefreshing = false
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
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
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        BorderCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Column {
                ExitButton(onExit)
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.weight(0.4f).verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            label = { Text("User Name", fontFamily = MaterialTheme.typography.headlineLarge.fontFamily) },
                            textStyle = MaterialTheme.typography.headlineLarge,
                            colors = RWOutlinedTextColors,
                            value = userName,
                            enabled = true,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            onValueChange =
                            {
                                userName = it
                            },
                        )

                        var hostDialogVisible by remember { mutableStateOf(false) }
                        RWTextButton(
                            "Host new game",
                            modifier = Modifier.padding(5.dp).fillMaxWidth()
                        ) { hostDialogVisible = true }
                        HostGameDialog(hostDialogVisible, { hostDialogVisible = false }) {
                            onExit(); onOpenRoomView(); context.setUserName(userName)
                        }

                        RWTextButton(
                            label = "Watch Reply",
                            modifier = Modifier.padding(5.dp, 0.dp, 5.dp, 5.dp).fillMaxWidth(),
                        ) {}

                        RWTextButton(
                            label = "Join the last game",
                            modifier = Modifier.padding(5.dp, 0.dp, 5.dp, 5.dp).fillMaxWidth(),
                        ) {
                            val lastIp = context.getConfig<String?>("lastNetworkIP")
                            if(lastIp != null) {
                                serverAddress = lastIp
                                isConnecting = true
                            }
                        }

                        FilterSurface()
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            RWSingleOutlinedTextField(
                                label = "Join Server",
                                value = joinServerAddress,
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowForward,
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
                                    context.setConfig("lastNetworkIP", it)
                                },
                            )
                        }

                        if(throwable != null) {
                            LazyColumn {
                                item {
                                    SelectionContainer {
                                        Text(throwable?.stackTraceToString() ?: "", color = Color.Red)
                                    }
                                }
                            }
                        } else {
                            RoomListAnimatedLazyColumn(currentViewList)
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Blacklists", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(15.dp))
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
                            style = MaterialTheme.typography.headlineLarge,
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
                            modifier = Modifier.padding(5.dp, 5.dp, 20.dp, 5.dp).clickable { onTapInfoButton(index) })
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
    ) { m, dismiss ->
        BorderCard(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .padding(10.dp)
                .then(m)
                .verticalScroll(rememberScrollState()),
            backgroundColor = Color.Gray
        ) {
            ExitButton(dismiss)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, modifier = Modifier.size(25.dp).padding(5.dp))
                Text("Join Server?", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.headlineLarge, color = Color(151, 188, 98))
            }
            LargeDividingLine { 5.dp }
            Text("creator: ${roomDescription.mapName}", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text("map: ${roomDescription.creator}", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text("version: ${roomDescription.version}", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                RWTextButton("Join", modifier = Modifier.padding(5.dp), onClick = { onJoin(dismiss) })
                RWTextButton("Add to Blacklist", modifier = Modifier.padding(5.dp)) {
                    blacklists.add(
                        Blacklist("${roomDescription.creator}: ${roomDescription.mapName}", roomDescription.uuid)
                    )

                    dismiss()
                }
            }
        }
    }
}