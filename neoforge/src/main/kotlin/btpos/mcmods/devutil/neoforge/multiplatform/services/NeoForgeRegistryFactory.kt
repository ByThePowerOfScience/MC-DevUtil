package btpos.mcmods.devutil.neoforge.multiplatform.services

import btpos.mcmods.devutil.common.registry.RegistrySupplier
import btpos.mcmods.devutil.multiplatform.services.IPlatformRegistry
import btpos.mcmods.devutil.multiplatform.services.PlatformRegistryFactory
import com.google.auto.service.AutoService
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.lang.ref.WeakReference

private val MOD_BUSES: MutableMap<String, WeakReference<IEventBus>> = mutableMapOf()

@AutoService(PlatformRegistryFactory::class)
class NeoForgeRegistryFactory : PlatformRegistryFactory {
	companion object {
		fun registerModEventBus(modId: String, modBus: IEventBus) {
			MOD_BUSES[modId] = WeakReference(modBus)
		}
	}
	
	override fun <T> create(modId: String, registryKey: ResourceKey<Registry<T>>): IPlatformRegistry<T> {
		return NeoForgeRegistry(DeferredRegister.create<T>(registryKey, modId))
	}
}

private class NeoForgeRegistry<T>(private val reg: DeferredRegister<T>) : IPlatformRegistry<T> {
	override val modId: String
		get() = reg.namespace
	
	override val registryKey: ResourceKey<Registry<T>>
		get() = reg.registryKey as ResourceKey<Registry<T>>
	
	override fun <R : T> register(id: String, supplier: () -> R): RegistrySupplier<R> {
		return ForgeRegistrySupplier(reg.register(id, supplier))
	}
	
	override fun register() {
		val modBus = MOD_BUSES[modId]?.get()
		requireNotNull(modBus) { "No Forge mod event bus found for mod id '$modId'" }
		reg.register(modBus)
	}
}

class ForgeRegistrySupplier<T>(private val internal: DeferredHolder<*, T>) : RegistrySupplier<T> {
	override val registeredName by internal::registeredName
	override val id: ResourceLocation by internal::id
	
	override fun get() = internal.get()
}