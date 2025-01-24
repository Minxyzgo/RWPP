/*
 * Copyright 2023-2025 RWPP contributors
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.appKoin
import io.github.rwpp.ui.ConstraintWindowManager
import io.github.rwpp.ui.MenuLoadingView
import io.github.rwpp.ui.RWSelectionColors
import io.github.rwpp.ui.v2.TitleBrush
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.LocalKoinApplication

class LoadingScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setContent {

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
                        LocalKoinApplication provides appKoin,
                        LocalTextSelectionColors provides RWSelectionColors,
                        LocalWindowManager provides ConstraintWindowManager(maxWidth, maxHeight)
                    ) {
                        var message by remember { mutableStateOf("loading") }

                        MenuLoadingView(message)

                        LaunchedEffect(Unit) {
                            withContext(Dispatchers.IO) {
                                launch {
                                    while (GameEngine.t()?.bg != true) {
                                        val msg = GameEngine.t()?.dF
                                        message = (if (msg.isNullOrBlank()) "loading..." else msg)
                                    }
                                }

                                GameEngine.c(this@LoadingScreen)

                                gameLoaded = true
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