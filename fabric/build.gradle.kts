@file:Suppress("UnstableApiUsage")

import btpos.gradle.mcmods.multiplatform.base.attributes.MCPlatform


plugins {
    id("fabric-loom")
    id("multiloader-loader")
}

btposMultiplatform {
    platform = MCPlatform.FABRIC
}

dependencies {
    minecraft ("com.mojang:minecraft:${rootProject.minecraft_version}")
    mappings (loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${rootProject.parchment_minecraft}:${project.parchment_version}@zip")
    })
    
    modImplementation("net.fabricmc:fabric-loader:${rootProject.fabric_loader_version}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.fabric_version}")
    modImplementation(libs.fabric.kotlin)
    
    testCompileOnly(project(path=":test-utils", configuration="apiElements"))
    testRuntimeOnly(project(path=":test-utils", configuration="runtimeElements"))
}

loom {
    val aw = project(":common").file("src/main/resources/${rootProject.mod_id}.accesswidener")
    if (aw.exists()) {
        accessWidenerPath.set(aw)
    }
    enableTransitiveAccessWideners = true
    
    mixin {
        defaultRefmapName.set("${rootProject.mod_id}.refmap.json")
    }
    runs {
        configureEach {
            vmArgs.addAll(listOf("-Dmixin.debug.export=true", "-Dmixin.debug.verbose=true"))
            vmArgs.add("-XX:+AllowEnhancedClassRedefinition")
        }
        maybeCreate("client").apply {
            client()
            configName = "Fabric Client"
            source(sourceSets.main.get())
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