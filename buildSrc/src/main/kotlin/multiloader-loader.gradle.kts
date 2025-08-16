import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("multiloader-common")
    id("com.github.johnrengelman.shadow")
}

configurations {
    create("commonSources") {
        isCanBeResolved = true
    }
    create("commonResources") {
        isCanBeResolved = true
    }
    create("shadowBundle") {
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
tasks.processResources {
    val common = project(":common").sourceSets.main.get()
    dependsOn(common.resources)
    from(common.resources)
}

tasks.named<Javadoc>("javadoc").configure {
    val common = project(":common").sourceSets.main.get()
    dependsOn(common.allSource)
    source(common.allSource)
}

tasks.named<Jar>("sourcesJar") {
    val common = project(":common").sourceSets.main.get()
    dependsOn(common.allSource, common.resources)
    from(common.allSource)
    from(common.resources)
    
    duplicatesStrategy = DuplicatesStrategy.WARN
}

tasks.named<ShadowJar>("shadowJar") {
    configurations.add(project.configurations["shadowBundle"])
}