package btpos.mcmods.devutil.fabric.services

import btpos.mcmods.devutil.common.registry.IRegistryBuilder
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.core.Registry
import net.minecraft.core.WritableRegistry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

class FabricRegistryBuilderImpl<T, REG : WritableRegistry<T>>(key: ResourceKey<out REG>) : IRegistryBuilder<T, REG>(key) {
	
	
	override fun build(): REG {
		TODO("Not yet implemented")
	}
	
}