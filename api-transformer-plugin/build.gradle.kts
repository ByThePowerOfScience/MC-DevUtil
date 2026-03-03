import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	`kotlin-dsl`
	kotlin("jvm") version "2.1.21"
	id("com.gradle.plugin-publish") version "1.2.1"
}

group = "btpos.mcmods.devutil.gradle"
version = properties["version"] as String

repositories {
	mavenLocal()
	mavenCentral()
	gradlePluginPortal()
	maven(url="https://maven.fabricmc.net/")
	maven(url="https://maven.architectury.dev/")
	maven(url="https://files.minecraftforge.net/maven/")
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

dependencies {
	api("btpos.gradle.mcmods.multiplatform.postprocessing:btpos.gradle.mcmods.multiplatform.postprocessing.gradle.plugin:1.0-SNAPSHOT")
}

gradlePlugin {
	plugins {
		create("devutilTransformer") {
			id = "btpos.mcmods.devutil.transformer"
			implementationClass = "btpos.mcmods.devutil.gradle.plugin.APITransformerPlugin"
		}
	}
}