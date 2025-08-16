import gradle.kotlin.dsl.accessors._edc4b8ef8cd23e8d2527d135f3d03813.implementation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id ("org.jetbrains.kotlin.jvm")
}

tasks.withType<KotlinCompile> {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_21)
		freeCompilerArgs.add("-Xcontext-receivers")
	}
}


dependencies {
	implementation(kotlin("reflect"))
}