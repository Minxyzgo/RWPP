/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.game.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalController
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.events.*
import io.github.rwpp.event.onDispose
import io.github.rwpp.game.ConnectingPlayer
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.FogMode
import io.github.rwpp.game.map.MapType
import io.github.rwpp.game.units.GameUnit
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.net.packets.ModPacket
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.shared.generated.resources.Res
import io.github.rwpp.shared.generated.resources.error_missingmap
import io.github.rwpp.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt


private val relayRegex = Regex("""R\d+""")

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MultiplayerRoomView(isSandboxGame: Boolean = false, onExit: () -> Unit) {
    BackHandler(true, onExit)

    val context = LocalController.current
    val room = context.gameRoom
    var update by remember { mutableStateOf(false) }
    var lastSelectedIndex by remember { mutableStateOf(0) }
    var selectedMap by remember(update) { mutableStateOf(room.selectedMap) }

    var optionVisible by remember { mutableStateOf(false) }
    var banUnitVisible by remember { mutableStateOf(false) }
    var downloadModViewVisible by remember { mutableStateOf(false) }
    var loadModViewVisible by remember { mutableStateOf(false) }
    var selectedBanUnits by remember { mutableStateOf(listOf<GameUnit>()) }

    var showMapSelectView by remember { mutableStateOf(false) }
    val players = remember(update) { room.getPlayers().sortedBy { it.team } }
    val isHost = remember(update) { room.isHost || room.isHostServer }
    val updateAction = { update = !update }

    val chatMessages = remember { SnapshotStateList<AnnotatedString>() }

    var playerOverrideVisible by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf(players.firstOrNull() ?: ConnectingPlayer) }

    val scope = rememberCoroutineScope()

    GlobalEventChannel.filter(CallReloadModEvent::class).onDispose {
        subscribeAlways {
            loadModViewVisible = true
            downloadModViewVisible = false
        }
    }

    GlobalEventChannel.filter(CallStartDownloadModEvent::class).onDispose {
        subscribeAlways { downloadModViewVisible = true }
    }

    GlobalEventChannel.filter(RefreshUIEvent::class).onDispose {
        subscribeAlways { updateAction() }
    }

    GlobalEventChannel.filter(ReturnMainMenuEvent::class).onDispose {
        subscribeAlways { onExit() }
    }

    GlobalEventChannel.filter(ChatMessageEvent::class).onDispose {
        subscribeAlways {
            scope.launch {
                chatMessages.add(
                    buildAnnotatedString {
                        if(it.sender == "RELAY_CN-ADMIN") {
                            val result = relayRegex.find(it.message)?.value

                            if(!result.isNullOrBlank()) {
                                context.setConfig("lastNetworkIP", result)
                            }
                        }

                        if(it.sender.isNotBlank()) {
                            withStyle(style = SpanStyle(color = Player.getTeamColor(it.spawn))) {
                                append(it.sender + ": ")
                            }
                        }

                        withStyle(style = SpanStyle(color = Color.White)) {
                            append(it.message)
                        }
                    }
                )
            }
        }
    }

    LoadingView(loadModViewVisible, { loadModViewVisible = false }) {
        message("reloading mods...")
        context.modReload()
        context.sendPacketToServer(ModPacket.ModReloadFinishPacket())
        true
    }

    LoadingView(downloadModViewVisible, { downloadModViewVisible = false }) {
        message("downloading mods...")
        delay(Long.MAX_VALUE)
        false
    }

    MapViewDialog(
        showMapSelectView,
        { showMapSelectView = false },
        lastSelectedIndex,
        if (room.isHostServer) MapType.SkirmishMap else selectedMap.mapType
    ) { index, map ->
        selectedMap = map
        room.selectedMap = map
        lastSelectedIndex = index
    }

    MultiplayerSwitchOption(
        optionVisible,
        { optionVisible = false },
        updateAction,
        room,
        { banUnitVisible = true; optionVisible = false },
        players
    )

    BanUnitViewDialog(banUnitVisible, { banUnitVisible = false }, selectedBanUnits) {
        selectedBanUnits = it
        context.onBanUnits(it)
    }

    PlayerOverrideDialog(playerOverrideVisible, { playerOverrideVisible = false }, updateAction, room, selectedPlayer)

    @Composable
    fun ContentView() {
        @Composable
        fun MessageTextField() {
            var chatMessage by remember { mutableStateOf("") }
            RWSingleOutlinedTextField(
                label = readI18n("multiplayer.room.sendMessage"),
                value = chatMessage,
                modifier = Modifier.fillMaxWidth().padding(10.dp)
                    .onKeyEvent {
                        if(it.key == androidx.compose.ui.input.key.Key.Enter && chatMessage.isNotEmpty()) {
                            room.sendChatMessage(chatMessage)
                            chatMessage = ""
                            true
                        } else false
                    },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowForward,
                        null,
                        modifier = Modifier.clickable {
                            room.sendChatMessage(chatMessage)
                            chatMessage = ""
                        },
                        tint = Color.White
                    )
                },
                onValueChange =
                {
                    chatMessage = it
                },
            )
        }

        @Composable
        fun MessageView(modifier: Modifier = Modifier) {
            SelectionContainer(modifier) {
                if(LocalWindowManager.current == WindowManager.Large) {
                    val listState = rememberLazyListState()
                    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                        items(chatMessages.size) {
                            Text(
                                chatMessages[chatMessages.size - 1 - it],
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(5.dp, 1.dp, 0.dp, 0.dp)
                            )
                        }
                    }
                } else {
                    Column {
                        for(i in chatMessages.indices) {
                            if(i >= 100) break
                            Text(
                                chatMessages[chatMessages.size - 1 - i],
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(5.dp, 1.dp, 0.dp, 0.dp)
                            )
                        }
                    }
                }
            }
        }

        BorderCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            Column {
                ExitButton(onExit)
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        BorderCard(
                            modifier = Modifier
                                .weight(.4f)
                                .padding(10.dp)
                                .then(if(LocalWindowManager.current == WindowManager.Small)
                                        Modifier.verticalScroll(rememberScrollState()) else Modifier),
                            backgroundColor = Color.DarkGray.copy(.7f)
                        ) {
                            var details by remember { mutableStateOf("Getting details...") }

                            @Composable
                            fun MapImage(modifier: Modifier = Modifier) {
                                Image(
                                    selectedMap.image ?: painterResource(Res.drawable.error_missingmap),
                                    null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.then(modifier).padding(10.dp)
                                        .border(BorderStroke(2.dp, Color.DarkGray))
                                        .clickable(isHost) { showMapSelectView = true }
                                )
                            }

                            remember(update) {
                                scope.launch { details = room.roomDetails().split("\n").filter { !it.startsWith("Map:") && it.isNotBlank() }.joinToString("\n") }
                            }

                            Row(modifier = Modifier.fillMaxWidth().then(
                                if(LocalWindowManager.current == WindowManager.Small)
                                    Modifier else Modifier.weight(1f)
                            ), horizontalArrangement = Arrangement.Center) {
                                Text(
                                    details,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(10.dp)
                                )
                                if(LocalWindowManager.current == WindowManager.Large) MapImage(Modifier.weight(.8f))
                            }

                            if(LocalWindowManager.current == WindowManager.Middle) MapImage(Modifier.weight(1f).fillMaxWidth())
                            if(LocalWindowManager.current == WindowManager.Small) MapImage(Modifier.defaultMinSize(minHeight = 200.dp).fillMaxWidth())
                            val mapType = remember(update) { room.mapType }

                            Text(
                                mapType.name,
                                modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                                style = MaterialTheme.typography.headlineLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color(151, 188, 98)
                            )

                            Text(
                                selectedMap.displayName(),
                                modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                                style = MaterialTheme.typography.headlineLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White
                            )

                            @Composable
                            fun OptionButtons() {
                                if(LocalWindowManager.current == WindowManager.Large) RWTextButton(
                                    readI18n("multiplayer.room.selectMap"),
                                    modifier = Modifier.padding(5.dp)
                                ) { showMapSelectView = true }
                                if(LocalWindowManager.current != WindowManager.Large && !isSandboxGame) {
                                    var isLocked by remember { mutableStateOf(false) }
                                    IconButton(
                                        { isLocked = !isLocked; room.lockedRoom = isLocked },
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp),
                                        enabled = room.isHost,
                                    ) {
                                        Icon(Icons.Default.Lock, null, tint = if(isLocked) Color(237, 112, 20) else Color.White)
                                    }
                                }
                                RWTextButton(
                                    readI18n("multiplayer.room.option"),
                                    modifier = Modifier.padding(5.dp)
                                ) { optionVisible = true }
                                RWTextButton(
                                    readI18n("multiplayer.room.start"),
                                    modifier = Modifier.padding(5.dp)
                                ) {
                                    val unpreparedPlayers = context.gameRoom.getPlayers().filter { !it.data.ready }
                                    if(unpreparedPlayers.isNotEmpty()) {
                                        context.gameRoom.sendSystemMessage(
                                            "Cannot start game. Because players: ${unpreparedPlayers.joinToString(", ") { it.name }} aren't ready.")
                                    } else if(room.isHostServer) room.sendQuickGameCommand("-start")  else room.startGame()
                                }
                            }

                            if(isHost) {
                                if(LocalWindowManager.current == WindowManager.Small) {
                                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                        OptionButtons()
                                    }
                                } else {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                        OptionButtons()
                                    }
                                }
                            }
                        }

                        val playerNameWeight = .6f
                        val playerSpawnWeight = .1f
                        val playerTeamWeight = .1f
                        val playerPingWeight = .2f

                        val state = rememberLazyListState()

                        Column(
                            modifier = Modifier.weight(.7f).padding(10.dp).then(
                                if(LocalWindowManager.current != WindowManager.Large) Modifier.verticalScroll(rememberScrollState())
                                else Modifier
                            ),
                        ) {
                            BorderCard(
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 200.dp).padding(5.dp),
                                backgroundColor = Color.DarkGray.copy(.7f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .border(BorderStroke(2.dp, Color(101, 147, 74)), CircleShape)
                                        .fillMaxWidth()
                                ) {
                                    TableCell("name", playerNameWeight, drawStroke = false)
                                    TableCell("spawn", playerSpawnWeight)
                                    TableCell("team", playerTeamWeight)
                                    TableCell("ping", playerPingWeight, drawStroke = false)
                                }

                                @Composable
                                fun ColumnScope.PlayerTable(index: Int) {
                                    val options = remember {
                                        context.getStartingUnitOptions()
                                    }
                                    val player = players[index]
                                    var (delay, easing) = state.calculateDelayAndEasing(index, 1)
                                    if(LocalWindowManager.current != WindowManager.Large) delay = 0
                                    val animation = tween<Float>(durationMillis = 500, delayMillis = delay, easing = easing)
                                    val args = ScaleAndAlphaArgs(fromScale = 2f, toScale = 1f, fromAlpha = 0f, toAlpha = 1f)
                                    val (scale, alpha) = scaleAndAlpha(args = args, animation = animation)
                                    Row(
                                        modifier = Modifier
                                            .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
                                            .height(IntrinsicSize.Max)
                                            .padding(5.dp)
                                            .border(BorderStroke(2.dp, Color(160, 191, 124)), CircleShape)
                                            .fillMaxWidth()
                                            .clickable(room.isHost || room.isHostServer || room.localPlayer == player) {
                                                selectedPlayer = player
                                                playerOverrideVisible = true
                                            }
                                    ) {
                                        TableCell(player.name + if(player.startingUnit != -1) " - ${options.first { it.first == player.startingUnit }.second}" else "",
                                            color = if(player.color != -1) Player.getTeamColor(player.color) else Color.White,
                                            weight = playerNameWeight, drawStroke = false,
                                            modifier = Modifier.fillMaxHeight())
                                        TableCell(
                                            if(player.isSpectator)
                                                "S"
                                            else (player.spawnPoint + 1).toString(),
                                            playerSpawnWeight,
                                            color = if(player.isSpectator)
                                                Color.Black
                                            else Player.getTeamColor(player.spawnPoint),
                                            modifier = Modifier.fillMaxHeight()
                                        )
                                        TableCell(player.teamAlias(), playerTeamWeight, color = Player.getTeamColor(player.team), modifier = Modifier.fillMaxHeight())
                                        val ping = remember(update) { player.ping }
                                        TableCell(ping, playerPingWeight, drawStroke = false, modifier = Modifier.fillMaxHeight())
                                    }
                                }

                                if(LocalWindowManager.current != WindowManager.Large) {
                                    for(i in players.indices) {
                                        PlayerTable(i)
                                    }
                                } else {
                                    LazyColumnWithScrollbar(
                                        state = state,
                                        data = players,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(
                                            count = players.size,
                                            //key = { players[it].connectHexId }
                                        ) { index ->
                                            PlayerTable(index)
                                        }
                                    }
                                }
                            }

                            if(LocalWindowManager.current != WindowManager.Large && !isSandboxGame) {
                                BorderCard(
                                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 200.dp).padding(5.dp),
                                    backgroundColor = Color.DarkGray.copy(.7f)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        RWTextButton(
                                            readI18n("multiplayer.room.changeSite"),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 30.dp)
                                        ) {
                                            if(players.isNotEmpty()) {
                                                selectedPlayer = room.localPlayer
                                                playerOverrideVisible = true
                                            }
                                        }

                                        MessageTextField()
                                    }

                                    MessageView()
                                }
                            }
                        }
                    }
                }

                if(LocalWindowManager.current == WindowManager.Large) {
                    BorderCard(
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth()
                                .height(IntrinsicSize.Max)
                                .padding(5.dp)) {
                                RWTextButton(
                                    readI18n("multiplayer.room.changeSite"),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 30.dp)
                                ) {
                                    if(players.isNotEmpty()) {
                                        selectedPlayer = room.localPlayer
                                        playerOverrideVisible = true
                                    }
                                }
                                if(isHost) {
                                    RWTextButton(
                                        readI18n("multiplayer.room.addAI"),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 30.dp)
                                    ) { room.addAI() }
                                }

                                var isLocked by remember(update) { mutableStateOf(room.lockedRoom) }
                                if(!isSandboxGame) IconButton(
                                    { isLocked = !isLocked; room.lockedRoom = isLocked },
                                    enabled = isHost,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 30.dp)
                                ) {
                                    Icon(Icons.Default.Lock, null, tint = if(isLocked) Color(237, 112, 20) else Color.White)
                                }

                                if(!isSandboxGame) MessageTextField()
                            }

                            if(!isSandboxGame) BorderCard(
                                modifier = Modifier
                                    .padding(5.dp),
                                backgroundColor = Color.DarkGray.copy(.7f)
                            ) {
                                MessageView(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    if(LocalWindowManager.current != WindowManager.Large) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                if(isHost) {
                    FloatingActionButton(
                        onClick = { room.addAI() },
                        shape = CircleShape,
                        modifier = Modifier.padding(5.dp),
                        containerColor = Color(151, 188, 98),
                    ) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) {
            ContentView()
        }
    } else {
        ContentView()
    }
}

