pluginManagement {
	repositories {
		maven(url="https://maven.fabricmc.net/")
		maven(url="https://maven.architectury.dev/")
		maven(url="https://files.minecraftforge.net/maven/")
		gradlePluginPortal()
		mavenLocal()
	}
	includeBuild("../api-transformer-plugin")
}

include("common", "fabric", "neoforge")

