/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.app.Activity
import android.content.Context
import com.corrodinggames.rts.appFramework.d
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import io.github.rwpp.app.PermissionHelper
import org.koin.core.annotation.Single
import org.koin.core.component.get

@Single
class PermissionHelperImpl : PermissionHelper {
    @Deprecated("Useless")
    override fun requestExternalStoragePermission() {
        d.c(get<Context>() as Activity)
    }

    override fun requestManageFilePermission(callback: (Boolean) -> Unit) {
        if (hasManageFilePermission()) {
            callback(true)
        } else {
            XXPermissions.with(get<Context>())
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request { _, allGranted -> callback(allGranted) }
        }
    }

    override fun hasManageFilePermission(): Boolean {
        return XXPermissions.isGranted(get(), Permission.MANAGE_EXTERNAL_STORAGE)
    }
}