import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("groovy-gradle-plugin")
    `kotlin-dsl`
    kotlin("jvm") version libs.versions.kotlin
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:${libs.versions.kotlin.get()}")
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    // val libs = the<LibrariesForLibs>()
    implementation(libs.kotlin.reflect.get())
    implementation("btpos.gradle.mcmods.multiplatform.base:btpos.gradle.mcmods.multiplatform.base.gradle.plugin:1.0-SNAPSHOT")
    implementation("btpos.gradle.mcmods.multiplatform.postprocessing:btpos.gradle.mcmods.multiplatform.postprocessing.gradle.plugin:1.0-SNAPSHOT")
}

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}