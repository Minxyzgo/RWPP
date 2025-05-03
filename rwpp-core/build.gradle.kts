/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

group = "io.github.rwpp"

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.application")
    //id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.github.gmazzo.buildconfig")
}


//        action {
//
//            // set all members of Main to public
//            com.github.minxyzgo.rwij.Libs.`game-lib`.classTree.defPool["com.corrodinggames.rts.java.Main"].apply {
//                this.declaredMethods.forEach {
//                    it.modifiers = Modifier.setPublic(it.modifiers)
//                }
//
//                this.declaredFields.forEach {
//                    it.modifiers = Modifier.setPublic(it.modifiers)
//                }
//
//                // support steam
//                val code = Bytecode(classFile.constPool)
//                code.maxLocals = 2
//                code.addIconst(0)
//                code.addAnewarray("java/lang/String")
//                code.addInvokestatic("io/github/rwpp/desktop/MainKt", "main", "([Ljava/lang/String;)V")
//                code.addReturn(CtClass.voidType)
//                this.getDeclaredMethod("main").methodInfo.codeAttribute = code.toCodeAttribute()
//            }
//        }
//    }
//}


ksp {
    arg("outputDir", project.buildDir.absolutePath + "/generated/libs")
    arg("lib", "android-game-lib")
}

kotlin {
    androidTarget()

    jvm("desktop")

    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }


    sourceSets {
        val commonMain by getting {

            dependencies {
                api(compose.preview)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.components.resources)
                api(compose.ui)
                api("org.jetbrains.compose.material:material-icons-core:1.7.3")
                val koinVersion = findProperty("koin.version") as String
                val koinAnnotationsVersion = findProperty("koin.annotations.version") as String
                val markdownVersion = findProperty("markdown.version") as String
                api("io.insert-koin:koin-core:$koinVersion")
                api("io.insert-koin:koin-compose:$koinVersion")
                api("io.insert-koin:koin-annotations:$koinAnnotationsVersion")
                api("com.squareup.okhttp3:okhttp:4.12.0")
                api("net.peanuuutz.tomlkt:tomlkt:0.3.7")
                api("com.eclipsesource.minimal-json:minimal-json:0.9.5")
                api("com.mikepenz:multiplatform-markdown-renderer:$markdownVersion")
                api("com.mikepenz:multiplatform-markdown-renderer-m3:$markdownVersion")
                api("party.iroiro.luajava:luajava:4.0.2")
                api("party.iroiro.luajava:lua54:4.0.2")
                api("org.slf4j:slf4j-api:2.0.16")
                api("sh.calvin.reorderable:reorderable:2.4.3")
            }
        }

        commonMain.kotlin.srcDirs("build/generated/ksp/main/kotlin")

        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.10.1")
                api("androidx.appcompat:appcompat:1.7.0")
                api("androidx.core:core-ktx:1.16.0")
                implementation("com.github.getActivity:XXPermissions:20.0")
                implementation("com.github.tony19:logback-android:3.0.0")
                compileOnly(fileTree(
                    "dir" to rootDir.absolutePath + "/lib",
                    "include" to "android-game-lib.jar",
                ))
                runtimeOnly(fileTree(
                    "dir" to buildDir.absolutePath + "/generated/libs",
                    "include" to "android-game-lib.jar",
                ))

                val koinVersion = findProperty("koin.version") as String
                implementation("io.insert-koin:koin-android:$koinVersion")
                runtimeOnly("party.iroiro.luajava:android:4.0.2:lua54@aar")
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
    namespace = "io.github.rwpp.android"
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

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    // For KSP
//    applicationVariants.configureEach {
//        val variant: com.android.build.gradle.api.ApplicationVariant = this
//        kotlin.sourceSets {
//            getByName(name) {
//                kotlin.srcDir("build/generated/ksp/${variant.name}/kotlin")
//            }
//        }
//    }

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
}

buildConfig {
    buildConfigField("VERSION", rootProject.version.toString())
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-test-junit:" + findProperty("kotlin.version") as String)
    commonMainApi(project(":rwpp-core-utils"))
    val koinAnnotationsVersion = findProperty("koin.annotations.version") as String
    ksp("io.insert-koin:koin-ksp-compiler:$koinAnnotationsVersion")
    ksp(project(":rwpp-ksp"))
}
