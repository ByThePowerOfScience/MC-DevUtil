plugins {
    id("multiloader-common")
    id("btpos.gradle.mcmods.multiplatform.postprocessing")
    id("ismodjar-convention")
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

val commonProject = project(":common")


val mod_id: String by rootProject.properties

dependencies {
    "commonSources"(commonProject.java.sourceSets.main.get().allSource)
}

tasks.compileJava {
    source(configurations["commonSources"])
}

sourceSets.main.get().resources.srcDir(commonProject.sourceSets.main.get().resources.srcDirs)

kotlin {
    sourceSets {
        main {
            dependsOn(commonProject.kotlin.sourceSets.main.get())
        }
        test {
            dependsOn(commonProject.kotlin.sourceSets.test.get())
        }
    }
}