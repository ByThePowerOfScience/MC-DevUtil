package btpos.mcmods.devutil.fabric.services

import btpos.mcmods.devutil.common.registry.DeferredRegistrar
import btpos.mcmods.devutil.common.registry.RegistrySupplier
import btpos.mcmods.devutil.multiplatform.services.IPlatformRegistryFactory
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import java.util.Optional
import java.util.function.Supplier

class FabricRegistryFactory : IPlatformRegistryFactory {
	override fun <T> create(modId: String, registryKey: ResourceKey<Registry<T>>): DeferredRegistrar<T> {
		@Suppress("UNCHECKED_CAST") // the typechecker is freaking out with this one fsr...
		return FabricDeferredRegistrar(modId, registryKey as ResourceKey<Registry<Any>>) as FabricDeferredRegistrar<T>
	}
}

private class FabricDeferredRegistrar<T : Any>(override val modId: String, override val registryKey: ResourceKey<Registry<T>>) : DeferredRegistrar<T> {
	@Suppress("NOTHING_TO_INLINE")
	inline fun <T> Registry<T>.gett(key: ResourceKey<T>): Optional<Holder.Reference<T>> {
		return this.get(key)
	}
	
	@Suppress("UNCHECKED_CAST")
	val registry = BuiltInRegistries.REGISTRY.let { it: Registry<*> ->
		// type checker just refuses to even resolve the method unless I do this
		(it as Registry<Registry<Any>>).gett(registryKey as ResourceKey<Registry<Any>>)
	}.orElseThrow() as Registry<T>
	
	/**
	 * Set of registry objects that are waiting to be registered in the [registerSelf] invocation.
	 *
	 * Will be null if [registerSelf] has already been invoked.
	 */
	private val registryQueue: MutableList<FabricRegistrySupplier<out T>> = mutableListOf()
	
	override fun <R : T> register(id: String, factory: () -> R): RegistrySupplier<R> {
		val f_registerItem = { Registry.register(registry, ResourceLocation.fromNamespaceAndPath(modId, id), factory()) }
		return FabricRegistrySupplier(f_registerItem, id).also { supp ->
			registryQueue.add(supp)
		}
	}
	
	override fun registerSelf() {
		// Send each item to the registry and remove the list so we aren't wasting memory
		registryQueue.forEach {
			it.get()
		} // ?: return LOGGER.warn("registerSelf called after objects were already registered.", Throwable())
	}
	
	
	override fun iterator(): Iterator<T> {
		return registryQueue.stream().map<T>(Supplier<out T>::get).iterator()
	}
	
	private inner class FabricRegistrySupplier<ITEM : T>(resolver: () -> ITEM, override val registeredName: String) : RegistrySupplier<ITEM> {
		override val modId: String
			get() = this@FabricDeferredRegistrar.modId
		
		override val registry: ResourceKey<out Registry<*>>
			get() = registryKey
		
		val value by lazy(resolver)
		
		override fun get(): ITEM = value
	}
}

