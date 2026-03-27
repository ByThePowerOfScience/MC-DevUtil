plugins {
    // see https://fabricmc.net/develop/ for new versions
    id("fabric-loom") version "1.11-SNAPSHOT" apply false
    // see https://projects.neoforged.net/neoforged/moddevgradle for new versions
    id("net.neoforged.moddev") version "2.0.107" apply false
}

repositories {
    mavenCentral()
}

tasks.register("publishAllToMavenLocal") {
    group = "custom"
    dependsOn(listOf(":fabric", ":neoforge", ":common", ":test-utils").map {
        project(it).tasks["publishToMavenLocal"]
    })
    dependsOn(gradle.includedBuild("api-transformer-plugin").task(":publishToMavenLocal"))
}