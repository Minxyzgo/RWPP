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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dalvik.system.BaseDexClassLoader
import dalvik.system.PathClassLoader
import io.github.rwpp.AppContext
import io.github.rwpp.LocalWindowManager
import io.github.rwpp.android.impl.GameEngine
import io.github.rwpp.app.PermissionHelper
import io.github.rwpp.appKoin
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.event.broadcastIn
import io.github.rwpp.event.events.GameLoadedEvent
import io.github.rwpp.generatedLibDir
import io.github.rwpp.inject.GameLibraries
import io.github.rwpp.inject.runtime.Builder
import io.github.rwpp.inject.runtime.Builder.logger
import io.github.rwpp.ui.InjectConsole
import io.github.rwpp.ui.defaultBuildLogger
import io.github.rwpp.utils.Reflect
import io.github.rwpp.widget.ConstraintWindowManager
import io.github.rwpp.widget.MenuLoadingView
import io.github.rwpp.widget.RWPPTheme
import io.github.rwpp.widget.RWSelectionColors
import io.github.rwpp.widget.v2.TitleBrush
import javassist.android.DexFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.KoinContext
import java.io.File
import kotlin.system.exitProcess


class LoadingScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        appKoin.declare(this, secondaryTypes = listOf(Context::class))

        appKoin.get<ConfigIO>().readAllConfig()
        Builder.outputDir = generatedLibDir
        Builder.logger = defaultBuildLogger

        val permissionHelper = appKoin.get<PermissionHelper>()
        var hasPermission by mutableStateOf(permissionHelper.hasManageFilePermission())
        val hasPermissionPast = hasPermission
        onInit(hasPermission)

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
                        contentAlignment = Alignment.Center
                    ) {
                        CompositionLocalProvider(
                            LocalTextSelectionColors provides RWSelectionColors,
                            LocalWindowManager provides ConstraintWindowManager(maxWidth, maxHeight)
                        ) {
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
                                if (requireReloadingLib) {
                                    LaunchedEffect(Unit) {
                                        launch(Dispatchers.IO) {
                                            runCatching {
                                                val resource = Thread
                                                    .currentThread()
                                                    .contextClassLoader!!
                                                    .getResourceAsStream("android-game-lib.jar")


                                                val tempJar = File.createTempFile("android-game-lib", ".jar")
                                                tempJar.deleteOnExit()
                                                tempJar.writeBytes(resource.readBytes())
                                                resource.close()

                                                if (!hasPermissionPast) {
                                                    Builder.prepareReloadingLib()
                                                }

                                                GameLibraries.defClassPool.appendDalvikClassPath()
                                                Builder.init(GameLibraries.`android-game-lib`, tempJar)

                                                val libPath = "$generatedLibDir/android-game-lib.jar"
                                                logger?.logging("compiling dex: $libPath")
                                                val dex = DexFile()
                                                dex.addJarFile(libPath)
                                                dex.writeFile("${dexFolder.absolutePath}/classes.dex")
                                                logger?.logging("Successfully compile dex")
                                                logger?.info("Apply config successfully. Now you can restart game to take effect. (已成功应用配置，请重启游戏以生效。)")
                                            }.onFailure {
                                                logger?.error("failed: ${it.stackTraceToString()}")
                                            }
                                        }
                                    }

                                    RWPPTheme(true) {
                                        InjectConsole()
                                    }

                                } else {
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

                                            async {
                                                try {
                                                    val engineImpl = GameEngine.dv.a(this@LoadingScreen)
                                                    Reflect.reifiedSet<GameEngine>(null, "ak", engineImpl)
                                                    loadingThread
                                                    engineImpl.a(this@LoadingScreen as Context)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                                //Reflect.callVoid<GameEngine>(null, "f", listOf(Context::class), listOf(this@LoadingScreen))
                                            }.await()

                                            gameLoaded = true
                                            GameLoadedEvent().broadcastIn()
                                            startActivityForResult(
                                                Intent(this@LoadingScreen, MainActivity::class.java),
                                                0
                                            )
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

    fun onInit(hasPermission: Boolean) {
        if (!init) {
            init = true
            dexFolder = getDir("odex", MODE_PRIVATE)
            requireReloadingLib = !hasPermission || Builder.prepareReloadingLib() || !File("${dexFolder.absolutePath}/classes.dex").exists()

            if (!requireReloadingLib) {
                loadDex(this)
            }
        }
    }
}