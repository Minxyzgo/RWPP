/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.rwpp.AppContext
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.appKoin
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.config.Settings
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.CloseUIPanelEvent
import io.github.rwpp.event.events.RefreshUIEvent
import io.github.rwpp.event.events.ReturnMainMenuEvent
import io.github.rwpp.event.onDispose
import io.github.rwpp.external.Extension
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.game.*
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.FogMode
import io.github.rwpp.game.map.MapType
import io.github.rwpp.game.team.TeamMode
import io.github.rwpp.game.units.UnitType
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.platform.BackHandler
import io.github.rwpp.scripts.Render
import io.github.rwpp.ui.UI.chatMessages
import io.github.rwpp.ui.color.getTeamColor
import io.github.rwpp.widget.*
import io.github.rwpp.widget.v2.LazyColumnScrollbar
import io.github.rwpp.widget.v2.LongPressFloatingActionButton
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MultiplayerRoomView(isSandboxGame: Boolean = false, onExit: () -> Unit) {
    BackHandler(true, onExit)
    DisposableEffect(Unit) {
        onDispose {
            CloseUIPanelEvent("multiplayerRoom").broadcastIn()
        }
    }

    val externalHandler = koinInject<ExternalHandler>()
    val game = koinInject<Game>()
    val room = game.gameRoom

    var update by remember { mutableStateOf(false) }
    var lastSelectedIndex by remember { mutableStateOf(0) }
    var selectedMap by remember(update) { mutableStateOf(room.selectedMap) }
    val displayMapName = remember(update) { room.displayMapName }

    var optionVisible by remember { mutableStateOf(false) }
    var banUnitVisible by remember { mutableStateOf(false) }
    //var downloadModViewVisible by remember { mutableStateOf(false) }
    //var loadModViewVisible by remember { mutableStateOf(false) }
    var selectedBanUnits by remember { mutableStateOf(listOf<UnitType>()) }

    var showMapSelectView by remember { mutableStateOf(false) }
    val isHost = remember(update) { room.isHost || room.isHostServer }

    val updateAction = { update = !update }

    val scope = rememberCoroutineScope()

    val extensions = remember {
        externalHandler.getAllExtensions().onFailure {
            UI.showWarning(it.message ?: "Unexpected error")
        }.getOrDefault(listOf()).filter { !it.config.hasResource }
    }

//
//    GlobalEventChannel.filter(CallReloadModEvent::class).onDispose {
//        subscribeAlways {
//            loadModViewVisible = true
//            downloadModViewVisible = false
//        }
//    }
//
//    GlobalEventChannel.filter(CallStartDownloadModEvent::class).onDispose {
//        subscribeAlways { downloadModViewVisible = true }
//    }

    GlobalEventChannel.filter(RefreshUIEvent::class).onDispose {
        subscribeAlways { updateAction() }
    }

    GlobalEventChannel.filter(ReturnMainMenuEvent::class).onDispose {
        subscribeAlways { onExit() }
    }


//    LoadingView(loadModViewVisible, { loadModViewVisible = false }) {
//        message("reloading mods...")
//        modManager.modReload()
//        net.sendPacketToServer(ModPacket.ModReloadFinishPacket())
//        true
//    }
//
//    LoadingView(downloadModViewVisible, { downloadModViewVisible = false }) {
//        message("downloading mods...")
//        delay(Long.MAX_VALUE)
//        false
//    }

    val players = remember(update) { room.getPlayers().sortedBy { it.team } }
    var selectedPlayer by remember { mutableStateOf(players.firstOrNull() ?: ConnectingPlayer) }
    var playerOverrideVisible by remember { mutableStateOf(false) }

    PlayerOverrideDialog(
        playerOverrideVisible,
        { playerOverrideVisible = false },
        updateAction,
        room,
        extensions,
        selectedPlayer
    )

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

    MultiplayerOption(
        optionVisible,
        { optionVisible = false },
        updateAction,
        extensions,
        { banUnitVisible = true; optionVisible = false },
    )

    BanUnitViewDialog(banUnitVisible, { banUnitVisible = false }, selectedBanUnits) {
        selectedBanUnits = it
        game.onBanUnits(it)
    }

    val chatFocusRequester = remember { FocusRequester() }

    @Composable
    fun ContentView() {
        @Composable
        fun MessageTextField(
            chatMessage: String,
            onFocusChanged: (FocusState) -> Unit = {},
            onChatMessageChange: (String) -> Unit
        ) {
            RWSingleOutlinedTextField(
                label = readI18n("multiplayer.room.sendMessage"),
                value = chatMessage,
                focusRequester = chatFocusRequester,
                onFocusChanged = onFocusChanged,
                modifier = Modifier.fillMaxWidth().padding(10.dp)
                    .onKeyEvent {
                        if (it.key == Key.Enter && chatMessage.isNotEmpty()) {
                            room.sendChatMessageOrCommand(chatMessage)
                            onChatMessageChange("")
                        }

                        true
                    },
                trailingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        modifier = Modifier.clickable {
                            room.sendChatMessageOrCommand(chatMessage)
                            onChatMessageChange("")
                        },
                        tint = MaterialTheme.colorScheme.surfaceTint
                    )
                },
                onValueChange =
                    {
                        onChatMessageChange(it)
                    },
            )
        }


        @Composable
        fun MessageView() {
            var value by remember(chatMessages) { mutableStateOf(TextFieldValue(chatMessages)) }

            if (LocalWindowManager.current == WindowManager.Large) {
                TextField(
                    value = value,
                    onValueChange = { value = it },
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxSize(),
                    colors = RWTextFieldColors,
                    maxLines = 100
                )
            } else {
                TextField(
                    value = value,
                    onValueChange = { value = it },
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    colors = RWTextFieldColors,
                    maxLines = 100
                )
            }
        }

        val globalFocusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        val interactionSource = remember { MutableInteractionSource() }
        val isDesktop = remember { appKoin.get<AppContext>().isDesktop() }

        BorderCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .focusRequester(globalFocusRequester)
                .clickable(
                    interactionSource,
                    null
                ) {
                    globalFocusRequester.requestFocus()
                    keyboardController?.hide()
                }.onKeyEvent {
                    when(it.key) {
                        Key.Enter -> {
                            chatFocusRequester.requestFocus()
                            true
                        }
                        Key.M -> {
                            showMapSelectView = true
                            true
                        }
                        Key.O -> {
                            optionVisible = true
                            true
                        }
                        Key.A -> {
                            room.addAI()
                            true
                        }
                        Key.C -> {
                            if (players.isNotEmpty()) {
                                selectedPlayer =
                                    room.localPlayer
                                playerOverrideVisible = true
                            }
                            true
                        }
                        else -> false
                    }
                }
        ) {
            Box {
                ExitButton(onExit)
                Column {
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            BorderCard(
                                modifier = Modifier
                                    .weight(.4f)
                                    .padding(10.dp)
                                    .then(
                                        if (LocalWindowManager.current != WindowManager.Large)
                                            Modifier.verticalScroll(rememberScrollState()).align(Alignment.CenterVertically)
                                        else Modifier
                                    ),
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(
                                    .7f
                                )
                            ) {
                                var details by remember { mutableStateOf("Getting details...") }

                                @Composable
                                fun MapImage(modifier: Modifier = Modifier) {
                                    AsyncImage(
                                        selectedMap,
                                        null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.then(modifier).padding(10.dp)
                                            .border(
                                                BorderStroke(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.surfaceContainer
                                                )
                                            )
                                            .clickable(isHost) { showMapSelectView = true }
                                    )
                                }

                                remember(update) {
                                    scope.launch {
                                        details = room.roomDetails().split("\n")
                                            .filter { !it.startsWith("Map:") && it.isNotBlank() }
                                            .joinToString("\n")
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().then(
                                        if (LocalWindowManager.current == WindowManager.Large)
                                            Modifier.weight(1f) else Modifier
                                    ), horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        details,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                    if (LocalWindowManager.current == WindowManager.Large) MapImage(
                                        Modifier.weight(.8f)
                                    )
                                }

                                if (LocalWindowManager.current != WindowManager.Large) {
                                    MapImage(
                                        Modifier.defaultMinSize(minHeight = 200.dp).fillMaxWidth()
                                    )
                                }

                                val mapType = remember(update) { room.mapType }

                                Text(
                                    mapType.displayName(),
                                    modifier = Modifier.padding(5.dp)
                                        .align(Alignment.CenterHorizontally),
                                    style = MaterialTheme.typography.headlineLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    displayMapName,
                                    modifier = Modifier.padding(5.dp)
                                        .align(Alignment.CenterHorizontally),
                                    style = MaterialTheme.typography.headlineMedium,
                                    //    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                @Composable
                                fun OptionButtons() {
                                    if (LocalWindowManager.current == WindowManager.Large) RWTextButton(
                                        readI18n("multiplayer.room.selectMap") + if (isDesktop) "(M)" else "",
                                        modifier = Modifier.padding(5.dp)
                                    ) { showMapSelectView = true }
                                    if (LocalWindowManager.current != WindowManager.Large && !isSandboxGame) {
                                        var isLocked by remember { mutableStateOf(false) }
                                        IconButton(
                                            { isLocked = !isLocked; room.lockedRoom = isLocked },
                                            modifier = Modifier.padding(
                                                horizontal = 5.dp,
                                                vertical = 5.dp
                                            ),
                                            enabled = room.isHost,
                                        ) {
                                            Icon(
                                                Icons.Default.Lock,
                                                null,
                                                tint = if (isLocked) Color(
                                                    237,
                                                    112,
                                                    20
                                                ) else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    RWTextButton(
                                        readI18n("multiplayer.room.option") + if (isDesktop) "(O)" else "",
                                        modifier = Modifier.padding(5.dp)
                                    ) { optionVisible = true }
                                    RWTextButton(
                                        readI18n("multiplayer.room.start"),
                                        modifier = Modifier.padding(5.dp)
                                    ) {
                                         val unpreparedPlayers = game.gameRoom.getPlayers().filter { !it.data.ready }
                                         if(unpreparedPlayers.isNotEmpty()) {
                                             game.gameRoom.sendSystemMessage(
                                                 "Cannot start game. Because players: ${unpreparedPlayers.joinToString(", ") { it.name }} aren't ready.")
                                        } else if (room.isHostServer) room.sendQuickGameCommand("-start") else room.startGame()
                                    }
                                }


                                if (isHost) {
                                    if (LocalWindowManager.current == WindowManager.Small) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            OptionButtons()
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
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
                                    /*if(LocalWindowManager.current != WindowManager.Large) Modifier.verticalScroll(rememberScrollState())
                                else*/ Modifier
                                ),
                            ) {
                                BorderCard(
                                    modifier = Modifier.fillMaxWidth()
                                        .defaultMinSize(minHeight = 200.dp).padding(5.dp),
                                    backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(
                                        .7f
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(5.dp)
                                            .border(
                                                BorderStroke(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.secondary
                                                ), CircleShape
                                            )
                                            .fillMaxWidth()
                                    ) {
                                        TableCell(
                                            "name",
                                            playerNameWeight,
                                            drawStroke = false,
                                            strokeColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                        TableCell(
                                            "spawn",
                                            playerSpawnWeight,
                                            strokeColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                        TableCell(
                                            "team",
                                            playerTeamWeight,
                                            strokeColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                        TableCell(
                                            "ping",
                                            playerPingWeight,
                                            drawStroke = false,
                                            strokeColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    }

                                    @Composable
                                    fun PlayerTable(
                                        index: Int,
                                        modifier: Modifier = Modifier
                                        //animateItem: (Modifier.() -> Unit)? = null
                                    ) {
                                        val options = remember {
                                            game.getStartingUnitOptions()
                                        }
                                        val player = players.getOrNull(index)

                                        if (player != null) {
                                            Row(
                                                modifier = Modifier
                                                    .then(modifier)
                                                    .height(IntrinsicSize.Max)
                                                    .padding(5.dp)
                                                    .border(
                                                        BorderStroke(
                                                            2.dp,
                                                            MaterialTheme.colorScheme.primary
                                                        ), CircleShape
                                                    )
                                                    .fillMaxWidth()
                                                    .clickable(room.isHost || room.isHostServer || room.localPlayer == player) {
                                                        selectedPlayer = player
                                                        playerOverrideVisible = true
                                                    }
                                            ) {
                                                TableCell(
                                                    player.name + if (player.startingUnit != -1) " - ${options.first { it.first == player.startingUnit }.second}" else "",
                                                    color = if (player.color != -1) Player.getTeamColor(
                                                        player.color
                                                    ) else MaterialTheme.colorScheme.onSurface,
                                                    weight = playerNameWeight, drawStroke = false,
                                                    modifier = Modifier.fillMaxHeight()
                                                ) {
                                                if (!player.data.ready) {
                                                    CircularProgressIndicator(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(15.dp).padding(0.dp, 2.dp, 0.dp, 2.dp)
                                                    )
                                                }
                                                }
                                                TableCell(
                                                    if (player.isSpectator)
                                                        "S"
                                                    else (player.spawnPoint + 1).toString(),
                                                    playerSpawnWeight,
                                                    color = if (player.isSpectator)
                                                        Color.Black
                                                    else Player.getTeamColor(player.spawnPoint),
                                                    modifier = Modifier.fillMaxHeight()
                                                )
                                                TableCell(
                                                    player.teamAlias(),
                                                    playerTeamWeight,
                                                    color = Player.getTeamColor(player.team),
                                                    modifier = Modifier.fillMaxHeight()
                                                )
                                                val ping = remember(update) { player.ping }
                                                TableCell(
                                                    ping,
                                                    playerPingWeight,
                                                    drawStroke = false,
                                                    modifier = Modifier.fillMaxHeight()
                                                )
                                            }
                                        }
                                    }

//                                if(LocalWindowManager.current != WindowManager.Large) {
//                                    for(i in players.indices) {
//                                        PlayerTable(i)
//                                    }
//                                } else {

                                    LazyColumnScrollbar(
                                        listState = state,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        var chatMessage by remember { mutableStateOf("") }
                                        LazyColumn(
                                            modifier = Modifier.fillMaxWidth(),
                                            state = state
                                        ) {
                                            items(
                                                count = players.size,
                                                key = { players[it].connectHexId }
                                            ) { index ->
                                                PlayerTable(
                                                    index,
                                                    if (koinInject<Settings>().enableAnimations)
                                                        Modifier.animateItem()
                                                    else Modifier
                                                )
                                            }

                                            item(key = "chat-field") {
                                                if (LocalWindowManager.current != WindowManager.Large && !isSandboxGame) {
                                                    BorderCard(
                                                        modifier = Modifier.fillMaxWidth()
                                                            .defaultMinSize(minHeight = 200.dp)
                                                            .padding(5.dp),
                                                        backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(
                                                            .7f
                                                        )
                                                    ) {
                                                        Row(modifier = Modifier.fillMaxWidth()) {
                                                            RWTextButton(
                                                                readI18n("multiplayer.room.changeSite") + if (isDesktop) "(C)" else "",
                                                                modifier = Modifier.padding(
                                                                    horizontal = 5.dp,
                                                                    vertical = 30.dp
                                                                )
                                                            ) {
                                                                if (players.isNotEmpty()) {
                                                                    selectedPlayer =
                                                                        room.localPlayer
                                                                    playerOverrideVisible = true
                                                                }
                                                            }

                                                            MessageTextField(chatMessage) { chatMessage = it }
                                                        }

                                                        MessageView()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    //}
                                }
                            }
                        }
                    }

                    if (LocalWindowManager.current == WindowManager.Large) {
                        BorderCard(
                            modifier = Modifier
                                .weight(1f)
                                .padding(10.dp),
                            backgroundColor = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .height(IntrinsicSize.Max)
                                        .padding(5.dp)
                                ) {
                                    RWTextButton(
                                        readI18n("multiplayer.room.changeSite") + if (isDesktop) "(C)" else "",
                                        modifier = Modifier.padding(
                                            horizontal = 5.dp,
                                            vertical = 30.dp
                                        )
                                    ) {
                                        if (players.isNotEmpty()) {
                                            selectedPlayer = room.localPlayer
                                            playerOverrideVisible = true
                                        }
                                    }
                                    if (isHost) {
                                        RWTextButton(
                                            readI18n("multiplayer.room.addAI") + if (isDesktop) "(A)" else "",
                                            modifier = Modifier.padding(
                                                horizontal = 5.dp,
                                                vertical = 30.dp
                                            ),
                                            onLongClick = { room.addAI(10) }
                                        ) { room.addAI() }
                                    }

                                    var isLocked by remember(update) { mutableStateOf(room.lockedRoom) }
                                    if (!isSandboxGame) IconButton(
                                        { isLocked = !isLocked; room.lockedRoom = isLocked },
                                        enabled = isHost,
                                        modifier = Modifier.padding(
                                            horizontal = 5.dp,
                                            vertical = 30.dp
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Lock,
                                            null,
                                            tint = if (isLocked) Color(
                                                237,
                                                112,
                                                20
                                            ) else MaterialTheme.colorScheme.surfaceTint
                                        )
                                    }

                                    var chatMessage by remember { mutableStateOf("") }
                                    if (!isSandboxGame) MessageTextField(chatMessage) { chatMessage = it }
                                }

                                if (!isSandboxGame) BorderCard(
                                    modifier = Modifier
                                        .padding(5.dp),
                                    backgroundColor = MaterialTheme.colorScheme.surface.copy(.7f)
                                ) {
                                    MessageView()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (LocalWindowManager.current != WindowManager.Large) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                if (isHost) {
                    LongPressFloatingActionButton(
                        Icons.Default.Add,
                        modifier = Modifier.padding(5.dp),
                        onClick = { room.addAI() },
                        onLongClick = { room.addAI(10) }
                    )
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
private fun MultiplayerOption(
    label: String,
    value: Boolean,
    enabled: Boolean = true,
    onValueChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RWCheckbox(
            value,
            onCheckedChange = { onValueChange(!value) },
            modifier = Modifier.padding(5.dp),
            enabled
        )
        Text(
            label,
            modifier = Modifier.padding(top = 5.dp),
            style = MaterialTheme.typography.headlineMedium,
            color = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.inversePrimary
        )
    }
}

@Composable
private fun PlayerOverrideDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    update: () -> Unit,
    room: GameRoom,
    extensions: List<Extension>,
    player: Player,
) {
    val game = koinInject<Game>()
    val items = remember {
        buildList {
            add(-1 to "Default")
            addAll(game.getStartingUnitOptions())
        }
    }

    AnimatedAlertDialog(
        visible, onDismissRequest = { onDismissRequest(); update() }
    ) { dismiss ->

        var playerSpawnPoint by remember(player) { mutableStateOf<Int?>(player.spawnPoint + 1) }
        var playerTeam by remember(player) { mutableStateOf<Int?>(-1) }
        var playerColor by remember(player) { mutableStateOf(player.color) }
        var playerStartingUnits by remember(player) { mutableStateOf(items.indexOfFirst { it.first == player.startingUnit }) }
        var aiDifficulty by remember(player) { mutableStateOf(player.difficulty ?: room.aiDifficulty) }

        BorderCard(
            modifier = Modifier
                .fillMaxSize(LargeProportion()),
        ) {
            Text(
                readI18n("multiplayer.room.playerConfig"),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            LargeDividingLine { 0.dp }
            Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                var expanded0 by remember { mutableStateOf(false) }
                RWSingleOutlinedTextField(
                    readI18n("common.spawnPoint"),
                    if (playerSpawnPoint == -3) "Spectator" else playerSpawnPoint?.toString() ?: "",
                    lengthLimitCount = 3,
                    modifier = Modifier.padding(10.dp),
                    typeInNumberOnly = true,
                    typeInOnlyInteger = true,
                    trailingIcon = {
                        val icon =
                            if (expanded0) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
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
                                playerSpawnPoint = if (v != "Spectator") i + 1 else -3
                            }
                        ) {
                            expanded0 = false
                        }
                    }
                ) {
                    val n = it.toIntOrNull()
                    if (n == null || n <= room.maxPlayerCount) playerSpawnPoint = n
                }

                Text(
                    readI18n("multiplayer.room.spawnPointTip"),
                    modifier = Modifier.padding(5.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )

                var expanded by remember { mutableStateOf(false) }
                RWSingleOutlinedTextField(
                    readI18n("common.team"),
                    if (playerTeam == -1) "auto" else playerTeam?.toString() ?: "",
                    lengthLimitCount = 3,
                    modifier = Modifier.padding(10.dp),
                    typeInNumberOnly = true,
                    typeInOnlyInteger = true,
                    trailingIcon = {
                        val icon =
                            if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
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
                                playerTeam = if (v == "auto") -1 else (v as Int)
                            }
                        ) {
                            expanded = false
                        }
                    }
                ) {
                    playerTeam = it.toIntOrNull()?.coerceAtMost(100)?.coerceAtLeast(1)
                }



                Text(
                    readI18n("multiplayer.room.teamTip"),
                    modifier = Modifier.padding(5.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (room.isHost) {
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
                        onItemSelected = { i, _ -> playerColor = i - 1 },
                        selectedItemColor = { _, i -> if (i > 0) Player.getTeamColor(i - 1) else MaterialTheme.colorScheme.onSurface }
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


                if (player.isAI) {
                    LargeDropdownMenu(
                        modifier = Modifier.padding(20.dp),
                        label = "Difficulty",
                        items = Difficulty.entries,
                        selectedIndex = aiDifficulty.ordinal,
                        onItemSelected = { _, v -> aiDifficulty = v }
                    )
                }
            }


            LargeDividingLine { 0.dp }
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {

                for (extension in extensions) {
                    if (extension.isEnabled && extension.extraPlayerOptions.isNotEmpty()) {
                        Column {
                            Text(
                                extension.config.displayName,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 5.dp)
                            )

                            HorizontalDivider(
                                thickness = 3.dp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 5.dp),
                                color = MaterialTheme.colorScheme.primary
                            )

                            for (widget in extension.extraPlayerOptions) {
                                widget.Render()
                            }
                        }
                    }
                }

                if (player != room.localPlayer && (room.isHost || room.isHostServer))
                    RWTextButton(readI18n("multiplayer.room.kick"), Modifier.padding(5.dp)) {
                        room.kickPlayer(player)
                        dismiss()
                    }

                RWTextButton(readI18n("multiplayer.room.apply"), Modifier.padding(5.dp)) {
                    player.applyConfigChange(
                        if (playerSpawnPoint == -3) -3 else ((playerSpawnPoint ?: 1) - 1).coerceAtLeast(0),
                        if (playerTeam == -1) 0 else if ((playerTeam ?: 0) > 10) (playerTeam ?: 0) + 1 else (playerTeam
                            ?: 1),
                        if (playerColor > -1) playerColor else null,
                        if (playerStartingUnits > 0) items[playerStartingUnits].first else null,
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
private fun MultiplayerOption(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    update: () -> Unit,
    extensions: List<Extension>,
    onShowBanUnitDialog: () -> Unit,
) = AnimatedAlertDialog(
    visible, onDismissRequest = { onDismissRequest(); update() }
) { dismiss ->
    val game = koinInject<Game>()
    val room = game.gameRoom
    val configIO = koinInject<ConfigIO>()

    val players = remember { room.getPlayers() }

    var noNukes by remember { mutableStateOf(room.noNukes) }
    var sharedControl by remember { mutableStateOf(room.sharedControl) }
    var allowSpectators by remember { mutableStateOf(room.allowSpectators) }
    var teamLock by remember { mutableStateOf(room.teamLock) }
    var aiDifficulty by remember { mutableStateOf(room.aiDifficulty) }
    var fogMode by remember { mutableStateOf(room.fogMode) }
    var startingUnits by remember { mutableStateOf(room.startingUnits) }
    var teamMode by remember { mutableStateOf(room.teamMode) }
    var startingCredits by remember { mutableStateOf(room.startingCredits) }
    var maxPlayerCount by remember { mutableStateOf(room.maxPlayerCount) }
    var realIncomeMultiplier by remember { mutableStateOf(room.incomeMultiplier) }

    val teamModes = remember {
        TeamMode.modes
    }

    BorderCard(
        modifier = Modifier
            .fillMaxSize(LargeProportion()),
    ) {
        LazyColumn(modifier = Modifier.weight(1f).padding(10.dp)) {
            item {
                LazyRow(horizontalArrangement = Arrangement.Center) {
                    item { MultiplayerOption(readI18n("multiplayer.room.noNukes"), noNukes) { noNukes = it } }
                    item {
                        MultiplayerOption(
                            readI18n("multiplayer.room.sharedControl"),
                            sharedControl
                        ) { sharedControl = it }
                    }
                    item {
                        MultiplayerOption(
                            readI18n("multiplayer.room.allowSpectators"),
                            allowSpectators, room.isHost
                        ) { allowSpectators = it }
                    }
                    item {
                        MultiplayerOption(readI18n("multiplayer.room.teamLock"), teamLock, room.isHost) {
                            teamLock = it
                        }
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
                        game.getStartingUnitOptions()
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
                        buildList {
                            add("Keep current")
                            addAll(teamModes)
                        }
                    }

                    var selectedIndex by remember {
                        mutableStateOf(teamMode?.let { teamModes.indexOf(teamMode) + 1 } ?: 0)
                    }
                    LargeDropdownMenu(
                        modifier = Modifier.weight(.5f).padding(5.dp),
                        label = readI18n("multiplayer.room.setTeam"),
                        enabled = room.isHost,
                        items = teamList,
                        selectedIndex = selectedIndex,
                        selectedItemToString = {
                            if (it is TeamMode) it.displayName else it.toString()
                        },
                        onItemSelected = { index, _ ->
                            selectedIndex = index
                            teamMode = when (index) {
                                0 -> null
                                else -> teamModes[index - 1]
                            }
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
                            if (range >= players.size) maxPlayerCount = range else range =
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
                                if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
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

                    var teamUnitCapHostedGame by remember { mutableStateOf(configIO.getGameConfig<Int?>("teamUnitCapHostedGame")) }
                    var expanded1 by remember { mutableStateOf(false) }
                    remember(teamUnitCapHostedGame) {
                        val count = teamUnitCapHostedGame ?: 100
                        configIO.setGameConfig("teamUnitCapHostedGame", count)
                        game.setTeamUnitCapHostGame(count)
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
                                if (expanded1) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
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

            if (room.isHost) {
                item {
                    RWTextButton(
                        readI18n("multiplayer.room.banUnits"),
                        modifier = Modifier.padding(5.dp).align(Alignment.CenterHorizontally),
                    ) {
                        onShowBanUnitDialog()
                    }
                }
            }

            items(extensions, key = { it.config.id }) { extension ->
                if (extension.isEnabled && extension.extraRoomOptions.isNotEmpty()) {
                    Column {
                        Text(
                            extension.config.displayName,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 5.dp)
                        )

                        HorizontalDivider(
                            thickness = 3.dp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 5.dp),
                            color = MaterialTheme.colorScheme.primary
                        )

                        for (widget in extension.extraRoomOptions) {
                            widget.Render()
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(5.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            RWTextButton(readI18n("multiplayer.room.apply")) {
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