/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.event.events

import io.github.rwpp.event.AbstractEvent
import io.github.rwpp.game.Player

sealed class GameEvent : AbstractEvent()

class RefreshUIEvent : GameEvent()

class ReturnMainMenuEvent : GameEvent()

class StartGameEvent : GameEvent()

class QuitGameEvent : GameEvent()

//TODO
class GameOverEvent : GameEvent()

class DisconnectEvent(val reason: String) : GameEvent()

class ChatMessageEvent(var sender: String, var message: String, val player: Player, var color: Int) : GameEvent()
