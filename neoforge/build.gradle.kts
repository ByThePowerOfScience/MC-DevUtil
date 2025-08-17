plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
}

fun Project.prop(name: String): String {
    return rootProject.property(name) as String
}

neoForge {
    version = prop("neoforge_version")
    // Automatically enable neoforge AccessTransformers if the file exists
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion = prop("parchment_minecraft")
        mappingsVersion = prop("parchment_version")
    }
    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", prop("mod_id"))
            ideName = "NeoForge ${name.capitalize()} (${project.path})" // Unify the run config names with fabric
        }
        create("client") {
            client()
        }
        create("data") {
            clientData()
        }
        create("server") {
            server()
        }
    }
    mods {
        create(rootProject.prop("mod_id")) {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
}

sourceSets.main.get().resources { srcDir ("src/generated/resources") }