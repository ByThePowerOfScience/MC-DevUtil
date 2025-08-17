package btpos.mcmods.devutil.fabric.services

import btpos.mcmods.devutil.common.registry.RegistrySupplier
import btpos.mcmods.devutil.multiplatform.services.IPlatformRegistry
import btpos.mcmods.devutil.multiplatform.services.PlatformRegistryFactory
import com.google.auto.service.AutoService
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import java.util.Optional

@AutoService(PlatformRegistryFactory::class)
class FabricRegistryFactory : PlatformRegistryFactory {
	override fun <T> create(modId: String, registryKey: ResourceKey<Registry<T>>): IPlatformRegistry<T> {
		@Suppress("UNCHECKED_CAST") // the typechecker is freaking out with this one fsr...
		return FabricRegistry(modId, registryKey as ResourceKey<Registry<Any>>) as FabricRegistry<T>
	}
}


private class FabricRegistry<T : Any>(override val modId: String, override val registryKey: ResourceKey<Registry<T>>) : IPlatformRegistry<T> {
	@Suppress("NOTHING_TO_INLINE")
	inline fun <T> Registry<T>.gett(key: ResourceKey<T>): Optional<Holder.Reference<T>> {
		return this.get(key)
	}
	
	@Suppress("UNCHECKED_CAST")
	val registry = BuiltInRegistries.REGISTRY.let { it: Registry<*> ->
		// type checker just refuses to even resolve the method unless I do this
		(it as Registry<Registry<Any>>).gett(registryKey as ResourceKey<Registry<Any>>)
	}.orElseThrow() as Registry<T>
	
	override fun <R : T> register(id: String, supplier: () -> R): RegistrySupplier<R> {
		val item: R = Registry.register(registry, ResourceLocation.fromNamespaceAndPath(modId, id), supplier())
		return FabricRegistrySupplier(item, id)
	}
	
	override fun register() {
		// NO-OP
	}
	
	private inner class FabricRegistrySupplier<T>(val resolved: T, override val registeredName: String) : RegistrySupplier<T> {
		override val id: ResourceLocation
			get() = ResourceLocation.fromNamespaceAndPath(modId, registeredName)
		
		override fun get(): T = resolved
	}
}

