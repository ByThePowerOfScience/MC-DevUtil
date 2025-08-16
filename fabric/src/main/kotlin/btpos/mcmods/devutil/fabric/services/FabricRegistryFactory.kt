package btpos.mcmods.devutil.fabric.services

import btpos.mcmods.devutil.multiplatform.services.IPlatformRegistry
import btpos.mcmods.devutil.multiplatform.services.PlatformRegistryFactory
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

class FabricRegistryFactory : PlatformRegistryFactory {
	override fun <T> create(modId: String, registryKey: ResourceKey<Registry<T>>): IPlatformRegistry<T> {
	
	}
}