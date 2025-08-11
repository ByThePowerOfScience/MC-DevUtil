package btpos.mcmods.devutil

import dev.architectury.platform.Platform
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
		if (!Platform.isForgeLike()) {
			return listOf("api.MConnectRedstone")
		}
		return null
	}
	
	override fun preApply(targetClassName: String?, targetClass: ClassNode?, mixinClassName: String?, mixinInfo: IMixinInfo?) {
	}
	
	override fun postApply(targetClassName: String?, targetClass: ClassNode?, mixinClassName: String?, mixinInfo: IMixinInfo?) {
	}
}