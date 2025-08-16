package btpos.mcmods.devutil.multiplatform

import btpos.mcmods.devutil.multiplatform.services.IPlatformInfoService
import java.util.ServiceLoader

/**
 * Platform-specific information injected by services.
 */
object PlatformInfo : IPlatformInfoService {
	/**
	 * We use explicit delegation instead of `by` so IDEA will autocomplete the methods.
	 */
	private val service = ServiceLoader.load(IPlatformInfoService::class.java).findFirst().orElseThrow { IllegalStateException("Unable to resolve btpos.mcmods.devutil.multiplatform.services.IPlatformInfoService instance for current platform!") }
	
	override val platformName by service::platformName
	
	override fun isModLoaded(modId: String) = service.isModLoaded(modId)
	
	override val isDevelopmentEnvironment by service::isDevelopmentEnvironment
	
	/**
	 * Gets the name of the environment type as a string.
	 *
	 * @return The name of the environment type.
	 */
	override val environment by service::environment
	
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