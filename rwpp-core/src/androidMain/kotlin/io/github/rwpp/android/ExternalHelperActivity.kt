/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import io.github.rwpp.app.PermissionHelper
import io.github.rwpp.appKoin
import io.github.rwpp.extensionPath
import io.github.rwpp.i18n.I18nType
import io.github.rwpp.i18n.readI18n
import io.github.rwpp.ui.UI
import org.koin.core.error.DefinitionOverrideException
import java.io.File

class ExternalHelperActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            appKoin.declare(this, secondaryTypes = listOf(Context::class), allowOverride = false)
        } catch (_: DefinitionOverrideException) {}

        val dataString = intent.dataString

        lateinit var endsAction: () -> Unit

        if (dataString != null) {
            val fileName = getFileName(intent.data!!)!!

            val path = if (dataString.endsWith(".rwres") || dataString.endsWith(".rwext")) {
                endsAction = {
                    UI.showExtensionView = true
                    UI.showWarning(
                        readI18n("android.importExtension", I18nType.RWPP, fileName)
                    )
                }
                extensionPath
            } else if (dataString.endsWith(".rwmod")) {
                endsAction = {
                    UI.showModsView = true
                    UI.showWarning(
                        readI18n("android.importMod", I18nType.RWPP, fileName)
                    )
                }
                "/storage/emulated/0/rustedWarfare/units/"
            } else if (dataString.endsWith(".rwsave")) {
                endsAction = {
                    UI.showWarning(
                        readI18n("android.importSave", I18nType.RWPP, fileName)
                    )
                }
                "/storage/emulated/0/rustedWarfare/saves/"
            } else if (dataString.endsWith(".reply")) {
                endsAction = {
                    UI.showWarning(
                        readI18n("android.importReply", I18nType.RWPP, fileName)
                    )
                }
                "/storage/emulated/0/rustedWarfare/replays/"
            } else if (dataString.endsWith(".tmx")) {
                endsAction = {
                    UI.showWarning(
                        readI18n("android.importMap", I18nType.RWPP, fileName)
                    )
                }
                "/storage/emulated/0/rustedWarfare/maps/"
            } else {
                null
            }

            if (path != null) {
                appKoin.get<PermissionHelper>().requestManageFilePermission {
                    contentResolver.openInputStream(intent.data!!).use {
                        val file = File(path, fileName)
                        if (!file.exists()) {
                            file.parentFile?.mkdirs()
                            file.createNewFile()
                        }

                        file.writeBytes(it!!.readBytes())
                    }
                }

                endsAction()
            }
        }

        if (gameLoaded) {
            finish()
        } else {
            startActivityForResult(Intent(this, LoadingScreen::class.java), 0)
        }
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.lastPathSegment
        }
        return result
    }

}