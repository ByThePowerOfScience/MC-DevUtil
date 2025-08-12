package btpos.mcmods.devutil.gradle.plugin

import btpos.gradle.architecturyextended.base.attributes.TargetPlatform
import btpos.gradle.architecturyextended.platform.ArchExtendedPlugin_Platform
import btpos.mcmods.devutil.gradle.plugin.transformers.ITransformerProvider
import btpos.mcmods.devutil.gradle.plugin.transformers.interfaces.TPlatformConnectRedstone
import org.gradle.api.Plugin
import org.gradle.api.Project
import dev.architectury.transformer.Transformer
import org.gradle.kotlin.dsl.getByType


class APITransformerPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		val platformExt = project.extensions.getByType<ArchExtendedPlugin_Platform.Extension>()
		
		val transformerProviders = listOf(
				TPlatformConnectRedstone
		)
		
		val getTransformersForPlatform = when (platformExt.platform.get().name) {
			TargetPlatform.NEOFORGE -> ITransformerProvider::forNeoForge
			TargetPlatform.FABRIC -> ITransformerProvider::forFabric
			else -> { _ -> listOf() }
		}
		
		platformExt.transformers.addAll(transformerProviders.flatMap(getTransformersForPlatform))
	}
}

//interface DevUtilExtension { // TODO: Dynamically inject mod id and other specified metadata like RFG
//	val packageName: String
//	val mod_id: String
//}