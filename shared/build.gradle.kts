/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

@file:OptIn(LibRequiredApi::class)

import com.github.minxyzgo.rwij.LibRequiredApi
import com.github.minxyzgo.rwij.Libs
import com.github.minxyzgo.rwij.ProxyFactory.with
import com.github.minxyzgo.rwij.injectionMultiplatform
import javassist.ClassMap
import javassist.CtClass
import javassist.Modifier

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.compose")
}

buildscript {
    dependencies {
        val rwijVersion = findProperty("rwij.version") as String
        classpath("com.github.minxyzgo.rw-injection:com.github.minxyzgo.rwij.gradle.plugin:$rwijVersion")
    }

    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}

apply<com.github.minxyzgo.rwij.GradlePlugin>()

injectionMultiplatform {
    enable = true
    com.github.minxyzgo.rwij.Libs.Companion.includes.add(com.github.minxyzgo.rwij.Libs.`android-game-lib`)
    com.github.minxyzgo.rwij.Builder.releaseLibActions[com.github.minxyzgo.rwij.Libs.`android-game-lib`] = { _, fi, _ ->
        if(!fi.exists()) {
            File(com.github.minxyzgo.rwij.Builder.libDir).mkdirs()
            fi.createNewFile()
        }
        fi.writeBytes(File(projectDir.absolutePath + "/android-game-lib-template.jar").readBytes())
    }
    android {
        setProxy(com.github.minxyzgo.rwij.Libs.`android-game-lib`, "com.corrodinggames.rts.appFramework.MultiplayerBattleroomActivity"
            .with("updateUI", "askPasswordInternal", "refreshChatLog", "addMessageToChatLog", "startGame"))
        setProxy(com.github.minxyzgo.rwij.Libs.`android-game-lib`, "com.corrodinggames.rts.gameFramework.k".with("g(Ljava/lang/String;)", "d(Ljava/lang/String;)"))
        setProxy(com.github.minxyzgo.rwij.Libs.`android-game-lib`, "com.corrodinggames.rts.appFramework.d".with("b(Landroid/app/Activity;)"))
        //setProxy(com.github.minxyzgo.rwij.Libs.`android-game-lib`, "com.corrodinggames.rts.game.i".with("n")) version
        setProxy(com.github.minxyzgo.rwij.Libs.`android-game-lib`, "com.corrodinggames.rts.gameFramework.j.ae".with(
            "X",
            "d(Ljava/lang/String;Ljava/lang/String;)",
            "x()",
            "a(Lcom/corrodinggames/rts/gameFramework/e;)",
            "a(Lcom/corrodinggames/rts/gameFramework/j/bi;)"
        ))
        setProxy(com.github.minxyzgo.rwij.Libs.`android-game-lib`,
            "com.corrodinggames.rts.game.units.custom.l".with(
                "a(Lcom/corrodinggames/rts/game/units/custom/ab;Ljava/util/HashMap;)"
            )
        )
        action {
            Libs.`android-game-lib`.classTree.defPool["com.corrodinggames.rts.gameFramework.j.ae"].apply {
                //make accessible
                getDeclaredMethod("f", arrayOf(CtClass.intType)).let {
                    it.modifiers = javassist.Modifier.setPublic(it.modifiers)
                }

                declaredMethods.filter { it.name == "a" }.forEach {
                    it.modifiers = javassist.Modifier.setPublic(it.modifiers)
                }

                getDeclaredField("bD").let {
                    it.modifiers = javassist.Modifier.setPublic(it.modifiers)
                }
            }

            val tag = listOf("anim", "array", "attr", "color", "drawable", "id", "layout", "raw", "string", "style", "styleable", "xml")
            val classMap = ClassMap().apply {
                put("com.corrodinggames.rts.R", "io.github.rwpp.R")
                tag.forEach {
                    put("com.corrodinggames.rts.R\$$it", "io.github.rwpp.R\$$it")
                }
            }
            com.github.minxyzgo.rwij.Libs.`android-game-lib`.classTree.allClasses.forEach {
                it.replaceClassName(classMap)
            }
        }
    }
    jvm {
        target = "desktopMain"
        setProxy(Libs.`game-lib`,
            "com.corrodinggames.librocket.scripts.Root".with(
                "showMainMenu", "showBattleroom", "receiveChatMessage", "makeSendMessagePopup", "makeSendTeamMessagePopupWithDefaultText"
            ),
            "com.corrodinggames.rts.java.Main".with("c", "b()"),
            "com.corrodinggames.rts.java.b.a".with("p"),
            "com.corrodinggames.rts.game.units.custom.l".with(
                "a(Lcom/corrodinggames/rts/game/units/custom/ab;Ljava/util/HashMap;)"
            ),
            "com.corrodinggames.rts.gameFramework.j.ad".with(
                "a(Lcom/corrodinggames/rts/gameFramework/e;)",
                "c(Lcom/corrodinggames/rts/gameFramework/j/au;)",
                "g(Lcom/corrodinggames/rts/gameFramework/j/c;)"
            )
        )
        action {
            // set all members of Main to public
            com.github.minxyzgo.rwij.Libs.`game-lib`.classTree.defPool["com.corrodinggames.rts.java.Main"].apply {
                this.declaredMethods.forEach {
                    it.modifiers = Modifier.setPublic(it.modifiers)
                }

                this.declaredFields.forEach {
                    it.modifiers = Modifier.setPublic(it.modifiers)
                }
            }
        }
    }
}

kotlin {
    androidTarget()

    jvm("desktop")

    sourceSets {
        val commonMain by getting {

            dependencies {
                api(compose.preview)
                api(compose.runtime)
                api(compose.foundation)
                //api(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.components.resources)
                api(compose.ui)
                api(compose.material3)
                api("com.halilibo.compose-richtext:richtext-ui:0.17.0")
                api("com.squareup.okhttp3:okhttp:4.11.0")
                api("net.peanuuutz.tomlkt:tomlkt:0.3.3")
                //api("com.github.nanihadesuka:LazyColumnScrollbar:1.7.2")
                //api("com.google.code.gson:gson:2.10.1")
                //api("io.github.oleksandrbalan:modalsheet:0.5.0")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.7.2")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.12.0")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    buildToolsVersion = "34.0.0"
    namespace = "io.github.rwpp.android"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    val rwijVersion = findProperty("rwij.version") as String
    commonMainApi("com.github.minxyzgo.rw-injection:core:$rwijVersion")
}