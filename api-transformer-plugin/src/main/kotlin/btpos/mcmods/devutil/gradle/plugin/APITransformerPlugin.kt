package btpos.mcmods.devutil.gradle.plugin

import btpos.gradle.architecturyextended.common.ArchCommonTransformerPlugin
import btpos.mcmods.devutil.gradle.plugin.transformers.ITransformerProvider
import btpos.mcmods.devutil.gradle.plugin.transformers.interfaces.TPlatformConnectRedstone
import dev.architectury.plugin.ArchitectPluginExtension
import dev.architectury.transformer.Transformer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByType


class APITransformerPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.pluginManager.apply(ArchCommonTransformerPlugin::class.java)
		
		project.afterEvaluate {
			val platformExt = (extensions.getByType<ArchitectPluginExtension>() as ExtensionAware)
				.extensions.getByType<ArchCommonTransformerPlugin.Extension>()
			
			addPlatformTransformers(platformExt)
		}
	}
	
	fun Project.addPlatformTransformers(tfExt: ArchCommonTransformerPlugin.Extension) {
		val ourTransformers = listOf(
				TPlatformConnectRedstone
		)
		
		fun getOurTransformersForPlatform(platformName: String): List<Transformer> {
			val getter = when (platformName) {
				"neoforge" -> ITransformerProvider::forNeoForge
				"fabric" -> ITransformerProvider::forFabric
				else -> return listOf()
			}
			
			return ourTransformers.flatMap(getter)
		}
		
		tfExt.transformersByPlatform
			.maybeCreate("neoforge")
			.transformers
			.addAll(getOurTransformersForPlatform("neoforge"))
		
		tfExt.transformersByPlatform
			.maybeCreate("fabric")
			.transformers
			.addAll(getOurTransformersForPlatform("fabric"))
	}
}

//interface DevUtilExtension { // TODO: Dynamically inject mod id and other specified metadata like RFG
//	val packageName: String
//	val mod_id: String
//}