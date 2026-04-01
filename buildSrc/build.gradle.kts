plugins {
    `kotlin-dsl`
    kotlin("jvm") version libs.versions.kotlin
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

fun pluginDep(id: String, version: String): String {
    return "$id:$id.gradle.plugin:$version"
}

dependencies {
    implementation(libs.kotlin.reflect.get())
    
    implementation(pluginDep("org.jetbrains.kotlin.jvm", libs.versions.kotlin.get()))
    
    implementation(pluginDep("btpos.gradle.mcmods.multiplatform.base", "1.0-SNAPSHOT"))
    implementation(pluginDep("btpos.gradle.mcmods.multiplatform.postprocessing", "1.0-SNAPSHOT"))
    implementation(pluginDep("btpos.plugins.kt.varinterfacedelegation", "0.1.0-SNAPSHOT"))
    
    implementation(pluginDep("com.github.gmazzo.buildconfig", "6.0.9"))
}