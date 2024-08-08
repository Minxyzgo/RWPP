/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

sourceSets.main.get().resources.srcDir(rootDir.absolutePath + "/shared/src/commonMain/resources")

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":shared"))
    compileOnly(fileTree(
        "dir" to project(":shared").dependencyProject.projectDir.absolutePath + "/build/generated/lib",
        "include" to "*.jar",
        "exclude" to "android-game-lib.jar"
    ))
    implementation(fileTree(
        "dir" to project(":shared").dependencyProject.projectDir.absolutePath + "/build/generated/lib",
        "include" to "game-lib.jar",
    ))
}

compose.desktop {
    application {
        mainClass = "io.github.rwpp.desktop.MainKt"
        buildTypes {
            release {
                proguard.isEnabled.set(false)
            }
        }

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Dmg, TargetFormat.Deb)
            packageName = "Rwpp"
            mainClass = "io.github.rwpp.desktop.MainKt"
            packageVersion = rootProject.version.toString()
           // copyright = "Copyright 2023-2024 RWPP contributors"
           // licenseFile.set(rootProject.file("LICENSE"))
            jvmArgs += listOf(
                "-Djava.net.preferIPv4Stack=true",
                "-Xmx2000M",
                "-Dfile.encoding=UTF-8",
                "-Djava.library.path=.",
                "--add-opens=java.base/java.net=ALL-UNNAMED"
            )

            args += listOf("-native")
        }
    }
}



