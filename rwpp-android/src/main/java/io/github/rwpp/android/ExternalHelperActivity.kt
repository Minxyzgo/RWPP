/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import io.github.rwpp.app.PermissionHelper
import io.github.rwpp.appKoin
import io.github.rwpp.core.UI
import io.github.rwpp.extensionPath
import java.io.File

class ExternalHelperActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataString = intent.dataString

        if (dataString != null && (dataString.endsWith(".rwres") || dataString.endsWith(".rwext"))) {
            appKoin.get<PermissionHelper>().requestManageFilePermission {
                contentResolver.openInputStream(intent.data!!).use {
                    val file = File(extensionPath, getFileName(intent.data!!)!!)
                    if (!file.exists()) {
                        file.parentFile?.mkdirs()
                        file.createNewFile()
                    }

                    file.writeBytes(it!!.readBytes())
                }
            }
        }

        UI.showExtensionView = true
        UI.updateExtensionWhenVisible = true

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