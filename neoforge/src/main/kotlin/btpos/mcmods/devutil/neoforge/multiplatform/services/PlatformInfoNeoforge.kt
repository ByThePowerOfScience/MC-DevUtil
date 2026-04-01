package btpos.mcmods.devutil.neoforge.multiplatform.services

import btpos.mcmods.devutil.multiplatform.services.IPlatformInfoService
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLLoader

class PlatformInfoNeoforge : IPlatformInfoService {
	override val platformName: String
		get() = "neoforge"
	
	override fun isModLoaded(modId: String): Boolean {
		return ModList.get().isLoaded(modId)
	}
	
	override val isDevelopmentEnvironment = !FMLLoader.isProduction()
}