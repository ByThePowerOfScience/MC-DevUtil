package btpos.mcmods.devutil.neoforge.multiplatform.services

import btpos.mcmods.devutil.common.registry.DeferredRegistrar
import btpos.mcmods.devutil.common.registry.RegistrySupplier
import btpos.mcmods.devutil.multiplatform.services.IPlatformRegistryFactory
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.lang.ref.WeakReference
import java.util.function.Supplier

private val MOD_BUSES: MutableMap<String, WeakReference<IEventBus>> = mutableMapOf()

class NeoForgeRegistryFactory : IPlatformRegistryFactory {
	companion object {
		fun registerModEventBus(modId: String, modBus: IEventBus) {
			MOD_BUSES[modId] = WeakReference(modBus)
		}
	}
	
	override fun <T : Any> create(modId: String, registryKey: ResourceKey<Registry<T>>): DeferredRegistrar<T> {
		return NeoForgeDeferredRegister(DeferredRegister.create<T>(registryKey, modId))
	}
}

private class NeoForgeDeferredRegister<T>(private val reg: DeferredRegister<T>) : DeferredRegistrar<T> {
	override val modId: String
		get() = reg.namespace
	
	override val registryKey: ResourceKey<out Registry<T>>
		get() = reg.registryKey
	
	
	override fun <R : T> register(id: String, factory: () -> R): RegistrySupplier<R> {
		return ForgeRegistrySupplier(reg.register(id, factory))
	}
	
	override fun registerSelf() {
		val modBus = MOD_BUSES[modId]?.get()
		requireNotNull(modBus) { "No Forge mod event bus found for mod id '$modId'" }
		reg.register(modBus)
	}
	
	override fun iterator(): Iterator<T> {
		return reg.entries.stream().map(Supplier<out T>::get).iterator()
	}
	
	private inner class ForgeRegistrySupplier<ITEM : T>(private val internal: DeferredHolder<*, ITEM>) : RegistrySupplier<ITEM> {
		override val modId: String by this@NeoForgeDeferredRegister::modId
		override val registry by reg::registryKey
		override val registeredName by internal.id::path
		
		override val id: ResourceLocation
			get() = internal.id
		
		override fun get() = internal.get()
	}
}

