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