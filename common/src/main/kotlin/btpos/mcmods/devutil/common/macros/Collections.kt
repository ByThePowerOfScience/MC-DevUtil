@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.macros

import com.google.common.collect.ImmutableMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap

inline fun <T : Any, U : Any> immutableMapOf(vararg pairs: Pair<T, U>): ImmutableMap<T, U> {
	val builder = ImmutableMap.builder<T, U>()
	for ((key, value) in pairs) {
		builder.put(key, value)
	}
	return builder.build()
}

inline fun <T, U> fastMapOf() : Object2ObjectOpenHashMap<T, U> {
	return Object2ObjectOpenHashMap()
}

inline fun <T, U> fastMapOf(vararg pairs: Pair<T, U>) : Object2ObjectOpenHashMap<T, U> {
	val map = if (pairs.isEmpty()) Object2ObjectOpenHashMap<T, U>() else Object2ObjectOpenHashMap(pairs.size)
	for ((key, value) in pairs)
		map.put(key, value)
	return map;
}

@JvmInline
value class NonNullMap<T : Any, U : Any>(val internal: MutableMap<T, U>): MutableMap<T, U> by internal {
	override operator fun get(key: T): U {
		return internal[key] as U
	}
}