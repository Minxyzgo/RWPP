/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.app.Application
import io.github.rwpp.appKoin
import io.github.rwpp.config.ConfigModule
import io.github.rwpp.game.team.TeamModeModule
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        koinApplication = startKoin {
            androidLogger()
            modules(ConfigModule().module, AndroidModule().module, TeamModeModule().module)
        }

        appKoin = koinApplication.koin
    }
}