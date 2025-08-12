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
	implementation("btpos.gradle.architecturyextended.platform:btpos.gradle.architecturyextended.platform.gradle.plugin:1.0.0-SNAPSHOT")
	implementation("btpos.gradle.architecturyextended:architectury-extended-base:1.0.0-SNAPSHOT")
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