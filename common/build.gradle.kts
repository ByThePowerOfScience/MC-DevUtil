import btpos.gradle.architectury.transformerplugin.attributes.ModuleType
import btpos.gradle.architectury.transformerplugin.attributes.PlatformType

plugins {
	id("common-platform-transformer")
	id("btpos.devutil-transformer")
}

val testJar = tasks.register("testJar", Jar::class) {
	group = "build"
	dependsOn(tasks.testClasses)
	// testClasses doesn't have outputs for some reason??? so we do it manually
	from(project.layout.buildDirectory.file("classes/kotlin/test/"), project.layout.buildDirectory.file("classes/java/test/"))
	archiveClassifier = "testJar"
}

architectury {
	common(PlatformType.FABRIC, PlatformType.NEOFORGE)
}

devTransformers {
	tasks.putAll(mapOf(
			testJar.get() to objects.named<ModuleType>(ModuleType.TEST)
	))
}

dependencies {
	modImplementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"]}")
	modImplementation("dev.architectury:architectury:${rootProject.properties["architectury_api_version"]}")
}

java {
	withSourcesJar()
	withJavadocJar()
}

tasks.withType<Test> {
	exclude("**/*")
	useJUnitPlatform()
}