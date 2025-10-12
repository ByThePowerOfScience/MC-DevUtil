package btpos.mcmods.devutil.multiplatform.services

import btpos.mcmods.devutil.common.registry.DeferredRegistrar
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

/**
 * Creates a [DeferredRegistrar] for a given modloader.
 *
 * Has a different implementation on each modloader.
 */
interface PlatformRegistryFactory {
	/**
	 * Creates a DeferredRegistrar tailored to the current modloader.
	 *
	 * NOTE: If you're on NeoForge, you *must call* [btpos.mcmods.devutil.neoforge.multiplatform.services.NeoForgeRegistryFactory.registerModEventBus]
	 *
	 * @param modId The ID for your mod.
	 * @param registryKey The key (from [BuiltInRegistries][net.minecraft.core.registries.BuiltInRegistries]) for the registry you want to register objects to.
	 */
	fun <T> create(modId: String, registryKey: ResourceKey<Registry<T>>): DeferredRegistrar<T>
}