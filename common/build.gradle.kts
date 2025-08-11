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
	/**
	 * The standard `common` method registers the tasks `transformProductionFabric` and `transformProductionNeoForge`,
	 * which have transformers made for the obfuscated prod environment.  These will break the deobf dev runs.
	 *
	 * Problem is, I still need to run transformers on my common module before merging it into the platform-specific ones.
	 *
	 * Since this is the only place we can actually learn what loaders are being targeted,
	 * we have to do all of our transformer task initialization here...
	 */
	common(PlatformType.FABRIC, PlatformType.NEOFORGE)
}

//devTransformers {
//	platforms.putAll(mapOf(
//			project.objects.named(PlatformType::class.java, PlatformType.FABRIC) to listOf(),
//			project.objects.named(PlatformType::class.java, PlatformType.NEOFORGE) to listOf()
//	))
//	tasks.putAll(mapOf(
//			project.tasks.jar.get() to objects.named<ModuleType>(ModuleType.MAIN),
//			testJar.get() to objects.named<ModuleType>(ModuleType.TEST)
//	))
//}


tasks.withType<Test> {
	useJUnitPlatform()
}

dependencies {
	modImplementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"]}")
	
	modImplementation("dev.architectury:architectury:${rootProject.properties["architectury_api_version"]}")
}

tasks.test {
	exclude("**/*")
}