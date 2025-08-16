@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.ext.java

import java.util.Optional

inline val <T : Any> Optional<T>.value: T?
	inline get() = this.orElse(null)

inline fun <T : Any> T?.asOptional(): Optional<T> = Optional.ofNullable(this)

/**
 * This converts `Optional<T & Any>` to `Optional<T>` so we can actually use them.
 *
 * This is clearly a bug in the Kotlin compiler.
 * `Optional` is already <T & Any>.  There's no reason that `Optional#ofNullable` should be returning an _even stricter_ Optional than normal.
 */
@Suppress("UNCHECKED_CAST")
inline fun <U, T : U & Any> Optional<T>.cast() = this as Optional<U>

