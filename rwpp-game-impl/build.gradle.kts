plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

ksp {
    arg("outputDir", project.buildDir.absolutePath + "/generated/libs")
    arg("lib", "common-game-lib")
}

dependencies {
    api(project(":rwpp-core"))
    compileOnly(fileTree(
        "dir" to rootDir.absolutePath + "/lib",
        "include" to "*.jar",
        "exclude" to listOf("game-lib.jar", "android-game-lib.jar", "android.jar")
    ))
    runtimeOnly(fileTree(
        "dir" to buildDir.absolutePath + "/generated/libs",
        "include" to "common-game-lib.jar",
    ))

    val koinAnnotationsVersion = findProperty("koin.annotations.version") as String
    ksp("io.insert-koin:koin-ksp-compiler:$koinAnnotationsVersion")
    ksp(project(":rwpp-ksp"))
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}