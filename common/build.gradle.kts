plugins {
    id("multiloader-common")
    id("net.neoforged.moddev")
}

inline fun Project.prop(name: String): String {
    return rootProject.property(name) as String
}

neoForge {
    neoFormVersion = project.prop("neo_form_version")
    // Automatically enable AccessTransformers if the file exists
    val at = file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion = project.prop("parchment_minecraft")
        mappingsVersion = project.prop("parchment_version")
    }
}

dependencies {
    compileOnly(group = "org.spongepowered", name = "mixin", version = "0.8.5")
    // fabric and neoforge both bundle mixinextras, so it is safe to use it in common
    compileOnly(group = "io.github.llamalad7", name = "mixinextras-common", version = "0.3.5")
    annotationProcessor(group = "io.github.llamalad7", name = "mixinextras-common", version = "0.3.5")
}

configurations {
    create("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    create("commonResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("commonJava", sourceSets.main.get().java.sourceDirectories.singleFile)
    add("commonResources", sourceSets.main.get().resources.sourceDirectories.singleFile)
}

