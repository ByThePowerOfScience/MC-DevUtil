package btpos.mcmods.devutil.common.structure.program

interface IReverseCloneable<T> {
	/**
	 * Populates the internal state of `this` to match the internal state of `other`.
	 */
	fun copyFrom(other: T)
}