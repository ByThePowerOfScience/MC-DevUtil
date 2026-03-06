package btpos.mcmods.devutil.common.registry

import btpos.mcmods.devutil.multiplatform.services.ServiceUtil
import net.minecraft.core.Registry
import net.minecraft.core.WritableRegistry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

/**
 * Builds a registry using the platform-specific builders.
 *
 * @param T The type contained in the registry
 * @param REG The type of the registry itself
 */
abstract class IRegistryBuilder<T, REG : WritableRegistry<T>>(val key: ResourceKey<out REG>) {
	var defaultId: ResourceLocation? = null
	
	var isSynced = false
	
	abstract fun build(): REG
	
	companion object : IRegistryBuilderFactory {
		private val service = ServiceUtil.findFirst<IRegistryBuilderFactory>()
		
		override fun <T, REG : WritableRegistry<T>> create(key: ResourceKey<REG>): IRegistryBuilder<T, REG> {
			return service.create(key)
		}
	}
}

/**
 * Service, implemented on different platforms
 */
interface IRegistryBuilderFactory {
	fun <T, REG : WritableRegistry<T>> create(key: ResourceKey<REG>): IRegistryBuilder<T, REG>
}