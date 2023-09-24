/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.corrodinggames.rts.appFramework.ClosingActivity
import com.corrodinggames.rts.appFramework.MultiplayerBattleroomActivity
import com.corrodinggames.rts.gameFramework.j.ae
import com.corrodinggames.rts.gameFramework.k
import com.github.minxyzgo.rwij.setFunction
import io.github.rwpp.R
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.RefreshUIEvent

//internal lateinit var mainMenuActivityInstance: MainMenuActivity
fun doProxy() {
//    Game::class.setFunction {
//        addProxy("n") { _: Game ->
//            "v1.15 Crazy Thursday V Me 50"
//        }
//    }

//    MainMenuActivity::class.setFunction {
//        val gameViewField = MainMenuActivity::class.java.getDeclaredField("gameView").apply { isAccessible = true }
//        addProxy("onCreate", android.os.Bundle::class) { activity: MainMenuActivity, _ : Bundle? ->
//            mainMenuActivityInstance = activity
//            val method: Method? =
//                IntroScreen::class.java.getMethod("overridePendingTransition", Integer.TYPE, Integer.TYPE)
//            if(method != null) {
//                try {
//                    method.invoke(this, Integer.valueOf(R.anim.mainfadein), Integer.valueOf(R.anim.splashfadeout))
//                } catch(e: Exception) {
//                }
//            }
//
//            if(d.b(activity, true)) {
//                activity.setContentView(
//                    ComposeView(activity).apply {
//                        setContent {
//                            CompositionLocalProvider(
//                                LocalController provides GameContextControllerImpl { exitProcess(0) }
//                            ) {
//                                App()
//                            }
//                        }
//                    }
//                )
//
//                gameViewField.set(activity, d.b(activity))
//                SettingsActivity.askAboutLastDebugOption()
//            }
//        }
//
//        addProxy("onResume") { activity: MainMenuActivity ->
//            val t: k? = GameEngine.t()
//            if(t != null) {
//                gameViewField.set(activity, d.a(activity, gameViewField.get(activity) as ab))
//                t.a(activity, gameViewField.get(activity) as ab, true)
//            }
//            d.a(activity, true)
//            a.c()
//        }
//    }

    MultiplayerBattleroomActivity::class.setFunction {
        addProxy(MultiplayerBattleroomActivity::updateUI) {
            RefreshUIEvent().broadCastIn()
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
                        ), 2
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
                            ), 2
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
    }
}