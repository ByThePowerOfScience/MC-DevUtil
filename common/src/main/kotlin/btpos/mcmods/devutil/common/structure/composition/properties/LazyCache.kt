package btpos.mcmods.devutil.common.structure.composition.properties

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A value that's cached on retrieval, similar to a [Lazy], but where the cache can be invalidated.
 *
 * Allows for caching a value dependent on another, changing, field: like with [btpos.mcmods.dungeondesigner.builder.blocks.actors.TriggerVarItemConverter.cachedInclusiveAABB] caching the AABB derived from [btpos.mcmods.dungeondesigner.builder.blocks.actors.TriggerVarItemConverter.value].
 */
class LazyCache<T>(val getter: () -> T) {
	@JvmField
	var cache: T? = null
	
	@JvmField
	var isInitialized = false
	
	operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
		if (!isInitialized) {
			cache = getter()
			isInitialized = true
		}
		@Suppress("UNCHECKED_CAST")
		return cache as T
	}
	
	fun invalidate() {
		isInitialized = false
	}
}