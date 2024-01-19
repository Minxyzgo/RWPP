/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.view.ViewGroup
import com.corrodinggames.rts.appFramework.ClosingActivity
import com.corrodinggames.rts.appFramework.GameView
import com.corrodinggames.rts.appFramework.GameViewOpenGL
import com.corrodinggames.rts.appFramework.GameViewThreaded
import com.corrodinggames.rts.appFramework.MultiplayerBattleroomActivity
import com.corrodinggames.rts.gameFramework.h.a
import com.corrodinggames.rts.gameFramework.j.ae
import com.corrodinggames.rts.gameFramework.j.ao
import com.corrodinggames.rts.gameFramework.k
import com.github.minxyzgo.rwij.InjectMode
import com.github.minxyzgo.rwij.InterruptResult
import com.github.minxyzgo.rwij.setFunction
import io.github.rwpp.R
import io.github.rwpp.android.*
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.*
import io.github.rwpp.game.units.GameCommandActions
import io.github.rwpp.game.units.GameInternalUnits
import java.util.concurrent.ConcurrentLinkedQueue

fun doProxy() {
    @Suppress("unchecked_cast")
    MultiplayerBattleroomActivity::class.setFunction {
        val messagesField = com.corrodinggames.rts.gameFramework.j.a::class.java.getDeclaredField("a").apply {
            isAccessible = true
        }
        val spawnField = com.corrodinggames.rts.gameFramework.j.b::class.java.getDeclaredField("a").apply {
            isAccessible = true
        }
        val senderField = com.corrodinggames.rts.gameFramework.j.b::class.java.getDeclaredField("b").apply {
            isAccessible = true
        }
        val messageField = com.corrodinggames.rts.gameFramework.j.b::class.java.getDeclaredField("c").apply {
            isAccessible = true
        }
        addProxy(MultiplayerBattleroomActivity::updateUI) {
            RefreshUIEvent().broadCastIn()
        }

        addProxy(MultiplayerBattleroomActivity::startGame) {
            if(!isGaming) controller.gameRoom.startGame()
        }

        addProxy(MultiplayerBattleroomActivity::refreshChatLog) {
            val messages = messagesField.get(GameEngine.t().bU.aE) as ConcurrentLinkedQueue<com.corrodinggames.rts.gameFramework.j.b>
            messages.clear() // 暂时忽略
        }

        addProxy(MultiplayerBattleroomActivity::addMessageToChatLog) {
            val messages = messagesField.get(GameEngine.t().bU.aE) as ConcurrentLinkedQueue<com.corrodinggames.rts.gameFramework.j.b>
            val last = messages.poll()
            val sender = senderField.get(last) as String?
            val spawn = spawnField.get(last) as Int
            val message = messageField.get(last) as String
            ChatMessageEvent(sender ?: "", message, spawn).broadCastIn()
        }

        addProxy(MultiplayerBattleroomActivity::askPasswordInternal) { ae: ao? ->
            if(ae == null) return@addProxy
            if(questionOption != null) {
                ae.a(questionOption)
                questionOption = null
                return@addProxy
            }
            QuestionDialogEvent(
                if(ae.b != null)
                    "Server Question"
                else ae.e ?: "Password Required",
                if(ae.b != null)
                    a.b(ae.b)
                else "This server requires a password to join"
            ).broadCastIn()
            GlobalEventChannel.filter(QuestionReplyEvent::class).subscribeOnce {
                if(!it.cancel) {
                    ae.a(it.message!!)
                } else {
                    ae.a()
                }
            }
        }
    }

    GameEngine::class.setFunction {
        addProxy("g", String::class) { _ : Any?, msg: String ->
            KickedEvent(msg).broadCastIn()
        }

        addProxy("d", String::class, mode = InjectMode.InsertBefore) { str: String ->
            if(str == "----- returnToBattleroom -----") {
                isReturnToBattleRoom = true
            }

            Unit
        }
    }

    com.corrodinggames.rts.appFramework.d::class.setFunction {
        addProxy("b", Activity::class) { activity: Activity ->
            val viewGroup = activity.getWindow().getDecorView().getRootView() as ViewGroup
            val i = com.corrodinggames.rts.appFramework.d.d;
            var i2 = i;
            val bMethod = com.corrodinggames.rts.appFramework.d::class.java.getDeclaredMethod("b").apply { isAccessible = true }
            if (i == com.corrodinggames.rts.appFramework.l.f) {
                i2 = bMethod.invoke(null) as Int
            }
            val gameViewThreaded =
                if(i2 == com.corrodinggames.rts.appFramework.l.c)
                    GameViewThreaded(activity, null)
                else if(i2 == com.corrodinggames.rts.appFramework.l.e) GameViewOpenGL(activity, null)
                else if(i2 == com.corrodinggames.rts.appFramework.l.a) GameView(activity, null)
                else if(i2 == com.corrodinggames.rts.appFramework.l.b)
                    if(Build.VERSION.SDK_INT >= 26) GameView(activity, null)
                    else RWPPGameViewNonSurface(activity, null)
                else RWPPGameViewNonSurface(activity, null);
            viewGroup.addView(gameViewThreaded, 0, ViewGroup.LayoutParams(-1, -1));
            gameViewThreaded
        }
    }

    ae::class.setFunction {
        addProxy("X") {
            if (!k.aR) {
                val t: k = k.t()
                val intent = Intent(
                    t.al,
                    ClosingActivity::class.java
                )
                // support for android 12
                val activity = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(
                        t.al, 0, Intent(
                            t.al,
                            ClosingActivity::class.java
                        ), PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    PendingIntent.getActivity(
                        t.al, 0, Intent(
                            t.al,
                            ClosingActivity::class.java
                        ), PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
                val notificationManager =
                    t.al.getSystemService("notification") as NotificationManager
                if (Build.VERSION.SDK_INT >= 11) {
                    val i = Build.VERSION.SDK_INT
                    val builder = Notification.Builder(t.al)
                    builder.setContentTitle("Rusted Warfare Multiplayer")
                    builder.setContentText("A multiplayer game is in progress")
                    builder.setSmallIcon(R.drawable.icon)
                    builder.setContentIntent(activity)
                    builder.setOngoing(true)
                    ae.a(notificationManager)
                    ae.a(builder, "multiplayerStatusId")
                    if (Build.VERSION.SDK_INT >= 16) {
                        builder.build()
                    }
                    notificationManager.notify(1, builder.notification)
                }
            }

        }
        addProxy("d", String::class, String::class) { self: ae, arg1: String, arg2: String ->
            if (!k.aR) {
                val t: k = k.t()
                if (!self.G && !t.bY.g()) {
                    var isActivityVisible: Boolean =
                        MultiplayerBattleroomActivity.isActivityVisible()
                    val abVar = t.an
                    if (abVar != null && !abVar.isPaused) {
                        isActivityVisible = true
                    }
                    if (isActivityVisible) {
                        if (self.bD) {
                            ae.f(2)
                            return@addProxy Unit
                        }
                        return@addProxy Unit
                    }
                    val notificationManager: NotificationManager =
                        t.al.getSystemService("notification") as NotificationManager
                    val intent = Intent(
                        t.al,
                        ClosingActivity::class.java
                    )
                    // support for android 12
                    val activity = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.getActivity(
                            t.al, 0, Intent(
                                t.al,
                                ClosingActivity::class.java
                            ), PendingIntent.FLAG_IMMUTABLE
                        )
                    } else {
                        PendingIntent.getActivity(
                            t.al, 0, Intent(
                                t.al,
                                ClosingActivity::class.java
                            ), PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }
                    if (Build.VERSION.SDK_INT >= 11) {
                        val builder: Notification.Builder = Notification.Builder(t.al)
                        builder.setContentTitle("Rusted Warfare Multiplayer")
                        builder.setContentText(arg1 + ": " + arg2)
                        builder.setSmallIcon(R.drawable.icon)
                        builder.setContentIntent(activity)
                        builder.setOngoing(false)
                        builder.setAutoCancel(true)
                        ae.a(notificationManager)
                        ae.a(builder, "multiplayerChatId")
                        notificationManager.notify(2, builder.getNotification())
                        self.bD = true
                    }
                }
            }
        }

        addProxy("x", mode = InjectMode.InsertBefore) { _: Any? ->
            isReturnToBattleRoom = true
            Unit
        }
        addProxy("a", com.corrodinggames.rts.gameFramework.e::class, mode = InjectMode.InsertBefore) { _: Any?, b3: com.corrodinggames.rts.gameFramework.e ->
            val actionString = b3.k.b

            if(actionString.removePrefix("c_").removePrefix("u_") in bannedUnitList) {
                return@addProxy InterruptResult(Unit)
            }

            if(b3.j == null) return@addProxy Unit
            val realAction = GameCommandActions.from(b3.j.a.ordinal)
            val u = b3.j.b
            if(realAction == GameCommandActions.BUILD && u is com.corrodinggames.rts.game.units.el) {
                if(u.i() in bannedUnitList) {
                    InterruptResult(Unit)
                } else Unit
            } else Unit
        }
    }
}