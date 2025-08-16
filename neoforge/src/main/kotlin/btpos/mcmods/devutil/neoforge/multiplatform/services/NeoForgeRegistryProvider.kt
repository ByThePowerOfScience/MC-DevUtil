package btpos.mcmods.devutil.neoforge.multiplatform.services

import btpos.mcmods.devutil.common.registry.RegistrySupplier
import btpos.mcmods.devutil.multiplatform.services.IPlatformRegistry
import btpos.mcmods.devutil.multiplatform.services.PlatformRegistryFactory
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister

class NeoForgeRegistryProvider : PlatformRegistryFactory {
	override fun <T> create(modId: String, registryKey: ResourceKey<Registry<T>>): IPlatformRegistry<T> {
		return NeoForgeRegistry(modId, registryKey)
	}
}

private class NeoForgeRegistry<T>(override val modId: String, override val registryKey: ResourceKey<Registry<T>>) : DeferredRegister<T>(registryKey, modId), IPlatformRegistry<T> {
	override fun <R : T> register(id: String, supplier: () -> R): RegistrySupplier<R> {
		TODO("Not yet implemented")
	}
	
	override fun register() {
		TODO()
	}
}