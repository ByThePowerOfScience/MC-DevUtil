plugins {
	`kotlin-dsl`
	kotlin("jvm") version "2.1.21"
	id("com.gradle.plugin-publish") version "1.2.1"
}

group = "btpos.mcmods.devutil.gradle"
version = "1.0-SNAPSHOT"

repositories {
	mavenLocal()
	mavenCentral()
	gradlePluginPortal()
	maven(url="https://maven.fabricmc.net/")
	maven(url="https://maven.architectury.dev/")
	maven(url="https://files.minecraftforge.net/maven/")
}

dependencies {
	api("btpos.gradle.multiloader.platformtransformers:btpos.gradle.multiloader.platformtransformers.gradle.plugin:1.0-SNAPSHOT")
}

gradlePlugin {
	plugins {
		create("devutilTransformer") {
			id = "btpos.mcmods.devutil.transformer"
			implementationClass = "btpos.mcmods.devutil.gradle.plugin.APITransformerPlugin"
		}
	}
}