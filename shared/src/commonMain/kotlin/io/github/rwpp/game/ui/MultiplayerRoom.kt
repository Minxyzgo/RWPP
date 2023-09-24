/*
 * Copyright 2023 RWPP contributors
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalController
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.events.ChatMessageEvent
import io.github.rwpp.event.events.RefreshUIEvent
import io.github.rwpp.event.events.ReturnMainMenuEvent
import io.github.rwpp.event.onDispose
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.FogMode
import io.github.rwpp.ui.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.reflect.KMutableProperty

object ConnectingPlayer : Player {
    override val connectHexId: String
        get() = ""
    override val spawnPoint: Int
        get() = 0
    override val name: String
        get() = "Connecting..."
    override val ping: String
        get() = ""
    override val team: Int
        get() = 0
    override val startingUnit: Int
        get() = 0
    override val color: Int
        get() = 0
    override val isSpectator: Boolean
        get() = false
    override val isAI: Boolean
        get() = false
    override val difficulty: Difficulty?
        get() = null

    override fun applyConfigChange(
        spawnPoint: Int,
        team: Int,
        color: Int?,
        startingUnits: Int?,
        aiDifficulty: Difficulty?,
        changeTeamFromSpawn: Boolean
    ) {}
}

@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
@Composable
fun MultiplayerRoomView(onExit: () -> Unit) {
    val context = LocalController.current
    val room = context.gameRoom
    var update by remember { mutableStateOf(false) }
    var lastSelectedIndex by remember { mutableStateOf(0) }
    var selectedMap by remember(update) { mutableStateOf(room.selectedMap) }

    var optionVisible by remember { mutableStateOf(false) }

    var showMapSelectView by remember { mutableStateOf(false) }
    val players = remember(update) { room.getPlayers().sortedBy { it.team } }
    val isHost = remember(update) { room.isHost }
    val updateAction = { update = !update }

    val chatMessages = remember { SnapshotStateList<AnnotatedString>() }

    var playerOverrideVisible by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf(players.firstOrNull() ?: ConnectingPlayer) }

    val scope = rememberCoroutineScope()

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
                        withStyle(style = SpanStyle(color = Player.getTeamColor(it.spawn))) {
                            append(it.sender)
                        }

                        withStyle(style = SpanStyle(color = Color.White)) {
                            append(": ${it.message}")
                        }
                    }
                )
            }
        }
    }

    MapViewDialog(showMapSelectView, { showMapSelectView = false }, lastSelectedIndex, selectedMap.mapType) { index, map ->
        selectedMap = map
        room.selectedMap = map
        lastSelectedIndex = index
    }

    MultiplayerOptionDialog(
        optionVisible,
        { optionVisible = false },
        updateAction,
        room,
        players
    )

    PlayerOverrideDialog(playerOverrideVisible, { playerOverrideVisible = false }, updateAction, room, selectedPlayer)

    BorderCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Column {
            ExitButton(onExit)
            Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    BorderCard(
                        modifier = Modifier
                            .weight(.4f)
                            .padding(10.dp),
                        backgroundColor = Color.DarkGray.copy(.7f)
                    ) {
                        var details by remember { mutableStateOf("Getting details...") }

                        remember(update) {
                            scope.launch { details = room.roomDetails().split("\n").filter { !it.startsWith("Map:") }.joinToString("\n") }
                        }

                        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.Center) {
                            Text(
                                details,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(10.dp)
                            )
                            Image(
                                selectedMap.image ?: painterResource("error_missingmap.png"),
                                null,
                                modifier = Modifier.weight(0.6f).padding(10.dp)
                                    .border(BorderStroke(2.dp, Color.DarkGray))
                                    .clickable(isHost) { showMapSelectView = true }
                            )
                        }

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
                            selectedMap.mapName,
                            modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.headlineLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )

                        if(isHost) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                RWTextButton(
                                    "Select Map",
                                    modifier = Modifier.padding(5.dp)
                                ) { showMapSelectView = true }
                                RWTextButton(
                                    "Option",
                                    modifier = Modifier.padding(5.dp)
                                ) { optionVisible = true }
                                RWTextButton(
                                    "Start",
                                    modifier = Modifier.padding(5.dp)
                                ) { room.startGame() }
                            }
                        }
                    }

                    val playerNameWeight = .6f
                    val playerSpawnWeight = .1f
                    val playerTeamWeight = .1f
                    val playerPingWeight = .2f

                    BorderCard(
                        modifier = Modifier.weight(.7f).padding(10.dp),
                        backgroundColor = Color.DarkGray.copy(.7f)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(5.dp)
                                .border(BorderStroke(2.dp, Color(199, 234, 70)), CircleShape)
                                .fillMaxWidth()
                        ) {
                            TableCell("name", playerNameWeight, drawStroke = false)
                            TableCell("spawn", playerSpawnWeight)
                            TableCell("team", playerTeamWeight)
                            TableCell("ping", playerPingWeight, drawStroke = false)
                        }

                        val state = rememberLazyListState()

                        LazyColumnWithScrollbar(
                            state = state,
                            data = players,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                count = players.size,
                                //key = { players[it].connectHexId }
                            ) { index ->
                                val player = players[index]
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
                                        .clickable(room.isHost || room.localPlayer == player) {
                                            selectedPlayer = player
                                            playerOverrideVisible = true
                                        }
                                ) {
                                    TableCell(player.name, playerNameWeight, drawStroke = false)
                                    TableCell(
                                        if(player.isSpectator)
                                            "S"
                                        else (player.spawnPoint + 1).toString(),
                                        playerSpawnWeight,
                                        color = if(player.isSpectator)
                                            Color.Black
                                        else Player.getTeamColor(player.spawnPoint)
                                    )
                                    TableCell(player.teamAlias(), playerTeamWeight, color = Player.getTeamColor(player.team))
                                    val ping = remember(update) { player.ping }
                                    TableCell(ping, playerPingWeight, drawStroke = false)
                                }
                            }
                        }
                    }
                }
            }

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
                            "Change Site",
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 30.dp)
                        ) {
                            if(players.isNotEmpty()) {
                                selectedPlayer = room.localPlayer
                                playerOverrideVisible = true
                            }
                        }
                        if(isHost) {
                            RWTextButton(
                                "Add AI",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 30.dp)
                            ) { room.addAI() }
                        }

                        var isLocked by remember(update) { mutableStateOf(room.lockedRoom) }
                        IconButton(
                            { isLocked = !isLocked; room.lockedRoom = isLocked },
                            enabled = isHost,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 30.dp)
                        ) {
                            Icon(Icons.Default.Lock, null, tint = if(isLocked) Color(237, 112, 20) else Color.White)
                        }

                        var chatMessage by remember { mutableStateOf("") }
                        RWSingleOutlinedTextField(
                            label = "Send Message",
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
                                    }
                                )
                            },
                            onValueChange =
                            {
                                chatMessage = it
                            },
                        )
                    }

                    val listState = rememberLazyListState()
                    BorderCard(modifier = Modifier.padding(10.dp).weight(1f), backgroundColor = Color.DarkGray) {
                        SelectionContainer {
                            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                                items(chatMessages.size) {
                                    Text(chatMessages[chatMessages.size - 1 - it], style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(5.dp, 1.dp, 0.dp, 0.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameRoom.MultiplayerOptionDialog(label: String, property: KMutableProperty<Boolean>) {
    var value by remember { mutableStateOf(property.getter.call()) }
    RWCheckbox(
        value,
        onCheckedChange = { value = it; property.setter.call(it) },
        modifier = Modifier.padding(5.dp),
        enabled = isHost
    )
    Text(label,
        modifier = Modifier.padding(5.dp),
        style = MaterialTheme.typography.headlineLarge,
        color = Color(151, 188, 98)
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
    var playerSpawnPoint by remember(player) { mutableStateOf<Int?>(player.spawnPoint + 1) }
    var playerTeam by remember(player) { mutableStateOf<Int?>(-1) }
    var aiDifficulty by remember(player) { mutableStateOf(player.difficulty ?: room.aiDifficulty) }
    AnimatedAlertDialog(
        visible, onDismissRequest = { onDismissRequest(); update() }
    ) { m, dismiss ->
        BorderCard(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .then(m)
                .verticalScroll(rememberScrollState()),
            backgroundColor = Color.Gray.copy(.7f)
        ) {
            ExitButton(dismiss)
            Text(
                "Player Config",
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp),
                style = MaterialTheme.typography.displayLarge,
                color = Color(151, 188, 98)
            )
            LargeDividingLine { 5.dp }
            Column {
                var expanded0 by remember { mutableStateOf(false) }
                RWSingleOutlinedTextField(
                    "Spawn Point",
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
                    "Team",
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
                    playerTeam = it.toIntOrNull()
                }

                Text(
                    "Players with the same team will be allied together.",
                    modifier = Modifier.padding(5.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )

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


            Row(modifier = Modifier.fillMaxWidth().padding(10.dp),
                 horizontalArrangement = Arrangement.Center) {

                if(player != room.localPlayer && room.isHost)
                    RWTextButton("Kick", Modifier.padding(10.dp)) {
                        room.kickPlayer(player)
                        dismiss()
                    }

                RWTextButton("Apply", Modifier.padding(10.dp)) {
                    player.applyConfigChange(
                        if(playerSpawnPoint == -3) -3 else ((playerSpawnPoint ?: 1) - 1).coerceAtLeast(0),
                        if(playerTeam == -1) 0 else if((playerTeam ?: 0) > 10) (playerTeam ?: 0) + 1 else (playerTeam ?: 1),
                        null,
                        null,
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
private fun MultiplayerOptionDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    update: () -> Unit,
    room: GameRoom,
    players: List<Player>
) = AnimatedAlertDialog(
    visible, onDismissRequest = { onDismissRequest(); update() }
) { m, _ ->
    BorderCard(
        modifier = Modifier
            .fillMaxSize(0.5f)
            .then(m),
        backgroundColor = Color.Gray.copy(.7f)
    ) {

        LazyColumn(modifier = Modifier.padding(10.dp)) {
            item {
                LazyRow {
                    with(room) {
                        item { MultiplayerOptionDialog("No Nukes", room::noNukes) }
                        item { MultiplayerOptionDialog("Shared Control", room::sharedControl) }
                        item { MultiplayerOptionDialog("Allow Spectators", room::allowSpectators) }
                        item { MultiplayerOptionDialog("Team Lock", room::teamLock) }
                    }
                }
            }
            item {
                var selectedIndex1 by remember(room) { mutableStateOf(room.aiDifficulty.ordinal) }
                LargeDropdownMenu(
                    modifier = Modifier.wrapContentSize().padding(5.dp),
                    label = "Difficulty",
                    items = Difficulty.entries,
                    selectedIndex = selectedIndex1,
                    onItemSelected = { index, _ -> selectedIndex1 = index }
                )

                remember(selectedIndex1) {
                    room.aiDifficulty = Difficulty.entries[selectedIndex1]
                }
            }

            item {
                var selectedIndex1 by remember(room) { mutableStateOf(room.fogMode.ordinal) }
                LargeDropdownMenu(
                    modifier = Modifier.wrapContentSize().padding(5.dp),
                    label = "Fog",
                    items = FogMode.entries,
                    selectedIndex = selectedIndex1,
                    onItemSelected = { index, _ -> selectedIndex1 = index }
                )

                remember(selectedIndex1) {
                    room.fogMode = FogMode.entries[selectedIndex1]
                }
            }

            item {
                var selectedIndex1 by remember(room) { mutableStateOf(room.startingUnits) }
                val startingOptionList = remember {
                    listOf(
                        "Default",
                        "Normal (1 builder)",
                        "Small Arm",
                        "3 Engineers",
                        "3 Engineers (No Command Center)",
                        "Experimental Spider"
                    )
                }
                LargeDropdownMenu(
                    modifier = Modifier.wrapContentSize().padding(5.dp),
                    label = "Starting Unit",
                    items = startingOptionList,
                    selectedIndex = selectedIndex1,
                    onItemSelected = { index, _ -> selectedIndex1 = index }
                )

                remember(selectedIndex1) {
                    room.startingUnits = selectedIndex1
                }
            }

            item {
                val startingUnitList = remember {
                    listOf(
                        "2 Teams (eg 5v5)",
                        "3 Teams (eg 1v1v1)",
                        "No teams (FFA)",
                        "All spectators"
                    )
                }
                LargeDropdownMenu(
                    modifier = Modifier.wrapContentSize().padding(5.dp),
                    label = "Set Team",
                    items = startingUnitList,
                    selectedIndex = 0,
                    hasValue = false,
                    onItemSelected = { index, _ ->
                        when(index) {
                            0 -> room.applyTeamChange("2t")
                            1 -> room.applyTeamChange("3t")
                            2 -> room.applyTeamChange("FFA")
                            3 -> room.applyTeamChange("spectators")
                            else -> throw RuntimeException()
                        }

                        update()
                    }
                )
            }

            item {
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
                    modifier = Modifier.wrapContentSize().padding(5.dp),
                    label = "Starting Credits",
                    items = startingCreditList,
                    selectedIndex = selectedIndex1,
                    onItemSelected = { index, _ -> selectedIndex1 = index }
                )

                remember(selectedIndex1) {
                    room.startingCredits = selectedIndex1
                }
            }

            item {
                var range by remember { mutableStateOf(room.maxPlayerCount) }

                Column(modifier = Modifier.wrapContentSize()) {
                    Text(
                        "Max Player : $range",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .padding(0.dp, 5.dp, 0.dp, 5.dp)
                    )
                    Slider(
                        valueRange = players.size.toFloat()..100f,
                        modifier = Modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, 5.dp),
                        value = range.toFloat(),
                        colors = RWSliderColors,
                        onValueChange = { range = it.roundToInt().coerceAtLeast(10) },
                        onValueChangeFinished = {
                            if(range >= players.size) room.maxPlayerCount = range else range =
                                room.maxPlayerCount
                        }
                    )
                }
            }

            item {
                var incomeMultiplier by remember { mutableStateOf(room.incomeMultiplier.toString()) }
                var expanded by remember { mutableStateOf(false) }
                RWSingleOutlinedTextField(
                    "incomeMultiplier",
                    incomeMultiplier,
                    lengthLimitCount = 5,
                    typeInNumberOnly = true,
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
                    room.incomeMultiplier = incomeMultiplier.toFloatOrNull() ?: 1f
                }
            }

            item {
                val context = LocalController.current
                var teamUnitCapHostedGame by remember { mutableStateOf(context.getConfig<Int?>("teamUnitCapHostedGame")) }
                var expanded by remember { mutableStateOf(false) }
                remember(teamUnitCapHostedGame) {
                    val count = teamUnitCapHostedGame ?: 100
                    context.setConfig("teamUnitCapHostedGame", count)
                    context.setTeamUnitCapHostGame(count)
                }
                RWSingleOutlinedTextField(
                    "teamUnitCapHostedGame",
                    teamUnitCapHostedGame?.toString() ?: "",
                    lengthLimitCount = 6,
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
                            listOf(100, 250, 500, 1000, 2000, 5000, 10000),
                            onItemSelected = { _, v -> teamUnitCapHostedGame = v }
                        ) {
                            expanded = false
                        }
                    }
                ) {
                    teamUnitCapHostedGame = it.toIntOrNull()
                }
            }
        }
    }
}