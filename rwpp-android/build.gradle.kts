/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

import com.android.build.api.variant.ApplicationVariant
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree.Companion.main

/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

plugins {
    kotlin("android")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

ksp {
    arg("outputDir", project.buildDir.absolutePath + "/generated/libs")
    arg("lib", "android-game-lib")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}



dependencies {
    api(project(":rwpp-game-impl"))
    implementation("com.github.getActivity:XXPermissions:20.0")
    implementation("com.github.tony19:logback-android:3.0.0")
    compileOnly(fileTree(
        "dir" to rootDir.absolutePath + "/lib",
        "include" to "*.jar",
        "exclude" to "game-lib.jar"
    ))
    runtimeOnly(fileTree(
        "dir" to buildDir.absolutePath + "/generated/libs",
        "include" to "android-game-lib.jar",
    ))

    val koinVersion = findProperty("koin.version") as String
    val koinAnnotationsVersion = findProperty("koin.annotations.version") as String
    ksp("io.insert-koin:koin-ksp-compiler:$koinAnnotationsVersion")
    implementation("io.insert-koin:koin-android:$koinVersion")
    ksp(project(":rwpp-ksp"))

    runtimeOnly("party.iroiro.luajava:android:4.0.2:lua54@aar")
}


android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    buildToolsVersion = "34.0.0"
    namespace = "io.github.rwpp"

    useLibrary("org.apache.http.legacy")

    packaging {
        resources.excludes.add("META-INF/*")
    }

    dexOptions {
        javaMaxHeapSize = "2G"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
           // isDebuggable = true
        }
    }

    sourceSets["main"].manifest.srcFile("src/main/AndroidManifest.xml")
    // For KSP
    applicationVariants.configureEach {
        val variant: com.android.build.gradle.api.ApplicationVariant = this
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/${variant.name}/kotlin")
            }
        }
    }

    defaultConfig {
        applicationId = "io.github.rwpp"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 1
        versionName = rootProject.version.toString()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }

    flavorDimensions.add("mode")

    productFlavors {
        create("cn") {
            manifestPlaceholders["app_label"] = "铁锈战争PP版"
            manifestPlaceholders["app_icon"] = "ic_launcher_2" //!
        }

        create("int") {
            manifestPlaceholders["app_label"] = "RWPP"
            manifestPlaceholders["app_icon"] = "ic_launcher_2"
        }
    }
}
