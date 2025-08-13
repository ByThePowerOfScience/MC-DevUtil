import btpos.gradle.architecturyextended.transformersonly.ArchCustomTransformers

plugins {
	id("com.github.johnrengelman.shadow")
//	id("btpos.gradle.architecturyextended.platform") version "1.0.0-SNAPSHOT"
}

architectury {
	platformSetupLoomIde()
	fabric()
	
//	platformExt {
//		platform.set(objects.named<MCPlatform>(MCPlatform.FABRIC))
//	}
}

// the below is straight from the template
configurations {
	val common by configurations.creating {
		isCanBeResolved = true
		isCanBeConsumed = false
	}
	
	
	//compileClasspath.get().extendsFrom(common)
	runtimeClasspath.get().extendsFrom(common)
	getByName("developmentFabric").extendsFrom(common) // This is a hardcoded requirement of the transformer toolchain. No, I don't know why there are two configurations for this.
	
	testCompileClasspath.get().extendsFrom(common)
	testRuntimeClasspath.get().extendsFrom(common)
	// Files in this configuration will be bundled into your mod using the Shadow plugin.
	// Don"t use the `shadow` configuration from the plugin itself as it"s meant for excluding files.
	create("shadowBundle") {
		isCanBeResolved = true
		isCanBeConsumed = false
	}
}

dependencies {
	modImplementation ("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"]}")
	modImplementation ("net.fabricmc.fabric-api:fabric-api:${rootProject.properties["fabric_api_version"]}")
	modImplementation("dev.architectury:architectury-fabric:${rootProject.properties["architectury_api_version"]}")
	modImplementation("net.fabricmc:fabric-language-kotlin:1.13.3+kotlin.2.1.21")
	
	// This was originally in the template as `common(project(path=":common", configuration="namedElements"))`,
	// but I needed my own transformers to exist in the dev runs, so I changed it to variant-aware.
	
	// compile against the source stuff
	compileOnly(project(path=":common", configuration="namedElements")) {
		isTransitive = false
	}
	// run with the dev-transformed stuff
	"common"(project(path=":common", configuration=ArchCustomTransformers.getDevConfigName("fabric"))) {
		isTransitive = false
	}
	
	// shadow the prod stuff
	"shadowBundle"(project(path= ":common", configuration= "transformProductionFabric"))
}

tasks.processResources {
	val replaceMap = mapOf(
			"version" to rootProject.version,
			"mod_id" to rootProject.property("mod_id"),
			"mod_name" to rootProject.property("mod_name"),
			"mod_desc" to rootProject.property("mod_desc")
	)
	inputs.properties(replaceMap)
	
	filesMatching("fabric.mod.json") {
		expand(replaceMap)
	}
}

tasks.shadowJar {
	configurations = listOf(project.configurations.getByName("shadowBundle"))
	archiveClassifier = "dev-shadow"
}

tasks.remapJar {
	inputFile.set (tasks.shadowJar.get().archiveFile)
}
