import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
	id ("org.jetbrains.kotlin.jvm")
	id("btpos.plugins.kt.varinterfacedelegation")
}

kotlin {
	jvmToolchain(21)
	compilerOptions {
		freeCompilerArgs.add("-Xcontext-parameters")
	}
}

repositories {
	mavenCentral()
}

val mockitoAgent by configurations.creating
dependencies {
	compileOnly(kotlin("reflect"))
	
	
	testImplementation(kotlin("test"))
	testImplementation("org.hamcrest:hamcrest:3.0")
	
	val mockitoVersion = "5.23.0"
	"org.mockito:mockito-core:$mockitoVersion".let {
		testImplementation(it)
		mockitoAgent(it) {
			isTransitive = false
		}
	}
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
	testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
}

configurations {
	testCompileClasspath.extendsFrom(compileClasspath)
	testRuntimeClasspath.extendsFrom(runtimeClasspath)
}

tasks.test {
	useJUnitPlatform()
	jvmArgs("-javaagent:${mockitoAgent.asPath}")
}