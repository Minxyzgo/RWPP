/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.rwpp.AppContext
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.app.PermissionHelper
import io.github.rwpp.appKoin
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.GameLoadedEvent
import io.github.rwpp.widget.ConstraintWindowManager
import io.github.rwpp.widget.MenuLoadingView
import io.github.rwpp.widget.RWSelectionColors
import io.github.rwpp.widget.v2.TitleBrush
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import java.io.File
import kotlin.system.exitProcess

class LoadingScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        appKoin.declare(this, secondaryTypes = listOf(Context::class))

        setContent {
            KoinContext {
                val brush = TitleBrush()

                MaterialTheme(
                    typography = typography,
                    colorScheme = lightColorScheme()
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush),
                    ) {
                        CompositionLocalProvider(
                            LocalTextSelectionColors provides RWSelectionColors,
                            LocalWindowManager provides ConstraintWindowManager(maxWidth, maxHeight)
                        ) {
                            val permissionHelper = koinInject<PermissionHelper>()
                            var hasPermission by remember { mutableStateOf(permissionHelper.hasManageFilePermission()) }

                            if (!hasPermission) {
                                LaunchedEffect(Unit) {
                                    Toast.makeText(
                                        appKoin.get(),
                                        "RWPP需要管理文件权限才能正常运行！",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    permissionHelper.requestManageFilePermission {
                                        if (it) hasPermission = true
                                        else {
                                            exitProcess(0)
                                        }
                                    }
                                }
                            } else {
                                var message by remember { mutableStateOf("loading") }

                                MenuLoadingView(message)

                                LaunchedEffect(Unit) {
                                    appKoin.get<AppContext>().init()

                                    withContext(Dispatchers.IO) {
                                        File("/storage/emulated/0/rustedWarfare/maps/")
                                            .walk()
                                            .filter { it.name.startsWith("generated_") }
                                            .forEach {
                                                it.delete()
                                            }

                                        val job = launch {
                                            while (true) {
                                                val msg = GameEngine.t()?.dF
                                                message = (if (msg.isNullOrBlank()) "loading..." else msg)
                                            }
                                        }

                                        async { GameEngine.c(this@LoadingScreen) }.await()

                                        job.cancel()

                                        gameLoaded = true
                                        GameLoadedEvent().broadcastIn()
                                        startActivityForResult(Intent(this@LoadingScreen, MainActivity::class.java), 0)
                                        finish()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}