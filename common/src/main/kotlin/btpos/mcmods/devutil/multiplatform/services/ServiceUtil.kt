package btpos.mcmods.devutil.multiplatform.services

import org.intellij.lang.annotations.Language
import java.util.ServiceLoader

object ServiceUtil {
	/**
	 * Returns the first found instance of the service [T] by type, or throws an exception if none could be found.
	 */
	inline fun <reified T : Any> findFirst(): T {
		return ServiceLoader.load(T::class.java).findFirst().orElseThrow { IllegalStateException("No platform-specific implementation found for service \"${T::class.java.name}\".") }
	}
	
	/**
	 * Returns the first found instance of the service [T] by type whose package matches the pattern in [packageFilter], or throws an exception if none could be found.
	 *
	 * @param packageFilter Regex pattern for the specific package the expected class will be in, to prevent injection by other mods.
	 */
	inline fun <reified T : Any> findFirst(@Language("REGEXP") packageFilter: String): T {
		val pattern = Regex.fromLiteral(packageFilter)
		return ServiceLoader.load(T::class.java).firstOrNull { it: T -> it.javaClass.`package`.name.matches(pattern) }
		       ?: throw IllegalStateException("No platform-specific implementation found for service \"${T::class.java.name}\" with package filter \"${packageFilter}\".")
	}
}