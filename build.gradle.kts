
//import btpos.gradle.architectury.architecturyextensions.attributes.ModuleType
//import btpos.gradle.architectury.architecturyextensions.attributes.ObfType
//import btpos.gradle.architectury.architecturyextensions.attributes.PlatformType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("dev.architectury.loom") version "1.10-SNAPSHOT" apply false
	id("architectury-plugin") version "3.4-SNAPSHOT"
	id("com.github.johnrengelman.shadow") version "8.1.1" apply false
	id("btpos.gradle.architecturyextended.transformersonly") version "1.0.0-SNAPSHOT" apply false
	kotlin("jvm") version "2.1.21"
	`maven-publish`
}

val Project.loom: net.fabricmc.loom.api.LoomGradleExtensionAPI
	get() = this.extensions.getByType()

fun Project.prop(name: String): String {
	return this.properties[name] as String
}

architectury {
	minecraft = project.prop("minecraft_version")
}

allprojects {
	group = rootProject.prop("maven_group")
	version = rootProject.prop("mod_version")
}

val generatedResources = project(":common").file("src/generated")

subprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "dev.architectury.loom")
	apply(plugin = "architectury-plugin")
	apply(plugin = "maven-publish")
	
	base {
		// Set up a suffixed format for the mod jar names, e.g. `example-fabric`.
		archivesName = "${rootProject.prop("mod_id")}-${project.prop("name")}"
	}
	
	repositories {
		// Add repositories to retrieve artifacts from in here.
		// You should only use this when depending on other mods because
		// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
		// See https://docs.gradle.org/current/userguide/declaring_repositories.html
		// for more information about repositories.
		
		maven {
			name = "ParchmentMC"
			url = uri("https://maven.parchmentmc.org")
		}
	}
	
	sourceSets.forEach {
		it.resources.srcDir(generatedResources)
	}
	
	
	dependencies {
		"minecraft"("net.minecraft:minecraft:${rootProject.prop("minecraft_version")}")
		@Suppress("UnstableApiUsage")
		"mappings"(loom.layered {
			officialMojangMappings()
			parchment("org.parchmentmc.data:parchment-1.21.8:2025.07.20@zip")
		})
		
		implementation(rootProject.libs.kotlin.reflect)
		
		testImplementation(kotlin("test"))
		testImplementation("org.hamcrest:hamcrest:3.0")
	}
	
	java {
		// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
		// if it is present.
		// If you remove this line, sources will not be generated.
		withSourcesJar()
		withJavadocJar()
		
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
	
	tasks.withType<JavaCompile>().configureEach {
		options.release = 21
		options.encoding = "UTF-8"
	}
	
	tasks.withType<KotlinCompile> {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_21)
			freeCompilerArgs.add("-Xcontext-receivers")
		}
	}
}
/*
//region Variants, Outputs, and Publishing
fun makeOutputsForPlatform(platformName: String): Triple<Configuration, Configuration, Configuration> {
	fun AttributeContainer.forAllOutgoing() {
		attribute(ModuleType.ATTRIBUTE, objects.named(ModuleType.MAIN))
		attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
	}
	
	fun AttributeContainer.sources() {
		attribute(ObfType.ATTRIBUTE, objects.named(ObfType.DEOBF))
		attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
		forAllOutgoing()
	}
	
	fun AttributeContainer.runtime(isObf: Boolean) {
		val attr = if (isObf) ObfType.OBF else ObfType.DEOBF
		attribute(ObfType.ATTRIBUTE, objects.named(attr))
		attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
		forAllOutgoing()
	}
	
	// Solely the sources for the platform-specific project
	val sources = configurations.create("${platformName}_source").apply {
		isCanBeConsumed = true
		isCanBeResolved = false
		
		description = "Solely the sources for this platform's platform-specific code, without any common stuff included."
		
		attributes {
			attribute(PlatformType.ATTRIBUTE, objects.named(platformName))
			sources()
		}
	}
	
	val dev_runtime = configurations.create("${platformName}_dev").apply {
		isCanBeConsumed = true
		isCanBeResolved = false
		
		description = "The compiled and transformed platform-specific code for use in dev runs, still without the common module shaded."
		
		attributes {
			attribute(PlatformType.ATTRIBUTE, objects.named(platformName))
			runtime(false)
		}
		
		outgoing {
			capability("$group:${project.name}-$platformName:$version")
		}
	}
	
	val prod_runtime = configurations.create("${platformName}_prod").apply {
		isCanBeConsumed = true
		isCanBeResolved = false
		
		description = "The obfuscated production variant, with all code merged, transformed, and mapped for client use."
		
		attributes {
			attribute(PlatformType.ATTRIBUTE, objects.named(platformName))
			runtime(true)
		}
		
		outgoing {
			capability("$group:${project.name}-$platformName:$version")
		}
	}
	
	artifacts {
		val proj = project(":$platformName")
		add(sources.name, proj.tasks["sourcesJar"])
		add(dev_runtime.name, proj.tasks.jar)
		add(prod_runtime.name, tasks.getByPath(":$platformName:shadowJar"))
	}
	
	return Triple(sources, dev_runtime, prod_runtime)
}
// TODO move this source-dev-prod to platform-specific convention plugins, because :rootProject:publishToMavenLocal will do all subprojects too
//     meaning we don't have to handle ALL of them in the root buildscript
val (neoforge_source, neoforge_dev, neoforge_prod) = makeOutputsForPlatform(PlatformType.NEOFORGE)
val (fabric_source, fabric_dev, fabric_prod) = makeOutputsForPlatform(PlatformType.FABRIC)

val common_sources by configurations.creating {
	isCanBeConsumed = true
	isCanBeResolved = false
	
	attributes {
		attribute(ModuleType.ATTRIBUTE, objects.named(ModuleType.MAIN))
		attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
		attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
		attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
	}
}

artifacts {
	add(common_sources.name, project(":common").tasks["sourcesJar"])
}

(components.findByName("java") as AdhocComponentWithVariants).run {
	addVariantsFromConfiguration(common_sources) {
		mapToMavenScope("compile")
	}
	
	listOf(neoforge_dev, fabric_dev, neoforge_prod, fabric_prod).forEach {
		addVariantsFromConfiguration(it) {
			mapToMavenScope("runtime")
		}
	}
}*/

//publishing {
//	publications {
//		create<MavenPublication>("mavenJava") {
//			from(components["java"])
//		}
//	}
//}
//endregion
