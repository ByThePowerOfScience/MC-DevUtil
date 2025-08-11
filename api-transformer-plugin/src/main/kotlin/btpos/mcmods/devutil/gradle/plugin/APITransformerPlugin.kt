package btpos.mcmods.devutil.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import btpos.gradle.architectury.transformerplugin.attributes.ModuleType
import btpos.gradle.architectury.transformerplugin.attributes.PlatformType
import btpos.gradle.architectury.transformerplugin.PlatformTransformersPluginExtension
import btpos.gradle.architectury.transformerplugin.CommonPlatformTransformersPlugin
import btpos.mcmods.devutil.gradle.plugin.transformers.MultiplatformPreTransformer_Fabric
import btpos.mcmods.devutil.gradle.plugin.transformers.MultiplatformPreTransformer_Forge
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named

class APITransformerPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.pluginManager.apply(CommonPlatformTransformersPlugin::class.java)
		project.extensions.configure<PlatformTransformersPluginExtension> {
			platforms.putAll(mapOf(
					project.objects.named(PlatformType::class.java, PlatformType.FABRIC) to listOf(MultiplatformPreTransformer_Fabric()),
					project.objects.named(PlatformType::class.java, PlatformType.NEOFORGE) to listOf(MultiplatformPreTransformer_Forge())
			))
			
			tasks.putAll(mapOf(
					project.tasks.getByName<Jar>("jar") to project.objects.named<ModuleType>(ModuleType.MAIN),
			))
		}
	}
}