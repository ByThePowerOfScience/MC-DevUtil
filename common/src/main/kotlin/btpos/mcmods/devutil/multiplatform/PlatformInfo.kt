package btpos.mcmods.devutil.multiplatform

import btpos.mcmods.devutil.multiplatform.services.IPlatformInfoService
import btpos.mcmods.devutil.multiplatform.services.ServiceUtil

/**
 * Provides information about the current platform, like its name, whether a mod is loaded, etc.
 */
object PlatformInfo : IPlatformInfoService {
	/**
	 * We use explicit delegation instead of `by` so IDEA will autocomplete the methods.
	 */
	private val service = ServiceUtil.findFirst<IPlatformInfoService>()
	
	override val platformName by service::platformName
	
	override fun isModLoaded(modId: String) = service.isModLoaded(modId)
	
	override val isDevelopmentEnvironment by service::isDevelopmentEnvironment
	
	override val environment by service::environment
	
	/**
	 * Get the current modloader as an enum.
	 */
	val loader: Loader
		get() = when (environment) {
			"neoforge" -> Loader.NEOFORGE
			"fabric" -> Loader.FABRIC
			else -> Loader.UNKNOWN
		}
	
	enum class Loader {
	    UNKNOWN,
		NEOFORGE,
		FABRIC;
		
		val isForgeLike
			get() = this == NEOFORGE
	}
}