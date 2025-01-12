/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("com.github.gmazzo.buildconfig")
}

group = "io.github.rwpp"

buildConfig {
    buildConfigField("DEFAULT_LIB_DIR", rootDir.absolutePath.replace("\\", "/") + "/lib")
}

kotlin {

    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("org.javassist:javassist:3.30.2-GA")
                implementation("com.google.devtools.ksp:symbol-processing-api:1.9.24-1.0.20")
                api(project(":rwpp-core-utils"))
                //compileOnly(project(":desktop"))
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}