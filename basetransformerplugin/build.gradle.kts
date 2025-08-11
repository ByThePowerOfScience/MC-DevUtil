
plugins {
	`kotlin-dsl`
	kotlin("jvm") version "2.1.21"
	id("com.gradle.plugin-publish") version "1.2.1"
}

repositories {
	gradlePluginPortal()
	mavenCentral()
	maven(url="https://maven.fabricmc.net/")
	maven(url="https://maven.architectury.dev/")
	maven(url="https://files.minecraftforge.net/maven/")
}

group = "btpos.gradle.architectury"
version = "1.0"

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.1.21")
	implementation("architectury-plugin:architectury-plugin.gradle.plugin:3.4-SNAPSHOT")
	implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.10-SNAPSHOT")
	implementation("dev.architectury:architectury-transformer:5.2.87")
	implementation("org.ow2.asm:asm:9.8")
	implementation("org.ow2.asm:asm-commons:9.8")
	implementation("org.ow2.asm:asm-tree:9.8")
}

gradlePlugin {
	plugins {
		create("archCommonTransformer") {
			id = "common-platform-transformer"
			implementationClass = "btpos.gradle.architectury.transformerplugin.CommonPlatformTransformersPlugin"
		}
	}
}