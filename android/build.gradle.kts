plugins {
    kotlin("android")
    id("com.android.application")
    id("org.jetbrains.compose")
}


dependencies {
    implementation(project(":shared"))
    implementation(fileTree(
        "dir" to project(":shared").dependencyProject.projectDir.absolutePath + "/build/generated/lib",
        "include" to "android-game-lib.jar",
    ))
}


android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    buildToolsVersion = "34.0.0"
    namespace = "io.github.rwpp"

    useLibrary("org.apache.http.legacy")

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

    dexOptions {
        javaMaxHeapSize = "2G"
    }

    sourceSets["main"].manifest.srcFile("src/main/AndroidManifest.xml")

    defaultConfig {
        applicationId = "io.github.rwpp"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
