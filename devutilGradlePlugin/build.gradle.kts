import btpos.gradle.architectury.transformerplugin.MultiplatformPreTransformer_Fabric
import btpos.gradle.architectury.transformerplugin.MultiplatformPreTransformer_Forge
import btpos.gradle.architectury.transformerplugin.attributes.ModuleType
import btpos.gradle.architectury.transformerplugin.attributes.PlatformType

plugins {
	`kotlin-dsl`
	id("common-platform-transformer")
	kotlin("jvm") version "2.1.21"
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

dependencies {

}