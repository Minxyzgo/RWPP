/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.ViewGroup
import io.github.rwpp.appKoin
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.logger


@SuppressLint("StaticFieldLeak")
@Volatile
private var context: Context? = null

fun setupActivity(activity: Activity): Boolean {
    logger.info("=== Activity setup: " + activity::class.simpleName + " ===")
    initContext(activity)
    activity.volumeControlStream = 3
    activity.requestWindowFeature(1)
    activity.window.addFlags(1024)

    if (Build.VERSION.SDK_INT >= 28
        && appKoin.get<ConfigIO>().getGameConfig("displayOverCutout")) {
        try {
            val attributes = activity.window.attributes
            attributes::class.java.getField("layoutInDisplayCutoutMode").setInt(attributes, 1)
        } catch (e: Exception) {
            logger.error("Failed to set layoutInDisplayCutoutMode", e as Throwable)
        }
    }
    return true
}

fun buildGameView(activity: Activity): IView {
    val view = GameView(activity, null)
    val viewGroup = activity.window.decorView.rootView as ViewGroup
    viewGroup.addView(view, 0, ViewGroup.LayoutParams(-1, -1))
    return view
}


fun initContext(activity: Activity) {
    context = context ?: activity.applicationContext
}