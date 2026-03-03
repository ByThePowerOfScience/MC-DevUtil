
plugins {
    id("multiloader-common")
    id("btpos.gradle.mcmods.multiplatform.postprocessing")
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
        val common = project(":common")
        main {
            // TODO figure out how to get this to depend on the java sources too for mixins
            dependsOn(common.kotlin.sourceSets.main.get())
            resources.srcDir(common.sourceSets.main.get().resources)
        }
        test {
            dependsOn(common.kotlin.sourceSets.test.get())
            resources.srcDir(common.sourceSets.test.get().resources)
        }
    }
}

//idea {
//    module {
//        // Fix IntelliJ not seeing that we can access the common sources despite not declaring a dependency on it
//        scopes["COMPILE"]!!["plus"]!!.add(configurations["commonSources"])
//    }
//}