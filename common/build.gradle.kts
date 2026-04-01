import btpos.gradle.mcmods.multiplatform.base.attributes.MCPlatform

plugins {
    id("multiloader-common")
    id("net.neoforged.moddev")
    id("ismodjar-convention")
}

btposMultiplatform {
    platform = MCPlatform.AGNOSTIC
}

neoForge {
    neoFormVersion = project.neo_form_version
    // Automatically enable AccessTransformers if the file exists
    val at = file("src/main/resources/META-INF/accesstransformer.cfg")
    
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion = project.parchment_minecraft
        mappingsVersion = project.parchment_version
    }
}

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.5") // don't run annotation processor on common, since it can't resolve remaps here
    compileOnly("org.ow2.asm:asm-tree:9.6") // because Mixin isn't giving any transitive deps fsr?
    // fabric and neoforge both bundle mixinextras, so it is safe to use it in common
    compileOnly(group = "io.github.llamalad7", name = "mixinextras-common", version = "0.3.5")
    
    testCompileOnly(project(path=":test-utils", configuration="apiElements"))
}

sourceSets.main.configure { resources.srcDir("src/main/generated") }