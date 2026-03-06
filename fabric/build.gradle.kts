@file:Suppress("UnstableApiUsage")

import btpos.gradle.mcmods.multiplatform.base.attributes.MCPlatform
import org.gradle.kotlin.dsl.named


plugins {
    id("fabric-loom")
    id("multiloader-loader")
}

fun Project.prop(name: String): String {
    return rootProject.property(name) as String
}

btposMultiplatform {
    platform = MCPlatform.FABRIC
}

dependencies {
    minecraft ("com.mojang:minecraft:${rootProject.prop("minecraft_version")}")
    mappings (loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${rootProject.prop("parchment_minecraft")}:${project.prop("parchment_version")}@zip")
    })
    modImplementation ("net.fabricmc:fabric-loader:${rootProject.prop("fabric_loader_version")}")
    modImplementation ("net.fabricmc.fabric-api:fabric-api:${rootProject.prop("fabric_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.7+kotlin.2.2.21")
    testImplementation("net.fabricmc:fabric-loader-junit:${rootProject.prop("fabric_loader_version")}")
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junit.launcher)
}

loom {
    val aw = project(":common").file("src/main/resources/${rootProject.prop("mod_id")}.accesswidener")
    if (aw.exists()) {
        accessWidenerPath.set(aw)
    }
    mixin {
        defaultRefmapName.set("${rootProject.prop("mod_id")}.refmap.json")
    }
    runs {
        configureEach {
            vmArgs.addAll(listOf("-Dmixin.debug.export=true", "-Dmixin.debug.verbose=true", "-XX:+AllowEnhancedClassRedefinition"))
        }
        maybeCreate("client").apply {
            client()
	        configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("runs/client")
        }
        maybeCreate("server").apply {
            server()
	        configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("runs/server")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}