@Composable
private fun MultiplayerSwitchOption(
    label: String,
    value: Boolean,
    enabled: Boolean = true,
    onValueChange: (Boolean) -> Unit,
) {
    RWCheckbox(
        value,
        onCheckedChange = { onValueChange(!value) },
        modifier = Modifier.padding(5.dp),
        enabled
    )
    Text(label,
        modifier = Modifier.padding(top = 10.dp),
        style = MaterialTheme.typography.headlineLarge,
        color = if (enabled) Color(151, 188, 98) else Color.DarkGray
    )
}

@Composable
private fun PlayerOverrideDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    update: () -> Unit,
    room: GameRoom,
    player: Player
) {
    val context = LocalController.current
    val items = remember {
        buildList {
            add(-1 to "Default")
            addAll(context.getStartingUnitOptions())
        }
    }

    AnimatedAlertDialog(
        visible, onDismissRequest = { onDismissRequest(); update() }
    ) { m, dismiss ->

        var playerSpawnPoint by remember(player) { mutableStateOf<Int?>(player.spawnPoint + 1) }
        var playerTeam by remember(player) { mutableStateOf<Int?>(-1) }
        var playerColor by remember(player) { mutableStateOf(player.color) }
        var playerStartingUnits by remember(player) { mutableStateOf(items.indexOfFirst { it.first == player.startingUnit }) }
        var aiDifficulty by remember(player) { mutableStateOf(player.difficulty ?: room.aiDifficulty) }

        BorderCard(
            modifier = Modifier
                .fillMaxSize(LargeProportion())
                .then(m),
            backgroundColor = Color.Gray
        ) {
            ExitButton(dismiss)
            Text(
                readI18n("multiplayer.room.playerConfig"),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp),
                style = MaterialTheme.typography.displayLarge,
                color = Color(151, 188, 98)
            )
            LargeDividingLine { 5.dp }
            Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                var expanded0 by remember { mutableStateOf(false) }
                RWSingleOutlinedTextField(
                    readI18n("common.spawnPoint"),
                    if(playerSpawnPoint == -3) "Spectator" else playerSpawnPoint?.toString() ?: "",
                    lengthLimitCount = 3,
                    modifier = Modifier.padding(10.dp),
                    typeInNumberOnly = true,
                    typeInOnlyInteger = true,
                    trailingIcon = {
                        val icon =
                            if(expanded0) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                        Icon(
                            icon,
                            "",
                            modifier = Modifier.clickable(!expanded0) { expanded0 = !expanded0 })
                    },
                    appendedContent = {
                        BasicDropdownMenu(
                            expanded0,
                            buildList<Any> { addAll(1..10); add("Spectator") },
                            onItemSelected = { i, v ->
                                playerSpawnPoint = if(v != "Spectator") i + 1 else -3
                            }
                        ) {
                            expanded0 = false
                        }
                    }
                ) {
                    val n = it.toIntOrNull()
                    if(n == null || n < room.maxPlayerCount) playerSpawnPoint = n
                }

                Text(
                    "The spawn point controls where on the map this player starts. Most maps use old-even spawn points.",
                    modifier = Modifier.padding(5.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )

                var expanded by remember { mutableStateOf(false) }
                RWSingleOutlinedTextField(
                    readI18n("common.team"),
                    if(playerTeam == -1) "auto" else playerTeam?.toString() ?: "",
                    lengthLimitCount = 3,
                    modifier = Modifier.padding(10.dp),
                    typeInNumberOnly = true,
                    typeInOnlyInteger = true,
                    trailingIcon = {
                        val icon =
                            if(expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                        Icon(
                            icon,
                            "",
                            modifier = Modifier.clickable(!expanded) { expanded = !expanded })
                    },
                    appendedContent = {
                        BasicDropdownMenu(
                            expanded,
                            buildList<Any> { add("auto"); addAll(1..10) },
                            onItemSelected = { _, v ->
                                playerTeam = if(v == "auto") -1 else (v as Int)
                            }
                        ) {
                            expanded = false
                        }
                    }
                ) {
                    playerTeam = it.toIntOrNull()?.coerceAtMost(100)
                }



                Text(
                    "Players with the same team will be allied together.",
                    modifier = Modifier.padding(5.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )

                if(room.isHost) {
                    LargeDropdownMenu(
                        modifier = Modifier.padding(20.dp),
                        label = "Color",
                        items = buildList {
                            add("Default")
                            add("Green")
                            add("Red")
                            add("Blue")
                            add("Yellow")
                            add("Cyan")
                            add("White")
                            add("Black")
                            add("Pink")
                            add("Orange")
                            add("Purple")
                        },
                        selectedIndex = playerColor + 1,
                        onItemSelected = { i, _ -> playerColor = i - 1 }
                    )

                    LargeDropdownMenu(
                        modifier = Modifier.padding(20.dp),
                        label = "Starting Units",
                        items = items,
                        selectedIndex = playerStartingUnits,
                        onItemSelected = { i, _ -> playerStartingUnits = i },
                        selectedItemToString = { (_, s) -> s }
                    )
                }


                if(player.isAI) {
                    LargeDropdownMenu(
                        modifier = Modifier.padding(20.dp),
                        label = "Difficulty",
                        items = Difficulty.entries,
                        selectedIndex = aiDifficulty.ordinal,
                        onItemSelected = { _, v -> aiDifficulty = v }
                    )
                }
            }


            LargeDividingLine { 5.dp }
            Row(modifier = Modifier.fillMaxWidth().padding(end = 10.dp),
                 horizontalArrangement = Arrangement.Center) {

                if(player != room.localPlayer && room.isHost)
                    RWTextButton(readI18n("multiplayer.room.kick"), Modifier.padding(5.dp)) {
                        room.kickPlayer(player)
                        dismiss()
                    }

                RWTextButton(readI18n("multiplayer.room.apply"), Modifier.padding(5.dp)) {
                    player.applyConfigChange(
                        if(playerSpawnPoint == -3) -3 else ((playerSpawnPoint ?: 1) - 1).coerceAtLeast(0),
                        if(playerTeam == -1) 0 else if((playerTeam ?: 0) > 10) (playerTeam ?: 0) + 1 else (playerTeam ?: 1),
                        if(playerColor > -1) playerColor else null,
                        if(playerStartingUnits > 0) items[playerStartingUnits].first else null,
                        aiDifficulty,
                        playerTeam == -1
                    )

                    dismiss()
                }
            }
        }
    }
}

@Composable
private fun MultiplayerSwitchOption(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    update: () -> Unit,
    room: GameRoom,
    onShowBanUnitDialog: () -> Unit,
    players: List<Player>
) = AnimatedAlertDialog(
    visible, onDismissRequest = { onDismissRequest(); update() }
) { m, dismiss ->
    val context = LocalController.current

    var noNukes by remember { mutableStateOf(room.noNukes) }
    var sharedControl by remember { mutableStateOf(room.sharedControl) }
    var allowSpectators by remember { mutableStateOf(room.allowSpectators) }
    var teamLock by remember { mutableStateOf(room.teamLock) }
    var aiDifficulty by remember { mutableStateOf(room.aiDifficulty) }
    var fogMode by remember { mutableStateOf(room.fogMode) }
    var startingUnits by remember { mutableStateOf(room.startingUnits) }
    var teamMode by remember { mutableStateOf<String?>(null) }
    var startingCredits by remember { mutableStateOf(room.startingCredits) }
    var maxPlayerCount by remember { mutableStateOf(room.maxPlayerCount) }
    var realIncomeMultiplier by remember { mutableStateOf(room.incomeMultiplier) }

    BorderCard(
        modifier = Modifier
            .fillMaxSize(LargeProportion())
            .then(m),
        backgroundColor = Color.Gray
    ) {
        ExitButton(dismiss)
        LazyColumn(modifier = Modifier.weight(1f).padding(10.dp)) {
            item {
                LazyRow(horizontalArrangement = Arrangement.Center) {
                    item { MultiplayerSwitchOption(readI18n("multiplayer.room.noNukes"), noNukes) { noNukes = it } }
                    item {
                        MultiplayerSwitchOption(
                            readI18n("multiplayer.room.sharedControl"),
                            sharedControl
                        ) { sharedControl = it }
                    }
                    item {
                        MultiplayerSwitchOption(
                            readI18n("multiplayer.room.allowSpectators"),
                            allowSpectators, room.isHost
                        ) { allowSpectators = it }
                    }
                    item {
                        MultiplayerSwitchOption(readI18n("multiplayer.room.teamLock"), teamLock, room.isHost) { teamLock = it }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    var selectedIndex1 by remember(room) { mutableStateOf(room.aiDifficulty.ordinal) }
                    LargeDropdownMenu(
                        modifier = Modifier.weight(.3f).padding(5.dp),
                        label = readI18n("common.difficulty"),
                        items = Difficulty.entries,
                        selectedIndex = selectedIndex1,
                        onItemSelected = { index, _ -> selectedIndex1 = index }
                    )

                    remember(selectedIndex1) {
                        aiDifficulty = Difficulty.entries[selectedIndex1]
                    }

                    var selectedIndex2 by remember(room) { mutableStateOf(room.fogMode.ordinal) }
                    LargeDropdownMenu(
                        modifier = Modifier.weight(.3f).padding(5.dp),
                        label = readI18n("common.fog"),
                        items = FogMode.entries,
                        selectedIndex = selectedIndex2,
                        onItemSelected = { index, _ -> selectedIndex2 = index }
                    )

                    remember(selectedIndex2) {
                        fogMode = FogMode.entries[selectedIndex2]
                    }

                    val startingOptionList = remember {
                        context.getStartingUnitOptions()
                    }
                    var selectedIndex3 by remember(room) { mutableStateOf(startingOptionList.indexOfFirst { it.first == room.startingUnits }) }

                    LargeDropdownMenu(
                        modifier = Modifier.weight(.3f).padding(5.dp),
                        label = readI18n("multiplayer.room.startingUnit"),
                        items = startingOptionList,
                        selectedIndex = selectedIndex3,
                        onItemSelected = { index, _ -> selectedIndex3 = index },
                        selectedItemToString = { (_, s) -> s }
                    )

                    remember(selectedIndex3) {
                        startingUnits = startingOptionList[selectedIndex3].first
                    }
                }

            }

            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    val teamList = remember {
                        listOf(
                            "2 Teams (eg 5v5)",
                            "3 Teams (eg 1v1v1)",
                            "No teams (FFA)",
                            "All spectators"
                        )
                    }
                    LargeDropdownMenu(
                        modifier = Modifier.weight(.5f).padding(5.dp),
                        label = "Set Team",
                        enabled = room.isHost,
                        items = teamList,
                        selectedIndex = 0,
                        hasValue = false,
                        onItemSelected = { index, _ ->
                            when(index) {
                                0 -> teamMode = "2t"
                                1 -> teamMode = "3t"
                                2 -> teamMode = "FFA"
                                3 -> teamMode = "spectators"
                                else -> throw RuntimeException()
                            }

                            update()
                        }
                    )

                    var selectedIndex1 by remember(room) { mutableStateOf(room.startingCredits) }
                    val startingCreditList = remember {
                        listOf(
                            "Default (4000$)",
                            "0$",
                            "1000$",
                            "2000$",
                            "5000$",
                            "10000$",
                            "50000$",
                            "100000$",
                            "200000$"
                        )
                    }
                    LargeDropdownMenu(
                        modifier = Modifier.weight(.5f).padding(5.dp),
                        label = readI18n("multiplayer.room.startingCredits"),
                        items = startingCreditList,
                        selectedIndex = selectedIndex1,
                        onItemSelected = { index, _ -> selectedIndex1 = index }
                    )

                    remember(selectedIndex1) {
                        startingCredits = selectedIndex1
                    }
                }
            }

            item {
                var range by remember { mutableStateOf(room.maxPlayerCount) }

                Column(modifier = Modifier.wrapContentSize()) {
                    Text(
                        "${readI18n("multiplayer.room.maxPlayer")} : $range",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .padding(0.dp, 5.dp, 0.dp, 5.dp)
                    )
                    Slider(
                        valueRange = players.size.toFloat()..100f,
                        modifier = Modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, 5.dp),
                        value = range.toFloat(),
                        enabled = room.isHost,
                        colors = RWSliderColors,
                        onValueChange = { range = it.roundToInt().coerceAtLeast(10) },
                        onValueChangeFinished = {
                            if(range >= players.size) maxPlayerCount = range else range =
                                room.maxPlayerCount
                        }
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    var incomeMultiplier by remember { mutableStateOf(room.incomeMultiplier.toString()) }
                    var expanded by remember { mutableStateOf(false) }
                    RWSingleOutlinedTextField(
                        readI18n("multiplayer.room.incomeMultiplier"),
                        incomeMultiplier,
                        lengthLimitCount = 5,
                        typeInNumberOnly = true,
                        modifier = Modifier.weight(.5f).padding(5.dp),
                        trailingIcon = {
                            val icon =
                                if(expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                            Icon(
                                icon,
                                "",
                                modifier = Modifier.clickable(!expanded) { expanded = !expanded })
                        },
                        appendedContent = {
                            BasicDropdownMenu(
                                expanded,
                                listOf(1f, 1.5f, 2f, 2.5f, 3f, 10f),
                                onItemSelected = { _, v -> incomeMultiplier = v.toString() }
                            ) {
                                expanded = false
                            }
                        }
                    ) {
                        incomeMultiplier = it
                    }

                    remember(incomeMultiplier) {
                        realIncomeMultiplier = incomeMultiplier.toFloatOrNull() ?: 1f
                    }

                    var teamUnitCapHostedGame by remember { mutableStateOf(context.getConfig<Int?>("teamUnitCapHostedGame")) }
                    var expanded1 by remember { mutableStateOf(false) }
                    remember(teamUnitCapHostedGame) {
                        val count = teamUnitCapHostedGame ?: 100
                        context.setConfig("teamUnitCapHostedGame", count)
                        context.setTeamUnitCapHostGame(count)
                    }
                    RWSingleOutlinedTextField(
                        readI18n("multiplayer.room.teamUnitCapHostedGame"),
                        teamUnitCapHostedGame?.toString() ?: "",
                        lengthLimitCount = 6,
                        typeInNumberOnly = true,
                        enabled = room.isHost,
                        modifier = Modifier.weight(.5f).padding(5.dp),
                        typeInOnlyInteger = true,
                        trailingIcon = {
                            val icon =
                                if(expanded1) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                            Icon(
                                icon,
                                "",
                                modifier = Modifier.clickable(!expanded1 && room.isHost) { expanded1 = !expanded1 })
                        },
                        appendedContent = {
                            BasicDropdownMenu(
                                expanded1,
                                listOf(100, 250, 500, 1000, 2000, 5000, 10000),
                                onItemSelected = { _, v -> teamUnitCapHostedGame = v }
                            ) {
                                expanded1 = false
                            }
                        }
                    ) {
                        teamUnitCapHostedGame = it.toIntOrNull()
                    }
                }
            }

            if(room.isHost) {
                item {
                    RWTextButton(
                        readI18n("multiplayer.room.banUnits"),
                        modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                    ) {
                        onShowBanUnitDialog()
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(5.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            RWTextButton("Apply") {
                room.applyRoomConfig(
                    maxPlayerCount,
                    sharedControl,
                    startingCredits,
                    startingUnits,
                    fogMode,
                    aiDifficulty,
                    realIncomeMultiplier,
                    noNukes,
                    allowSpectators,
                    teamLock,
                    teamMode
                )

                dismiss()
            }
        }
    }
}