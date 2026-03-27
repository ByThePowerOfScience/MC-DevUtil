import btpos.gradle.mcmods.multiplatform.base.attributes.MCPlatform
import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    id("multiloader-common")
    id("net.neoforged.moddev")
}

fun Project.prop(name: String): String {
    return rootProject.property(name) as String
}

btposMultiplatform {
    platform = MCPlatform.AGNOSTIC
}

neoForge {
    version = prop("neoforge_version")
    // Automatically enable AccessTransformers if the file exists
    val at = file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion = project.prop("parchment_minecraft")
        mappingsVersion = project.prop("parchment_version")
    }
    
    mods {
        create("devutil_testutils") {
            sourceSet(sourceSets.main.get())
        }
    }
    
    unitTest {
        enable()
        
        testedMod = mods["devutil_testutils"]
    }
}

dependencies {
    implementation(libs.bundles.junit)
    api(libs.hamcrest)
    api(libs.bundles.mockito)
}

configurations {
    testCompileClasspath {
        extendsFrom(compileClasspath.get())
    }
    testRuntimeClasspath {
        extendsFrom(runtimeClasspath.get())
    }
}

tasks.test {
    useJUnitPlatform()
}
