/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.corrodinggames.rts.appFramework.d
import io.github.rwpp.*
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.app.PermissionHelper
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.config.Settings
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.QuitGameEvent
import io.github.rwpp.event.events.ReturnMainMenuEvent
import org.koin.compose.KoinContext
import java.io.File


class MainActivity : ComponentActivity() {
    private val configIO: ConfigIO by appKoin.inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //koinApplication.androidContext(this)
        appKoin.declare(this, secondaryTypes = listOf(Context::class, Activity::class))

        gameLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            isGaming = false
            gameOver = false
            QuitGameEvent().broadcastIn()
            isSinglePlayerGame = false
            if(!isReturnToBattleRoom) {
                ReturnMainMenuEvent().broadcastIn()
            } else {
                GameEngine.t().a(appKoin.get(), gameView)
            }
            isReturnToBattleRoom = false
        }

        fileChooser = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val uri = result.data?.data
            appKoin.get<PermissionHelper>().requestManageFilePermission {
                if (uri != null) {
                    pickFileActions.forEach { it(File(FileHelper.getRealPathFromURI(this, uri))) }
                }
            }

            pickFileActions.clear()
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        Log.i("RWPP", "check permission: ${checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED}")

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

        val settings = appKoin.get<Settings>()
        var backgroundImagePath by mutableStateOf(settings.backgroundImagePath ?: "")

        if(d.b(this, true, true)) {
            gameView = d.b(this)
            activityResume()
        }

        setContent {
            KoinContext(appKoin) {
                val view = LocalView.current
                val window = (view.context as Activity).window
                WindowCompat.getInsetsController(window, view).hide(
                    WindowInsetsCompat.Type.statusBars() or
                            WindowInsetsCompat.Type.navigationBars()
                )

                val isPremium = true

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val painter = remember(backgroundImagePath) {
                        if (backgroundImagePath.isNotBlank() && isPremium && appKoin.get<PermissionHelper>()
                                .hasManageFilePermission()
                        ) {
                            runCatching {
                                BitmapPainter(
                                    BitmapFactory.decodeFile(backgroundImagePath).asImageBitmap()
                                )
                            }.getOrNull()
                        } else {
                            null
                        }
                    }

                    if (backgroundImagePath.isNotBlank() && isPremium && painter != null) {
                        Image(
                            painter = painter,
                            null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }



                    App(isPremium = isPremium) {
                        backgroundImagePath = it
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        configIO.saveAllConfig()
    }

    override fun onPause() {
        super.onPause()
        if(gameView != null) GameEngine.t()?.b(gameView)
    }
//
//    override fun onResume() {
//        super.onResume()
//        activityResume()
//    }

    override fun onStop() {
        super.onStop()
        configIO.saveAllConfig()
        if(gameView != null) GameEngine.t()?.b(gameView)
    }

    companion object {
        var gameView: com.corrodinggames.rts.appFramework.ab? = null

        fun activityResume() {
            GameEngine.t()?.let {
                gameView = d.a(appKoin.get(), gameView)
                it.a(appKoin.get(), gameView, true)
            }

            d.a(appKoin.get(), true)
            com.corrodinggames.rts.gameFramework.h.a.c()
        }
    }
}