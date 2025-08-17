import btpos.gradle.architecturyextended.base.tasks.ClassTransformTask

plugins {
    id("multiloader-common")
    id("btpos.gradle.multiloader.platformtransformers")
    idea
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
    
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    
//    "commonSources"(project.project(":common").sourceSets.main.get().allSource)
}

kotlin {
    sourceSets {
        main {
            dependsOn(project(":common").kotlin.sourceSets.main.get())
        }
    }
}

//idea {
//    module {
//        // Fix IntelliJ not seeing that we can access the common sources despite not declaring a dependency on it
//        scopes["COMPILE"]!!["plus"]!!.add(configurations["commonSources"])
//    }
//}