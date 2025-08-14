import btpos.gradle.architecturyextended.common.ArchCommonTransformerPlugin

plugins {
	id("com.github.johnrengelman.shadow")
	id("btpos.gradle.architecturyextended.platform")
}

architectury {
	platformSetupLoomIde()
	neoForge()
}

configurations {
	val common by creating {
		isCanBeResolved = true
		isCanBeConsumed = false
	}
 
//	compileClasspath.get().extendsFrom(common)
	runtimeClasspath.get().extendsFrom(common)
	getByName("developmentNeoForge").extendsFrom(common)
	
	// Files in this configuration will be bundled into your mod using the Shadow plugin.
	// Don"t use the `shadow` configuration from the plugin itself as it"s meant for excluding files.
	create("shadowBundle") {
		isCanBeResolved = true
		isCanBeConsumed = false
	}
}

repositories {
	maven {
		name = "NeoForged"
		url = uri("https://maven.neoforged.net/releases")
	}
	maven {
		name = "Kotlin for Forge"
		url = uri("https://thedarkcolour.github.io/KotlinForForge/")
	}
}



dependencies {
	neoForge("net.neoforged:neoforge:${rootProject.properties["neoforge_version"]}")
	
	modImplementation("dev.architectury:architectury-neoforge:${rootProject.properties["architectury_api_version"]}")
	
	implementation("thedarkcolour:kotlinforforge-neoforge:5.9.0")
	
	testImplementation("net.neoforged:testframework:${rootProject.properties["neoforge_version"]}")
	
	modImplementation("btpos.mcmods.devutil:devutil-neoforge:1.0-SNAPSHOT")
	
	// compile against the source stuff
	compileOnly(project(path=":common", configuration="namedElements")) {
		isTransitive = false
	}
	// run with the dev-transformed stuff
	"common"(project(path=":common", configuration=ArchCommonTransformerPlugin.getDevConfigName("neoforge")))
	// shadow the prod stuff
	"shadowBundle"(project(path= ":common", configuration= "transformProductionNeoForge"))
}



tasks.processResources {
	val replaceMap = mapOf(
			"version" to rootProject.version,
			"mod_id" to rootProject.property("mod_id"),
			"mod_name" to rootProject.property("mod_name"),
			"mod_desc" to rootProject.property("mod_desc")
	)
	inputs.properties(replaceMap)
	
	filesMatching("META-INF/neoforge.mods.toml") {
		expand(replaceMap)
	}
}

loom {
	runs {
		create("clientData") {
			@Suppress("UnstableApiUsage")
			clientData()
			programArgs("--all", "--mod", rootProject.properties["mod_id"] as String)
			programArgs("--output", project.rootProject.file("src/generated").absolutePath)
			programArgs("--existing", project.rootProject.file("src/main/resources").absolutePath)
		}
	}
}

tasks.shadowJar {
	configurations = listOf(project.configurations.getByName("shadowBundle"))
	archiveClassifier = "dev-shadow"
}

tasks.remapJar {
	inputFile.set(tasks.shadowJar.get().archiveFile)
}