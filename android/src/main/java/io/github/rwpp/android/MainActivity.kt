/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import io.github.rwpp.App
import io.github.rwpp.ContextController
import io.github.rwpp.LocalController
import io.github.rwpp.android.impl.GameContextControllerImpl
import io.github.rwpp.android.impl.doProxy
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.ReturnMainMenuEvent
import io.github.rwpp.game.units.GameInternalUnits
import kotlin.system.exitProcess


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            isGaming = false
            isSandboxGame = false
            if(!isReturnToBattleRoom) ReturnMainMenuEvent().broadCastIn()
            isReturnToBattleRoom = false
        }
        instance = this

        Log.i("RWPP", "check permission: ${checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED}")

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        doProxy()

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

        setContent {
            CompositionLocalProvider(
                LocalController provides GameContextControllerImpl { exitProcess(0) }.also {
                    controller = it
                }
            ) {
                App()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.saveAllConfig()
    }

    override fun onStop() {
        super.onStop()
        controller.saveAllConfig()
    }

    companion object {
        lateinit var controller: ContextController
        lateinit var instance: MainActivity
        lateinit var gameLauncher: ActivityResultLauncher<Intent>
        var isSandboxGame: Boolean = false
        var isGaming = false
        var isReturnToBattleRoom = false
        var bannedUnitList: List<GameInternalUnits> = listOf()
    }
}