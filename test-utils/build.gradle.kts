import btpos.gradle.mcmods.multiplatform.base.attributes.MCPlatform

plugins {
    id("multiloader-common")
    id("net.neoforged.moddev")
}


btposMultiplatform {
    platform = MCPlatform.AGNOSTIC
}

neoForge {
    version = neoforge_version // has to be neoforge to allow unit testing
    
    parchment {
        minecraftVersion = parchment_minecraft
        mappingsVersion = parchment_version
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