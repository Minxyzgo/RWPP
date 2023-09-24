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
            .with("updateUI"))
        setProxy(com.github.minxyzgo.rwij.Libs.`android-game-lib`, "com.corrodinggames.rts.game.i".with("n"))
        setProxy(com.github.minxyzgo.rwij.Libs.`android-game-lib`, "com.corrodinggames.rts.gameFramework.j.ae".with("X", "d(Ljava/lang/String;Ljava/lang/String;)"))
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
            "com.corrodinggames.librocket.scripts.Root".with("showMainMenu", "showBattleroom", "receiveChatMessage", "makeSendMessagePopup", "makeSendTeamMessagePopupWithDefaultText"),
            "com.corrodinggames.rts.java.Main".with("c()", "b()"),
            "com.corrodinggames.rts.java.b.a".with("p")
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
                //api("com.github.nanihadesuka:LazyColumnScrollbar:1.7.2")
                //api("com.google.code.gson:gson:2.10.1")
                //api("io.github.oleksandrbalan:modalsheet:0.5.0")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.6.1")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.9.0")
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
    namespace = "io.github.rwpp.android"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
}

dependencies {
    val rwijVersion = findProperty("rwij.version") as String
    commonMainApi("com.github.minxyzgo.rw-injection:core:$rwijVersion")
}