package btpos.mcmods.devutil.gradle.plugin.transformers

import dev.architectury.transformer.Transformer

/**
 * Add transformers to implement the given behavior for each platform
 */
interface ITransformerProvider {
	fun forNeoForge(): List<Transformer>
	fun forFabric(): List<Transformer>
}