/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.app.Application
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.android.LogcatAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import io.github.rwpp.android.impl.GameSoundPoolImpl
import io.github.rwpp.appKoin
import io.github.rwpp.config.ConfigModule
import io.github.rwpp.game.audio.GameSoundPool
import io.github.rwpp.koinInit
import io.github.rwpp.logger
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import org.slf4j.LoggerFactory


class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        koinApplication = startKoin {
            androidLogger()
            modules(ConfigModule().module, AndroidModule().module)
        }

        koinInit = true
        appKoin = koinApplication.koin

        appKoin.declare(GameSoundPoolImpl(), secondaryTypes = listOf(GameSoundPool::class))

        val lc = LoggerFactory.getILoggerFactory() as LoggerContext
        lc.stop()

        // setup FileAppender
        val encoder1 = PatternLayoutEncoder()
        encoder1.context = lc
        encoder1.pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
        encoder1.start()

        val fileAppender = FileAppender<ILoggingEvent>()
        fileAppender.isAppend = false
        fileAppender.context = lc
        fileAppender.file = "/storage/emulated/0/rustedWarfare/rwpp-log.txt"
        fileAppender.encoder = encoder1
        fileAppender.start()


        // setup LogcatAppender
        val encoder2 = PatternLayoutEncoder()
        encoder2.context = lc
        encoder2.pattern = "[%thread] %msg%n"
        encoder2.start()

        val logcatAppender = LogcatAppender()
        logcatAppender.context = lc
        logcatAppender.encoder = encoder2
        logcatAppender.start()

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        root.addAppender(fileAppender)
        root.addAppender(logcatAppender)

        logger = LoggerFactory.getLogger(packageName)
    }
}