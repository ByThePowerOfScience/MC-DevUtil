package btpos.mcmods.devutil.gradle.plugin

import btpos.gradle.mcmods.multiplatform.base.MultiplatformBasePlugin
import btpos.gradle.mcmods.multiplatform.base.attributes.MCPlatform
import btpos.gradle.mcmods.multiplatform.base.transformerutils.ChainableClassVisitor
import btpos.gradle.mcmods.multiplatform.postprocessing.PostProcessingPlugin
import btpos.mcmods.devutil.gradle.plugin.transformers.ITransformerProvider
import btpos.mcmods.devutil.gradle.plugin.transformers.interfaces.TPlatformConnectRedstone
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue


class APITransformerPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.pluginManager.apply(PostProcessingPlugin::class.java)
		
		val platformExt: MultiplatformBasePlugin.Extension = project.extensions.getByType()
		val transformersExt: PostProcessingPlugin.Extension = (platformExt as ExtensionAware).extensions.getByType()
		
		addPlatformTransformers(platformExt.platform, transformersExt)
	}
	
	fun addPlatformTransformers(platform: Provider<String>, tfExt: PostProcessingPlugin.Extension) {
		val ourTransformers = listOf(
				TPlatformConnectRedstone
		)
		
		fun getOurTransformersForPlatform(platformName: String): List<ChainableClassVisitor> {
			val getter = when (platformName) {
				MCPlatform.NEOFORGE -> ITransformerProvider::forNeoForge
				MCPlatform.FABRIC -> ITransformerProvider::forFabric
				else -> return listOf();
			}
			
			return ourTransformers.flatMap(getter)
		}
		
		tfExt.transformers.addAll(platform.map { getOurTransformersForPlatform(it) })
	}
}

//interface DevUtilExtension { // TODO: Dynamically inject mod id and other specified metadata like RFG
//	val packageName: String
//	val mod_id: String
//}