import btpos.gradle.mcmods.multiplatform.base.attributes.MCPlatform

plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
}

btposMultiplatform {
    platform = MCPlatform.NEOFORGE
}


@Suppress("NOTHING_TO_INLINE")
inline fun String.capitalize() { // why on earth would KT deprecate this? this is like basic standardlib stuff.
    this.replaceFirstChar { it.uppercase() }
}

neoForge {
    version = neoforge_version
    // Automatically enable neoforge AccessTransformers if the file exists
    val commonProject = project(":common")
    val at = commonProject.file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion = parchment_minecraft
        mappingsVersion = parchment_version
    }
    
    runs {
        create("client") {
            client()
        }
        create("data") {
            clientData()
            
            val generatedResources = commonProject.file("src/main/generated")
            programArguments.addAll(
                "--output", generatedResources.absolutePath,
                "--existing", (commonProject.sourceSets.main.get().resources.srcDirs - generatedResources).joinToString(File.pathSeparator),
                "--mod", mod_id
            )
        }
        create("server") {
            server()
        }
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
            ideName = "NeoForge ${name.capitalize()} (${project.path})" // Unify the run config names with fabric
            if (name != "data")
                jvmArguments.addAll(listOf("-Dmixin.debug.export=true", "-Dmixin.debug.verbose=true"))
//            jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")
        }
    }
    mods {
        create(rootProject.mod_id) {
            sourceSet(sourceSets.main.get())
        }
    }
    
    unitTest {
        enable()
        
        testedMod = mods[mod_id]
    }
}

repositories {
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

dependencies {
    implementation("thedarkcolour:kotlinforforge-neoforge:5.10.0")
    arrayOf("kfflang", "kfflib", "kffmod").forEach {
        testRuntimeOnly("thedarkcolour:$it-neoforge:5.10.0")
    }
    
    testCompileOnly(project(path=":test-utils", configuration="apiElements"))
    testRuntimeOnly(project(path=":test-utils", configuration="runtimeElements"))
}

configurations {
    runtimeClasspath {
        exclude(group="btpos.plugins.kt.varinterfacedelegation")
    }
    
    testRuntimeClasspath {
        // exclude kff's shaded kotlin libs from our test runs,
        // cause the way they're loaded conflicts with itself (I think?)
        // and causes a "multiple KFunction1 interfaces" LinkageError
        exclude(group="thedarkcolour", module="kotlinforforge-neoforge")
    }
}

sourceSets.main.get().resources { srcDir ("src/generated/resources") }

tasks.test {
    useJUnitPlatform()
}