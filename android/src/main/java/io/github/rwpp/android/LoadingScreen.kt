/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import com.corrodinggames.rts.gameFramework.j.at
import io.github.rwpp.LocalController
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.android.impl.GameContextControllerImpl
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.android.impl.doProxy
import io.github.rwpp.ui.ConstraintWindowManager
import io.github.rwpp.ui.LoadingView
import io.github.rwpp.ui.RWSelectionColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.orEmpty
import org.jetbrains.compose.resources.rememberImageBitmap
import org.jetbrains.compose.resources.resource
import kotlin.system.exitProcess

class LoadingScreen : ComponentActivity() {
    @OptIn(ExperimentalResourceApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        doProxy()

        setContent {
            val image = resource("metal.png").rememberImageBitmap()

            val brush = remember(image) {
                ShaderBrush(ImageShader(image.orEmpty(), TileMode.Repeated, TileMode.Repeated))
            }

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
                        LocalController provides GameContextControllerImpl { exitProcess(0) }.also {
                            controller = it
                        },
                        LocalTextSelectionColors provides RWSelectionColors,
                        LocalWindowManager provides ConstraintWindowManager(maxWidth, maxHeight)
                    ) {
                        LoadingView(true, {}) {
                            withContext(Dispatchers.IO) {
                                launch {
                                    while(GameEngine.t()?.bg != true) {
                                        message(GameEngine.t()?.dF ?: "loading...")
                                    }
                                }

                                GameEngine.c(this@LoadingScreen)
                                GameEngine.t().bU.aA.a = at.a
                                GameEngine.t().bU.aB = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
                                GameEngine.t().bU.aA.b = "[z;p10]Crossing Large (10p).tmx"

                                startActivityForResult(Intent(this@LoadingScreen, MainActivity::class.java), 0)

                                finish()

                                true
                            }
                        }
                    }
                }
            }
        }
    }
}