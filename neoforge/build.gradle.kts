import btpos.gradle.mcmods.multiplatform.base.attributes.MCPlatform

plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
}

fun Project.prop(name: String): String {
    return rootProject.property(name) as String
}

tasks.test {
    useJUnitPlatform()
}

btposMultiplatform {
    platform = MCPlatform.NEOFORGE
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
            
            ideName = "NeoForge ${@Suppress("DEPRECATION") name.capitalize()} (${project.path})" // Unify the run config names with fabric
            jvmArguments.addAll(listOf("-Dmixin.debug.export=true", "-Dmixin.debug.verbose=true", "-XX:+AllowEnhancedClassRedefinition"))
        }
        create("client") {
            client()
        }
//        create("data") {
//            clientData()
//        }
        create("server") {
            server()
        }
    }
    mods {
        create(rootProject.prop("mod_id")) {
            sourceSet(sourceSets.main.get())
        }
    }
    unitTest {
        enable()
        testedMod = mods[rootProject.prop("mod_id")]
    }
}

repositories {
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
    mavenCentral()
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases/")
    }
}

dependencies {
    implementation("thedarkcolour:kotlinforforge-neoforge:5.10.0")
	testImplementation("net.neoforged:testframework:${prop("neoforge_version")}")
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junit.launcher)
}

sourceSets.main.get().resources {
    srcDir ("src/generated/resources")
}