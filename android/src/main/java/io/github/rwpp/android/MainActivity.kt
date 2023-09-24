/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import io.github.rwpp.App
import io.github.rwpp.LocalController
import io.github.rwpp.android.impl.GameContextControllerImpl
import kotlin.system.exitProcess


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {}
        instance = this

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        doProxy()

        setContent {
            CompositionLocalProvider(
                LocalController provides GameContextControllerImpl { exitProcess(0) }
            ) {
                App()
            }
        }
    }

    companion object {
        lateinit var instance: MainActivity
        lateinit var gameLauncher: ActivityResultLauncher<Intent>
    }
}