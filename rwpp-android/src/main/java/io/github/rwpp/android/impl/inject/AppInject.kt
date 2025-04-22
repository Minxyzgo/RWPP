/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl.inject

import android.app.Activity
import android.os.Build
import android.view.ViewGroup
import com.corrodinggames.rts.appFramework.GameView
import com.corrodinggames.rts.appFramework.GameViewOpenGL
import com.corrodinggames.rts.appFramework.GameViewThreaded
import io.github.rwpp.android.impl.RWPPGameViewNonSurface
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode

@InjectClass(com.corrodinggames.rts.appFramework.d::class)
object AppInject {
    @Inject("b", InjectMode.Override, "(Landroid/app/Activity;)Lcom/corrodinggames/rts/appFramework/ab;")
    fun redirectSurfaceView(activity: Activity): com.corrodinggames.rts.appFramework.ab {
        val viewGroup = activity.window.decorView.rootView as ViewGroup
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
        return gameViewThreaded
    }
}