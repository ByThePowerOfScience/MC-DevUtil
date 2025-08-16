package btpos.mcmods.devutil

import btpos.mcmods.devutil.multiplatform.PlatformInfo
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class MixinConfigPlugin : IMixinConfigPlugin {
	override fun onLoad(mixinPackage: String?) {}
	
	override fun getRefMapperConfig(): String? = null
	
	override fun shouldApplyMixin(targetClassName: String?, mixinClassName: String?): Boolean {
		return true
	}
	
	override fun acceptTargets(myTargets: Set<String?>?, otherTargets: Set<String?>?) {}
	
	override fun getMixins(): List<String?>? {
		if (!PlatformInfo.loader.isForgeLike) {
			return listOf("api.MConnectRedstone")
		}
		return null
	}
	
	override fun preApply(p0: String?, p1: ClassNode?, p2: String?, p3: IMixinInfo?) {}
	
	override fun postApply(p0: String?, p1: ClassNode?, p2: String?, p3: IMixinInfo?) {}
}