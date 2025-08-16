
plugins {
    id("groovy-gradle-plugin")
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:${libs.versions.kotlin.get()}")
    implementation("btpos.gradle.architecturyextended.base:btpos.gradle.architecturyextended.base.gradle.plugin:1.0.0-SNAPSHOT")
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    // val libs = the<LibrariesForLibs>()
    implementation(libs.kotlin.reflect.get())
    implementation("com.github.johnrengelman.shadow:com.github.johnrengelman.shadow.gradle.plugin:8.1.1")
    implementation("btpos.gradle.multiloader.commontransformers:btpos.gradle.multiloader.commontransformers.gradle.plugin:1.0-SNAPSHOT")
}