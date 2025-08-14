plugins {
	`kotlin-dsl`
	kotlin("jvm") version "2.1.21"
	id("com.gradle.plugin-publish") version "1.2.1"
}

group = "btpos.mcmods.devutil.gradle"
version = "1.0.0"

repositories {
	mavenLocal()
	mavenCentral()
	gradlePluginPortal()
	maven(url="https://maven.fabricmc.net/")
	maven(url="https://maven.architectury.dev/")
	maven(url="https://files.minecraftforge.net/maven/")
}

dependencies {
	implementation("architectury-plugin:architectury-plugin.gradle.plugin:3.4-SNAPSHOT")
	implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.10-SNAPSHOT")
	implementation("btpos.gradle.architecturyextended.common:btpos.gradle.architecturyextended.common.gradle.plugin:1.0.0-SNAPSHOT")
	implementation("dev.architectury:architectury-transformer:5.2.87")
}

gradlePlugin {
	plugins {
		create("devutilTransformer") {
			id = "btpos.devutil-transformer"
			implementationClass = "btpos.mcmods.devutil.gradle.plugin.APITransformerPlugin"
		}
	}
}