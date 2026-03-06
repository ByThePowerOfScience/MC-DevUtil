import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id ("org.jetbrains.kotlin.jvm")
}

java {
	targetCompatibility = JavaVersion.VERSION_21
	sourceCompatibility = JavaVersion.VERSION_21
}

kotlin {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_21)
		freeCompilerArgs.add("-Xcontext-parameters")
	}
}


dependencies {
	implementation(kotlin("reflect"))
	testImplementation(kotlin("test"))
}