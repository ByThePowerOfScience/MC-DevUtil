package btpos.mcmods.devutil.multiplatform.services

import java.util.ServiceLoader

object ServiceUtil {
	/**
	 * Loads a service by type, or throws an exception if the service could not be found.
	 */
	inline fun <reified T> loadService(): T {
		return ServiceLoader.load(T::class.java).findFirst().orElseThrow { IllegalStateException("No platform-specific implementation found for service \"${T::class.java.name}\".") }
	}
}