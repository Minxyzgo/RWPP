import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

//buildscript {
//    dependencies {
//        classpath("com.guardsquare:proguard-gradle:7.2.1")
//    }
//}
//java {
//    this.targetCompatibility = JavaVersion.VERSION_1_8
//}
//kotlin {
//    this.compilerOptions {
//        this.jvmTarget.set(JvmTarget.JVM_1_8)
//    }
//}

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

//val obfuscate by tasks.registering(proguard.gradle.ProGuardTask::class)

//fun mapObfuscatedJarFile(file: File) =
    //File("${project.buildDir}/tmp/obfuscated/${file.nameWithoutExtension}.min.jar")
tasks.withType<org.gradle.jvm.tasks.Jar>() {
    //exclude("META-INF/**")
}

compose.desktop {
    application {
        mainClass = "io.github.rwpp.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Rwpp"
            packageVersion = "1.0.0"
        }

//        disableDefaultConfiguration()
//        fromFiles(obfuscate.get().outputs.files.asFileTree)
//        mainJar.set(tasks.jar.map { RegularFile { mapObfuscatedJarFile(it.archiveFile.get().asFile) } })
    }
}
//
//obfuscate.configure {
//    dependsOn(tasks.jar.get())
//
//    val allJars = tasks.jar.get().outputs.files + sourceSets.main.get().runtimeClasspath.filter { it.path.endsWith(".jar") }
//        .filterNot { it.name.startsWith("skiko-awt-") && !it.name.startsWith("skiko-awt-runtime-") } // walkaround https://github.com/JetBrains/compose-jb/issues/1971
//
//    for (file in allJars) {
//        injars(file)
//        outjars(mapObfuscatedJarFile(file))
//    }
//
//    libraryjars("${compose.desktop.application.javaHome ?: System.getProperty("java.home")}/jmods")
//
//    configuration("proguard-rules.pro")
//}
