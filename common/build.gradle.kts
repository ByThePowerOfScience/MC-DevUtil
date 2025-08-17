plugins {
    id("multiloader-common")
    id("net.neoforged.moddev")
}

fun Project.prop(name: String): String {
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
    compileOnly("org.spongepowered:mixin:0.8.5")
    compileOnly("org.ow2.asm:asm-tree:9.6") // because Mixin isn't giving any transitive deps fsr?
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    // fabric and neoforge both bundle mixinextras, so it is safe to use it in common
    compileOnly(group = "io.github.llamalad7", name = "mixinextras-common", version = "0.3.5")
    annotationProcessor(group = "io.github.llamalad7", name = "mixinextras-common", version = "0.3.5")
}

