@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.ext.java

import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

inline operator fun <T : Any?> Consumer<T>.invoke(it: T) = this.accept(it)
inline operator fun <T : Any?, U: Any?> BiConsumer<T, U>.invoke(it: T, it2: U) = this.accept(it, it2)

inline operator fun <T : Any?, U: Any?> Function<T, U>.invoke(it: T) = this.apply(it)
inline operator fun <T : Any?, U: Any?, V: Any?> BiFunction<T, U, V>.invoke(it: T, it2: U) = this.apply(it, it2)

inline operator fun <T : Any?> Supplier<T>.invoke() = this.get()

