package btpos.mcmods.devutil.common.registry

import btpos.mcmods.devutil.common.registry.DeferredRegistrar.Companion.create
import btpos.mcmods.devutil.multiplatform.services.IPlatformRegistryFactory
import btpos.mcmods.devutil.multiplatform.services.ServiceUtil
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Registers objects to some registry at a user-defined point in time.  Basically a custom implementation of Architectury's and NeoForge's `DeferredRegister`.
 *
 * Do not subclass this. Use the [create] method to create an instance.
 *
 * @see create Creates an instance for a given registry type.
 */
interface DeferredRegistrar<REG>: Iterable<REG> {
	val modId: String
	
	/**
	 * The registry this DeferredRegistrar is targeting.
	 */
	val registryKey: ResourceKey<out Registry<REG>>
	
	/**
	 * Queue an object to be created when this registry is itself registered.
	 * @param id The identifier of the given object.
	 * @param factory The function that should be invoked to create an instance of the object.
	 * @return A wrapper around the item. Should not be invoked until [registerSelf] has been called.
	 */
	fun <R : REG> register(id: String, factory: () -> R): RegistrySupplier<R>
	
	/**
	 * Invoke this when this mod should submit its queued objects to the platform-specific registry.
	 */
	fun registerSelf()
	
	/**
	 * Returns an iterator view of the objects in this registry.
	 */
	override fun iterator(): Iterator<REG>
	
	companion object : IPlatformRegistryFactory by ServiceUtil.findFirst<IPlatformRegistryFactory>()
}

/**
 * Wraps an object in a way that works with the deferred registration of different platforms.
 */
interface RegistrySupplier<EL> : Supplier<EL>, ReadOnlyProperty<Any?, EL> {
	val registry: ResourceKey<out Registry<*>>
	
	val modId: String
	
	/**
	 * The path of this element in a ResourceLocation. (e.g. "minecraft:dirt" -> "dirt")
	 */
	val registeredName: String
	
	/**
	 * The namespaced identifier of this element.
	 */
	val id: ResourceLocation
		get() = ResourceLocation.fromNamespaceAndPath(modId, registeredName)
	
	
	operator fun invoke() = this.get()
	
	override fun getValue(thisRef: Any?, property: KProperty<*>): EL {
		return this.get()
	}
}

