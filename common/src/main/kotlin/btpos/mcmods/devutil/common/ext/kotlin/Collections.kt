@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.ext.kotlin

import kotlin.collections.ArrayList

/**
 * Collects the iterable starting with an initial value. This is a terminal operation.
 *
 * ...nevermind, turns out this is just [fold]
 */
inline fun <ITEM, YIELD> Iterable<ITEM>.mapReduce(startingValue: YIELD, reductionFunction: (YIELD, ITEM) -> YIELD): YIELD {
	var yield: YIELD = startingValue
	for (el in this) {
		yield = reductionFunction(yield, el)
	}
	return yield
}

/**
 * Split a list into two lists: ones that match the predicate and ones that don't.
 */
inline fun <ITEM> Iterable<ITEM>.filterSplit(predicate: (ITEM) -> Boolean): FilterResult<ITEM> {
	val matching = mutableListOf<ITEM>()
	val notMatching = mutableListOf<ITEM>()
	for (it in this) {
		if (predicate(it))
			matching += it
		else
			notMatching += it
	}
	
	return FilterResult(matching, notMatching)
}

data class FilterResult<T>(val matching: List<T>, val notMatching: List<T>) {
	inline fun onTrue(action: (List<T>) -> Unit): FilterResult<T> {
		matching.apply(action)
		return this
	}
	
	inline fun onFalse(action: (List<T>) -> Unit): FilterResult<T> {
		notMatching.apply(action)
		return this
	}
}

/**
 * Returns a _mutable_ list containing the results of applying the given [transform] function
 * to each element in the original collection.
 */
inline fun <T, R> Iterable<T>.mapMutable(transform: (T) -> R): MutableList<R> {
	return mapTo(ArrayList(collectionSizeOrDefault(10)), transform)
}

inline fun <T> Iterable<T>.collectionSizeOrDefault(default: Int) = if (this is Collection<*>) this.size else default