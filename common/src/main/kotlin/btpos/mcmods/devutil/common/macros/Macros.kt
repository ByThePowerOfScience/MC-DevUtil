@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.macros

import com.mojang.datafixers.util.Pair

inline fun panic(message: String = "") : Nothing {
	throw IllegalStateException(message)
}

/**
 * Kotlin's [to] but for Mojang's [Pair]
 */
inline infix fun <T, U> T.mto(u: U): Pair<T, U> = Pair(this, u)