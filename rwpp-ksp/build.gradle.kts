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