import btpos.gradle.architecturyextended.base.tasks.ClassTransformTask

plugins {
    id("multiloader-common")
    id("btpos.gradle.multiloader.platformtransformers")
}

configurations {
    create("commonSources") {
        isCanBeResolved = true
    }
    create("commonResources") {
        isCanBeResolved = true
    }
}

val mod_id: String by rootProject.properties

dependencies {
    compileOnly(project(":common")) {
        capabilities {
            requireCapability("$group:$mod_id")
        }
    }
}

kotlin {
    sourceSets {
        main {
            dependsOn(project(":common").kotlin.sourceSets.main.get())
        }
    }
}

