
plugins {
	id("btpos.gradle.architecturyextended.common")
}

val testJar = tasks.register("testJar", Jar::class) {
	group = "build"
	dependsOn(tasks.testClasses)
	// testClasses doesn't have outputs, so we do it manually
	from(project.layout.buildDirectory.file("classes/kotlin/test/"), project.layout.buildDirectory.file("classes/java/test/"))
	archiveClassifier = "testJar"
}

architectury {
	common("fabric", "neoforge")
	
	transformExt {
		platforms.addAll("fabric", "neoforge")
	}
}

dependencies {
	modImplementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"]}")
	modImplementation("dev.architectury:architectury:${rootProject.properties["architectury_api_version"]}")
	implementation("btpos.mcmods.devutil:devutil:1.0-SNAPSHOT")
}

java {
	withSourcesJar()
	withJavadocJar()
}

tasks.withType<Test> {
	exclude("**/*")
	useJUnitPlatform()
}