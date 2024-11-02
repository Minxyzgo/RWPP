plugins {
    kotlin("jvm")
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect:${findProperty("kotlin.version")}")
}