package btpos.mcmods.devutil.multiplatform.services

import btpos.mcmods.devutil.common.registry.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

interface IPlatformRegistry<T> {
	val modId: String
	val registryKey: ResourceKey<Registry<T>>
	
	/**
	 * Queue an object to be created when this registry is itself registered.
	 */
	fun <R : T> register(id: String, supplier: () -> R): RegistrySupplier<R>
	
	/**
	 * Invoke when this mod should submit its queued objects to the platform-specific registry.
	 */
	fun register()
	
	companion object : PlatformRegistryFactory by ServiceUtil.loadService<PlatformRegistryFactory>()
}

interface PlatformRegistryFactory {
	fun <T> create(modId: String, registryKey: ResourceKey<Registry<T>>): IPlatformRegistry<T>
}