package btpos.mcmods.devutil

import btpos.mcmods.devutil.multiplatform.PlatformInfo
import org.objectweb.asm.tree.ClassNode
import org.slf4j.LoggerFactory
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import org.spongepowered.asm.service.MixinService

class DevUtilMixinConfigPlugin : IMixinConfigPlugin {
	override fun onLoad(mixinPackage: String?) {}
	
	override fun getRefMapperConfig(): String? = null
	
	override fun shouldApplyMixin(targetClassName: String?, mixinClassName: String?): Boolean { return true }
	
	override fun acceptTargets(myTargets: Set<String>?, otherTargets: Set<String>?) {}
	
	override fun getMixins(): List<String>? {
		if (PlatformInfo.loader == PlatformInfo.Loader.FABRIC) {
			return listOf("api.MConnectRedstone")
		}
		return null
	}
	
	
	override fun preApply(targetClassName: String?, targetClass: ClassNode?, mixinClassName: String?, mixinInfo: IMixinInfo?) {}
	
	override fun postApply(targetClassName: String?, targetClass: ClassNode?, mixinClassName: String?, mixinInfo: IMixinInfo?) {}
}