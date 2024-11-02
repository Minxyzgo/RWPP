plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}


kotlin {

    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("com.google.devtools.ksp:symbol-processing-api:1.9.24-1.0.20")
                implementation("com.squareup:kotlinpoet-ksp:1.18.1")
                implementation(project(":utils"))
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}