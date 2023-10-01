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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import io.github.rwpp.App
import io.github.rwpp.ContextController
import io.github.rwpp.LocalController
import io.github.rwpp.android.impl.GameContextControllerImpl
import kotlin.system.exitProcess


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            isGaming = false
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


//            ActivityCompat.requestPermissions(
//                this,
//                permissions,
//                101
//            )

        requestPermissions(permissions, 1)

        setContent {
            CompositionLocalProvider(
                LocalController provides GameContextControllerImpl { exitProcess(0) }.also {
                    controller = it
                }
            ) {
                App()

//                val launcher = rememberLauncherForActivityResult(
//                    ActivityResultContracts.RequestPermission()
//                ) { isGranted: Boolean ->
//                    if (isGranted) {
//                        // Permission Accepted: Do something
//                        Toast.makeText(this, "permission admitted", Toast.LENGTH_SHORT).show()
//                    } else {
//                        // Permission Denied: Do something
//                    }
//                }

//                LaunchedEffect(Unit) {
//                    if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                        launcher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                    }
//                    launcher.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
//                    launcher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
//                }

            }
        }

//        requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
//
//        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
//                1)
//        }
    }


    companion object {
        lateinit var controller: ContextController
        lateinit var instance: MainActivity
        lateinit var gameLauncher: ActivityResultLauncher<Intent>
        var isGaming = false
    }
}