package btpos.mcmods.devutil.gradle.plugin.transformers.forge

import btpos.mcmods.devutil.gradle.plugin.transformers.ClassNode

private const val ITF_NAME = "btpos/mcmods/devutil/multiplatform/api/IPlatformConnectRedstone"

/**
 * All we need is to remove the interface to match Forge's IBlockExtensions.
 * Since the method name and signature is the same, it should be fine.
 */
fun TConnectRedstoneForge(node: ClassNode) {
	node.interfaces = node.interfaces.filter { it != ITF_NAME }
}

/*
class TConnectRedstoneForge : ClassVisitor2() {
	override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String?>?) {
		if (interfaces == null || ITF_NAME !in interfaces)
			return super.visit(version, access, name, signature, superName, interfaces)
		
		val newInterfaces = interfaces.filterTo(ArrayList(interfaces.size - 1)) { it != ITF_NAME }.toTypedArray()
		
		return super.visit(version, access, name, signature, superName, newInterfaces)
	}
}*/
