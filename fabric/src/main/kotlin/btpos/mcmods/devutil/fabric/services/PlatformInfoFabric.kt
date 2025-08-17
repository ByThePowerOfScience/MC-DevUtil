package btpos.mcmods.devutil.fabric.services

import btpos.mcmods.devutil.multiplatform.services.IPlatformInfoService
import com.google.auto.service.AutoService
import net.fabricmc.loader.api.FabricLoader

@AutoService(IPlatformInfoService::class)
class PlatformInfoFabric : IPlatformInfoService {
	override val platformName: String
		get() = "fabric"
	
	override fun isModLoaded(modId: String): Boolean {
		return FabricLoader.getInstance().isModLoaded(modId)
	}
	
	override val isDevelopmentEnvironment: Boolean
		get() = FabricLoader.getInstance().isDevelopmentEnvironment
}