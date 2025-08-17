package btpos.mcmods.devutil.gradle.plugin

import btpos.gradle.architecturyextended.base.transformerutils.ChainableClassVisitor
import btpos.gradle.architecturyextended.platformtf.PostProcessingPlugin
import btpos.mcmods.devutil.gradle.plugin.transformers.ITransformerProvider
import btpos.mcmods.devutil.gradle.plugin.transformers.interfaces.TPlatformConnectRedstone
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByType


class APITransformerPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.pluginManager.apply(PostProcessingPlugin::class.java)
		
		project.afterEvaluate {
			val platformExt = extensions.getByType<PostProcessingPlugin.Extension>()
			
			addPlatformTransformers(platformExt)
		}
	}
	
	fun Project.addPlatformTransformers(tfExt: PostProcessingPlugin.Extension) {
		val ourTransformers = listOf(
				TPlatformConnectRedstone
		)
		
		fun getOurTransformersForPlatform(platformName: String): List<ChainableClassVisitor> {
			val getter = when (platformName) {
				"neoforge" -> ITransformerProvider::forNeoForge
				"fabric" -> ITransformerProvider::forFabric
				else -> return listOf()
			}
			
			return ourTransformers.flatMap(getter)
		}
		
		tfExt.transformers.addAll(tfExt.platform.map { getOurTransformersForPlatform(it) })
	}
}

//interface DevUtilExtension { // TODO: Dynamically inject mod id and other specified metadata like RFG
//	val packageName: String
//	val mod_id: String
//}