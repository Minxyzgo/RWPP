/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.event.events

import io.github.rwpp.event.AbstractEvent

sealed class GameEvent : AbstractEvent()

class RefreshUIEvent : GameEvent()

class ReturnMainMenuEvent : GameEvent()

class ReturnBattleRoomEvent : GameEvent()

class StartGameEvent : GameEvent()

class ChatMessageEvent(val sender: String, val message: String, val spawn: Int) : GameEvent()

class KickedEvent(val reason: String) : GameEvent()

class QuestionDialogEvent(val title: String, val message: String) : GameEvent()

class QuestionReplyEvent(val message: String?, val cancel: Boolean) : GameEvent()