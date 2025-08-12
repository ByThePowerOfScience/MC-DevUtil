package btpos.mcmods.devutil.gradle.plugin.transformers.interfaces

import btpos.mcmods.devutil.gradle.plugin.transformers.ITransformerProvider
import dev.architectury.transformer.Transformer
import dev.architectury.transformer.input.FileAccess
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.ClassNode
import dev.architectury.transformer.transformers.base.AssetEditTransformer
import dev.architectury.transformer.transformers.base.ClassEditTransformer
import dev.architectury.transformer.transformers.base.edit.TransformerContext

object TPlatformConnectRedstone : ITransformerProvider {
	override fun forNeoForge(): List<Transformer> {
		return listOf(TPlatformConnectRedstone_Forge())
	}
	
	/**
	 * We don't need to do anything to mods that depend on this.
	 *
	 * The DevUtil mod's MixinConfigPlugin will automatically inject the Mixin that implements the behavior.
	 */
	override fun forFabric(): List<Transformer> {
		return listOf()
	}
}


private const val ITF_NAME = "btpos/mcmods/devutil/multiplatform/api/IPlatformConnectRedstone"
/**
 * All we need is to remove the interface to match Forge's IBlockExtensions.
 *
 * Since the method name and signature for `IPlatformConnectRedstone#canConnectRedstone` is exactly the same as `IBlockExtensions#canConnectRedstone`,
 * it should work perfectly as long as nothing cast the object to `IPlatformConnectRedstone`.
 */
private class TPlatformConnectRedstone_Forge : ClassEditTransformer {
	override fun doEdit(s: String?, node: ClassNode): ClassNode {
		node.interfaces = node.interfaces.filter { it != ITF_NAME }
		return node
	}
}

