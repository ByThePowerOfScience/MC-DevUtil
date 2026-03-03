package btpos.mcmods.devutil.gradle.plugin.transformers.interfaces

import btpos.gradle.mcmods.multiplatform.base.transformerutils.ChainableClassVisitor
import btpos.mcmods.devutil.gradle.plugin.transformers.ITransformerProvider

object TPlatformConnectRedstone : ITransformerProvider {
	override fun forNeoForge(): List<ChainableClassVisitor> {
		return listOf(TPlatformConnectRedstone_Forge())
	}
	
	/**
	 * We don't need to do anything to mods that depend on this.
	 *
	 * The DevUtil mod's MixinConfigPlugin will automatically inject the Mixin that implements the behavior.
	 */
	override fun forFabric(): List<ChainableClassVisitor> {
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
private class TPlatformConnectRedstone_Forge : ChainableClassVisitor() {
	override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String?>) {
		val itfs = when {
			interfaces.contains(ITF_NAME) -> interfaces.filterTo(ArrayList(interfaces.size - 1)) { it != ITF_NAME }.toTypedArray()
			else -> interfaces
		}
		super.visit(version, access, name, signature, superName, itfs)
	}
}

