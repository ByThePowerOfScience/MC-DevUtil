pluginManagement {
	repositories {
		maven(url="https://maven.fabricmc.net/")
		maven(url="https://maven.architectury.dev/")
		maven(url="https://files.minecraftforge.net/maven/")
		gradlePluginPortal()
		mavenLocal()
	}
}


rootProject.name = "devutil"

include("common", "fabric", "neoforge")
includeBuild("api-transformer-plugin")
includeBuild("testProject")