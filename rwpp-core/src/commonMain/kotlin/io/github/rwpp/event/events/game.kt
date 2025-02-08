/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.event.events

import io.github.rwpp.event.AbstractEvent
import io.github.rwpp.game.Player
import io.github.rwpp.game.map.GameMap

sealed class GameEvent : AbstractEvent()


class RefreshUIEvent : GameEvent()

class ReturnMainMenuEvent : GameEvent()

/**
 * Event that is fired when the game is loaded.
 */
class GameLoadedEvent : GameEvent()

/**
 * Event that is fired when the game is started.
 */
class StartGameEvent : GameEvent()

/**
 * Event that is fired when hosting a game.
 */
class HostGameEvent : GameEvent()

/**
 * Event that is fired when joining a game.
 */
class JoinGameEvent(val address: String) : GameEvent()

/**
 * Event that is fired when the local player quits the game.
 */
class QuitGameEvent : GameEvent()

/**
 * Event that is fired when the game is over.
 */
class GameOverEvent(val gameTime: Int, val winTeam: Int) : GameEvent()

/**
 * Event that is fired when the map changes.
 */
class MapChangedEvent(val mapName: String) : GameEvent()

/**
 * Event that is fired when a player disconnects from the game.
 */
class DisconnectEvent(val reason: String) : GameEvent()

/**
 * Event that is fired when a player sends a chat message.
 */
class ChatMessageEvent(var sender: String, var message: String, val player: Player, var color: Int) : GameEvent()

/**
 * Event that is fired when the room sends a system message.
 */
class SystemMessageEvent(val message: String) : GameEvent()

