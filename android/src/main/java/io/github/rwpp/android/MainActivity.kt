/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.corrodinggames.rts.appFramework.d
import io.github.rwpp.App
import io.github.rwpp.LocalController
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.ReturnMainMenuEvent
import io.github.rwpp.i18n.parseI18n
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            isGaming = false
            isSandboxGame = false
            if(!isReturnToBattleRoom) { ReturnMainMenuEvent().broadCastIn() }
            isReturnToBattleRoom = false
        }


        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        instance = this

        Log.i("RWPP", "check permission: ${checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED}")

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }

        requestPermissions(permissions, 1)

        runBlocking { parseI18n() }
        controller.readAllConfig()

        if(d.b(this, true, true)) {
            gameView = d.b(this)
        }

        setContent {
            CompositionLocalProvider(
                LocalController provides controller
            ) {
                val view = LocalView.current
                val window = (view.context as Activity).window
                WindowCompat.getInsetsController(window, view).hide(
                    WindowInsetsCompat.Type.statusBars() or
                            WindowInsetsCompat.Type.navigationBars()
                )

                App()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.saveAllConfig()
    }

    override fun onPause() {
        super.onPause()
        if(gameView != null) GameEngine.t()?.b(gameView)
    }

    override fun onResume() {
        super.onResume()
        activityResume()
    }

    override fun onStop() {
        super.onStop()
        controller.saveAllConfig()
        if(gameView != null) GameEngine.t()?.b(gameView)
    }



    companion object {
        var gameView: com.corrodinggames.rts.appFramework.ab? = null
        lateinit var instance: MainActivity

        fun activityResume() {
            GameEngine.t()?.let {
                gameView = d.a(instance, gameView)
                it.a(instance, gameView, true)
            }

            d.a(instance, true)
            com.corrodinggames.rts.gameFramework.h.a.c()
        }
    }
}