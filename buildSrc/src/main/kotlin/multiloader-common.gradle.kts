@file:Suppress("UnstableApiUsage")

import btpos.gradle.mcmods.multiplatform.base.attributes.MCPlatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.utils.extendsFrom


plugins {
	id("java-library")
	id("maven-publish")
	id("kotlin-configure")
	id("btpos.gradle.mcmods.multiplatform.base")
	idea
}

base {
	archivesName = "${mod_id}-${project.name}-${minecraft_version}"
}

java {
	toolchain.languageVersion = JavaLanguageVersion.of(java_version.toInt())
	withSourcesJar()
//	withJavadocJar() // kotlin project, uses kdoc not javadoc sadly
}

repositories {
	mavenCentral()
	// https://docs.gradle.org/current/userguide/declaring_repositories.html#declaring_content_exclusively_found_in_one_repository
	exclusiveContent {
		forRepository {
			maven {
				name = "Sponge"
				url = uri("https://repo.spongepowered.org/repository/maven-public")
			}
		}
		filter { includeGroupAndSubgroups("org.spongepowered") }
	}
	exclusiveContent {
		forRepositories(
			maven {
				name = "ParchmentMC"
				url = uri("https://maven.parchmentmc.org/")
			},
			maven {
				name = "NeoForge"
				url = uri("https://maven.neoforged.net/releases")
			}
		)
		filter { includeGroup("org.parchmentmc.data") }
	}
	mavenLocal()
	exclusiveContent {
		forRepository {
			maven {
				url = uri("https://maven.teamresourceful.com/repository/maven-public/")
				name = "Common Storage Lib"
			}
		}
		filter {
//			includeGroupAndSubgroups("earth.terrarium.common_storage_lib")
			includeGroupAndSubgroups("com.terraformersmc")
		}
	}
	exclusiveContent {
		forRepository {
			maven(url="https://maven.bawnorton.com/releases")
		}
		filter {
			includeGroup("com.github.bawnorton.mixinsquared")
		}
	}
	maven {
		name = "BlameJared"
		url = uri("https://maven.blamejared.com")
	}
}



// Declare capabilities on the outgoing configurations.
// Read more about capabilities here: https://docs.gradle.org/current/userguide/component_capabilities.html#sec:declaring-additional-capabilities-for-a-local-component
configurations.named { when (it) {
	"apiElements", "sourcesElements", "javadocElements" -> true
	else -> it.startsWith("runtimeElements")
} }.configureEach {
	outgoing {
		capability("$group:$mod_id-${project.name}:$version")
		capability("$group:${base.archivesName.get()}:$version")
		capability("$group:$mod_id-${project.name}-${minecraft_version}:$version")
		
		attributes {
			attributeProvider(MCPlatform.TARGET_PLATFORM, btposMultiplatform.platform.map { objects.named<MCPlatform>(it) })
		}
	}
	val cfg = this@configureEach
	publishing.publications.configureEach {
		if (this is MavenPublication)
			suppressPomMetadataWarningsFor(cfg.name)
	}
}

tasks.getByName<Jar>("sourcesJar") {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_${mod_name}" }
	}
}

tasks.jar {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_${mod_name}" }
	}
	
	manifest {
		attributes(
			mapOf(
				"Specification-Title" to mod_name,
				"Specification-Vendor" to mod_author,
				"Specification-Version" to project.tasks.jar.get().archiveVersion,
				"Implementation-Title" to project.name,
				"Implementation-Version" to project.tasks.jar.get().archiveVersion,
				"Implementation-Vendor" to mod_author,
				"Built-On-Minecraft" to minecraft_version
			)
		)
	}
}

tasks.processResources {
	val expandProps = mapOf<String, String>(
		"version" to version.toString(),
		"group" to project.group.toString(), //Else we target the task"s group.
		"minecraft_version" to minecraft_version,
		"minecraft_version_range" to minecraft_version_range,
		"fabric_version" to fabric_version,
		"fabric_loader_version" to fabric_loader_version,
		"mod_name" to mod_name,
		"mod_author" to mod_author,
		"mod_id" to mod_id,
		"license" to license,
		"description" to description,
		"neoforge_version" to neoforge_version,
		"neoforge_loader_version_range" to neoforge_loader_version_range,
		"credits" to credits,
		"java_version" to java_version,
//		"devutil_version" to devutil_version
	)
	
	val jsonExpandProps = expandProps.mapValues { entry ->
		entry.value.replace("\n", "\\\\n")
	}
	
	filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
		expand(expandProps)
	}
	
	filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) {
		expand(jsonExpandProps)
	}
	
	inputs.properties(expandProps)
}

publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}
	repositories {
		System.getenv("local_maven_url")?.let {
			maven {
				url = uri(it)
			}
		}
	}
}