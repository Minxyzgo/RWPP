package io.github.rwpp.android

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import com.corrodinggames.rts.appFramework.*
import com.corrodinggames.rts.gameFramework.h.a
import com.corrodinggames.rts.gameFramework.j.ae
import com.corrodinggames.rts.gameFramework.k
import com.github.minxyzgo.rwij.setFunction
import io.github.rwpp.App
import io.github.rwpp.LocalController
import io.github.rwpp.R
import io.github.rwpp.android.impl.GameContextControllerImpl
import io.github.rwpp.android.impl.KClass
import java.lang.reflect.Method
import kotlin.system.exitProcess


fun doProxy() {
//    Game::class.setFunction {
//        addProxy("n") { _: Game ->
//            "v1.15 Crazy Thursday V Me 50"
//        }
//    }

    MainMenuActivity::class.setFunction {
        val gameViewField = MainMenuActivity::class.java.getDeclaredField("ab").apply { isAccessible = true }
        addProxy("onCreate") { activity: MainMenuActivity, _ : Bundle ->
            val method: Method? =
                IntroScreen::class.java.getMethod("overridePendingTransition", Integer.TYPE, Integer.TYPE)
            if(method != null) {
                try {
                    method.invoke(this, Integer.valueOf(R.anim.mainfadein), Integer.valueOf(R.anim.splashfadeout))
                } catch(e: Exception) {
                }
            }

            if(d.b(activity, true)) {
                activity.setContentView(
                    ComposeView(activity).apply {
                        setContent {
                            CompositionLocalProvider(
                                LocalController provides GameContextControllerImpl { exitProcess(0) }
                            ) {
                                App()
                            }
                        }
                    }
                )

                gameViewField.set(activity, d.b(activity))
                SettingsActivity.askAboutLastDebugOption()
            }
        }

        addProxy("onResume") { activity: MainMenuActivity ->
            val t: k? = KClass.t()
            if(t != null) {
                gameViewField.set(activity, d.a(activity, gameViewField.get(activity) as ab))
                t.a(activity, gameViewField.get(activity) as ab, true)
            }
            d.a(activity, true)
            a.c()
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