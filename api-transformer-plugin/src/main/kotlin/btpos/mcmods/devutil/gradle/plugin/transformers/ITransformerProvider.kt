package btpos.mcmods.devutil.gradle.plugin.transformers

import btpos.gradle.mcmods.multiplatform.base.transformerutils.ChainableClassVisitor


/**
 * Add transformers to implement the given behavior for each platform
 */
interface ITransformerProvider {
	fun forNeoForge(): List<ChainableClassVisitor>
	fun forFabric(): List<ChainableClassVisitor>
}