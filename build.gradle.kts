import btpos.gradle.architecturyextended.base.util.standardJavaArchiveAttributes
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import btpos.gradle.architecturyextended.base.attributes.*

plugins {
	id("dev.architectury.loom") version "1.10-SNAPSHOT" apply false
	id("architectury-plugin") version "3.4-SNAPSHOT"
	id("com.github.johnrengelman.shadow") version "8.1.1" apply false
	id("btpos.gradle.architecturyextended.base") version "1.0.0-SNAPSHOT"
	id("btpos.gradle.architecturyextended.common") version "1.0.0-SNAPSHOT" apply false
	id("btpos.gradle.architecturyextended.platform") version "1.0.0-SNAPSHOT" apply false
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
	apply(plugin = "btpos.gradle.architecturyextended.base")
	
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
	
	publishing {
		publications {
			create<MavenPublication>("mavenJava") {
				artifactId = if (project.name == "common") {
					rootProject.name
				} else {
					"${rootProject.name}-${project.name}"
				}
				from(components["java"])
			}
		}
	}
}
