package btpos.mcmods.devutil.gradle.plugin

import btpos.gradle.architecturyextended.transformersonly.ArchCustomTransformers
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
		project.pluginManager.apply(ArchCustomTransformers::class.java)
		
		val platformExt = (project.extensions.getByType<ArchitectPluginExtension>() as ExtensionAware)
			.extensions.getByType<ArchCustomTransformers.Extension>()
			
		project.afterEvaluate {
			addPlatformTransformers(platformExt)
		}
	}
	/**
	 * With Gradle's lazy initialization, do this:
	 * ```kotlin
	 * platformExt.transformers.addAll(listOf(ourTransformerProviders).flatMap(ITransformerProvider::getTransformersForPlatform))
	 * ```
	 */
	fun Project.addPlatformTransformers(tfExt: ArchCustomTransformers.Extension) {
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
		
		val tfPlatOld = tfExt.transformersByPlatform.get()
		tfExt.transformersByPlatform.set(tfExt.platforms.get().let { l ->
			tfPlatOld.let { oldMap ->
				val newMap = oldMap.toMutableMap()
				l.forEach { platformName ->
					newMap.computeIfAbsent(platformName, { mutableListOf() }).addAll(getOurTransformersForPlatform(platformName))
				}
				newMap
			}
		})
	}
}

//interface DevUtilExtension { // TODO: Dynamically inject mod id and other specified metadata like RFG
//	val packageName: String
//	val mod_id: String
//}